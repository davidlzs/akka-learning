package com.dliu.akka.typed.cqrs;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class Guardian {

    public static Behavior<Void> create() {
        return Behaviors.setup(ctx -> {
            ShoppingCart.init(ctx.getSystem());
            return Behaviors.empty();
        });
    }
}
