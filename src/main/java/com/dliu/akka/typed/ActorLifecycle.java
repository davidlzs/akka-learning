package com.dliu.akka.typed;

import akka.actor.DeadLetter;
import akka.actor.typed.*;
import akka.actor.typed.eventstream.EventStream;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ActorLifecycle {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        ActorSystem<Object> system = ActorSystem.create(Behaviors.setup(ctx -> {
            ActorRef<Object> a1 = ctx.spawn(getStringBehavior(), "a1");
            ActorRef<Object> a2 = ctx.spawn(getStringBehavior(), "a2");
            ctx.getLog().info("a1: {} and a2: {}", a1.path(), a2.path());
            return Behaviors.receive(Object.class)
                    .onMessage(String.class, m -> {
                        a1.tell(m);
                        a2.tell(m);
                        return Behaviors.same();
                    })
                    .onMessage(DeadLetter.class, d -> {
                        ctx.getLog().info("Received dead letter " + d);
                        return Behaviors.same();
                    })
                    .build();

        }), "lifecycle");

        // How to register

        system.eventStream().tell(new EventStream.Subscribe(DeadLetter.class, system));


        system.tell("Hello");

        CompletableFuture<Object> future = AskPattern.ask(system, (replyTo) -> "Dummy Message", Duration.ofSeconds(3), system.scheduler())
                .toCompletableFuture();
        system.tell("Exception");
        system.tell("Hey");

        System.in.read();
        system.terminate();
    }

    private static Behavior<Object> getStringBehavior() {
        return Behaviors.setup(ctx -> {
            ctx.getLog().info("ctx getSelf: {}", ctx.getSelf().path());
            return Behaviors.receive(Object.class)
                    .onMessage(String.class, m -> {
                        System.out.println(ctx.getSelf().path() + " Received message: " + m);
                        if (m.equals("Exception")) {
                            throw new RuntimeException("Exception");
                        } else if (m.equals("Stop")) {
                            Behaviors.stopped();
                        } else {
                        }
                        return Behaviors.same();
                    })
                    .onSignal(PreRestart.class, pr -> {
                        ctx.getLog().info("PreRestart " + ctx.getSelf());
                        return Behaviors.same();
                    })
                    .onSignal(PostStop.class, s -> {
                        ctx.getLog().info("PostStop " + ctx.getSelf());
                        return Behaviors.same();
                    })
                    .build();
        });
    }
}
