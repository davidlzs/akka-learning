package com.dliu.akka.typed.cqrs;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;

public class Main {
    private static Logger LOGGER;

    public static void main(String[] args) throws Exception {
        Config config = ConfigFactory.load("cqrs-shoppingcart.conf");
        ActorSystem<Void> shoppingSystem = ActorSystem.create(Guardian.create(), "dliuShopping", config);
        LOGGER = shoppingSystem.log();

        // Testing the event souring actor
        Thread.sleep(2000); // waiting for the shard started

        testSendCommandToShoppingCart(shoppingSystem);
    }

    private static void testSendCommandToShoppingCart(ActorSystem<Void> shoppingSystem) throws InterruptedException {

        ClusterSharding sharding = getSharding(shoppingSystem);
        sendingCommand(sharding);
        //Thread.sleep(4000);
        sendingCommand(sharding);
        sendingCommand(sharding);
    }

    private static void sendingCommand(ClusterSharding sharding) {
        EntityRef<ShoppingCart.Command> dliuCartRef = sharding.entityRefFor(ShoppingCart.ENTITY_TYPE_KEY, "dliu-cart");
        LOGGER.info("Sending the command out...");
        List<Long> start = new ArrayList();
        CompletionStage<ShoppingCart.Confirmation> result = dliuCartRef.ask(replyTo -> {
            LOGGER.info("Sending command in the lambda: " + replyTo);
            start.add(System.currentTimeMillis());
            return new ShoppingCart.AddItem("boots", 2, replyTo);
        }, Duration.ofSeconds(5));
        result.whenComplete((confirmation, error) -> {
            if (error == null) {
                LOGGER.info("Confirmation is : {} in {} ms", confirmation, (System.currentTimeMillis() - start.get(start.size() - 1)));
            } else {
                LOGGER.error("Error is: {} in {} ms", error, (System.currentTimeMillis() - start.get(start.size() -1)));
            }
        });
    }

    private static ClusterSharding getSharding(ActorSystem<Void> shoppingSystem) {
        ClusterSharding sharding = ClusterSharding.get(shoppingSystem);
        return sharding;
    }
}
