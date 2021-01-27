package com.dliu.akka.patterns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryMain.class);
    public static final int NUMBER_OF_FAILURES = 4;
    public static final int ATTEMPTS = 5;

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("retry-system");

        Callable<CompletionStage<String>> attempt = simulateFailedAttempts();
        CompletionStage<String> retriedFuture = fixedIntervalRetries(system, attempt);
        executeRetries(retriedFuture);

        Callable<CompletionStage<String>> backoffAttempt = simulateFailedAttempts();
        CompletionStage<String> backoffRetriedFuture = backOffRetries(system, backoffAttempt);
        executeRetries(backoffRetriedFuture);


    }

    private static CompletionStage<String> fixedIntervalRetries(ActorSystem system, Callable<CompletionStage<String>> attempt) {
        CompletionStage<String> retriedFuture =
                Patterns.retry(attempt, ATTEMPTS, java.time.Duration.ofMillis(200), system.scheduler(), system.dispatcher());
        return retriedFuture;
    }

    private static CompletionStage<String> backOffRetries(ActorSystem system, Callable<CompletionStage<String>> attempt) {
        Duration minBackoff = Duration.ofSeconds(1);
        Duration maxBackoff = Duration.ofSeconds(30);

        CompletionStage<String> retriedFuture =
                Patterns.retry(attempt, ATTEMPTS, minBackoff, maxBackoff, 0.2, system.scheduler(), system.dispatcher());
        return retriedFuture;
    }

    private static void executeRetries(CompletionStage<String> retriedFuture) {
        try {
            retriedFuture
                    .handle((ack, ex) -> {
                        if (ex != null) {
                            System.err.println("got ex: " + ex.getMessage());
                            throw new RuntimeException("Boom!");
                        }
                        else return ack;
                    }).toCompletableFuture() //.join()
                    .exceptionally((ex) -> {
                        LOGGER.error("failed ", ex);
                        throw new RuntimeException("Exception happened with message " + ex.getMessage());
                    })
                    .thenAccept((s) -> LOGGER.info("got response {}", s))
                    //.join() // when you join or get, it will block the main thread and the exception will be print out as java.util.concurrent.CompletionException(runtimeException)
                    ;
        } catch (Exception e) {
            System.err.println("got exception " + e);
        }
    }

    private static Callable<CompletionStage<String>> simulateFailedAttempts() {
        AtomicInteger attempts = new AtomicInteger(0);
        // simulate failed future call
        return () -> {
            int i = attempts.incrementAndGet();
            LOGGER.info("Serve attempt {}", i);
            if (attempts.get() < NUMBER_OF_FAILURES) {
                throw new RuntimeException("Failed to response");
            }
            return CompletableFuture.completedFuture(UUID.randomUUID().toString());
        };
    }
}
