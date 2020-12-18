package com.dliu.akka.typed.cqrs;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;

public class Main {
    public static void main(String[] args) throws Exception {
        Config config = ConfigFactory.load("cqrs-shoppingcart.conf");
        ActorSystem<Void> shoppingSystem = ActorSystem.create(Guardian.create(), "dliuShopping", config);

        Thread.sleep(2000); // waiting for the shard started
        ClusterSharding sharding = ClusterSharding.get(shoppingSystem);
        EntityRef<ShoppingCart.Command> dliuCartRef = sharding.entityRefFor(ShoppingCart.ENTITY_TYPE_KEY, "dliu-cart");
        dliuCartRef.tell(new ShoppingCart.AddItem("boots", 2));
    }
}
