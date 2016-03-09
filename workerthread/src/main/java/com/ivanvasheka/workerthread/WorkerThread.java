package com.ivanvasheka.workerthread;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class WorkerThread {

    private static volatile WorkerThread instance;

    private Executor executor;
    private Handler mainThread;

    private List<Event> events;
    private List<Object> subscribers;

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

        if (Event.TYPE_ONE_SHOT != type) {
            events.add(event);
        }

        deliverEvents();
    }

    /**
     * Subscribes the subscriber to receive worker thread execution events.
     * The most appropriate scheme is to subscribe in onResume and unsubscribe in onPause
     * lifecycle methods.
     *
     * @param subscriber object that contains {@link com.ivanvasheka.workerthread.annotation.Subscribe}
     *                   annotated method(s).
     */
    public void subscribe(@NonNull Object subscriber) {
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
     * @param subscriber object that contains {@link com.ivanvasheka.workerthread.annotation.Subscribe}
     *                   annotated method(s).
     */
    public void unsubscribe(@NonNull Object subscriber) {
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
                    for (Object subscriber : subscribers) {
                        deliverEvent(subscriber, event);
                    }
                    iterator.remove();
                } else {
                    for (Object subscriber : subscribers) {
                        if (subscriber.getClass().equals(eventSubscriber)) {
                            deliverEvent(subscriber, event);
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    private void deliverEvent(final Object subscriber, final Event event) {
        Set<Method> methods = AnnotationProcessor.getSubscribeMethods(subscriber.getClass());
        if (methods != null) {
            for (final Method method : methods) {
                // Should be non empty array with one parameter type, if not
                // the AnnotationProcessor will throw exception before
                Class<?>[] params = method.getParameterTypes();
                if (params[0].equals(event.getClass())) {
                    if (event.useMainThread()) {
                        invokeInMainThread(method, subscriber, event);
                    } else {
                        invoke(method, subscriber, event);
                    }
                }
            }
        }
    }

    private void invoke(Method method, Object subscriber, Event event) {
        //noinspection TryWithIdenticalCatches
        try {
            method.invoke(subscriber, event);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void invokeInMainThread(final Method method, final Object subscriber, final Event event) {
        mainThread.post(new Runnable() {
                            @Override
                            public void run() {
                                invoke(method, subscriber, event);
                            }
                        }
        );
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