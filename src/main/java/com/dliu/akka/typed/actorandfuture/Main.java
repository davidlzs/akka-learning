package com.dliu.akka.typed.actorandfuture;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Main {
    public static void main(String[] args) throws IOException {
        ActorSystem<String> system = ActorSystem.create(Behaviors.setup(ctx -> {
            ctx.getLog().info("Create actor");
            return pipeToBehavior(ctx);
//            return blockingBehavior(ctx);
        }), "actorsystem");

        system.tell("Hello");

        System.in.read();
        system.terminate();
    }

    private static Behavior<String> pipeToBehavior(akka.actor.typed.javadsl.ActorContext<String> ctx) {
        return Behaviors.receive(String.class)
                .onMessage(String.class, m -> {
                    ctx.getLog().info("received message {}", m);
                    if ("pipeTo + got it".equals(m)) {
                        return Behaviors.stopped();
                    } else {
                        ctx.pipeToSelf(
                                greet(),
                                (ok, exc) -> {
                                    if (exc == null) {
                                        return "pipeTo + " + ok;
                                    } else {
                                        return "pipeTo exception";
                                    }
                                }
                        );
                        return Behaviors.same();
                    }
                })
                .build();
    }

    private static Behavior<String> blockingBehavior(akka.actor.typed.javadsl.ActorContext<String> ctx) {
        return Behaviors.receive(String.class)
                .onMessage(String.class, m -> {
                    ctx.getLog().info("received message {}", m);
                    ctx.getLog().info(greet().toCompletableFuture().get());
                    return Behaviors.same();
                })
                .build();
    }

    public static CompletionStage<String> greet() {
        try {
            System.out.println("Async thread is  " + Thread.currentThread().getName());
            Thread.sleep(500000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture("got it");
    }


}
