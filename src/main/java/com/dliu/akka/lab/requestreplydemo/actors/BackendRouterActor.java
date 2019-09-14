package com.dliu.akka.lab.requestreplydemo.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class BackendRouterActor extends AbstractActor {
    private final ActorRef backendActor;

    public static Props props(ActorRef backendActor) {
        return Props.create(BackendRouterActor.class, backendActor);
    }

    public BackendRouterActor(ActorRef backendActor) {
        this.backendActor = backendActor;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BackendProtocol.ExecuteCommandCmd.class, this::handleExecuteCommandCmd)
                .build();
    }


    private void handleExecuteCommandCmd(BackendProtocol.ExecuteCommandCmd cmd) {

        getContext().getSystem()
                .actorOf(BackendResponseActor.props(backendActor, sender()), cmd.correlationId.toString())
                .forward(cmd, getContext());
    }
}
