package com.dliu.akka.dependencyinjection.playguice.domain;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.PFBuilder;

import javax.inject.Inject;

public class UserActor extends AbstractActor {
    public static Props props() {
        return Props.create(UserActor.class);
    }

    private final UserRepository userRepository;

    @Inject
    public UserActor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // COMMANDS
    public static class Cmd {}
    public static class GetUserCmd extends Cmd {
        public final String name;

        public GetUserCmd(String name) {
            this.name = name;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Cmd.class, this::handleCommand)
                .matchAny(this::unhandledMessage)
                .build();
    }

    private void handleCommand(Cmd cmd) {
        new PFBuilder<Cmd, Void>()
                .match(GetUserCmd.class, this::handelGetUserCmd)
                .build()
                .apply(cmd);
    }

    private Void  handelGetUserCmd(GetUserCmd cmd) {
        sender().tell(userRepository.retrieve(cmd.name), self());
        return null;
    }

    private void unhandledMessage(Object message) {
        System.out.println("Unhandled message received: " + message);
    }
}
