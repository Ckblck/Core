package com.oldust.core.pool;

import lombok.Getter;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool extends ThreadPoolExecutor {
    @Getter
    private static ThreadPool instance;

    public ThreadPool() {
        super(0, Integer.MAX_VALUE, 50, TimeUnit.MILLISECONDS, new SynchronousQueue<>());

        setThreadFactory(factory -> new Thread(factory) {{
            setName("Dust Thread #" + getId());
        }});

        instance = this;
    }

}
