package com.ppd;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class ScalarBenchmark {
    @Param({"1", "2", "4", "8", "16", "32", "64", "128"})
    public int queueSize;

    int[] a;
    int[] b;

    @Setup
    public void setup() {
        int n = 100_000;
        a = new int[n];
        b = new int[n];
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            a[i] = rand.nextInt(10) + 1;
            b[i] = rand.nextInt(10) + 1;
        }
    }

    @Benchmark
    public long scalarProduct() throws InterruptedException {
        BoundedQueue bq = new BoundedQueue(queueSize);
        Consumer consumer = new Consumer(bq, a.length);
        Thread tp = new Thread(new Producer(a, b, bq));
        Thread tc = new Thread(consumer);
        tp.start();
        tc.start();
        tp.join();
        tc.join();
        return consumer.getSum();
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(ScalarBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
