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

import org.apache.commons.io.IOUtils;

import android.net.Uri;

public class HootRequest<T> {

    /**
     * The interface for request listeners to implement to be notified of events
     * in the request lifecycle. All callbacks are guaranteed to be called from
     * the UI thread.
     * 
     * @author bjdupuis
     * 
     * @param <T>
     *            the type of object expected from the request
     */
    public interface HootRequestListener<T> {
        /**
         * Called when the request is actually being transmitted.
         * 
         * @param request
         *            the request
         */
        public void onRequestStarted(HootRequest<T> request);

        /**
         * Called when the request has been transmitted and any response
         * received. This is called whether the request was successful or not.
         * Examples of usage include dismissing progress dialogs.
         * 
         * @param request
         *            the request... again.
         */
        public void onRequestCompleted(HootRequest<T> request);

        /**
         * Called once the request is complete and the result code indicates a
         * successful request.
         * 
         * @param request
         * @param result
         */
        public void onSuccess(HootRequest<T> request, HootResult<T> result);

        /**
         * Called once the request is complete and the result code indicates an
         * unsuccessful request.
         * 
         * @param request
         * @param result
         */
        public void onFailure(HootRequest<T> request, HootResult<T> result);

        /**
         * Called once the request is complete and the result code indicates a
         * successful request.
         * 
         * @param request
         * @param result
         */
        public void onCancelled(HootRequest<T> request);
    }

    /**
     * @return the result
     */
    public HootResult<T> getResult() {
        return mResult;
    }

    public Class<T> getRequestClass() {
        return mClazz;
    }

    public Object getTag() {
        return mOpaqueTag;
    }

    public boolean isComplete() {
        return mComplete;
    }

    public HootRequest<T> setResource(String resource) {
        mResource = resource;
        return this;
    }

    public HootRequest<T> setDeserializer(HootDeserializer<T> deserializer) {
        mDeserializer = deserializer;
        return this;
    }

    public HootRequest<T> get() {
        mOperation = Operation.GET;
        return this;
    }

    public HootRequest<T> post(InputStream postData) {
        mOperation = Operation.POST;
        mData = postData;
        return this;
    }

    public HootRequest<T> post(String string, String encoding)
            throws UnsupportedEncodingException {
        mOperation = Operation.POST;
        mData = new ByteArrayInputStream(string.getBytes("UTF-8"));
        return this;
    }

    public HootRequest<T> head() {
        mOperation = Operation.HEAD;
        return this;
    }

    public HootRequest<T> delete() {
        mOperation = Operation.DELETE;
        return this;
    }

    public HootRequest<T> patch() {
        mOperation = Operation.PATCH;
        return this;
    }

    public HootRequest<T> put(String string, String encoding)
            throws UnsupportedEncodingException {
        mOperation = Operation.PUT;
        mData = new ByteArrayInputStream(string.getBytes("UTF-8"));
        return this;
    }

    public HootRequest<T> put(InputStream postData) {
        mOperation = Operation.PUT;
        mData = postData;
        return this;
    }

    public HootRequest<T> setNumRetries(int retries) {
        mNumRetries = retries;
        return this;
    }

    public HootRequest<T> setQueryParameters(Map<String, String> queryParameters) {
        mQueryParameters = queryParameters;
        return this;
    }

    public HootRequest<T> setHeaders(Properties headers) {
        mHeaders = headers;
        return this;
    }

    public HootRequest<T> setSuccessfulResponseCodes(List<Integer> codes) {
        mResult.setSuccessfulResponseCodes(codes);
        return this;
    }

    public HootRequest<T> setBasicAuth(String username, String password) {
        // TODO
        return this;
    }

    public HootRequest<T> setTag(Object opaque) {
        mOpaqueTag = opaque;
        return this;
    }

    @SuppressWarnings("unchecked")
    public HootRequest<T> execute() throws IllegalStateException {
        if (mTask == null && !isComplete()) {
            mTask = new HootTask<T>();
            mTask.execute(this);
        } else {
            throw new IllegalStateException(
                    "Can't execute the same request more than once");
        }
        return this;
    }

    public void cancel() {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
    }

    /**
     * To meet contract, caller should only call this from the UI thread, since
     * this call could generate an immediate call to the listener.
     * 
     * @param listener
     */
    public void bindListener(HootRequestListener<T> listener) {
        mListener = listener;
        if (mComplete) {
            if (getResult().isSuccess()) {
                listener.onSuccess(this, getResult());
            } else {
                listener.onFailure(this, getResult());
            }
        }
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
    public HootRequestListener<T> getListener() {
        return mListener;
    }

    // -------------------------------------------------------------------------
    // END OF PUBLIC INTERFACE
    // -------------------------------------------------------------------------
    private static final int DEFAULT_NUM_RETRIES = 0;
    private Operation mOperation;
    private int mNumRetries = DEFAULT_NUM_RETRIES;
    private HootTask<T> mTask = null;
    private HootResult<T> mResult = new HootResult<T>();
    private Map<String, String> mQueryParameters = null;
    private Properties mHeaders = null;
    private InputStream mData = null;
    private HootRequestListener<T> mListener = null;
    private HootDeserializer<T> mDeserializer;
    private String mResource;
    private Hoot mHoot;
    private boolean mComplete = false;
    private Class<T> mClazz;
    private Object mOpaqueTag = null;

    HootRequest(Hoot hoot, HootRequestListener<T> listener, Class<T> clazz) {
        mHoot = hoot;
        mOperation = Operation.GET;
        mListener = listener;
        mClazz = clazz;
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
    Map<String, String> getQueryParameters() {
        return mQueryParameters;
    }

    /**
     * @return the mHeaders
     */
    Properties getHeaders() {
        return mHeaders;
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
     * @param mResult
     *            the mResult to set
     */
    void setResult(HootResult<T> result) {
        mResult = result;
    }

    void setComplete(boolean isComplete) {
        mComplete = isComplete;
        mTask = null;
    }

    void deserializeResult() throws IOException {

        // see if we need to read out to a string. Either we have no
        // deserializer or we do and it operates on a string.
        if (mDeserializer == null
                || (mDeserializer != null && !mDeserializer
                        .isStreamDeserializer())) {
            mResult.setResponse(convertStreamToString(mResult
                    .getResponseStream()));
        }

        if (mDeserializer != null) {
            if (mDeserializer.isStreamDeserializer()) {
                mResult.setDeserializedResult(mDeserializer.deserialize(mResult
                        .getResponseStream()));
            } else {
                mResult.setDeserializedResult(mDeserializer.deserialize(mResult
                        .getResponseString()));
            }
        }
    }

    Uri buildUri() {
        Uri.Builder builder = Uri.parse(mHoot.getBaseUrl()).buildUpon()
                .appendEncodedPath(mResource == null ? "" : mResource);

        if (mQueryParameters != null && !mQueryParameters.isEmpty()) {
            Iterator<Entry<String, String>> iter = mQueryParameters.entrySet()
                    .iterator();
            while (iter.hasNext()) {
                Entry<String, String> entry = iter.next();
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    private static String convertStreamToString(InputStream is)
            throws IOException {
        return IOUtils.toString(is, "UTF-8");
    }

}
