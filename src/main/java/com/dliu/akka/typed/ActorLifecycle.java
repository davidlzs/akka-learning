package com.dliu.akka.typed;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ActorLifecycle {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        ActorSystem<String> system = ActorSystem.<String>create(Behaviors.setup(ctx -> {

            Behavior<String> behavior = Behaviors.receive(String.class)
                    .onMessage(String.class, m -> {
                        ctx.getLog().info("Received message: {} ", m);
                        if (m.equals("Exception")) {
                            throw new RuntimeException("Exception");
                        } else if (m.equals("Stop")) {
                            Behaviors.stopped();
                        } else {
                        }
                        return Behaviors.same();
                    })
                    .onSignal(PreRestart.class, pr -> {
                        ctx.getLog().info("PreRestart");
                        return Behaviors.same();
                    })
                    .onSignal(PostStop.class, s -> {
                        ctx.getLog().info("PostStop");
                        return Behaviors.same();
                    })
                    .build();
            return behavior;

        }), "lifecycle");

        system.tell("Hello");

        CompletableFuture<Object> future = AskPattern.ask(system, (replyTo) -> "Dummy Message", Duration.ofSeconds(3), system.scheduler())
                .toCompletableFuture();
        system.tell("Exception");
        system.tell("Hey");

        System.in.read();
        system.terminate();
    }
}
