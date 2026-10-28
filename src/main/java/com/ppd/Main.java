package com.ppd;

import java.util.Random;

public class Main {


    public static void run(int queueSize){
        int n=1000;


        int[] a = new int[n];
        int[] b = new int[n];
        Random rand = new Random(42);
        for (int i = 0; i < n; i++) {
            a[i] = rand.nextInt(10) + 1;
            b[i] = rand.nextInt(10) + 1;
        }

        BoundedQueue bq = new BoundedQueue(queueSize);
        Producer producer=new Producer(a,b,bq);
        Consumer consumer=new Consumer(bq,n);

        Thread producerThread=new Thread(producer);
        Thread consumerThread=new Thread(consumer);

        long startTime = System.nanoTime();
        producerThread.start();
        consumerThread.start();
        try {
            producerThread.join();
            consumerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.nanoTime();

        long expected = 0;
        for (int i = 0; i < n; i++) {
            expected += (long) a[i] * b[i];
        }


        System.out.println("Queue size: " + queueSize);
        System.out.println("Suma: " + consumer.getSum());
        System.out.println("Verificare: " + expected);
        System.out.println("Timp: " + (endTime - startTime) / 1_000_000 + " ms");
    }

    public static void main(String[] args) {

        run(1);
        run(2);
        run(4);
        run(8);
        run(16);
        run(32);


    }
}