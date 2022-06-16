package com.dliu.akka.typed.cqrs;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class Subscriber {
    public static Behavior<Message> create() {
        return Behaviors.receive(Message.class).onMessage(Message.class, (msg) -> {
            System.out.println(msg);
            return Behaviors.same();
        }).build();
    }
}
