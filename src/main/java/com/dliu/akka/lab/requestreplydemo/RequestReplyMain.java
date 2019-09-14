package com.dliu.akka.lab.requestreplydemo;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import com.dliu.akka.lab.requestreplydemo.actors.BackendEchoActor;
import com.dliu.akka.lab.requestreplydemo.actors.BackendProtocol;
import com.dliu.akka.lab.requestreplydemo.actors.BackendRouterActor;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class RequestReplyMain {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("reqeust-reply-system");

        ActorRef backendActor = system.actorOf(BackendEchoActor.props());

        ActorRef backendRouter = system.actorOf(BackendRouterActor.props(backendActor));

        UUID requestId = UUID.randomUUID();
        BackendProtocol.ExecuteCommandCmd cmd = new BackendProtocol.ExecuteCommandCmd(requestId, "getTemperature");

        Inbox inbox = Inbox.create(system);
        inbox.send(backendRouter, cmd);

        try {
            System.out.println("response received" + inbox.receive(Duration.ofSeconds(10)));
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            system.terminate();
        }
    }

}
