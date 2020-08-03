package com.dliu.evictingqueue;

import com.google.common.collect.EvictingQueue;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EvictingQueueTest {

    @Test
    public void testEvictingQueue(){
        EvictingQueue<Integer> queue = EvictingQueue.create(5);
        for (int i = 0; i < 10; i++) {
            queue.add(i);
            System.out.println(String.format("Current queue size:%d，Elements in queue:%s",queue.size(),StringUtils.join(queue.iterator(), ',')));
        }
    }

    public static void main(String[] arg){
        EvictingQueue<Integer> queue = EvictingQueue.create(10);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        //Ten production threads continuously write data to the queue
        for(int i=0;i<10;i++){
            executorService.submit(new Producer(queue));
        }
        //A production thread continuously detects the number of satisfied conditions in the queue
        new Thread(new Consumer(queue)).start();
    }

    private static class Producer implements Runnable{

        private EvictingQueue<Integer> queue;

        public Producer(EvictingQueue<Integer> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            Random random = new Random();
            while (true){
                queue.add(random.nextInt(100));
            }
        }
    }

    private static class Consumer implements Runnable{

        private EvictingQueue<Integer> queue;

        public Consumer(EvictingQueue<Integer> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            while (true){
                int count = 0;
                Iterator<Integer> iterator = queue.iterator();
                while (iterator.hasNext()){
                    Integer integer = iterator.next();
                    if(integer < 50){
                        count++;
                    }
                }
                System.out.println("count:" + count);
            }
        }
    }
}
