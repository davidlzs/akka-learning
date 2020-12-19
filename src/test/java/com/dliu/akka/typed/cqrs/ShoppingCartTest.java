package com.dliu.akka.typed.cqrs;

import org.junit.ClassRule;
import org.junit.Test;

import java.time.Duration;
import java.util.UUID;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.ReceiveBuilder;

public class ShoppingCartTest {
    @ClassRule
    public static TestKitJunitResource testKit = new TestKitJunitResource(
            "akka.persistence.journal.plugin = \"akka.persistence.journal.inmem\" \n" +
                    "akka.persistence.snapshot-store.plugin = \"akka.persistence.snapshot-store.local\"  \n" +
                    "akka.persistence.snapshot-store.local.dir = \"target/snapshot-" + UUID.randomUUID().toString() + "\"  \n"
    );

    private static String newCartId() {
        return "cart-" + 1;
    }

    @Test
    public void shouldAddItem() {
        // setup
        ActorRef<ShoppingCart.Command> cart = testKit.spawn(ShoppingCart.create(newCartId()));
        TestProbe<ShoppingCart.Confirmation> probe = testKit.createTestProbe();
        // execute
        long start = System.currentTimeMillis();
        cart.tell(new ShoppingCart.AddItem("foo", 42, probe.getRef()));
        // assert
        ShoppingCart.Confirmation result = probe.expectMessageClass(ShoppingCart.Confirmation.class);
        System.out.println("Got result " + result + " in " + (System.currentTimeMillis() - start) + " ms");
    }

    private static Behavior<Command> dummyBehavior() {
        return Behaviors.receive(Command.class).onMessage(Get.class, cmd -> {
            System.out.println("got message " + cmd);
            return Behaviors.same();
        }).build();
    }

    @Test
    public void plainTypedActorAsk() {
        // setup
        ActorSystem<Command> system = ActorSystem.create(
                Behaviors.setup(ctx ->
                                        Behaviors.receiveMessage(cmd -> {
                                            ctx.getLog().info("received");
                                            return Behaviors.same();
                                        })), "test");


        // execute
        //AskPattern.ask(system, Get::new, Duration.ofSeconds(2));
    }

    public interface Command {
    }

    public static final class Get implements Command {
        public ActorRef<Object> replyTo;

        public Get(ActorRef<Object> replyTo) {
            this.replyTo = replyTo;
        }
    }
}