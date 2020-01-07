package com.dliu.natsio.natsstreaming;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import io.nats.streaming.Message;
import io.nats.streaming.MessageHandler;
import io.nats.streaming.NatsStreaming;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.Subscription;
import io.nats.streaming.SubscriptionOptions;

public class NatsStreamingPubSub {
    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {

        StreamingConnection sc = NatsStreaming.connect("test-cluster", "bar", new Options.Builder()
                .natsUrl("nats://localhost:4223")
                .build());

        // This simple synchronous publish API blocks until an acknowledgement
        // is returned from the server.  If no exception is thrown, the message
        // has been stored in NATS streaming.
        sc.publish("foo", "Hello World".getBytes());

        // Use a countdown latch to wait for our subscriber to receive the
        // message we published above.
        final CountDownLatch doneSignal = new CountDownLatch(1);

        // Simple Async Subscriber that retrieves all available messages.
        Subscription sub = sc.subscribe("foo", new MessageHandler() {

            public void onMessage(Message m) {
                System.out.printf("Received a message: %s\n", new String(m.getData()));
                doneSignal.countDown();
            }
        }, new SubscriptionOptions.Builder().deliverAllAvailable().build());

        doneSignal.await();

        // Unsubscribe to clean up
        sub.unsubscribe();

        // Close the logical connection to NATS streaming
        sc.close();
    }
}
