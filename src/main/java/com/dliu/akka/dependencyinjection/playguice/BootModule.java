package com.dliu.akka.dependencyinjection.playguice;

import com.dliu.akka.dependencyinjection.playguice.domain.UserActor;
import com.dliu.akka.dependencyinjection.playguice.domain.UserRepository;
import com.dliu.akka.dependencyinjection.playguice.respository.InMemoryUserRepository;
import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;

public class BootModule extends AbstractModule implements AkkaGuiceSupport {
    @Override
    protected void configure() {
//        bind(ActorSystem.class).toInstance(ActorSystem.create("injected-system"));

//        bind(play.api.inject.Injector.class).to(GuiceInjector.class);

        bind(UserRepository.class).to(InMemoryUserRepository.class);

        bindActor(UserActor.class, "userActor");
    }
}
