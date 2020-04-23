package com.dliu.natsio;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import io.nats.client.Connection;
import io.nats.client.Message;


/**
 * RequestReply needs the other side: SubscribeWithReply running
 */
public class RequestReply {
    public static void main(String[] args) {

        try {
            // [begin request_reply]
            Connection nc = NatsConnectionFactory.connect("nats://localhost:4222", "nats", "password");

            // Send the request
            Duration timeout = Duration.ofSeconds(1);
            Message msg = nc.request("time", null, timeout);

            // Use the response
            if (msg == null) {
                System.out.println("Failed to receive reply after " + timeout);
            } else {
                System.out.println(new String(msg.getData(), StandardCharsets.UTF_8));
            }

            // Close the connection
            nc.close();
            // [end request_reply]
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
