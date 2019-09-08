package com.fan.thread;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Created on 2019.9.8 17:46
 *
 * @author: Fan
 */
public class ThreadExample {
    private static final Logger logger = LoggerFactory.getLogger(ThreadExample.class);

    @Test
    public void test01() {
        // 在1.8以前，如果B线程依赖A线程的返回结果，我们需要自己做线程排序
        logger.info("主线程启动，等待其他线程返回结果");
        FutureTask<String> futureTask = new FutureTask<>(() -> "success");
        new Thread(futureTask).start();
        logger.info("do something other");
        String result = null;
        try {
            result = futureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        logger.info("返回的结果是 {}", result);
    }

    @Test
    public void test02() {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        Future<String> future = threadPool.submit(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "success";
        });
        threadPool.shutdown();
        logger.info("do something other");
        try {
            String result = future.get();
            logger.info("result is {}", result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test03() {
        String result = CompletableFuture.supplyAsync(()->{return "Hello ";}).thenApplyAsync(v -> v + "world").join();
        logger.info(result);
        CompletableFuture.supplyAsync(()->{return "Hello ";}).thenAccept(v -> { System.out.println("consumer: " + v);});
    }

    @Test
    public void test04() throws InterruptedException {
        Thread thread = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                if (Thread.interrupted()) {
                    System.out.println("interrupted");
                    break;
                }
                System.out.println("子线程" + "---" + i);
                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
        });
        thread.start();
        for (int i = 0; i < 50; i++) {
            System.out.println("main thread ---"+i);
            if (i == 20) {
                thread.interrupt();
            }
        }
    }

    @Test
    public void test05() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 2000; i++) {
                System.out.println("thread1--" + i);
            }
        });
        thread1.start();
        //thread1.join();
        System.out.println("end ");

    }

    @Test
    public void test06() {
        final Object lock = new Object();
        MyThread myThread = new MyThread(lock,"Thread-1");
        ThreadTwo myThread2 = new ThreadTwo(lock);
        myThread.start();
        myThread2.start();
        try {
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            myThread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class MyThread extends Thread {

    private final Object lock;

    public MyThread(Object lock, String name) {
        super(name);
        this.lock = lock;
    }

    @Override
    public void run() {
        synchronized (lock) {
            for (int i = 0; i < 50; i++) {
                System.out.println(Thread.currentThread().getName()+" ---"+i);
                if (i == 20) {
                    try {
                        System.out.println("线程锁了");
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }
}

class ThreadTwo extends Thread {
    private final Object lock;

    public ThreadTwo(Object lock) {
        this.lock = lock;
    }

    public void run() {
        synchronized (lock) {
            for (int i = 0; i < 50; i++) {
                System.out.println("Thread-2 --"+i);
                if (i ==20) {
                    System.out.println("unlock");
                    lock.notify();
                }
            }
        }
    }
}