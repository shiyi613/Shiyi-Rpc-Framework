package com.shiyi.utils.concurrent.threadpool;

import java.util.concurrent.*;

public class CustomThreadPoolExecutor{

    public CustomThreadPoolExecutor() {
    }

    public static ThreadPoolExecutor getThreadPool(String name){
        return new ThreadPoolExecutor(10, 20, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(100), new ExecutorsFactory("thread-pool-" + name + "-"));
    }

}
