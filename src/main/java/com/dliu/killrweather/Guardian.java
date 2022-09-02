package com.dliu.killrweather;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class Guardian {

    public static Behavior<Void> create(int httpPort) {
        return Behaviors.setup( ctx -> {
            // init sharding
            WeatherStation.initSharding(ctx.getSystem());
            // start http server
            WeatherRoutes routes = new WeatherRoutes(ctx.getSystem());
            WeatherHttpServer.start(routes.weather(), httpPort, ctx.getSystem());
            return Behaviors.empty();
        });
    }
}
