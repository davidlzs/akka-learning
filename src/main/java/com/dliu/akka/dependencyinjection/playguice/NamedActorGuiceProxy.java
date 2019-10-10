package com.dliu.akka.dependencyinjection.playguice;

import javax.inject.Inject;
import javax.inject.Named;

import akka.actor.ActorRef;

public class NamedActorGuiceProxy {
    private ActorRef actorRef;

    @Inject
    public NamedActorGuiceProxy(@Named("userActor") ActorRef actorRef) {
        this.actorRef = actorRef;
    }

    public ActorRef getActor() {
        return actorRef;
    }
}
