package com.dliu.natsio.natsstreaming;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import io.nats.streaming.NatsStreaming;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;

public class NatsStreamingPublisher {
    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {

        StreamingConnection sc = NatsStreaming.connect("test-cluster", "NatsStreamingPublisher", new Options.Builder()
                .natsUrl("nats://localhost:4223")
                .build());

        // This simple synchronous publish API blocks until an acknowledgement
        // is returned from the server.  If no exception is thrown, the message
        // has been stored in NATS streaming.
        int totalNumberOfMessages = 100;
        for (int i = 0; i < totalNumberOfMessages; i++) {
            String message = "Hello World " + i;
            sc.publish("foo", message.getBytes());
        }

        // Close the logical connection to NATS streaming
        sc.close();
    }
}
