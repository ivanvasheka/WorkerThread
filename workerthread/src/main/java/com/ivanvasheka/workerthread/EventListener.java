package com.ivanvasheka.workerthread;

public interface EventListener<T extends Event> {
    void onEvent(T event);
}
