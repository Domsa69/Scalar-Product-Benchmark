package com.ppd;

public class Producer implements Runnable {
    private final int[] a;
    private final int[] b;
    private final BoundedQueue bq;
    public Producer(int[] a, int[] b,BoundedQueue boundedQueue) {
        this.a = a;
        this.b = b;
        this.bq = boundedQueue;
    }

    @Override
    public void run() {
        try {
            for(int i=0;i<a.length;i++){
                long product=(long)a[i]*b[i];
                bq.put(product);
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }


}
