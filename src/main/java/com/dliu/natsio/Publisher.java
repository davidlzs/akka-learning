package com.dliu.natsio;

import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import io.nats.client.Connection;
import io.nats.client.Nats;

public class Publisher {
    public static void main(String[] args) {

        try {
            // [begin publish_bytes]
            Connection nc = Nats.connect("nats://localhost:4222");
            for (int i = 0; i < 100; i++) {
                nc.publish("updates", ("All is Well " + i).getBytes(StandardCharsets.UTF_8));
                Thread.sleep(5000L);
            }
            // Make sure the message goes through before we close
            nc.flush(Duration.ZERO);
            nc.close();
            // [end publish_bytes]
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
