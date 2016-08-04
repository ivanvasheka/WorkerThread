package com.ivanvasheka.workerthreadsample;

import com.ivanvasheka.workerthread.Event;

public class CustomEvent extends Event {

    public int T;

    public CustomEvent() {
        T = 100;
    }

    @Override
    public String toString() {
        return "CustomEvent{" +
                "T=" + T +
                '}';
    }
}
