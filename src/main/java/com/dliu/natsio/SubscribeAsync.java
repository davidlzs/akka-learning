package com.dliu.natsio;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;

public class SubscribeAsync {
    public static void main(String[] args) {
        try (Connection nc = Nats.connect("nats://localhost:4222")) {
            CountDownLatch latch = new CountDownLatch(10);

            Dispatcher dispatcher = nc.createDispatcher((msg) -> {
                String str = new String(msg.getData());
                System.out.println(str);
                latch.countDown();
            });

            dispatcher.subscribe("updates");

            latch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
