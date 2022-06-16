package com.dliu.akka.typed.cqrs;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Subscriber {
    private static Logger LOGGER = LoggerFactory.getLogger(Subscriber.class);
    public static Behavior<Message> create() {
        return Behaviors.receive(Message.class).onMessage(Message.class, (msg) -> {
            LOGGER.info("received message from distributed pubsub {}", msg);
            return Behaviors.same();
        }).build();
    }
}
