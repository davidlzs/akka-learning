package com.dliu.akka.lab.requestreplydemo.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;

public class BackendResponseActor extends AbstractActor {

    private final ActorRef backendActor;
    private final ActorRef replyTo;

    public static Props props(ActorRef backendActor, ActorRef replyTo) {
        return Props.create(BackendResponseActor.class, backendActor, replyTo);
    }


    public BackendResponseActor(ActorRef backendActor, ActorRef replyTo) {
        this.backendActor = backendActor;
        this.replyTo = replyTo;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BackendProtocol.ExecuteCommandCmd.class, this::handleExecuteCommandCmd)
                .build();
    }

    private void handleExecuteCommandCmd(BackendProtocol.ExecuteCommandCmd cmd) {
        backendActor.forward(cmd, getContext());

        getContext().become(receiveBuilder()
                .match(BackendProtocol.ExecuteCommandResponse.class, response -> {
                    replyTo.tell(response, getSelf());
                    getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
                })
                .build());
    }

}
