package com.dliu.akka.dependencyinjection.playguice;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.dliu.akka.dependencyinjection.playguice.domain.UserActor;
import com.dliu.akka.dependencyinjection.playguice.domain.UserRepository;
import com.google.inject.Guice;
import com.google.inject.Injector;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new BootModule());
        UserRepository userRepository = injector.getInstance(UserRepository.class);
        System.out.println(userRepository.retrieve("david"));

        ActorSystem system = ActorSystem.create("main-system");
//        ActorRef actorRef = system.actorOf(UserActor.props());
        ActorRef actorRef = injector.getInstance(play.api.inject.Injector.class).instanceOf(UserActor.class).getSelf();
        Timeout timeout = Timeout.create(Duration.ofSeconds(5));
        Future<Object> future = Patterns.ask(actorRef, new UserActor.GetUserCmd("david"), timeout);

        String result = (String) Await.result(future, timeout.duration());
        System.out.printf("Actor get user is " + result);

        System.in.read();
        system.terminate();
    }
}
