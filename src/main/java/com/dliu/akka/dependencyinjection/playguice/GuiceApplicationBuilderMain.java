package com.dliu.akka.dependencyinjection.playguice;

import play.Application;
import play.inject.guice.GuiceApplicationBuilder;


public class GuiceApplicationBuilderMain {
    public static void main(String[] args) {
        Application app = new GuiceApplicationBuilder()
                .bindings(new BootModule())
//                .bindings(bind(ActorSystem.class).toInstance(ActorSystem.create("injected-system")))
                .build();
    }
}
