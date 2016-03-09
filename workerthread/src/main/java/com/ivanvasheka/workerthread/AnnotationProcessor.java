package com.ivanvasheka.workerthread;

import com.ivanvasheka.workerthread.annotation.Subscribe;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class AnnotationProcessor {

    // Cache methods per class for subscribers.
    private static final ConcurrentMap<Class<?>, Set<Method>> SUBSCRIBERS_CACHE = new ConcurrentHashMap<>();

    private AnnotationProcessor() {
    }

    static Set<Method> getSubscribeMethods(Class<?> source) {
        if (!SUBSCRIBERS_CACHE.containsKey(source)) {
            process(source);
        }

        return SUBSCRIBERS_CACHE.get(source);
    }

    static void process(Class<?> source) {
        Set<Method> methods = SUBSCRIBERS_CACHE.get(source);
        if (methods == null) {
            methods = new HashSet<>();
        }

        for (Method method : source.getDeclaredMethods()) {
            if (method.isBridge()) {
                continue;
            }

            if (method.isAnnotationPresent(Subscribe.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new IllegalArgumentException("@Subscribe method " + method + " requires "
                            + parameterTypes.length + " arguments. @Subscribe methods must have one argument only.");
                }

                Class<?> eventType = parameterTypes[0];
                if (!Event.class.isAssignableFrom(eventType)) {
                    throw new IllegalArgumentException("@Subscribe method " + method + " must have an Event type parameter.");
                }

                if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                    throw new IllegalArgumentException("@Subscribe method " + method + " must be public.");
                }

                methods.add(method);
            }
        }

        SUBSCRIBERS_CACHE.put(source, methods);
    }
}
