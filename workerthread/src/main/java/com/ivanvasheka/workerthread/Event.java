package com.ivanvasheka.workerthread;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("unused")
public class Event {

    /**
     * Default event type. Allows to store multiple events for one subscriber.
     * If there are no suitable subscribers, event will wait for subscription of those.
     */
    public static final int TYPE_NONE = 0;

    /**
     * Events posted with this type will overwrite any previously stored events with this type for
     * the same subscriber. Useful for events, that contains, for example, progress updates.
     * If there are no suitable subscribers, event will wait for subscription of those.
     */
    public static final int TYPE_LATEST_ONLY = 1;

    /**
     * WorkerThread will try to deliver events of this type only once. If there are no suitable
     * subscribers, they will NOT be stored.
     */
    public static final int TYPE_ONE_SHOT = 2;

    @IntDef({TYPE_NONE, TYPE_LATEST_ONLY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    @Nullable
    private Class<?> subscriber;
    @Type
    private int type;
    private boolean useMainThread = true;

    @Nullable
    private String message;
    @Nullable
    private Number number;
    @Nullable
    private Bundle extra;
    @Nullable
    private Object data;

    public Event() {
    }

    public Event(@Nullable Class<?> subscriber) {
        this.subscriber = subscriber;
    }

    @Nullable
    public Number getNumber() {
        return number;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public Bundle getExtra() {
        return extra;
    }

    @Nullable
    public Object getData() {
        return data;
    }

    @Nullable
    public Class<?> getSubscriber() {
        return subscriber;
    }

    public void post() {
        WorkerThread.get().post(this);
    }

    @Type
    public int getType() {
        return type;
    }

    boolean useMainThread() {
        return useMainThread;
    }

    void setNumber(int number) {
        this.number = number;
    }

    void setMessage(@NonNull String message) {
        this.message = message;
    }

    void setExtra(@NonNull Bundle extra) {
        this.extra = extra;
    }

    void setData(@NonNull Object data) {
        this.data = data;
    }

    void setSubscriber(@NonNull Class<?> subscriber) {
        this.subscriber = subscriber;
    }

    void setType(@Type int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (type != event.type) return false;
        if (subscriber != null ? !subscriber.equals(event.subscriber) : event.subscriber != null)
            return false;
        if (message != null ? !message.equals(event.message) : event.message != null)
            return false;
        if (number != null ? !number.equals(event.number) : event.number != null) return false;
        //noinspection SimplifiableIfStatement
        if (extra != null ? !extra.equals(event.extra) : event.extra != null) return false;
        return !(data != null ? !data.equals(event.data) : event.data != null);

    }

    @Override
    public int hashCode() {
        int result = subscriber != null ? subscriber.hashCode() : 0;
        result = 31 * result + type;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (extra != null ? extra.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Event{" +
                "subscriber='" + subscriber + '\'' +
                ", type=" + type +
                ", number=" + number +
                ", message='" + message + '\'' +
                ", extra=" + extra +
                ", data=" + data +
                '}';
    }

    public static Builder toEveryone() {
        return new Event().new Builder();
    }

    public static Builder to(@NonNull Class<?> subscriber) {
        return new Event(subscriber).new Builder();
    }

    public class Builder {

        public Builder withType(@Type int type) {
            Event.this.type = type;
            return this;
        }

        public Builder withNumber(@NonNull Number number) {
            Event.this.number = number;
            return this;
        }

        public Builder withMessage(@NonNull String message) {
            Event.this.message = message;
            return this;
        }

        public Builder withExtra(@NonNull Bundle extra) {
            Event.this.extra = extra;
            return this;
        }

        public Builder withData(@NonNull Object data) {
            Event.this.data = data;
            return this;
        }

        /**
         * Indicates that this event should be delivered in the source thread.
         * By default events are delivered in main thread.
         *
         * @return event builder.
         */
        public Builder useSourceThread() {
            Event.this.useMainThread = false;
            return this;
        }

        public Event build() {
            return Event.this;
        }

        public void post() {
            Event.this.post();
        }
    }
}
