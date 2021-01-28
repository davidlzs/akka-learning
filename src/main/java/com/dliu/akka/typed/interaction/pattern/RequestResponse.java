package com.dliu.akka.typed.interaction.pattern;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

/**
 *  Usecases:
 *      subscribing to an actor that will send many response messages back
 *  Problems:
 *      actors seldom have a response message from another actor as a part of their protocol
 *      It is hard to detect that a message request was not delivered or processed.
 *      unless the protocol already includes a way to provide context (for example, a requestId), it is not possible to tie a response to the request with context
 */
public class RequestResponse {
    public static void main(String[] args) throws InterruptedException {
        // The guardian actor is the the requestor - sending request and receive the response - in this code demo, the requestor is talking in the other's language - look at adapted response pattern
        ActorSystem<CookieFabric.Response> system = ActorSystem.create(Behaviors.setup(ctx -> {
            // CookieFabric is the actor to serve the query
            ActorRef<CookieFabric.Request> cookieFabric = ctx.spawn(CookieFabric.create(), "cookie_fabric");
            cookieFabric.tell(new CookieFabric.Request("david", ctx.getSelf()));
            return Behaviors.receive(CookieFabric.Response.class)
                    .onMessage(CookieFabric.Response.class, (res) -> {
                        ctx.getLog().info("Got response " + res);
                        return Behaviors.same();
                    }).build();
        }), "guardian");

        //ActorRef<CookieFabric.Response> guardian = system;

        //Thread.sleep(5000);
        //ActorRef<CookieFabric.Response> ignoreRef = system.ignoreRef();
        //ActorRef<CookieFabric.Request> cookieFabric = Guardian.cookieFabric;

        //cookieFabric.tell(new CookieFabric.Request("david", guardian));


    }

    public static class Guardian {
        public static ActorRef<CookieFabric.Request> cookieFabric;

        public static Behavior<CookieFabric.Response> create() {

            return Behaviors.setup(ctx -> {
                cookieFabric = ctx.spawn(CookieFabric.create(), "cookie_fabric");
                return Behaviors.receive(CookieFabric.Response.class)
                        .onMessage(CookieFabric.Response.class, (res) -> {
                            ctx.getLog().info("Got response " + res);
                            return Behaviors.same();
                        }).build();
            });
        }
    }

    public static class CookieFabric {
        public static Behavior<Request> create() {
            return Behaviors.setup(ctx -> Behaviors.receive(Request.class)
                    .onMessage(Request.class, r -> onRequest(r, ctx))
                    .build());
        }

        private static Behavior<Request> onRequest(Request request, ActorContext<Request> ctx) {
            // processing request
            ctx.getLog().info("processing query " + request.query);
            request.replyTo.tell(new Response("Here is the cookie for " + request.query));
            return Behaviors.same();
        }

        public static class Request {
            public final String query;
            public final ActorRef<Response> replyTo;

            public Request(String query, ActorRef<Response> replyTo) {
                this.query = query;
                this.replyTo = replyTo;
            }
        }

        public static class Response {
            public final String result;

            public Response(String result) {
                this.result = result;
            }

            @Override
            public String toString() {
                return "Response{" +
                        "result='" + result + '\'' +
                        '}';
            }
        }
    }
}
