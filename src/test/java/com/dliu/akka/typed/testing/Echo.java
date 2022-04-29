package com.dliu.akka.typed.testing;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.Objects;

// https://doc.akka.io/docs/akka/current/typed/testing-async.html#basic-example
public class Echo{
    public static class Ping {
        public final String message;
        public final ActorRef<Pong> replyTo;

        public Ping(String message, ActorRef<Pong> replyTo) {
            this.message = message;
            this.replyTo = replyTo;
        }
    }

    public static class Pong {
        public final String message;

        public Pong(String message) {
            this.message = message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pong pong = (Pong) o;
            return Objects.equals(message, pong.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message);
        }

        @Override
        public String toString() {
            return "Pong{" +
                    "message='" + message + '\'' +
                    '}';
        }
    }

    public static Behavior<Ping> create() {
        return Behaviors.receive(Ping.class)
                .onMessage(Ping.class, msg -> {
                    msg.replyTo.tell(new Pong(msg.message));
                    return Behaviors.same();
                })
                .build();
    }

    private static ActorTestKit testKit = ActorTestKit.create();

    @AfterClass
    public static void cleanUp() {
        testKit.shutdownTestKit();
    }

    @Test
    public void testEcho() {
        ActorRef<Ping> pinger = testKit.spawn(Echo.create());
        TestProbe<Pong> pong = testKit.createTestProbe();
        pinger.tell(new Ping("hello", pong.ref()));
        pong.expectMessage(new Pong("hello"));
    }

    @Test
    public void testStopActor() {
        ActorRef<Ping> pinger1 = testKit.spawn(Echo.create(), "pinger");
        TestProbe<Pong> probe = testKit.createTestProbe();
        pinger1.tell(new Ping("hi", probe.ref()));
        probe.expectMessage(new Pong("hi"));
        testKit.stop(pinger1);

        ActorRef<Ping> pinger2 = testKit.spawn(Echo.create(), "pinger");
        pinger2.tell(new Ping("hello", probe.ref()));
        probe.expectMessage(new Pong("hello"));
        testKit.stop(pinger2);
    }
}
