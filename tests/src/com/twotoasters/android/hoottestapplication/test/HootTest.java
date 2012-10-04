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

package com.twotoasters.android.hoottestapplication.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.twotoasters.android.hoot.Hoot;
import com.twotoasters.android.hoot.HootDeserializer;
import com.twotoasters.android.hoot.HootRequest;
import com.twotoasters.android.hoot.HootRequest.HootRequestListener;
import com.twotoasters.android.hoot.HootResult;
import com.twotoasters.android.hoottestapplication.data.Get;
import com.twotoasters.android.hoottestapplication.data.GetWithHeaders;
import com.twotoasters.android.hoottestapplication.data.GetWithHeadersAndParams;
import com.twotoasters.android.hoottestapplication.data.GetWithParams;
import com.twotoasters.android.hoottestapplication.data.Post;
import com.twotoasters.android.hoottestapplication.data.PostWithHeaders;

public class HootTest extends InstrumentationTestCase {

    protected static final String TAG = HootTest.class.getSimpleName();
    private Hoot mHootRestClient;

    protected void setUp() throws Exception {
        super.setUp();
        mHootRestClient = Hoot
                .createInstanceWithBaseUrl("http://10.0.2.2:4567");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGet() {
        final CountDownLatch latch = new CountDownLatch(1);
        final HootRequest<Get> request = mHootRestClient
                .createRequest(new TestHootListener<Get>(latch, true)).get()
                .setDeserializer(new TestHootDeserializer<Get>(Get.class));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null
                && request.getResult().getDeserializedResult().test
                        .equals("This is a test"));
    }

    public void testGetWithQueryParams() {
        final CountDownLatch latch = new CountDownLatch(1);
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("this", "that");
        params.put("here", "there");
        final HootRequest<GetWithParams> request = mHootRestClient
                .createRequest(new TestHootListener<GetWithParams>(latch, true))
                .get()
                .setResource("params")
                .setQueryParameters(params)
                .setDeserializer(
                        new TestHootDeserializer<GetWithParams>(
                                GetWithParams.class));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null
                && request.getResult().getDeserializedResult().thisString
                        .equals("that")
                && request.getResult().getDeserializedResult().hereString
                        .equals("there"));
    }

    public void testGetWithHeaders() {
        final CountDownLatch latch = new CountDownLatch(1);
        Properties headers = new Properties();
        headers.put("HOOT_TEST_HEADER", "header");
        final HootRequest<GetWithHeaders> request = mHootRestClient
                .createRequest(
                        new TestHootListener<GetWithHeaders>(latch, true))
                .get()
                .setResource("headers")
                .setHeaders(headers)
                .setDeserializer(
                        new TestHootDeserializer<GetWithHeaders>(
                                GetWithHeaders.class));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null
                && request.getResult().getDeserializedResult().headers
                        .equals("header"));

    }

    public void testGetWithHeadersAndParams() {
        final CountDownLatch latch = new CountDownLatch(1);
        Properties headers = new Properties();
        headers.put("HOOT_TEST_HEADER", "header");
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("this", "that");
        params.put("here", "there");
        final HootRequest<GetWithHeadersAndParams> request = mHootRestClient
                .createRequest(
                        new TestHootListener<GetWithHeadersAndParams>(latch,
                                true))
                .get()
                .setResource("headers.and.params")
                .setHeaders(headers)
                .setQueryParameters(params)
                .setDeserializer(
                        new TestHootDeserializer<GetWithHeadersAndParams>(
                                GetWithHeadersAndParams.class));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null
                && request.getResult().getDeserializedResult().headers
                        .equals("header")
                && request.getResult().getDeserializedResult().params.thisString
                        .equals("that")
                && request.getResult().getDeserializedResult().params.hereString
                        .equals("there"));
    }

    public void testGetFailure404() {
        final CountDownLatch latch = new CountDownLatch(1);
        final HootRequest<Get> request = mHootRestClient
                .createRequest(new TestHootListener<Get>(latch, true)).get()
                .setResource("error/404")
                .setDeserializer(new TestHootDeserializer<Get>(Get.class));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && !request.getResult().isSuccess()
                && request.getResult().getResponseCode() == 404);
    }

    public void testGetAcceptNotModified() {
        final CountDownLatch latch = new CountDownLatch(1);
        final List<Integer> successfulResults = new ArrayList<Integer>();
        successfulResults.add(HttpURLConnection.HTTP_NOT_MODIFIED);
        final HootRequest<Void> request = mHootRestClient
                .createRequest(new TestHootListener<Void>(latch, false)).get()
                .setResource("error/304")
                .setSuccessfulResponseCodes(successfulResults);

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getResponseCode() == 304);
    }

    public void testPost() {
        final CountDownLatch latch = new CountDownLatch(1);
        InputStream is = new ByteArrayInputStream(
                new String("this is a post").getBytes());
        final HootRequest<Post> request = mHootRestClient
                .createRequest(new TestHootListener<Post>(latch, true))
                .post(is)
                .setDeserializer(new TestHootDeserializer<Post>(Post.class));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null
                && request.getResult().getDeserializedResult().post
                        .equals("this is a post"));
    }

    public void testPostWithHeaders() {
        final CountDownLatch latch = new CountDownLatch(1);
        Properties headers = new Properties();
        headers.put("HOOT_TEST_HEADER", "some header");
        InputStream is = new ByteArrayInputStream(
                new String("this is a post").getBytes());
        final HootRequest<PostWithHeaders> request = mHootRestClient
                .createRequest(
                        new TestHootListener<PostWithHeaders>(latch, true))
                .post(is)
                .setResource("headers")
                .setHeaders(headers)
                .setDeserializer(
                        new TestHootDeserializer<PostWithHeaders>(
                                PostWithHeaders.class));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null
                && request.getResult().getDeserializedResult().headers
                        .equals("some header")
                && request.getResult().getDeserializedResult().post
                        .equals("this is a post"));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private <T> void executeTest(final HootRequest<T> request,
            CountDownLatch latch) {
        try {
            runTestOnUiThread(new Runnable() {

                @Override
                public void run() {
                    request.execute();
                }
            });
        } catch (Throwable e1) {
            fail(e1.getMessage());
        }

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class TestHootListener<T> implements HootRequestListener<T> {
        CountDownLatch mLatch = null;
        boolean mShouldHaveDeserializedResult;

        public TestHootListener(CountDownLatch latch,
                boolean shouldHaveDeserializedResult) {
            mShouldHaveDeserializedResult = shouldHaveDeserializedResult;
            mLatch = latch;
        }

        @Override
        public void onRequestStarted(HootRequest<T> request) {
            Log.v(TAG, "onRequestStarted");
        }

        @Override
        public void onSuccess(HootRequest<T> request, HootResult<T> result) {
            Log.v(TAG, "onSuccess");
            if (mShouldHaveDeserializedResult) {
                assertNotNull(result.getDeserializedResult());
            }
            mLatch.countDown();
        }

        @Override
        public void onFailure(HootRequest<T> request, HootResult<T> result) {
            Log.v(TAG, "onFailure");
            mLatch.countDown();
        }

        @Override
        public void onCancelled(HootRequest<T> request) {
            Log.v(TAG, "onCancelled");
            mLatch.countDown();
        }

        @Override
        public void onRequestCompleted(HootRequest<T> request) {
            Log.v(TAG, "onRequestCompleted");
        }
    }

    /**
     * Convenient if ugly. Don't use for production code, only for unit tests.
     * 
     * @author bjdupuis
     * 
     * @param <T>
     */
    private class TestHootDeserializer<T extends Object> extends
            HootDeserializer<T> {
        Class<T> _clazz;

        public TestHootDeserializer(Class<T> clazz) {
            super(false);
            _clazz = clazz;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T deserialize(String string) {
            try {
                Method method = _clazz.getDeclaredMethod("fromJson",
                        JSONObject.class);
                return (T) method.invoke(null, new Object[] { new JSONObject(
                        string) });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
