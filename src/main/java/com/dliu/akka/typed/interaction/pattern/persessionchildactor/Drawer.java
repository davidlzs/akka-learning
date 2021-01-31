package com.dliu.akka.typed.interaction.pattern.persessionchildactor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class Drawer {
    public static Behavior<GetWallet> create() {
        return Behaviors.setup(ctx -> Behaviors.receiveMessage(msg -> Drawer.onGetWallet(ctx, msg)));
    }

    private static Behavior<GetWallet> onGetWallet(ActorContext<GetWallet> ctx, GetWallet message) {
        ctx.getLog().info("Drawer got message {}", message);
        message.replyTo.tell(new Wallet());
        return Behaviors.same();
    }

    public static class GetWallet {
        public final String whoseWallet;
        public final ActorRef<Result> replyTo;

        public GetWallet(String whoseWallet, ActorRef<Result> replyTo) {
            this.whoseWallet = whoseWallet;
            this.replyTo = replyTo;
        }

        @Override
        public String toString() {
            return "GetWallet{" +
                    "whoseWallet='" + whoseWallet + '\'' +
                    ", replyTo=" + replyTo +
                    '}';
        }
    }
}
