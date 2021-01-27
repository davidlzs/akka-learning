package com.dliu.akka.typed.interaction.pattern;

import java.io.IOException;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

/**
 * Use cases:
 *  Not critical to be sure that the message was processed.
 *  There is no way to act on non successful delivery or processing.
 *  We want to minimize the number of messages created to get higher throughput(sending a response creating twice the number of messages)
 *
 *  Problems:
 *  If the inflow of messages is higher than the actor can process the inbox will fill up and can cause OOM Error.
 *  If the message got lost, the sender will not know.
 */
public class FireAndForget {
    public static void main(String[] args) throws IOException {
        ActorSystem<Printer.PrintMe> system = ActorSystem.create(Printer.create(), "fire_and_forget");

        // the actor system is also an actor reference to the Guardian actor
        ActorRef<Printer.PrintMe> ref = system;

        ref.tell(new Printer.PrintMe("First Message"));
        ref.tell(new Printer.PrintMe("Second Message"));

        System.in.read();
        system.terminate();;

    }

    public static class Printer {
        public static class PrintMe {
            private final String message;

            public PrintMe(String message) {
                this.message = message;
            }
        }

        public static Behavior<PrintMe> create() {
            return Behaviors.setup(ctx -> Behaviors
                    .receive(PrintMe.class)
                    .onMessage(PrintMe.class, (printMe) -> {
                        ctx.getLog().info(printMe.message);
                        return Behaviors.same();
                    })
                    .build());
        }
    }
}
