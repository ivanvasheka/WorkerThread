package com.ivanvasheka.workerthread;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class WorkerThread {

    private static volatile WorkerThread instance;

    private Executor executor;
    private Handler mainThread;

    private List<Event> events;
    private HashSet<String> tasks;
    private List<EventListener> subscribers;

    //region Singleton implementation

    private WorkerThread() {
        executor = Executors.newCachedThreadPool();
        mainThread = new Handler(Looper.getMainLooper());

        events = new ArrayList<>();
        tasks = new HashSet<>();
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

    //endregion

    //region Public methods

    /**
     * Executes a single task. Execution will go in a background (non ui) thread.
     *
     * @param task to be executed.
     */
    public void execute(@NonNull Runnable task) {
        executor.execute(task);
    }

    /**
     * Executes a single task. Execution will go in a background (non ui) thread.
     * Use <b>tag</b> parameter as a unique identifier to each separate task. Must not be null.
     * Can be used with method {@link WorkerThread#isRunning(String)} as a parameter
     * to check if task is still running.
     *
     * @param task to be executed.
     * @param tag  that represents this task.
     */
    public void execute(@NonNull final Runnable task, @NonNull String tag) {
        Task taskWrapper = new Task(tag) {
            @Override
            public void execute() {
                task.run();
            }
        };

        executor.execute(taskWrapper);
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

    /**
     * Checks whether task that was send to execution with <b>tag</b> is still running.
     *
     * @param tag that represents single task.
     * @return <b>true</b> task is running, <b>false</b> otherwise.
     */
    public boolean isRunning(@NonNull String tag) {
        return tasks.contains(tag);
    }

    /**
     * Check if there is undelivered event for the <b>subscriber</b>.
     * Note, that if there is undelivered event - it will be delivered through
     * {@link EventListener#onEvent(Event)} method.
     * This method should be called before {@link WorkerThread#subscribe(EventListener)}, cause
     * any undelivered events will be delivered when the appropriate subscriber is subscribed.
     *
     * @param subscriber .
     * @return <b>true</b> if there is undelivered event for this subscriber, <b>false</b> otherwise.
     */
    public boolean hasEvent(@NonNull EventListener subscriber) {
        for (Event event : events) {
            if (subscriber.getClass().equals(event.getSubscriber())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if there is undelivered event for the <b>subscriber</b> and with <b>number</b>.
     * Note, that if there is undelivered event - it will be delivered through
     * {@link EventListener#onEvent(Event)} method.
     * This method should be called before {@link WorkerThread#subscribe(EventListener)}, cause
     * any undelivered events will be delivered when the appropriate subscriber is subscribed.
     *
     * @param subscriber .
     * @param number     to compare with events number.
     * @return <b>true</b> if there is undelivered event with passed number for this subscriber,
     * <b>false</b> otherwise.
     */
    public boolean hasEventWithNumber(@NonNull EventListener subscriber, @NonNull Number number) {
        for (Event event : events) {
            if (subscriber.getClass().equals(event.getSubscriber())) {
                if (event.hasNumber()) {
                    //noinspection ConstantConditions
                    return event.getNumber().equals(number);
                }
            }
        }
        return false;
    }

    //endregion

    //region Package private methods

    void registerRunningTask(@NonNull String tag) {
        tasks.add(tag);
    }

    void unregisterRunningTask(@NonNull String tag) {
        tasks.remove(tag);
    }

    //endregion

    //region Private methods

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

    //endregion
}