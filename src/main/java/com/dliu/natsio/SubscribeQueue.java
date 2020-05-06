package com.dliu.natsio;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;

public class SubscribeQueue {
    // Use a latch to wait for 10 messages to arrive
    private static CountDownLatch latch = new CountDownLatch(10);

    public static void main(String[] args) throws InterruptedException {
        List<Connection> connections = startSubscriptions(10);
        // Wait for 10 messages to come in
        latch.await();

        for (Connection connection : connections) {
            connection.close();
        }
    }

    private static List<Connection> startSubscriptions(int count) {
        List<Connection> connections = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {

                Connection nc = NatsConnectionFactory.connect("nats://localhost:4222", "nats", "password");
                connections.add(nc);

                // Create a dispatcher and inline message handler
                Dispatcher d = nc.createDispatcher((msg) -> {
                    String str = new String(msg.getData(), StandardCharsets.UTF_8);
                    System.out.println("Connection: " + nc + " Thread: " + Thread.currentThread().getName() + " handling: " + str);
                    latch.countDown();
                });

                // Subscribe
                d.subscribe("updates", "workers");

                // [end subscribe_queue]
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return connections;
    }
}