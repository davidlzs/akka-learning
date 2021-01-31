package com.dliu.akka.typed.interaction.pattern.persessionchildactor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class Home {
    private final ActorContext<Command> context;
    private final ActorRef<KeyCabinet.GetKeys> keyCabinet;
    private final ActorRef<Drawer.GetWallet> drawer;

    private Home(ActorContext<Command> ctx) {
        this.context = ctx;
        this.keyCabinet = ctx.spawn(KeyCabinet.create(), "key-cabinet");
        this.drawer = ctx.spawn(Drawer.create(), "drawer");
    }

    public interface Command {
    }

    public static class LeaveHome implements Command {
        public final String who;
        public final ActorRef<ReadyToLeaveHome> respondTo;

        public LeaveHome(String who, ActorRef<ReadyToLeaveHome> respondTo) {
            this.who = who;
            this.respondTo = respondTo;
        }

        @Override
        public String toString() {
            return "LeaveHome{" +
                    "who='" + who + '\'' +
                    ", respondTo=" + respondTo +
                    '}';
        }
    }

    public static class ReadyToLeaveHome {
        public final String who;
        public final Keys keys;
        public final Wallet wallet;

        public ReadyToLeaveHome(String who, Keys keys, Wallet wallet) {
            this.who = who;
            this.keys = keys;
            this.wallet = wallet;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ctx -> new Home(ctx).behavior());
    }

    private Behavior<Command> behavior() {
        return Behaviors.receive(Command.class)
                .onMessage(LeaveHome.class, this::onLeaveHome)
                .build();
    }

    private Behavior<Command> onLeaveHome(LeaveHome message) {
        context.getLog().info("Leaving home {}", message);
        context.spawn(PrepareToLeaveHome.create(message.who, message.respondTo, keyCabinet, drawer), "leaving-" + message.who);
        return Behaviors.same();
    }
}
