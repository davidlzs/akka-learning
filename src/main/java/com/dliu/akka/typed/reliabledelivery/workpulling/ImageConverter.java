package com.dliu.akka.typed.reliabledelivery.workpulling;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.delivery.ConsumerController;
import akka.actor.typed.delivery.DurableProducerQueue;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.ServiceKey;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class ImageConverter {
    interface Command {}

    public static class ConversionJob {
        public final UUID resultId;
        public final String fromFormat;
        public final String toFormat;
        public final byte[] image;

        public ConversionJob(UUID resultId, String fromFormat, String toFormat, byte[] image) {
            this.resultId = resultId;
            this.fromFormat = fromFormat;
            this.toFormat = toFormat;
            this.image = image;
        }
    }

    private static class WrappedDelivery implements Command {
        final ConsumerController.Delivery<ConversionJob> delivery;

        private WrappedDelivery(ConsumerController.Delivery<ConversionJob> delivery) {
            this.delivery = delivery;
        }
    }

    public static ServiceKey<ConsumerController.Command<ConversionJob>> serviceKey =
            ServiceKey.create(ConsumerController.serviceKeyClass(), "ImageConverter");

    public static Behavior<Command> create() {
        return Behaviors.setup(
                context -> {
                    ActorRef<ConsumerController.Delivery<ConversionJob>> deliveryAdapter =
                            context.messageAdapter(ConsumerController.deliveryClass(), WrappedDelivery::new);
                    ActorRef<ConsumerController.Command<ConversionJob>> consumerController =
                            context.spawn(ConsumerController.create(serviceKey), "consumerController");
                    consumerController.tell(new ConsumerController.Start<>(deliveryAdapter));

                    return Behaviors.receive(Command.class)
                            .onMessage(WrappedDelivery.class, ImageConverter::onDelivery)
                            .build();
                });
    }

    private static Behavior<Command> onDelivery(WrappedDelivery w) {
        byte[] image = w.delivery.message().image;
        String fromFormat = w.delivery.message().fromFormat;
        String toFormat = w.delivery.message().toFormat;
        // convert image...
        // store result with resultId key for later retrieval
        System.out.println("Converting from " + fromFormat + " to " + toFormat);

        //randomSleep();
        // and when completed confirm
        if (!randomFail()) {
            System.out.println("Succeeded converting from " + fromFormat + " to " + toFormat);
            //TODO: success confirm to ConsumerController, how to communicate failure to ConsumerController? or ImageConverter needs to retry? how does the producer know failure?
            w.delivery.confirmTo().tell(ConsumerController.confirmed());
        } else {
            System.out.println("Failed in converting from " + fromFormat + " to " + toFormat);
        }
        return Behaviors.same();
    }
    private static Random random = new Random();
    private static void randomSleep() {
        try {
            Thread.sleep(random.nextInt(100) * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static boolean randomFail() {
        int rand = random.nextInt(100);
        return rand < 80;
    }
}
