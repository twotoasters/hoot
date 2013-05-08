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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

public class HootResult {

    private static final String TAG = HootResult.class.getSimpleName();

    public boolean isSuccess() {
        return mSuccessfulResponseCodes != null ? mSuccessfulResponseCodes
                .contains(mResponseCode) : sDefaultSuccessfulCodes
                .contains(mResponseCode);
    }

    public String getResponseString() {
        return mResponse;
    }

    public int getResponseCode() {
        return mResponseCode;
    }

    public Exception getException() {
        return mException;
    }

    public Map<String, List<String>> getHeaders() {
        return mHeaders;
    }

    public Object getDeserializedResult() {
        return mDeserializedResult;
    }

    // -------------------------------------------------------------------------
    // END OF PUBLIC INTERFACE
    // -------------------------------------------------------------------------
    private static List<Integer> sDefaultSuccessfulCodes = new ArrayList<Integer>();
    static {
        sDefaultSuccessfulCodes.add(HttpURLConnection.HTTP_OK);
        sDefaultSuccessfulCodes.add(HttpURLConnection.HTTP_ACCEPTED);
        sDefaultSuccessfulCodes.add(HttpURLConnection.HTTP_CREATED);
    }
    private List<Integer> mSuccessfulResponseCodes = null;
    private String mResponse;
    private int mResponseCode;
    private Exception mException;
    private Map<String, List<String>> mHeaders;
    private InputStream mResponseStream;
    private HootDeserializer<?> mDeserializer;
    private Object mDeserializedResult;

    <T> void setDeserializer(HootDeserializer<T> deserializer) {
        mDeserializer = deserializer;
    }

    void setSuccessfulResponseCodes(List<Integer> codes) {
        mSuccessfulResponseCodes = codes;
    }

    void setResponse(String response) {
        mResponse = response;
    }

    void setResponseCode(int responseCode) {
        mResponseCode = responseCode;
    }

    void setException(Exception e) {
        mException = e;
    }

    void setHeaders(Map<String, List<String>> headerFields) {
        mHeaders = headerFields;
    }

    void setResponseStream(InputStream responseStream) {
        mResponseStream = responseStream;
    }

    InputStream getResponseStream() {
        return mResponseStream;
    }

    void deserializeResult(HootGlobalDeserializer globalDeserializer,
            Class<?> expectedType) throws IOException {
        Log.v(TAG,
                globalDeserializer == null ? (mDeserializer == null ? "No deserializer"
                        : "Request deserializer")
                        : "Global deserializer");
        // see if we need to read out to a string. Either we have no
        // deserializer or we do and it operates on a string.
        if (mDeserializer == null
                && globalDeserializer == null
                || (mDeserializer != null && !mDeserializer
                        .isStreamDeserializer())
                || (mDeserializer == null && globalDeserializer != null
                        && expectedType != null && !globalDeserializer
                            .isStreamDeserializer())) {
            setResponse(convertStreamToString(getResponseStream()));
        }

        if (mDeserializer != null) {
            if (mDeserializer.isStreamDeserializer()) {
                mDeserializedResult = mDeserializer
                        .performDeserialize(getResponseStream());
            } else {
                mDeserializedResult = mDeserializer
                        .performDeserialize(getResponseString());
            }
        } else if (globalDeserializer != null && expectedType != null) {
            if (globalDeserializer.isStreamDeserializer()) {
                mDeserializedResult = globalDeserializer.performDeserialize(
                        getResponseStream(), expectedType);
            } else {
                mDeserializedResult = globalDeserializer.performDeserialize(
                        getResponseString(), expectedType);
            }
        }

    }

    private static String convertStreamToString(InputStream is)
            throws IOException {
        return IOUtils.toString(is, "UTF-8");
    }

}
