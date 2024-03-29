package com.dliu.akka.typed.cqrs;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class Guardian {
    public static Behavior<Void> create() {
        return Behaviors.setup(ctx -> {
            DistributedPubSub.init(ctx);
            ShoppingCart.init(ctx.getSystem());
            Ledger.init(ctx.getSystem());
            return Behaviors.empty();
        });
    }
}
