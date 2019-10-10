package com.dliu.akka.dependencyinjection.playguice;

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.dliu.akka.dependencyinjection.playguice.domain.User;
import com.dliu.akka.dependencyinjection.playguice.domain.UserActor;
import com.dliu.akka.dependencyinjection.playguice.domain.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new BootModule());

        getUserFromRepository(injector);

        ActorRef actorRef = getActorRefViaNamedActorProxy(injector);
        getUserByNameFromActor(actorRef);
    }

    private static void getUserFromRepository(Injector injector) {
        UserRepository userRepository = injector.getInstance(UserRepository.class);
        LOGGER.info("From repository: {} ", userRepository.retrieve("david"));
    }

    private static void getUserByNameFromActor(ActorRef actorRef) throws Exception {
        Timeout timeout = Timeout.create(Duration.ofSeconds(5));
        Future<Object> future = Patterns.ask(actorRef, new UserActor.GetUserCmd("david"), timeout);

        User result = (User) Await.result(future, timeout.duration());
        LOGGER.info("From actor: {}", result);
    }

    private static ActorRef getActorRefViaNamedActorProxy(Injector injector) {
        return injector.getInstance(NamedActorGuiceProxy.class).getActor();
    }
}
