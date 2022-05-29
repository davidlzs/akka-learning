package com.dliu.akka.typed.reliabledelivery.workpulling;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;

public class WorkPullingMain {
    public static void main(String[] args) {
        ActorSystem<ImageWorkManager.Command> system = ActorSystem.create(
                Behaviors.setup(ctx -> {
                    ctx.spawn(ImageConverter.create(), "worker");
                    return ImageWorkManager.create();
                }), "system");
        for(;;) {
            for (int i = 0; i < 1000; i++) {
                String from = String.format("jpg%04d", i);
                String to = String.format("gif%04d", i);
                system.tell(new ImageWorkManager.Convert(from, to, null));
            }

            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
