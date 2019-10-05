package example.myapp.helloworld;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.grpc.GrpcClientSettings;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Source;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import example.myapp.helloworld.grpc.GreeterService;
import example.myapp.helloworld.grpc.GreeterServiceClient;
import example.myapp.helloworld.grpc.HelloReply;
import example.myapp.helloworld.grpc.HelloRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GreeterClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GreeterClient.class);

    public static void main(String[] args) throws IOException {

        Config config = ConfigFactory.load("akka_grpc_client.conf");

        ActorSystem system = ActorSystem.create("HelloWorldClient", config);
        ActorMaterializer materializer = ActorMaterializer.create(system);

        GrpcClientSettings settings = GrpcClientSettings.fromConfig(GreeterService.name, system);

        GreeterServiceClient client = GreeterServiceClient.create(settings, materializer, system.dispatcher());

        singleRequestReply(client);

        streamingRequest(client);

        streamingReply(materializer, client);

        streamingHellos(materializer, client);

        stopClient(system);


    }

    private static void streamingHellos(ActorMaterializer materializer, GreeterServiceClient client) {
        try {
            List<HelloRequest> requests = Arrays.asList("David", "Joe", "John")
                    .stream()
                    .map(name -> HelloRequest.newBuilder().setName(name).build())
                    .collect(Collectors.toList());
            Source<HelloReply, NotUsed> reply = client.streamHellos(Source.from(requests));
            CompletableFuture<Done> done = reply.runForeach(message -> LOGGER.info("got message {}", message), materializer).toCompletableFuture();
            done.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static void streamingReply(ActorMaterializer materializer, GreeterServiceClient client) {
        try {
            HelloRequest request = HelloRequest.newBuilder()
                    .setName("John")
                    .build();
            CompletionStage<Done> done = client.itKeepsReplying(request).runForeach(message -> LOGGER.info("got reply {}", message), materializer);

            done.toCompletableFuture().get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static void stopClient(ActorSystem system) throws IOException {
        LOGGER.info("Press any key to stop client");
        System.in.read();
        system.terminate();
    }

    private static void streamingRequest(GreeterServiceClient client) {
        try {
            List<HelloRequest> requests = Arrays.asList("David", "Susan", "Bob")
                    .stream()
                    .map(name -> HelloRequest.newBuilder().setName(name).build())
                    .collect(Collectors.toList());
            CompletionStage<HelloReply> reply = client.itKeepsTalking(Source.from(requests));
            LOGGER.info("got reply {}", reply.toCompletableFuture().get(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static void singleRequestReply(GreeterServiceClient client) {
        try {
            HelloRequest request = HelloRequest.newBuilder()
                    .setName("David")
                    .build();
            CompletionStage<HelloReply> reply = client.sayHello(request);
            LOGGER.info("got reply {}", reply.toCompletableFuture().get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
