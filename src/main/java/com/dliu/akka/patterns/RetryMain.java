package com.dliu.akka.patterns;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

import akka.actor.ActorSystem;
import akka.pattern.Patterns;

/*
Demo of Akka Retry Pattern https://doc.akka.io/docs/akka/current/futures.html#retry

 */
public class RetryMain {

    public static final int NUMBER_OF_FAILURES = 2;
    public static final int ATTEMPTS = 3;

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("retry-system");
        AtomicInteger attempts = new AtomicInteger(0);
        // simulate failed future call
        Callable<CompletionStage<String>> attempt = () -> {
            int i = attempts.incrementAndGet();
            System.out.println("calling... for try#: " + i);
            if (attempts.get() < NUMBER_OF_FAILURES) {
                throw new RuntimeException("Failed to response");
            }
            return CompletableFuture.completedFuture(UUID.randomUUID().toString());
        };

        CompletionStage<String> retriedFuture =
                Patterns.retry(attempt, ATTEMPTS, java.time.Duration.ofMillis(200), system.scheduler(), system.dispatcher());

        try {
            retriedFuture
                    .handle((ack, ex) -> {
                        if (ex != null) {
                            System.err.println("got ex: " + ex.getMessage());
                            throw new RuntimeException("Boom!");
                        }
                        else return ack;
                    }).toCompletableFuture() //.join()
                    /*.exceptionally((ex) -> {
                        System.err.println("failed " + ex.getMessage());
                        throw new RuntimeException("Exception happened with message " + ex.getMessage());
                    })*/
                    .thenAccept((s) -> System.out.println("got response " + s))
                    //.join() // when you join or get, it will block the main thread and the exception will be print out as java.util.concurrent.CompletionExcepiton(runtimeException)
                    ;
        } catch (Exception e) {
            System.err.println("got exception " + e);
        }
    }
}
