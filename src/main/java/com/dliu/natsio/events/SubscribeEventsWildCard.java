package com.dliu.natsio.events;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;

public class SubscribeEventsWildCard {
    public static void main(String[] args) {
        try (Connection nc = Nats.connect("nats://localhost:4222")) {
            CountDownLatch latch = new CountDownLatch(100);

            Dispatcher dispatcher = nc.createDispatcher((msg) -> {
                String str = new String(msg.getData());
                System.out.println(str);
                latch.countDown();
            });

            //dispatcher.subscribe("gateway.>");
            //dispatcher.subscribe("gateway.*.>");
            //dispatcher.subscribe("gateway.e0b8c1d3-ab3b-454b-a8f7-86d34acc4594.>");
            //dispatcher.subscribe("gateway.*.*.*.lock");
            dispatcher.subscribe("gateway.*.*.*.Thermostat");
            //dispatcher.subscribe("gateway.*.*.*.*");

            latch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
