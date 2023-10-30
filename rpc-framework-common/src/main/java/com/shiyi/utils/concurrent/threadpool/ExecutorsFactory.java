package com.shiyi.utils.concurrent.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorsFactory implements ThreadFactory {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    private static String prefixName;

    public ExecutorsFactory(String name){
        prefixName = name;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable,prefixName + ATOMIC_INTEGER.getAndIncrement());
    }
}
