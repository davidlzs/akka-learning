package com.dliu.natsio.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.dliu.natsio.GenericEventDto;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;

public class SubscribeEventsWildCard {

    private static Gson gson = new GsonBuilder().create();

    public static void main(String[] args) {
        try (Connection nc = Nats.connect("nats://localhost:4222")) {
            CountDownLatch latch = new CountDownLatch(10000);

            Dispatcher dispatcher = nc.createDispatcher((msg) -> {
                String str = new String(msg.getData());
                try {
                    GenericEventDto event = gson.fromJson(str, GenericEventDto.class);
                    System.out.println(str);
                    System.out.println(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            });

            //dispatcher.subscribe(">"); //
            dispatcher.subscribe("hub.>");
            //dispatcher.subscribe("hub.*.>");
            //dispatcher.subscribe("hub.e0b8c1d3-ab3b-454b-a8f7-86d34acc4594.>");
            //dispatcher.subscribe("hub.*.*.*.lock");
            //dispatcher.subscribe("hub.*.*.*.Thermostat");
            //dispatcher.subscribe("hub.*.*.*.*");

            latch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
