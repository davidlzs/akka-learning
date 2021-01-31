package com.dliu.akka.typed.interaction.pattern.persessionchildactor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;

public class Main {
    public static void main(String[] args) {
        ActorSystem.create(Behaviors.<Home.ReadyToLeaveHome>setup(ctx -> {
            ActorRef<Home.Command> home = ctx.spawn(Home.create(), "home");
            home.tell(new Home.LeaveHome("david", ctx.getSelf()));
            return Behaviors.receiveMessage((Home.ReadyToLeaveHome msg) -> {
                ctx.getLog().info("Ready to leave home for {}", msg.who);
                return Behaviors.stopped();
                //return Behaviors.same();
            });
        }), "system");
    }
}
