package com.dliu.akka.typed.cqrs;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.pubsub.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributedPubSub {
    private static Logger LOGGER = LoggerFactory.getLogger(DistributedPubSub.class);
    private static DistributedPubSub INSTANCE;
    private final ActorRef<Topic.Command<Message>> adminTopic;
    private final ActorRef<Message> subscriber;

    private DistributedPubSub(ActorContext<?> ctx) {
        adminTopic = ctx.spawn(Topic.create(Message.class, "admin-topic"), "AdminTopic");
        subscriber = ctx.spawn(Subscriber.create(), "subscriber");
    }

    public static void init(ActorContext<?> ctx) {
        if (INSTANCE == null) {
            INSTANCE = new DistributedPubSub(ctx);
        }
    }

    public static DistributedPubSub getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("INSTANCE does not exist, did you forget to init the DistributedPubsub?");
        }
        return INSTANCE;
    }

    public ActorRef<Topic.Command<Message>> getAdminTopic() {
        return adminTopic;
    }

    public ActorRef<Message> getSubscriber() {
        return subscriber;
    }
}
