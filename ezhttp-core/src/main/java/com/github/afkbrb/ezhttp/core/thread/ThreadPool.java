package com.github.afkbrb.ezhttp.core.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {

    private static final ThreadPoolExecutor executor;

    /**
     * The configuration should be modified according the actual situation.
     */
    static {
        executor = new ThreadPoolExecutor(20, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(500), new ThreadPoolExecutor.DiscardPolicy());
    }

    public static void execute(Runnable task) {
        executor.execute(task);
    }
}
