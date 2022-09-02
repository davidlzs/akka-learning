package com.dliu.killrweather;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class Guardian {

    public static Behavior<Void> create(int httpPort) {
        return Behaviors.setup( ctx -> {
            // init sharding
            WeatherStation.initSharding(ctx.getSystem());
            // start http server
            ctx.getLog().info("needs to start http server on port: {}", httpPort);
            return Behaviors.empty();
        });
    }
}
