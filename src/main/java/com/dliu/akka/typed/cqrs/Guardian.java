package com.dliu.akka.typed.cqrs;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.pubsub.Topic;

public class Guardian {
    private static ActorRef<Topic.Command<Message>> adminTopic;
    private static ActorRef<Message> subscriber;
    private ActorRef<Topic.Command<Message>> topic;
    public static Behavior<Void> create() {
        return Behaviors.setup(ctx -> {
            adminTopic = ctx.spawn(Topic.create(Message.class, "admin-topic"), "AdminTopic");
            subscriber = ctx.spawn(Subscriber.create(), "subscriber");
            ShoppingCart.init(ctx.getSystem());
            Ledger.init(ctx.getSystem());
            return Behaviors.empty();
        });
    }

    public static ActorRef<Topic.Command<Message>> getAdminTopic() {
        return adminTopic;
    }

    public static ActorRef<Message> getSubscriber() {
        return subscriber;
    }
}
