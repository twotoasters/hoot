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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.util.Log;

class HootTransportHttpClient<T> implements HootTransport<T> {

    private static final String TAG = HootTransportHttpClient.class
            .getSimpleName();

    @Override
    public void synchronousExecute(HootRequest<T> request) {
        lock.lock();
        if (sClient == null) {
            HttpParams params = new BasicHttpParams();
            ConnManagerParams.setMaxTotalConnections(params, 10);
            ConnManagerParams.setTimeout(params, 2000);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpConnectionParams.setConnectionTimeout(params, 15000);
            HttpConnectionParams.setSoTimeout(params, 15000);
            HttpConnectionParams.setTcpNoDelay(params, true);

            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory
                    .getSocketFactory(), 80));
            schemeRegistry.register(new Scheme("https", SSLSocketFactory
                    .getSocketFactory(), 443));

            ClientConnectionManager cm = new ThreadSafeClientConnManager(
                    params, schemeRegistry);
            sClient = new DefaultHttpClient(cm, params);
            if (request.getHoot().isBasicAuth()) {
                sClient.getCredentialsProvider().setCredentials(
                        AuthScope.ANY,
                        new UsernamePasswordCredentials(request.getHoot()
                                .getBasicAuthUsername(), request.getHoot()
                                .getBasicAuthPassword()));
            }
        }
        lock.unlock();

        mRequestBase = null;
        HootResult<T> result = request.getResult();
        try {
            String uri = request.buildUri().toString();
            switch (request.getOperation()) {
                case DELETE:
                    mRequestBase = new HttpDelete(uri);
                    break;
                case GET:
                    mRequestBase = new HttpGet(uri);
                    break;
                case PUT:
                    HttpPut put = new HttpPut(uri);
                    put.setEntity(getEntity(request.getData()));
                    mRequestBase = put;
                    break;
                case POST:
                    HttpPost post = new HttpPost(uri);
                    post.setEntity(getEntity(request.getData()));
                    mRequestBase = post;
                    break;
                case HEAD:
                    mRequestBase = new HttpHead(uri);
                    break;
            }
        } catch (UnsupportedEncodingException e1) {
            result.setException(e1);
            e1.printStackTrace();
            return;
        } catch (IOException e) {
            result.setException(e);
            e.printStackTrace();
            return;
        }
        if (request.getHeaders() != null && request.getHeaders().size() > 0) {
            for (Object propertyKey : request.getHeaders().keySet()) {
                mRequestBase.addHeader((String) propertyKey, (String) request
                        .getHeaders().get(propertyKey));
            }
        }

        InputStream is = null;
        try {
            Log.v(TAG, "URI: [" + mRequestBase.getURI().toString() + "]");
            HttpResponse response = sClient.execute(mRequestBase);

            if (response != null) {
                result.setResponseCode(response.getStatusLine().getStatusCode());
                Map<String, List<String>> headers = new HashMap<String, List<String>>();
                for (Header header : response.getAllHeaders()) {
                    List<String> values = new ArrayList<String>();
                    values.add(header.getValue());
                    headers.put(header.getName(), values);
                }
                result.setHeaders(headers);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    is = entity.getContent();
                    result.setResponseStream(new BufferedInputStream(is));
                    request.deserializeResult();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setException(e);
        } finally {
            mRequestBase = null;
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return;
    }

    private HttpEntity getEntity(InputStream data)
            throws UnsupportedEncodingException, IOException {

        return new StringEntity(IOUtils.toString(data), HTTP.UTF_8);
    }

    @Override
    public void cancel() {
        if (mRequestBase != null) {
            mRequestBase.abort();
        }
    }

    // -------------------------------------------------------------------------
    // END OF PUBLIC INTERFACE
    // -------------------------------------------------------------------------
    private static DefaultHttpClient sClient = null;
    private static final Lock lock = new ReentrantLock();
    private HttpRequestBase mRequestBase;

}
