package com.github.afkbrb.ezhttp.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {

    private static final ThreadPoolExecutor executor;

    // 可以根据实际情况修改
    static {
        executor = new ThreadPoolExecutor(20, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(500), new ThreadPoolExecutor.DiscardPolicy());
    }

    public static void execute(Runnable task) {
        executor.execute(task);
    }
}
