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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.http.conn.ssl.X509HostnameVerifier;

import android.util.Log;

class HootTransportHttpUrlConnection implements HootTransport {
	
    @Override
    public void setup(Hoot hoot) {
        mTimeout = hoot.getTimeout();
        mSSLHostNameVerifier = hoot.getSSLHostNameVerifier();
    }

    @Override
    public HootResult synchronousExecute(HootRequest request) {
        if (request.isCancelled()) {
            return request.getResult();
        }

        mStreamingMode = (request.getQueryParameters() == null && request
                .getData() == null) ? StreamingMode.CHUNKED
                : StreamingMode.FIXED;
        HttpURLConnection connection = null;
        try {
            String url = request.buildUri().toString();
            Log.v(TAG, "Executing [" + url + "]");
            connection = (HttpURLConnection) new URL(url).openConnection();
            if (connection instanceof HttpsURLConnection) {
            	HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
            	httpsConnection.setHostnameVerifier(mSSLHostNameVerifier);
            }
            connection.setConnectTimeout(mTimeout);
            connection.setReadTimeout(mTimeout);
            synchronized (mConnectionMap) {
                mConnectionMap.put(request, connection);
            }

            setRequestMethod(request, connection);
            setRequestHeaders(request, connection);

            if (request.getData() != null) {
                setRequestData(request, connection);
            }
            HootResult hootResult = request.getResult();
            hootResult.setResponseCode(connection.getResponseCode());
            Log.d(TAG,
                    " - received response code ["
                            + connection.getResponseCode() + "]");
            if (request.getResult().isSuccess()) {
                hootResult.setHeaders(connection.getHeaderFields());
                hootResult.setResponseStream(new BufferedInputStream(connection
                        .getInputStream()));
            } else {
                hootResult.setResponseStream(new BufferedInputStream(connection
                        .getErrorStream()));
            }
            request.deserializeResult();
        } catch (Exception e) {
            request.getResult().setException(e);
            e.printStackTrace();
        } finally {
            if (connection != null) {
                synchronized (mConnectionMap) {
                    mConnectionMap.remove(request);
                }
                connection.disconnect();
                connection = null;
            }
        }
        return request.getResult();
    }

    @Override
    public void cancel(HootRequest request) {
        synchronized (mConnectionMap) {
            HttpURLConnection connection = mConnectionMap.get(request);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // -------------------------------------------------------------------------
    // END OF PUBLIC INTERFACE
    // -------------------------------------------------------------------------
    private int mTimeout = 15 * 1000;
    
    private X509HostnameVerifier mSSLHostNameVerifier;

    private enum StreamingMode {
        CHUNKED, FIXED
    };

    private void setRequestData(HootRequest request,
            HttpURLConnection connection) throws IOException {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(connection.getOutputStream());
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

    private void setRequestHeaders(HootRequest request,
            HttpURLConnection connection) {
        if (request.getHeaders() != null) {
            Iterator<Object> iter = request.getHeaders().keySet().iterator();
            while (iter.hasNext()) {
                String name = (String) iter.next();
                connection.addRequestProperty(name, request.getHeaders()
                        .getProperty(name));
            }
        }

        if (request.getHoot().isBasicAuth()) {
            connection.addRequestProperty("Authorization", request.getHoot()
                    .calculateBasicAuthHeader());
        }
    }

    private void setRequestMethod(HootRequest request,
            HttpURLConnection connection) throws ProtocolException {
        switch (request.getOperation()) {
            case DELETE:
                connection.setRequestMethod("DELETE");
                break;
            case POST:
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                break;
            case PUT:
                connection.setRequestMethod("PUT");
                connection.setDoOutput(true);
                break;
            case HEAD:
                connection.setRequestMethod("HEAD");
                break;
            default:
                connection.setRequestMethod("GET");
                break;
        }

        if (mStreamingMode == StreamingMode.CHUNKED) {
            connection.setChunkedStreamingMode(0);
        }

        if (request.getOperation() == HootRequest.Operation.PATCH) {
            request.getHeaders().put("X-HTTP-Method-Override", "PATCH");
        }

        // TODO handle other OP types
    }

    private Map<HootRequest, HttpURLConnection> mConnectionMap =
            new HashMap<HootRequest, HttpURLConnection>();

    private static final String TAG = HootTransportHttpUrlConnection.class
            .getSimpleName();
    private StreamingMode mStreamingMode = StreamingMode.CHUNKED;

}
