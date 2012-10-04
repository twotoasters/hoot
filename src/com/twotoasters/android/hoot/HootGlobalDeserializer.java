package com.twotoasters.android.hoot;

import java.io.InputStream;

public abstract class HootGlobalDeserializer {

    public HootGlobalDeserializer() {
        this(false);
    }

    public HootGlobalDeserializer(boolean isStreamDeserializer) {
        mIsStreamDeserializer = isStreamDeserializer;
    }

    public <T> T deserialize(InputStream is, Class<T> clazz) {
        return null;
    }

    public <T> T deserialize(String string, Class<T> clazz) {
        return null;
    }

    <T> T performDeserialize(InputStream is, Class<T> clazz) {
        return deserialize(is, clazz);
    }

    <T> T performDeserialize(String string, Class<T> clazz) {
        return deserialize(string, clazz);
    }

    boolean isStreamDeserializer() {
        return mIsStreamDeserializer;
    }

    private boolean mIsStreamDeserializer;

}
