package com.dliu.akka.typed.actorandfuture;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;

public class AskTimeoutMain {
    public static void main(String[] args) throws IOException {
        ActorSystem<BaseMessage> system = ActorSystem.create(Behaviors.setup(ctx -> {
            ctx.getLog().info("Create actor");
            //return pipeToBehavior(ctx);
            return blockingBehavior(ctx);
        }), "actorsystem");

        //system.tell(new Message(Actor.noSender(),"Hello"));

        //asks(system);
        system.tell(new BaseMessage("tell1"));
        system.tell(new BaseMessage("tell2"));
        system.tell(new BaseMessage("tell3"));


        System.in.read();
        system.terminate();
    }

    private static void asks(ActorSystem<AskMessage> system) {
        CompletableFuture<String> future1 = AskPattern.ask(system, (ActorRef<String> replyTo) -> new AskMessage(replyTo, "Hello1"), Duration.ofSeconds(1), system.scheduler()).toCompletableFuture();
        CompletableFuture<String> future2 = AskPattern.ask(system, (ActorRef<String> replyTo) -> new AskMessage(replyTo, "Hello2"), Duration.ofSeconds(1), system.scheduler()).toCompletableFuture();
        CompletableFuture<String> future3 = AskPattern.ask(system, (ActorRef<String> replyTo) -> new AskMessage(replyTo, "Hello3"), Duration.ofSeconds(1), system.scheduler()).toCompletableFuture();

        future1.whenComplete((e, t) -> {
            if (t != null) {
                t.printStackTrace();
            } else {
                System.out.println("whencomplete " + e);
            }
        });
        future2.whenComplete((e, t) -> {
            if (t != null) {
                t.printStackTrace();
            } else {
                System.out.println("whencomplete " + e);
            }
        });
        future3.whenComplete((e, t) -> {
            if (t != null) {
                t.printStackTrace();
            } else {
                System.out.println("whencomplete " + e);
            }
        });
    }

    private static Behavior<String> pipeToBehavior(akka.actor.typed.javadsl.ActorContext<String> ctx) {
        return Behaviors.receive(String.class)
                .onMessage(String.class, m -> {
                    ctx.getLog().info("received message {}", m);
                    if ("pipeTo + got it".equals(m)) {
                        return Behaviors.stopped();
                    } else {
                        ctx.pipeToSelf(
                                greet(m),
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

    private static Behavior<BaseMessage> blockingBehavior(akka.actor.typed.javadsl.ActorContext<BaseMessage> ctx) {
        return Behaviors.receive(BaseMessage.class)
                .onMessage(BaseMessage.class, m -> {
                    ctx.getLog().info("received message {}", m);
                    //try {
                    //    Thread.sleep(2000);
                    //} catch (Exception e) {
                    //    e.printStackTrace();
                    //}

                    ctx.getLog().info(greet(m.message).toCompletableFuture().get());
                    if (m instanceof AskMessage) {
                        ((AskMessage)m).replyTo.tell("sleep done and reply for " + m.message);
                    }
                    return Behaviors.same();
                })
                .build();
    }

    public static CompletionStage<String> greet(String message) {
        try {
            System.out.println("Async thread is  " + Thread.currentThread().getName());
            Thread.sleep(20000);
            System.out.println(("sleep done " + message));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture("got it");
    }

}
