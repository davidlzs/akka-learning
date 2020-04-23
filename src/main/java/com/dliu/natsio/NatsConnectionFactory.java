package com.dliu.natsio;

import java.io.IOException;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;

public class NatsConnectionFactory {
    public static Connection connect(String url, String username, String password) {
        Options.Builder optionsBuilder = new Options.Builder().
                server(url);

        if (username != null && password != null) {
            optionsBuilder.userInfo(username.toCharArray(), password.toCharArray());
        }

        try {
            return Nats.connect(optionsBuilder.build());
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException((e));
        }
    }
}
