package com.dliu.akka.typed.interaction.pattern.persessionchildactor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class KeyCabinet {
    public static class GetKeys {

        public final String whoseKeys;
        public final ActorRef<Result> replyTo;
        public GetKeys(String whoseKeys, ActorRef<Result> replyTo) {
            this.whoseKeys = whoseKeys;
            this.replyTo = replyTo;
        }

        @Override
        public String toString() {
            return "GetKeys{" +
                    "whoseKeys='" + whoseKeys + '\'' +
                    ", replyTo=" + replyTo +
                    '}';
        }
    }
    public static Behavior<GetKeys> create() {
        return Behaviors.setup(ctx -> Behaviors.receiveMessage(msg -> KeyCabinet.onMessage(ctx, msg)));
    }

    private static Behavior<GetKeys> onMessage(ActorContext<GetKeys> ctx, GetKeys message) {
        ctx.getLog().info("KeyCabinet got message: {}", message);
        message.replyTo.tell(new Keys());
        return Behaviors.same();
    }
}
