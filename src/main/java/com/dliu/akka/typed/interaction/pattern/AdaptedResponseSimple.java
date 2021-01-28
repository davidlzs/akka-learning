package com.dliu.akka.typed.interaction.pattern;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

/**
 * Most of the time sending actor (requestor) does not, should not support receiving response defined in the service (CookieFabric) protocol,
 * so we introduce the "translator" - message adaptor to translate the service's response to the requestor's command
 *
 * Usecases:
 *  translating between different actor message protocols.
 *  subscribing to an actor that will send many response messages back
 * Problems:
 *  Hard to detect that a message request was not delivered or processed
 *  Only one adaption can be registered for a response type, if different target actors (service actor) share the same response type (protocol), unless you have some correlation
 *  in the messages.
 *  No context to correlate response to request, unless you include the context/correlation info in the messages.
 */
public class AdaptedResponseSimple {

    public static void main(String[] args) {
        ActorSystem.create(Guardian.create(), "guardian");
    }


    public static class Guardian {
        public static Behavior<WrappedResponse> create() {
            return Behaviors.setup(ctx -> {
                // spawn service actor: CookieFabric
                ActorRef<CookieFabric.Request> cookieFabric = ctx.spawn(CookieFabric.create(), "cookie_fabric");
                // send request to service actor
                //      messageAdapter translate the service's response to Guardian (requestor)'s protocol command
                ActorRef<CookieFabric.Response> replyTo = ctx.messageAdapter(CookieFabric.Response.class, WrappedResponse::new);
                cookieFabric.tell(new CookieFabric.Request("susie", replyTo));
                return Behaviors.receive(WrappedResponse.class)
                        .onMessage(WrappedResponse.class, (WrappedResponse res) -> {
                            ctx.getLog().info("got response {} ", res.response);
                            return Behaviors.same();
                        }).build();
            });
        }

        public static class WrappedResponse {
            public final CookieFabric.Response response;

            public WrappedResponse(CookieFabric.Response response) {
                this.response = response;
            }
        }
    }

    public static class CookieFabric {
        public static Behavior<CookieFabric.Request> create() {
            return Behaviors.setup(ctx -> Behaviors.receive(CookieFabric.Request.class)
                    .onMessage(CookieFabric.Request.class, r -> onRequest(r, ctx))
                    .build());
        }

        private static Behavior<CookieFabric.Request> onRequest(CookieFabric.Request request, ActorContext<CookieFabric.Request> ctx) {
            // processing request
            ctx.getLog().info("processing query " + request.query);
            request.replyTo.tell(new CookieFabric.Response("Here is the cookie for " + request.query));
            return Behaviors.same();
        }

        public static class Request {
            public final String query;
            public final ActorRef<CookieFabric.Response> replyTo;

            public Request(String query, ActorRef<CookieFabric.Response> replyTo) {
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
