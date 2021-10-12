package com.dliu.akka.lab.streams.operators;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class MapAsync {
    private final Source<Event, NotUsed> events =
            Source.fromIterator(() -> Stream.iterate(1, i -> i + 1).iterator())
                    .throttle(1, Duration.ofMillis(50))
                    .map(Event::new);
    private Random random = new Random();

    private ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 10, 0, TimeUnit.SECONDS, new SynchronousQueue(), new ThreadFactory() {
        private AtomicInteger counter = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("my-pool-thread-" + counter.getAndIncrement());
            return t;
        }
    });

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("mapasync");
        new MapAsync().runStream(system);
    }


    public CompletionStage<Integer> eventHandler(Event in) throws InterruptedException {
        System.out.println("Processing event number " + in + "... with thread " + Thread.currentThread().getName());
        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
        pool.execute(() -> {
            int anInt = random.nextInt(1000);
            try {
                Thread.sleep(anInt);
                System.out.println("complete the future in thread: " + Thread.currentThread().getName());
                completableFuture.complete(in.id);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        return completableFuture;
    }

    private void runStream(ActorSystem system) {
        events
                .mapAsync(2, this::eventHandler)
                .map(in -> "`mapSync` emitted event number " + in)
                .runWith(Sink.foreach(System.out::println), system);

    }

    private static class Event {
        private int id;

        public Event(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Event{" +
                    "id=" + id +
                    '}';
        }
    }
}
