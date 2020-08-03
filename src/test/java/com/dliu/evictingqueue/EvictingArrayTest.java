package com.dliu.evictingqueue;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

public class EvictingArrayTest {

    public static void main(String[] arg) {
        EvictingArray<Integer> queue = EvictingArray.create(10);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executorService.submit(new Producer(queue));
        }
        new Thread(new Consumer(queue)).start();
    }

    private static class Producer implements Runnable {

        private EvictingArray<Integer> array;

        public Producer(EvictingArray<Integer> array) {
            this.array = array;
        }

        @Override
        public void run() {
            Random random = new Random();
            while (true) {
                array.add(random.nextInt(100));
            }
        }
    }

    private static class Consumer implements Runnable {

        private EvictingArray<Integer> array;

        public Consumer(EvictingArray<Integer> array) {
            this.array = array;
        }

        @Override
        public void run() {
            Predicate predicate = new Predicate<Integer>() {
                @Override
                public boolean test(Integer integer) {
                    return integer < 50;
                }
            };
            while (true) {
                System.out.println("count:" + array.getQualifiedNums(predicate));
            }
        }
    }
}