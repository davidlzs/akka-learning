package com.dliu.akka.typed.cqrs;

import akka.actor.typed.ActorRef;
import akka.cluster.typed.Cluster;
import akka.pattern.StatusReply;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;

import static com.dliu.akka.typed.cqrs.Ledger.*;

public class Main {
    private static Logger LOGGER;

    public static void main(String[] args) throws Exception {
        Config config = ConfigFactory.load("cqrs-shoppingcart.conf");
        ActorSystem<Void> shoppingSystem = ActorSystem.create(Guardian.create(), "dliuShopping", config);
        LOGGER = shoppingSystem.log();

        // Testing the event souring actor
        Thread.sleep(2000); // waiting for the shard started

        testSendCommandToShoppingCart(shoppingSystem);
        getClusterState(shoppingSystem);
    }

    private static void getClusterState(ActorSystem<?> system) {
        LOGGER.info("Cluster state: {}", Cluster.get(system).state());
    }

    private static void testSendCommandToShoppingCart(ActorSystem<Void> shoppingSystem) throws InterruptedException {

        ClusterSharding sharding = getSharding(shoppingSystem);
        sendingLedgerCommand(sharding);
        sendingCommand(sharding);
        Thread.sleep(4000);
        sendingCommand(sharding);
        sendingCommand(sharding);
    }

    private static void sendingLedgerCommand(ClusterSharding sharding) {
        EntityRef<Command> ledger = sharding.entityRefFor(ENTITY_TYPE_KEY, "GeneralLedger");
        CompletionStage<StatusReply<Result>> result = ledger.ask((ActorRef<StatusReply<Result>> replyTo) -> new Credit("david", BigDecimal.TEN, replyTo), Duration.ofSeconds(5));
        result.whenComplete((rst, err) -> {
            if (err != null) {
                LOGGER.error("Error", err);
            } else {
                LOGGER.info("{}", rst);
            }
        });
    }

    private static void sendingCommand(ClusterSharding sharding) {
        EntityRef<ShoppingCart.Command> dliuCartRef = sharding.entityRefFor(ShoppingCart.ENTITY_TYPE_KEY, "dliu-cart");
        LOGGER.info("Sending the command out...");
        List<Long> start = new ArrayList();
        CompletionStage<StatusReply<ShoppingCart.Summary>> result = dliuCartRef.ask(replyTo -> {
            LOGGER.info("Sending command in the lambda: " + replyTo);
            start.add(System.currentTimeMillis());
            return new ShoppingCart.AddItem("boots", 2, replyTo);
        }, Duration.ofSeconds(5));
        result.whenComplete((confirmation, error) -> {
            if (error == null) {
                if (confirmation.isSuccess()) {
                    LOGGER.info("Confirmation is : {} in {} ms", confirmation, (System.currentTimeMillis() - start.get(start.size() - 1)));
                } else {
                    LOGGER.error("Error message is : {} in {} ms", confirmation.getError().getMessage(), (System.currentTimeMillis() - start.get(start.size() - 1)));
                }
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
