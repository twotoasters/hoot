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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;

import android.util.Log;


class HootTransportHttpUrlConnection<T> implements HootTransport<T> {

    @Override
    public void synchronousExecute(HootRequest<T> request) {
        if (mCancelled) {
            return;
        }

        mStreamingMode = (request.getQueryParameters() == null && request
                .getData() == null) ? StreamingMode.CHUNKED
                : StreamingMode.FIXED;
        mConnection = null;
        try {
            String url = request.buildUri().toString();
            Log.v(TAG, "Executing [" + url + "]");
            mConnection = (HttpURLConnection) new URL(url).openConnection();

            setRequestMethod(request);
            setRequestHeaders(request);

            if (request.getData() != null) {
                setRequestData(request);
            }
            HootResult<T> hootResult = request.getResult();
            hootResult.setResponseCode(mConnection.getResponseCode());
            Log.d(TAG,
                    " - received response code ["
                            + mConnection.getResponseCode() + "]");
            if (request.getResult().isSuccess()) {
                hootResult.setHeaders(mConnection.getHeaderFields());
                hootResult.setResponseStream(new BufferedInputStream(
                        mConnection.getInputStream()));
                request.deserializeResult();
            } else {
                hootResult.setResponseStream(new BufferedInputStream(
                        mConnection.getErrorStream()));
            }
        } catch (Exception e) {
            request.getResult().setException(e);
            e.printStackTrace();
        } finally {
            synchronized (this) {
                if (mConnection != null) {
                    mConnection.disconnect();
                    mConnection = null;
                }
            }
        }

    }

    @Override
    public void cancel() {
        synchronized (this) {
            mCancelled = true;
            if (mConnection != null) {
                mConnection.disconnect();
            }
        }
    }

    // -------------------------------------------------------------------------
    // END OF PUBLIC INTERFACE
    // -------------------------------------------------------------------------
    private enum StreamingMode {
        CHUNKED, FIXED
    };

    private void setRequestData(HootRequest<T> request) throws IOException {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(mConnection.getOutputStream());
            IOUtils.copy(request.getData(), os);
        } finally {
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setRequestHeaders(HootRequest<T> request) {
        if (request.getHeaders() != null) {
            Iterator<Object> iter = request.getHeaders().keySet().iterator();
            while (iter.hasNext()) {
                String name = (String) iter.next();
                mConnection.addRequestProperty(name, request.getHeaders()
                        .getProperty(name));
            }
        }

        if (request.getHoot().isBasicAuth()) {
            mConnection.addRequestProperty("Authorization", request.getHoot()
                    .calculateBasicAuthHeader());
        }
    }

    private void setRequestMethod(HootRequest<T> request)
            throws ProtocolException {
        switch (request.getOperation()) {
            case DELETE:
                mConnection.setRequestMethod("DELETE");
                mConnection.setDoOutput(true);
                break;
            case POST:
                mConnection.setRequestMethod("POST");
                mConnection.setDoOutput(true);
                break;
            case PUT:
                mConnection.setRequestMethod("PUT");
                mConnection.setDoOutput(true);
                break;
            case HEAD:
                mConnection.setRequestMethod("HEAD");
                break;
            default:
                mConnection.setRequestMethod("GET");
                break;
        }

        if (mStreamingMode == StreamingMode.CHUNKED) {
            mConnection.setChunkedStreamingMode(0);
        }

        if (request.getOperation() == HootRequest.Operation.PATCH) {
            request.getHeaders().put("X-HTTP-Method-Override", "PATCH");
        }

        // TODO handle other OP types
    }

    private HttpURLConnection mConnection;
    volatile private boolean mCancelled = false;

    private static final String TAG = HootTransportHttpUrlConnection.class
            .getSimpleName();
    private StreamingMode mStreamingMode = StreamingMode.CHUNKED;

}
