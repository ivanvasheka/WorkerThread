package com.ivanvasheka.workerthread;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("unused")
public class Event<T> {

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

    @IntDef({TYPE_NONE, TYPE_LATEST_ONLY, TYPE_ONE_SHOT})
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
    private T data;

    public Event() {
    }

    public Event(@Nullable Class<?> subscriber) {
        this.subscriber = subscriber;
    }

    //region Subscriber methods

    @Nullable
    public Class<?> getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(@NonNull Class<?> subscriber) {
        this.subscriber = subscriber;
    }

    //endregion

    //region Type methods

    @Type
    public int getType() {
        return type;
    }

    public void setType(@Type int type) {
        this.type = type;
    }

    //endregion

    //region Number methods

    @Nullable
    public Number getNumber() {
        return number;
    }

    public void setNumber(@Nullable Number number) {
        this.number = number;
    }

    public boolean hasNumber() {
        return number != null;
    }

    /**
     * Returns int value if the event was supplied with int as a number.
     *
     * @return <b>int</b> value of events number, or <b>0</b>.
     */
    public int getInt() {
        return getInt(0);
    }

    /**
     * Returns int value if the event was supplied with int as a number.
     *
     * @param defValue to return if number is missing.
     * @return <b>int</b> value of events number, or <b>defValue</b>.
     */
    public int getInt(int defValue) {
        if (number == null) {
            return defValue;
        } else {
            return number.intValue();
        }
    }

    //endregion

    //region Message methods

    @Nullable
    public String getMessage() {
        return message;
    }

    public void setMessage(@Nullable String message) {
        this.message = message;
    }

    /**
     * @return <b>true</b> if message not null, <b>false</b> otherwise
     */
    public boolean hasMessage() {
        return message != null;
    }

    //endregion

    //region Bundle methods

    @Nullable
    public Bundle getExtra() {
        return extra;
    }

    public void setExtra(@Nullable Bundle extra) {
        this.extra = extra;
    }

    /**
     * Returns true if the given key is contained in the mapping.
     *
     * @param key a String or null
     * @return true if the key is part of the mapping, false otherwise
     */
    public boolean hasExtra(String key) {
        return extra != null && extra.containsKey(key);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the
     * desired type exists for the given key.
     *
     * @param key          a String or null
     * @param defaultValue value to return if key does not exists
     * @return a boolean value
     */
    public boolean getBooleanExtra(String key, boolean defaultValue) {
        return extra == null ? defaultValue : extra.getBoolean(key, defaultValue);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the
     * desired type exists for the given key.
     *
     * @param key          a String or null
     * @param defaultValue value to return if key does not exists
     * @return a byte value
     */
    public byte getByteExtra(String key, byte defaultValue) {
        return extra == null ? defaultValue : extra.getByte(key, defaultValue);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the
     * desired type exists for the given key.
     *
     * @param key          a String or null
     * @param defaultValue value to return if key does not exists
     * @return a char value
     */
    public char getCharExtra(String key, char defaultValue) {
        return extra == null ? defaultValue : extra.getChar(key, defaultValue);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the
     * desired type exists for the given key.
     *
     * @param key          a String or null
     * @param defaultValue value to return if key does not exists
     * @return a short value
     */
    public short getShortExtra(String key, short defaultValue) {
        return extra == null ? defaultValue : extra.getShort(key, defaultValue);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the
     * desired type exists for the given key.
     *
     * @param key          a String or null
     * @param defaultValue value to return if key does not exists
     * @return a integer value
     */
    public int getIntExtra(String key, int defaultValue) {
        return extra == null ? defaultValue : extra.getInt(key, defaultValue);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the
     * desired type exists for the given key.
     *
     * @param key          a String or null
     * @param defaultValue value to return if key does not exists
     * @return a long value
     */
    public long getLongExtra(String key, long defaultValue) {
        return extra == null ? defaultValue : extra.getLong(key, defaultValue);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the
     * desired type exists for the given key.
     *
     * @param key          a String or null
     * @param defaultValue value to return if key does not exists
     * @return a float value
     */
    public float getFloatExtra(String key, float defaultValue) {
        return extra == null ? defaultValue : extra.getFloat(key, defaultValue);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the
     * desired type exists for the given key.
     *
     * @param key          a String or null
     * @param defaultValue value to return if key does not exists
     * @return a double value
     */
    public double getDoubleExtra(String key, double defaultValue) {
        return extra == null ? defaultValue : extra.getDouble(key, defaultValue);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the
     * desired type exists for the given key.
     *
     * @param key a String or null
     * @return a String value or null
     */
    public String getStringExtra(String key) {
        return extra == null ? null : extra.getString(key);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the
     * desired type exists for the given key.
     *
     * @param key a String or null
     * @return a CharSequence value or null
     */
    public CharSequence getCharSequenceExtra(String key) {
        return extra == null ? null : extra.getCharSequence(key);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the
     * desired type exists for the given key.
     *
     * @param key a String or null
     * @return a Parcelable value or null
     */
    public <E extends Parcelable> E getParcelableExtra(String key) {
        return extra == null ? null : extra.<E>getParcelable(key);
    }

    /**
     * Adds a boolean value to the extra, replacing any existing value for the given key.
     * Added boolean can be obtained by calling getBooleanExtra method with given key.
     *
     * @param key   a String or null
     * @param value a boolean
     */
    public void putExtra(String key, boolean value) {
        if (extra == null) extra = new Bundle();
        extra.putBoolean(key, value);
    }

    /**
     * Adds a byte value to the extra, replacing any existing value for the given key.
     * Added byte can be obtained by calling getByteExtra method with given key.
     *
     * @param key   a String or null
     * @param value a byte
     */
    public void putExtra(String key, byte value) {
        if (extra == null) extra = new Bundle();
        extra.putByte(key, value);
    }

    /**
     * Adds a char value to the extra, replacing any existing value for the given key.
     * Added char can be obtained by calling getCharExtra method with given key.
     *
     * @param key   a String or null
     * @param value a char
     */
    public void putExtra(String key, char value) {
        if (extra == null) extra = new Bundle();
        extra.putChar(key, value);
    }

    /**
     * Adds a short value to the extra, replacing any existing value for the given key.
     * Added short can be obtained by calling getShortExtra method with given key.
     *
     * @param key   a String or null
     * @param value a short
     */
    public void putExtra(String key, short value) {
        if (extra == null) extra = new Bundle();
        extra.putShort(key, value);
    }

    /**
     * Adds an integer value to the extra, replacing any existing value for the given key.
     * Added integer can be obtained by calling getIntExtra method with given key.
     *
     * @param key   a String or null
     * @param value an int
     */
    public void putExtra(String key, int value) {
        if (extra == null) extra = new Bundle();
        extra.putInt(key, value);
    }

    /**
     * Adds a long value to the extra, replacing any existing value for the given key.
     * Added long can be obtained by calling getLongExtra method with given key.
     *
     * @param key   a String or null
     * @param value a long
     */
    public void putExtra(String key, long value) {
        if (extra == null) extra = new Bundle();
        extra.putLong(key, value);
    }

    /**
     * Adds a float value to the extra, replacing any existing value for the given key.
     * Added float can be obtained by calling getFloatExtra method with given key.
     *
     * @param key   a String or null
     * @param value a float
     */
    public void putExtra(String key, float value) {
        if (extra == null) extra = new Bundle();
        extra.putFloat(key, value);
    }

    /**
     * Adds a double value to the extra, replacing any existing value for the given key.
     * Added double can be obtained by calling getDoubleExtra method with given key.
     *
     * @param key   a String or null
     * @param value a double
     */
    public void putExtra(String key, double value) {
        if (extra == null) extra = new Bundle();
        extra.putDouble(key, value);
    }

    /**
     * Adds a String value to the extra, replacing any existing value for the given key.
     * Added String can be obtained by calling getStringExtra method with given key.
     *
     * @param key   a String or null
     * @param value a String or null
     */
    public void putExtra(String key, String value) {
        if (extra == null) extra = new Bundle();
        extra.putString(key, value);
    }

    /**
     * Adds a CharSequence value to the extra, replacing any existing value for the given key.
     * Added CharSequence can be obtained by calling getCharSequenceExtra method with given key.
     *
     * @param key   a String or null
     * @param value a CharSequence or null
     */
    public void putExtra(String key, CharSequence value) {
        if (extra == null) extra = new Bundle();
        extra.putCharSequence(key, value);
    }

    /**
     * Adds a Parcelable value to the extra, replacing any existing value for the given key.
     * Added Parcelable can be obtained by calling getParcelableExtra method with given key.
     *
     * @param key   a String or null
     * @param value a Parcelable or null
     */
    public void putExtra(String key, Parcelable value) {
        if (extra == null) extra = new Bundle();
        extra.putParcelable(key, value);
    }

    /**
     * Removes mapped value from the extra for the given key.
     *
     * @param key a String or null
     */
    public void removeExtra(String key) {
        if (extra == null) extra = new Bundle();
        extra.remove(key);
    }

    //endregion

    //region Data methods

    @Nullable
    public T getData() {
        return data;
    }

    public void setData(@Nullable T data) {
        this.data = data;
    }

    //endregion

    boolean useMainThread() {
        return useMainThread;
    }

    public void post() {
        WorkerThread.get().post(this);
    }

    //region Builder pattern realisation

    public static <T> Builder<T> toEveryone() {
        return new Builder<>();
    }

    public static <T> Builder<T> to(@NonNull Class<?> subscriber) {
        return new Builder<>(subscriber);
    }

    public static class Builder<T> {

        private Event<T> event;

        public Builder() {
            event = new Event<>();
        }

        public Builder(@NonNull Class<?> subscriber) {
            event = new Event<>(subscriber);
        }

        public Builder<T> withType(@Type int type) {
            event.type = type;
            return this;
        }

        public Builder<T> latestOnly() {
            event.type = TYPE_LATEST_ONLY;
            return this;
        }

        public Builder<T> oneShot() {
            event.type = TYPE_ONE_SHOT;
            return this;
        }

        public Builder<T> withNumber(@Nullable Number number) {
            event.number = number;
            return this;
        }

        public Builder<T> withMessage(@Nullable String message) {
            event.message = message;
            return this;
        }

        public Builder<T> withExtra(@Nullable Bundle extra) {
            event.extra = extra;
            return this;
        }

        public Builder<T> withData(@Nullable T data) {
            event.data = data;
            return this;
        }

        /**
         * Indicates that this event should be delivered in the source thread.
         * By default events are delivered in main thread.
         *
         * @return event builder.
         */
        public Builder<T> useSourceThread() {
            event.useMainThread = false;
            return this;
        }

        public Event<T> build() {
            return event;
        }

        public void post() {
            event.post();
        }
    }

    //endregion

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
}
