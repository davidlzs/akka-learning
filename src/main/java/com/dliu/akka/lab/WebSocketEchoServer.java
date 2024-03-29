package com.dliu.akka.lab;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Terminated;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.HttpTerminated;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.TextMessage;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.japi.Pair;
import akka.japi.function.Function;
import akka.stream.*;
import akka.stream.javadsl.*;
import scala.concurrent.Future;

public class WebSocketEchoServer extends AllDirectives {
    ActorSystem system = ActorSystem.create("websocket-server-system");
    Materializer materializer = ActorMaterializer.create(system);
    Http http = Http.get(system);
    ServerBinding serverBinding;

    Set<SharedKillSwitch> killSwitches = Collections.synchronizedSet(new HashSet<>());


    public WebSocketEchoServer() throws ExecutionException, InterruptedException, TimeoutException {
        Flow<HttpRequest, HttpResponse, ?> handler = createRoute().flow(system, materializer);
        serverBinding = http.bindAndHandle(handler, ConnectHttp.toHost("127.0.0.1", 9090), materializer).toCompletableFuture().get(3, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        WebSocketEchoServer webSocketEchoServer = new WebSocketEchoServer();
        webSocketEchoServer.registerShutdownHook();
        webSocketEchoServer.runDoubleFlow();
        webSocketEchoServer.readCommand();
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            disconnect();
//            system.terminate();
        }));
    }

    private void readCommand() {
        Scanner keyboard = new Scanner(System.in);
        System.out.println("enter an command");
        loop: while (true) {
            String cmd = keyboard.nextLine();
            switch (cmd) {
                case "quit" :
                    terminate();
                    break loop;
                case "disconnect":
                    disconnect();
                    break;
            }
        }
    }

    private void disconnect() {
        System.out.println("disconnecting " + killSwitches.size());
        killSwitches.forEach(SharedKillSwitch::shutdown);
    }

    private  void terminate() {

        // once ready to terminate the server, invoke terminate:
        CompletionStage<HttpTerminated> onceAllConnectionsTerminated =
                serverBinding.terminate(Duration.ofSeconds(3));

// once all connections are terminated,
        onceAllConnectionsTerminated.toCompletableFuture().
                thenAccept(terminated -> {
                    System.out.println("terminating...");
                    system.terminate();
                });
    }

    private Route createRoute() {
        Route route = path("gw", () -> handleWebSocketMessages(stackedWebSocketFlow()));
        return route;
    }

    // disconnect websocket by using Source.actorRef
    private Flow<Message, Message, NotUsed> websocketActorRefFlow() {
        ActorSource actorSource = new ActorSource(materializer);
        ActorRef actor = actorSource.getActor();
        Source<Message, NotUsed> source = actorSource.getPublisher();

        Source<Message, NotUsed> multiplyTwoSource = source
                .map(msg -> Integer.parseInt(msg.asTextMessage().getStrictText()))
                .via(doubleFlow())
                .withAttributes(ActorAttributes.withSupervisionStrategy(decider()))
                .map(n -> TextMessage.create(n.toString()));

        Sink<Message, CompletionStage<Done>> sink = Sink.foreach(message -> {
            String messageString = message.asTextMessage().getStrictText();
            System.out.println(messageString);
            if (messageString.equals("bye")) {
                actor.tell(PoisonPill.getInstance(), ActorRef.noSender());
            } else {
                actor.tell(message, ActorRef.noSender());
            }
        });

        return Flow.fromSinkAndSource(sink, multiplyTwoSource);
    }

    // disconnect websocket by using source.queue
    private Flow<Message, Message, NotUsed> websocketSourceQueueFlow() {
        Pair<SourceQueueWithComplete<Message>, Source<Message, NotUsed>> pair = Source.<Message>queue(1, OverflowStrategy.backpressure()).preMaterialize(materializer);

        SourceQueueWithComplete<Message> queue = pair.first();
        Source<Message, NotUsed> source = pair.second();

        Source<Message, NotUsed> multiplyTwoSource = source
                .map(msg -> Integer.parseInt(msg.asTextMessage().getStrictText()))
                .via(doubleFlow())
                .withAttributes(ActorAttributes.withSupervisionStrategy(decider()))
                .map(n -> TextMessage.create(n.toString()));


        Sink<Message, CompletionStage<Done>> sink = Sink.foreach(message -> {
            String messageString = message.asTextMessage().getStrictText();
            System.out.println(messageString);
            if (messageString.equals("bye")) {
                queue.complete();
            } else {
                queue.offer(message);
            }
        });

        return Flow.fromSinkAndSource(sink, multiplyTwoSource);
    }

    private Function<Throwable, Supervision.Directive> decider() {
        return ex -> {
            if (ex instanceof NumberFormatException) {
                return Supervision.resume();
            } else {
                return Supervision.stop();
            }
        };
    }

    // Double number flow
    private Flow<Integer, Integer, NotUsed> doubleFlow() {
        return Flow.of(Integer.class)
                .map(n -> 2 * n);
    }

    private void runDoubleFlow() {
        Source.from(Arrays.asList(1, 2, 3, 4))
                .via(doubleFlow())
                .to(Sink.foreach(System.out::println))
                .run(materializer);
    }

    // codec flow - reverse
    private Flow<Message, Message, NotUsed> stackedWebSocketFlow() {
        SharedKillSwitch killSwitch = new SharedKillSwitch("killSwitch");
        killSwitches.add(killSwitch);
        BidiFlow<String, String, String, String, NotUsed> codec = BidiFlow.fromFunctions(this::reverse, s -> s);
        BidiFlow<Message, String, String, Message, NotUsed> messageStringConverter = BidiFlow.fromFlows(messageToStringFlow(), stringToMessageFlow());
        return messageStringConverter.join(codec.join(Flow.of(String.class)))
                .watchTermination((mat, termination) -> {
                    System.out.println("connected - add watchTermination");
                    termination.whenComplete((done, throwable) -> {
                        killSwitches.remove(killSwitch);
                        if (throwable != null) {
                            System.err.println("disconnected with exception: " + throwable);
                        } else {
                            System.out.println("disconnected");
                        }
                    });
                    return mat;
                }).viaMat(killSwitch.flow(), Keep.left());
    }

    private String reverse(String text) {
        return new StringBuilder(text).reverse().toString();
    }

    private Flow<Message, String, NotUsed> messageToStringFlow() {
        return Flow.fromFunction((Message msg) -> msg.asTextMessage().getStrictText());
    }

    private Flow<String, Message, NotUsed> stringToMessageFlow() {
        return Flow.fromFunction(text -> (Message) TextMessage.create(text));
    }
}
