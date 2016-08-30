package com.ivanvasheka.workerthread;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class WorkerThread {

    private static volatile WorkerThread instance;

    private Executor executor;
    private Handler mainThread;

    private List<Event> events;
    private List<EventListener> subscribers;

    private WorkerThread() {
        executor = Executors.newCachedThreadPool();
        mainThread = new Handler(Looper.getMainLooper());

        events = new ArrayList<>();
        subscribers = new ArrayList<>();
    }

    /**
     * Returns a WorkerThread instance.
     *
     * @return <b>WorkerTread</b> instance.
     */
    public static WorkerThread get() {
        if (instance == null) {
            synchronized (WorkerThread.class) {
                if (instance == null) {
                    instance = new WorkerThread();
                }
            }
        }

        return instance;
    }

    /**
     * Executes a single task. Execution will go in a background (non ui) thread.
     *
     * @param task to be execute.
     */
    public void execute(@NonNull Runnable task) {
        executor.execute(task);
    }

    /**
     * Removes all previously posted events for the passed subscriber. Pass null as a parameter to
     * remove events added for all subscribers.
     *
     * @param subscriber .
     */
    public synchronized void invalidate(@Nullable Class<?> subscriber) {
        Iterator<Event> iterator = events.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            if (subscriber == null) {
                if (event.getSubscriber() == null) {
                    iterator.remove();
                }
            } else {
                if (subscriber.equals(event.getSubscriber())) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Sets the {@link Event} to be delivered. If the event has subscriber, event will
     * be delivered to this subscriber only, if the event subscriber is null then the event will
     * be delivered to all subscribers if any. Event will be delivered right away if
     * the event subscriber is subscribed, or when the subscriber subscription happens.
     *
     * @param event to be delivered.
     */
    public synchronized void post(@NonNull Event event) {
        int type = event.getType();
        if (Event.TYPE_LATEST_ONLY == type) {
            removePreviousLatestEvents(event);
        }

        if (Event.TYPE_ONE_SHOT == type) {
            Class<?> eventSubscriber = event.getSubscriber();
            if (eventSubscriber == null) {
                for (EventListener subscriber : subscribers) {
                    deliverEvent(subscriber, event);
                }
            } else {
                for (EventListener subscriber : subscribers) {
                    if (subscriber.getClass().equals(eventSubscriber)) {
                        deliverEvent(subscriber, event);
                    }
                }
            }
        } else {
            events.add(event);
            deliverEvents();
        }
    }

    /**
     * Subscribes the subscriber to receive worker thread execution events.
     * The most appropriate scheme is to subscribe in onResume and unsubscribe in onPause
     * lifecycle methods.
     *
     * @param subscriber .
     */
    public void subscribe(@NonNull EventListener subscriber) {
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
            deliverEvents();
        }
    }

    /**
     * Unsubscribes the subscriber to receive worker thread execution events.
     * The most appropriate scheme is to subscribe in onResume and unsubscribe in onPause
     * lifecycle methods.
     *
     * @param subscriber .
     */
    public void unsubscribe(@NonNull EventListener subscriber) {
        if (subscribers.contains(subscriber)) {
            subscribers.remove(subscriber);
        }
    }

    private void deliverEvents() {
        if (!events.isEmpty() && !subscribers.isEmpty()) {
            Iterator<Event> iterator = events.iterator();
            while (iterator.hasNext()) {
                Event event = iterator.next();
                Class<?> eventSubscriber = event.getSubscriber();
                if (eventSubscriber == null) {
                    for (EventListener subscriber : subscribers) {
                        deliverEvent(subscriber, event);
                    }
                    iterator.remove();
                } else {
                    for (EventListener subscriber : subscribers) {
                        if (subscriber.getClass().equals(eventSubscriber)) {
                            deliverEvent(subscriber, event);
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    private void deliverEvent(final EventListener subscriber, final Event event) {
        if (event.useMainThread()) {
            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    subscriber.onEvent(event);
                }
            });
        } else {
            subscriber.onEvent(event);
        }
    }

    private void removePreviousLatestEvents(Event event) {
        Class<?> subscriber = event.getSubscriber();
        Iterator<Event> iterator = events.iterator();
        while (iterator.hasNext()) {
            Event previous = iterator.next();
            if (Event.TYPE_LATEST_ONLY == previous.getType()) {
                if (subscriber == null) {
                    if (previous.getSubscriber() == null) {
                        iterator.remove();
                        break;
                    }
                } else if (subscriber.equals(previous.getSubscriber())) {
                    iterator.remove();
                    break;
                }
            }
        }
    }
}