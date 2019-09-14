package com.dliu.akka.lab.requestreplydemo.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class BackendEchoActor extends AbstractActor {

    public static Props props() {
        return Props.create(BackendEchoActor.class);
    }

    public BackendEchoActor() {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BackendProtocol.ExecuteCommandCmd.class, this::handleExecuteCommandCmd)
                .build();
    }

    private void handleExecuteCommandCmd(BackendProtocol.ExecuteCommandCmd cmd) {
        getSender().tell(new BackendProtocol.ExecuteCommandResponse(cmd.correlationId, "processed " + cmd.command), self());
    }
}
