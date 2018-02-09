package com.luna.lunaframework;

/**
 * Created by huhu on 2018/2/8.
 */

public class SyncThread implements Runnable {
    private static int count;

    public SyncThread() {
        count = 0;
    }

    public void run() {
        /*synchronized (this) */{
            for (int i = 0; i < 5; i++) {
                try {
                    System.out.println(Thread.currentThread().getName() + ":" + (count++));
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getCount() {
        return count;
    }
}
