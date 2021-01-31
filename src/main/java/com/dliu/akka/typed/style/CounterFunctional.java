package com.dliu.akka.typed.style;

import java.util.concurrent.CountDownLatch;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import com.dliu.akka.typed.JsonSerializable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.typesafe.config.ConfigFactory;

public class CounterFunctional {
    private static CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) {
        ActorSystem.create(Behaviors.setup(ctx -> {
            ActorRef<Counter.Command> counter = ctx.spawn(Counter.create(), "counter");
            ActorRef<Counter.Value> valueProcessor = ctx.spawn(ValueProcessor.create(), "valueProcessor");
            for (int i = 0; i < 10000; i++) {
                counter.tell(Counter.Increment.INSTANCE);
            }
            counter.tell(new Counter.GetValue(valueProcessor));
            // wait for one success of response for GetValue then shutdown the actor system
            latch.await();
            return Behaviors.stopped();
        }), "system", ConfigFactory.load("application_force_message_serialization.conf"));
    }

    /**
     * Behavior to process the Value message returned for get value
     */
    public static class ValueProcessor {
        public static Behavior<Counter.Value> create() {

            return Behaviors.setup(ctx -> {
                return Behaviors.receive(Counter.Value.class)
                        .onMessage(Counter.Value.class, v -> {
                            ctx.getLog().info("got value {}", v);
                            latch.countDown();
                            return Behaviors.same();
                        })
                        .build();
            });
        }
    }

    /**
     * Behavior to serve the Increment and GetValue
     */
    public static class Counter {
        public static Behavior<Command> create() {
            return Behaviors.setup(ctx -> count(ctx, 0));
        }

        private static Behavior<Command> count(ActorContext<Command> ctx, int n) {
            return Behaviors.receive(Command.class)
                    .onMessage(Increment.class, (notUsed) -> onIncrement(ctx, n))
                    .onMessage(GetValue.class, (cmd) -> onGetValue(n, cmd))
                    .build();
        }

        private static Behavior<Command> onGetValue(int n, GetValue cmd) {
            cmd.replyTo.tell(new Value(n));
            return Behaviors.same();
        }

        // recursive to hold the state
        private static Behavior<Command> onIncrement(ActorContext<Command> ctx, int n) {
            return count(ctx, n + 1);
        }

        // protocol
        public interface Command extends JsonSerializable {
        }

        public enum Increment implements Command {
            INSTANCE
        }


        public static class GetValue implements Command {
            public final ActorRef<Value> replyTo;

            @JsonCreator
            public GetValue(ActorRef<Value> replyTo) {
                this.replyTo = replyTo;
            }
        }

        // end of protocol
        public static class Value implements JsonSerializable {
            public final int value;

            @JsonCreator
            public Value(int value) {
                this.value = value;
            }

            @Override
            public String toString() {
                return "Value{" +
                        "value=" + value +
                        '}';
            }
        }
    }

}
