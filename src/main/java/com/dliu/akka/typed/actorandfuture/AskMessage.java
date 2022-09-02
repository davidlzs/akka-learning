package com.dliu.akka.typed.actorandfuture;

import akka.actor.typed.ActorRef;

public class AskMessage extends BaseMessage {
    public ActorRef<String> replyTo;

    public AskMessage(ActorRef<String> replyTo, String message) {
        super(message);
        this.replyTo = replyTo;
    }

    @Override
    public String toString() {
        return "Message{" +
                "replyTo=" + replyTo +
                ", message='" + message + '\'' +
                '}';
    }
}
