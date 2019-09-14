package com.dliu.akka.lab.requestreplydemo.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class BackendEchoActor extends AbstractActor {
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

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
        log.debug("processing: {}", cmd);
        getSender().tell(new BackendProtocol.ExecuteCommandResponse(cmd.correlationId, "processed " + cmd.command), self());
    }
}
