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


import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;

import android.os.Build;
import android.util.Base64;

public class Hoot {

    public static Hoot createInstanceWithBaseUrl(String baseUrl) {
        return new Hoot(baseUrl);
    }

    public HootRequest createRequest() {
        synchronized (this) {
            if (mTransport == null) {
                setupTransport();
            }
        }
        return new HootRequest(this);
    }

    public HootResult executeRequestSynchronously(HootRequest request) {
        synchronized (this) {
            if (mTransport == null) {
                setupTransport();
            }
        }
        return mTransport.synchronousExecute(request);
    }

    public Hoot setBasicAuth(String username, String password) {
        mBasicAuthUsername = username;
        mBasicAuthPassword = password;
        return this;
    }
    
    public Hoot setSSLHostNameVerifier(X509HostnameVerifier sslHostNameVerifier) {
    	mSSLHostNameVerifier = sslHostNameVerifier;
    	return this;
    }

    public Hoot setGlobalDeserializer(HootGlobalDeserializer deserializer) {
        mGlobalDeserializer = deserializer;
        return this;
    }
    
    public static void setMinLogPriority(int minLogPriority) {
    	Log.setMinPriority(minLogPriority);
    }

    /**
     * Set the connection timeout.
     * @param timeout the timeout in milliseconds.
     */
    public Hoot setTimeout(int timeout) {
        mTimeout = timeout;
        return this;
    }

    // -------------------------------------------------------------------------
    // END OF PUBLIC INTERFACE
    // -------------------------------------------------------------------------
    private String mBasicAuthUsername = null;
    private String mBasicAuthPassword = null;
    private X509HostnameVerifier mSSLHostNameVerifier = SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
    private String mBaseUrl;

    private int mTimeout = 15 * 1000;

    private HootTransport mTransport;
    private HootGlobalDeserializer mGlobalDeserializer;

    private Hoot(String baseUrl) {
        mBaseUrl = baseUrl;
    }

    String getBaseUrl() {
        return mBaseUrl;
    }

    boolean isBasicAuth() {
        return mBasicAuthUsername != null && mBasicAuthPassword != null;
    }

    String getBasicAuthUsername() {
        return mBasicAuthUsername;
    }

    String getBasicAuthPassword() {
        return mBasicAuthPassword;
    }
    
    X509HostnameVerifier getSSLHostNameVerifier() {
    	return mSSLHostNameVerifier;
    }

    String calculateBasicAuthHeader() {
        return "Basic "
                + Base64.encodeToString(new String(getBasicAuthUsername() + ":"
                        + getBasicAuthPassword()).getBytes(), Base64.NO_WRAP);
    }

    int getTimeout() {
        return mTimeout;
    }

    void cancelRequest(HootRequest hootRequest) {
        mTransport.cancel(hootRequest);
    }

    private void setupTransport() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            mTransport = new HootTransportHttpUrlConnection();
        } else {
            mTransport = new HootTransportHttpClient();
        }
        mTransport.setup(this);
    }

    HootGlobalDeserializer getGlobalDeserializer() {
        return mGlobalDeserializer;
    }

}
