package com.dliu.akka.dependencyinjection.playguice;

import play.api.Application;
import play.api.inject.guice.GuiceApplicationBuilder;


public class GuiceApplicationBuilderMain {
    public static void main(String[] args) {
        Application app = new GuiceApplicationBuilder()
               // .bindings(new BootModule())
                .build();
    }
}
