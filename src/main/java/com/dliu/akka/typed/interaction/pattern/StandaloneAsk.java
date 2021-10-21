package com.dliu.akka.typed.interaction.pattern;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class StandaloneAsk {
    public static void main(String[] args) throws IOException {

        ActorSystem<CookieFabric.Command> system = ActorSystem.create(CookieFabric.create(), "standalone_ask");
        ActorRef<CookieFabric.Command> cookieFabric = system;

        CompletionStage<CookieFabric.Reply> result =
                AskPattern.ask(
                        cookieFabric,
                        replyTo -> {
                            System.out.println(replyTo);
                            return new CookieFabric.GiveMeCookies(3, replyTo);
                            },
                        // asking someone requires a timeout and a scheduler, if the timeout hits without
                        // response the ask is failed with a TimeoutException
                        Duration.ofSeconds(3),
                        system.scheduler());

        result.whenComplete(
                (reply, failure) -> {
                    if (reply instanceof CookieFabric.Cookies) {
                        System.out.println("Yay, " + ((CookieFabric.Cookies) reply).count + " cookies!");
                    } else if (reply instanceof CookieFabric.InvalidRequest) {
                        System.out.println(
                                "No cookies for me. " + ((CookieFabric.InvalidRequest) reply).reason);
                    } else {
                        System.out.println("Boo! didn't get cookies in time. " + failure);
                    }
                });

        System.in.read();
        system.terminate();;
    }

    public static class CookieFabric extends AbstractBehavior<CookieFabric.Command> {

        private CookieFabric(ActorContext<Command> context) {
            super(context);
        }

        public static Behavior<Command> create() {
            return Behaviors.setup(CookieFabric::new);
        }

        @Override
        public Receive<Command> createReceive() {
            return newReceiveBuilder().onMessage(GiveMeCookies.class, this::onGiveMeCookies).build();
        }

        private Behavior<Command> onGiveMeCookies(GiveMeCookies request) {
            if (request.count >= 5) {
                request.replyTo.tell(new InvalidRequest("Too many cookies."));
            } else {
                request.replyTo.tell(new Cookies(request.count));
            }

            return this;
        }

        interface Command {
        }

        interface Reply {
        }

        public static class GiveMeCookies implements Command {
            public final int count;
            public final ActorRef<Reply> replyTo;

            public GiveMeCookies(int count, ActorRef<Reply> replyTo) {
                this.count = count;
                this.replyTo = replyTo;
            }
        }

        public static class Cookies implements Reply {
            public final int count;

            public Cookies(int count) {
                this.count = count;
            }
        }

        public static class InvalidRequest implements Reply {
            public final String reason;

            public InvalidRequest(String reason) {
                this.reason = reason;
            }
        }
    }
}


