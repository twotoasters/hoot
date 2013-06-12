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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.http.entity.mime.MultipartEntity;

import android.net.Uri;

public class HootRequest {

    public static final int STREAMING_MODE_CHUNKED = 1;
    public static final int STREAMING_MODE_FIXED = 2;

    /**
     * The interface for request listeners to implement to be notified of events
     * in the request lifecycle. All callbacks are guaranteed to be called from
     * the UI thread.
     * 
     * @author bjdupuis
     * 
     * @param <T> the type of object expected from the request
     */
    public interface HootRequestListener {
        /**
         * Called when the request is actually being transmitted.
         * 
         * @param request the request
         */
        public void onRequestStarted(HootRequest request);

        /**
         * Called when the request has been transmitted and any response
         * received. This is called whether the request was successful or not.
         * Examples of usage include dismissing progress dialogs.
         * 
         * @param request the request... again.
         */
        public void onRequestCompleted(HootRequest request);

        /**
         * Called once the request is complete and the result code indicates a
         * successful request.
         * 
         * @param request
         * @param result
         */
        public void onSuccess(HootRequest request, HootResult result);

        /**
         * Called once the request is complete and the result code indicates an
         * unsuccessful request.
         * 
         * @param request
         * @param result
         */
        public void onFailure(HootRequest request, HootResult result);

        /**
         * Called once the request is complete and the result code indicates a
         * successful request.
         * 
         * @param request
         * @param result
         */
        public void onCancelled(HootRequest request);
    }

    /**
     * @return the result
     */
    public HootResult getResult() {
        return mResult;
    }

    public Object getTag() {
        return mOpaqueTag;
    }

    public boolean isComplete() {
        return mComplete;
    }

    public boolean isCancelled() {
        return mCancelled;
    }

    public HootRequest setResource(String resource) {
        mResource = resource;
        return this;
    }

    public String getResource() {
        return mResource;
    }

    public int getStreamingMode() {
        return streamingMode;
    }

    public <T> HootRequest setDeserializer(HootDeserializer<T> deserializer) {
        mResult.setDeserializer(deserializer);
        return this;
    }

    public HootRequest get() {
        mOperation = Operation.GET;
        return this;
    }

    public HootRequest post(InputStream postData) {
        mOperation = Operation.POST;
        mData = postData;
        return this;
    }

    public HootRequest post(String string) {
        try {
            post(string, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            // UTF-8 is supported
        }
        return this;
    }

    public HootRequest post(MultipartEntity multipartEntity) {
        mOperation = Operation.POST;
        mMultipartEntity = multipartEntity;
        return this;
    }

    public HootRequest post(String string, String encoding) throws UnsupportedEncodingException {
        mOperation = Operation.POST;
        mData = new ByteArrayInputStream(string.getBytes(encoding));
        return this;
    }

    public HootRequest post(Map<String, String> queryParameters) {
        mOperation = Operation.POST;
        mData = getPostDataStream(queryParameters);
        return this;
    }

    public HootRequest post() {
        mOperation = Operation.POST;
        return this;
    }

    public HootRequest head() {
        mOperation = Operation.HEAD;
        return this;
    }

    public HootRequest delete() {
        mOperation = Operation.DELETE;
        return this;
    }

    public HootRequest patch() {
        mOperation = Operation.PATCH;
        return this;
    }

    public HootRequest put(String string, String encoding) throws UnsupportedEncodingException {
        mOperation = Operation.PUT;
        mData = new ByteArrayInputStream(string.getBytes("UTF-8"));
        return this;
    }

    public HootRequest put(InputStream postData) {
        mOperation = Operation.PUT;
        mData = postData;
        return this;
    }

    public HootRequest setNumRetries(int retries) {
        mNumRetries = retries;
        return this;
    }

    public HootRequest setQueryParameters(Map<String, String> queryParameters) {
        mQueryParameters = queryParameters;
        return this;
    }

    public HootRequest setHeaders(Properties headers) {
        mHeaders = headers;
        return this;
    }

    public HootRequest setSuccessfulResponseCodes(List<Integer> codes) {
        mResult.setSuccessfulResponseCodes(codes);
        return this;
    }

    public HootRequest setTag(Object opaque) {
        mOpaqueTag = opaque;
        return this;
    }

    public HootRequest setExpectedType(Class<?> type) {
        mExpectedType = type;
        return this;
    }

    public HootRequest setStreamingMode(int streamingMode) {
        this.streamingMode = streamingMode;
        return this;
    }

    public InputStream getPostDataStream(Map<String, String> queryParameters) {
        boolean isFirst = true;
        StringBuffer sb = new StringBuffer();
        for (String key : queryParameters.keySet()) {
            appendIfNotNullOrEmpty(sb, isFirst ? null : "&");
            sb.append(key).append('=').append(queryParameters.get(key));
            isFirst = false;
        }

        try {
            return new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {

        }
        return null;
    }

    public void appendIfNotNullOrEmpty(StringBuffer sb, String s) {
        if (s != null && s.length() > 0) {
            sb.append(s);
        }
    }

    public HootRequest execute() throws IllegalStateException {
        if (mTask == null && !isComplete()) {
            mTask = new HootTask();
            mTask.executeOnThreadPoolExecutor(this);
        }
        else {
            throw new IllegalStateException("Can't execute the same request more than once");
        }
        return this;
    }

    public void cancel() {
        mCancelled = true;
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }

        getHoot().cancelRequest(this);
    }

    /**
     * To meet contract, caller should only call this from the UI thread, since
     * this call could generate an immediate call to the listener.
     * 
     * @param listener
     */
    public HootRequest bindListener(HootRequestListener listener) {
        mListener = listener;
        if (mComplete) {
            handleCompletion();
        }

        return this;
    }

    public void unbindListener(boolean cancel) {
        if (cancel) {
            cancel();
        }
        mListener = null;
    }

    /**
     * @return the mListener
     */
    public HootRequestListener getListener() {
        return mListener;
    }

    // -------------------------------------------------------------------------
    // END OF PUBLIC INTERFACE
    // -------------------------------------------------------------------------
    private static final int DEFAULT_NUM_RETRIES = 0;
    private Operation mOperation;
    private int mNumRetries = DEFAULT_NUM_RETRIES;
    private HootTask mTask;
    private HootResult mResult = new HootResult();
    private Map<String, String> mQueryParameters;
    private Properties mHeaders;
    private MultipartEntity mMultipartEntity;
    private InputStream mData;
    private HootRequestListener mListener;
    private String mResource;
    private Hoot mHoot;
    private boolean mComplete = false;
    private Object mOpaqueTag;
    private boolean mCancelled;
    private Class<?> mExpectedType;
    private int streamingMode;

    HootRequest(Hoot hoot) {
        mHoot = hoot;
        mOperation = Operation.GET;
    }

    enum Operation {
        GET, POST, HEAD, PATCH, DELETE, PUT
    }

    /**
     * @return the owning Hoot
     */
    Hoot getHoot() {
        return mHoot;
    }

    /**
     * @return the mOperation
     */
    Operation getOperation() {
        return mOperation;
    }

    /**
     * @return the mQueryParameters
     */
    public Map<String, String> getQueryParameters() {
        return mQueryParameters;
    }

    /**
     * @return the mHeaders
     */
    public Properties getHeaders() {
        return mHeaders;
    }

    /**
     * 
     * @return the mMultipartEntity
     */
    public MultipartEntity getMultipartEntity() {
        return mMultipartEntity;
    }

    /**
     * @return the mPostData
     */
    InputStream getData() {
        return mData;
    }

    boolean shouldRetry() {
        return mNumRetries-- > 0;
    }

    /**
     * @param mResult the mResult to set
     */
    void setResult(HootResult result) {
        mResult = result;
    }

    void setComplete(boolean isComplete) {
        mComplete = isComplete;
        mTask = null;
    }

    Uri buildUri() {
        // fix the "double-slash" issue with base URLs ending in slash and
        // resource beginning with slash
        if (mHoot.getBaseUrl().endsWith("/") && mResource != null && mResource.startsWith("/")) {
            mResource = mResource.substring(1);
        }

        Uri.Builder builder = Uri.parse(mHoot.getBaseUrl()).buildUpon();
        if (mResource != null) {
            builder = builder.appendEncodedPath(mResource);
        }

        if (mQueryParameters != null && !mQueryParameters.isEmpty()) {
            Iterator<Entry<String, String>> iter = mQueryParameters.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, String> entry = iter.next();
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    void deserializeResult() throws IOException {
        mResult.deserializeResult(mHoot.getGlobalDeserializer(), mExpectedType);
    }

    /**
     * Handle all aspects of completion from a task perspective.
     */
    void handleCompletion() {
        setComplete(true);
        if (getListener() != null) {
            getListener().onRequestCompleted(this);
            HootResult result = getResult();
            if (result != null && result.isSuccess()) {
                getListener().onSuccess(this, result);
            }
            else {
                getListener().onFailure(this, result);
            }

            // unbind to remove the reference 
            unbindListener(false);
        }
    }

}
