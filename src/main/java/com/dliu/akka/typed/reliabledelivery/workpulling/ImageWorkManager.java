package com.dliu.akka.typed.reliabledelivery.workpulling;

import java.util.Optional;
import java.util.UUID;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.delivery.WorkPullingProducerController;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.StashBuffer;

public class ImageWorkManager {

    interface Command {}

    public static class Convert implements Command {
        public final String fromFormat;
        public final String toFormat;
        public final byte[] image;

        public Convert(String fromFormat, String toFormat, byte[] image) {
            this.fromFormat = fromFormat;
            this.toFormat = toFormat;
            this.image = image;
        }
    }

    public static class GetResult implements Command {
        public final UUID resultId;
        public final ActorRef<Optional<byte[]>> replyTo;

        public GetResult(UUID resultId, ActorRef<Optional<byte[]>> replyTo) {
            this.resultId = resultId;
            this.replyTo = replyTo;
        }
    }

    private static class WrappedRequestNext implements Command {
        final WorkPullingProducerController.RequestNext<ImageConverter.ConversionJob> next;

        private WrappedRequestNext(
                WorkPullingProducerController.RequestNext<ImageConverter.ConversionJob> next) {
            this.next = next;
        }
    }


    private final ActorContext<Command> context;
    private final StashBuffer<Command> stashBuffer;

    private ImageWorkManager(ActorContext<Command> context, StashBuffer<Command> stashBuffer) {
        this.context = context;
        this.stashBuffer = stashBuffer;
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(
                context -> {
                    ActorRef<WorkPullingProducerController.RequestNext<ImageConverter.ConversionJob>>
                            requestNextAdapter =
                            context.messageAdapter(
                                    WorkPullingProducerController.requestNextClass(), WrappedRequestNext::new);
                    ActorRef<WorkPullingProducerController.Command<ImageConverter.ConversionJob>>
                            producerController =
                            context.spawn(
                                    WorkPullingProducerController.create(
                                            ImageConverter.ConversionJob.class,
                                            "workManager",
                                            ImageConverter.serviceKey,
                                            Optional.empty()),
                                    "producerController");
                    producerController.tell(new WorkPullingProducerController.Start<>(requestNextAdapter));

                    return Behaviors.withStash(
                            1000, stashBuffer -> new ImageWorkManager(context, stashBuffer).waitForNext());
                });
    }

    private Behavior<Command> waitForNext() {
        return Behaviors.receive(Command.class)
                .onMessage(WrappedRequestNext.class, this::onWrappedRequestNext)
                .onMessage(Convert.class, this::onConvertWait)
                .onMessage(GetResult.class, this::onGetResult)
                .build();
    }

    private Behavior<Command> onWrappedRequestNext(WrappedRequestNext w) {
        return stashBuffer.unstashAll(active(w.next));
    }

    private Behavior<Command> onConvertWait(Convert convert) {
        if (stashBuffer.isFull()) {
            context.getLog().warn("Too many Convert requests.");
            return Behaviors.same();
        } else {
            stashBuffer.stash(convert);
            return Behaviors.same();
        }
    }

    private Behavior<Command> onGetResult(GetResult get) {
        // TODO retrieve the stored result and reply
        return Behaviors.same();
    }

    private Behavior<Command> active(
            WorkPullingProducerController.RequestNext<ImageConverter.ConversionJob> next) {
        return Behaviors.receive(Command.class)
                .onMessage(Convert.class, c -> onConvert(c, next))
                .onMessage(GetResult.class, this::onGetResult)
                .onMessage(WrappedRequestNext.class, this::onUnexpectedWrappedRequestNext)
                .build();
    }

    private Behavior<Command> onUnexpectedWrappedRequestNext(WrappedRequestNext w) {
        throw new IllegalStateException("Unexpected RequestNext");
    }

    private Behavior<Command> onConvert(
            Convert convert,
            WorkPullingProducerController.RequestNext<ImageConverter.ConversionJob> next) {
        UUID resultId = UUID.randomUUID();
        next.sendNextTo()
                .tell(
                        new ImageConverter.ConversionJob(
                                resultId, convert.fromFormat, convert.toFormat, convert.image));
        return waitForNext();
    }
}
