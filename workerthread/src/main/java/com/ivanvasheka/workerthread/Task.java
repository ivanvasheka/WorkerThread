package com.ivanvasheka.workerthread;

import android.support.annotation.NonNull;

public abstract class Task implements Runnable {

    private String tag;

    public Task() {
        tag = getClass().getCanonicalName();
    }

    public Task(@NonNull String tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Task tag cannot be null.");
        }

        this.tag = tag;
    }

    @Override
    public void run() {
        WorkerThread.get().registerRunningTask(tag);
        try {
            execute();
        } finally {
            WorkerThread.get().unregisterRunningTask(tag);
        }
    }

    public abstract void execute();
}