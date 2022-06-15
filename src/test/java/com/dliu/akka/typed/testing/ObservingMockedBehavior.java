package com.dliu.akka.typed.testing;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import org.junit.AfterClass;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class ObservingMockedBehavior {
    public static class Message {
        public int message;
        public ActorRef<Integer> replyTo;

        public Message(int message, ActorRef<Integer> replyTo) {
            this.message = message;
            this.replyTo = replyTo;
        }
    }

    public static class Producer {
        private final Scheduler scheduler;
        private final ActorRef<Message> publisher;

        public Producer(Scheduler scheduler, ActorRef<Message> publisher) {
            this.scheduler = scheduler;
            this.publisher = publisher;
        }

        public void produce(int numOfMessages) {
            IntStream.range(0, numOfMessages).forEach(i -> {
                publish(i).thenApply(r -> {
                    System.out.println("Got reply integer " + r);
                    return r;
                }).exceptionally( th -> {
                    System.err.println("Got reply error " + th.getCause() + " for " + i);
                    return null;
                });
            });
        }

        private CompletionStage<Integer> publish(int i) {
            return AskPattern.ask(publisher,
                    (ActorRef<Integer> replyTo) -> new Message(i, replyTo),
                    Duration.ofSeconds(3),
                    scheduler);
//            return CompletableFuture.<Integer>completedFuture(i);
        }
    }
    private static ActorTestKit testKit = ActorTestKit.create();

    @AfterClass
    public static void cleanUp() {
        testKit.shutdownTestKit();
    }

    @Test
    public void testMockingBehavior () {
        Behavior<Message> mockingBehavior = Behaviors.receiveMessage(msg -> {
            if (msg.message != 1) {
                msg.replyTo.tell(msg.message);
            } else {
//                throw new RuntimeException("Bad 1");
                Thread.sleep(5000);
            }
            return Behaviors.same();
        });

        TestProbe<Message> probe = testKit.createTestProbe();

        ActorRef<Message> mockingPublisher = testKit.spawn(Behaviors.monitor(Message.class, probe.getRef(), mockingBehavior));

        Producer producer = new Producer(testKit.scheduler(), mockingPublisher);

        int numOfMessages = 3;
        producer.produce(numOfMessages);

        IntStream.range(0, numOfMessages)
                .forEach(i -> {
                    Message message = probe.expectMessageClass(Message.class);
                    assertEquals(i, message.message);
                });

    }
}
