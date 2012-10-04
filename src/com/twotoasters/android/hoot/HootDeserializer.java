/*
 * Copyright (C) 2012 Two Toasters, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twotoasters.android.hoot;

import java.io.InputStream;

public abstract class HootDeserializer<T> {

    public HootDeserializer(boolean isStreamDeserializer) {
        mIsStreamDeserializer = isStreamDeserializer;
    }

    public T deserialize(String string) {
        return null;
    }

    public T deserialize(InputStream stream) {
        return null;
    }

    public boolean isStreamDeserializer() {
        return mIsStreamDeserializer;
    }

    // -------------------------------------------------------------------------
    // End of public interface
    // -------------------------------------------------------------------------
    private T mDeserializedStorage;
    private boolean mIsStreamDeserializer;

    public T getDeserializedResult() {
        return mDeserializedStorage;
    }

    T performDeserialize(InputStream responseStream) {
        mDeserializedStorage = deserialize(responseStream);
        return mDeserializedStorage;
    }

    T performDeserialize(String responseString) {
        mDeserializedStorage = deserialize(responseString);
        return mDeserializedStorage;
    }

}
