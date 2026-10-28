package com.ppd;

public class Consumer implements Runnable {
    private final BoundedQueue bq;
    private final int count;
    private long sum=0;

    public Consumer(BoundedQueue bq, int count) {
        this.bq = bq;
        this.count = count;
    }
    @Override
    public void run() {
        try {
            for(int i=0;i<count;i++){
                sum+=bq.take();
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    public long getSum() {
        return sum;
    }



}
