package com.dliu.akka.lab.requestreplydemo;

import com.dliu.akka.lab.requestreplydemo.actors.BackendEchoActor;
import com.dliu.akka.lab.requestreplydemo.actors.BackendProtocol;
import com.dliu.akka.lab.requestreplydemo.actors.BackendRouterActor;

import java.time.Duration;
import java.util.UUID;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;

public class RequestReplyMain {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("reqeust-reply-system");

        ActorRef backendActor = system.actorOf(BackendEchoActor.props());

        ActorRef backendRouter = system.actorOf(BackendRouterActor.props(backendActor));

        UUID requestId = UUID.randomUUID();
        BackendProtocol.ExecuteCommandCmd cmd = new BackendProtocol.ExecuteCommandCmd(requestId, "getTemperature");

        try {
            Object result = Patterns.ask(backendRouter, cmd, Duration.ofSeconds(10))
                    .toCompletableFuture()
                    .join();
            System.out.println("response received" + result);
        } finally {
            system.terminate();
        }
    }

}
