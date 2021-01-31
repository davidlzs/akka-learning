package com.dliu.akka.typed.interaction.pattern.persessionchildactor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Optional;

public class PrepareToLeaveHome extends AbstractBehavior<Result> {
    private String who;
    private ActorRef<Home.ReadyToLeaveHome> respondTo;
    private ActorRef<KeyCabinet.GetKeys> keyCabinet;
    private ActorRef<Drawer.GetWallet> drawer;
    private Optional<Keys> keys = Optional.empty();
    private Optional<Wallet> wallet = Optional.empty();

    public PrepareToLeaveHome(ActorContext<Result> ctx,
                              String who,
                              ActorRef<Home.ReadyToLeaveHome> respondTo,
                              ActorRef<KeyCabinet.GetKeys> keyCabinet,
                              ActorRef<Drawer.GetWallet> drawer) {
        super(ctx);

        this.who = who;
        this.respondTo = respondTo;
        this.keyCabinet = keyCabinet;
        this.drawer = drawer;
        // TODO: get keys and wallet
        keyCabinet.tell(new KeyCabinet.GetKeys(who, ctx.getSelf()));
        drawer.tell(new Drawer.GetWallet(who, ctx.getSelf()));
    }

    public static Behavior<Result> create(String who, ActorRef<Home.ReadyToLeaveHome> respondTo, ActorRef<KeyCabinet.GetKeys> keyCabinet, ActorRef<Drawer.GetWallet> drawer) {
        return Behaviors.setup(ctx -> new PrepareToLeaveHome(ctx, who, respondTo, keyCabinet, drawer));
    }

    @Override
    public Receive<Result> createReceive() {
        return newReceiveBuilder()
                .onMessage(Keys.class, this::onKeys)
                .onMessage(Wallet.class, this::onWallet)
                .build();
    }

    private Behavior<Result> onWallet(Wallet wallet) {
        this.wallet = Optional.of(wallet);
        return continueOrStop();
    }

    private Behavior<Result> onKeys(Keys keys) {
        this.keys = Optional.of(keys);
        return continueOrStop();
    }

    private Behavior<Result> continueOrStop() {
        if (!keys.isPresent() || !wallet.isPresent()) {
            return this;
        } else {
            respondTo.tell(new Home.ReadyToLeaveHome(who, keys.get(), wallet.get()));
            return Behaviors.stopped();
        }
    }
}

