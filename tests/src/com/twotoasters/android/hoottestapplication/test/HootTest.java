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
import java.io.IOException;
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

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.twotoasters.android.hoot.Hoot;
import com.twotoasters.android.hoot.HootDeserializer;
import com.twotoasters.android.hoot.HootGlobalDeserializer;
import com.twotoasters.android.hoot.HootRequest;
import com.twotoasters.android.hoot.HootRequest.HootRequestListener;
import com.twotoasters.android.hoot.HootResult;
import com.twotoasters.android.hoottestapplication.data.Delete;
import com.twotoasters.android.hoottestapplication.data.DeleteWithHeaders;
import com.twotoasters.android.hoottestapplication.data.DeleteWithHeadersAndParams;
import com.twotoasters.android.hoottestapplication.data.DeleteWithParams;
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
        final HootDeserializer<Get> deserializer = new TestHootDeserializer<Get>(
                Get.class);
        final HootRequest request = mHootRestClient.createRequest().get()
                .setDeserializer(deserializer)
                .bindListener(new TestHootListener(latch, true));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null
                && deserializer.getDeserializedResult().test
                        .equals("This is a test"));
    }

    @SmallTest
    public void testGlobalDeserializer() {
        final CountDownLatch latch = new CountDownLatch(1);
        mHootRestClient.setGlobalDeserializer(new TestHootGlobalDeserializer());
        final HootRequest request = mHootRestClient.createRequest().get()
                .setExpectedType(Get.class)
                .bindListener(new TestHootListener(latch, true));

        assertNotNull(request);

        executeTest(request, latch);

        mHootRestClient.setGlobalDeserializer(null);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null
                && ((Get) request.getResult().getDeserializedResult()).test
                        .equals("This is a test"));
    }

    public void testGetWithQueryParams() {
        final CountDownLatch latch = new CountDownLatch(1);
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("this", "that");
        params.put("here", "there");
        final HootDeserializer<GetWithParams> deserializer = new TestHootDeserializer<GetWithParams>(
                GetWithParams.class);
        final HootRequest request = mHootRestClient.createRequest().get()
                .setResource("params").setQueryParameters(params)
                .setDeserializer(deserializer)
                .bindListener(new TestHootListener(latch, true));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null);

        assertTrue(deserializer.getDeserializedResult().thisString
                .equals("that")
                && deserializer.getDeserializedResult().hereString
                        .equals("there"));
    }

    public void testGetWithHeaders() {
        final CountDownLatch latch = new CountDownLatch(1);
        Properties headers = new Properties();
        headers.put("HOOT_TEST_HEADER", "header");
        final HootDeserializer<GetWithHeaders> deserializer = new TestHootDeserializer<GetWithHeaders>(
                GetWithHeaders.class);
        final HootRequest request = mHootRestClient.createRequest().get()
                .setResource("headers").setHeaders(headers)
                .setDeserializer(deserializer)
                .bindListener(new TestHootListener(latch, true));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null
                && deserializer.getDeserializedResult().headers
                        .equals("header"));

    }

    public void testGetWithHeadersAndParams() {
        final CountDownLatch latch = new CountDownLatch(1);
        Properties headers = new Properties();
        headers.put("HOOT_TEST_HEADER", "header");
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("this", "that");
        params.put("here", "there");
        HootDeserializer<GetWithHeadersAndParams> deserializer = new TestHootDeserializer<GetWithHeadersAndParams>(
                GetWithHeadersAndParams.class);
        final HootRequest request = mHootRestClient.createRequest().get()
                .setResource("headers.and.params").setHeaders(headers)
                .setQueryParameters(params).setDeserializer(deserializer)
                .bindListener(new TestHootListener(latch, true));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null);

        assertTrue(deserializer.getDeserializedResult().headers
                .equals("header")
                && deserializer.getDeserializedResult().params.thisString
                        .equals("that")
                && deserializer.getDeserializedResult().params.hereString
                        .equals("there"));
    }

    public void testGetFailure404() {
        final CountDownLatch latch = new CountDownLatch(1);
        final HootRequest request = mHootRestClient.createRequest().get()
                .setResource("error/404")
                .bindListener(new TestHootListener(latch, true));

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
        final HootRequest request = mHootRestClient.createRequest().get()
                .setResource("error/304")
                .setSuccessfulResponseCodes(successfulResults)
                .bindListener(new TestHootListener(latch, false));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getResponseCode() == 304);
    }
    
    // start delete
    public void testDelete() {
        final CountDownLatch latch = new CountDownLatch(1);
        final HootDeserializer<Delete> deserializer = new TestHootDeserializer<Delete>(
                Delete.class);
        final HootRequest request = mHootRestClient.createRequest().delete()
                .setDeserializer(deserializer)
                .bindListener(new TestHootListener(latch, true));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null
                && deserializer.getDeserializedResult().test
                        .equals("This is a test"));
    }
    
    public void testDeleteWithQueryParams() {
        final CountDownLatch latch = new CountDownLatch(1);
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("this", "that");
        params.put("here", "there");
        final HootDeserializer<DeleteWithParams> deserializer = new TestHootDeserializer<DeleteWithParams>(
        		DeleteWithParams.class);
        final HootRequest request = mHootRestClient.createRequest().delete()
                .setResource("params").setQueryParameters(params)
                .setDeserializer(deserializer)
                .bindListener(new TestHootListener(latch, true));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null);

        assertTrue(deserializer.getDeserializedResult().thisString
                .equals("that")
                && deserializer.getDeserializedResult().hereString
                        .equals("there"));
    }

    public void testDeleteWithHeaders() {
        final CountDownLatch latch = new CountDownLatch(1);
        Properties headers = new Properties();
        headers.put("HOOT_TEST_HEADER", "header");
        final HootDeserializer<DeleteWithHeaders> deserializer = new TestHootDeserializer<DeleteWithHeaders>(
        		DeleteWithHeaders.class);
        final HootRequest request = mHootRestClient.createRequest().delete()
                .setResource("headers").setHeaders(headers)
                .setDeserializer(deserializer)
                .bindListener(new TestHootListener(latch, true));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null
                && deserializer.getDeserializedResult().headers
                        .equals("header"));

    }

    public void testDeleteWithHeadersAndParams() {
        final CountDownLatch latch = new CountDownLatch(1);
        Properties headers = new Properties();
        headers.put("HOOT_TEST_HEADER", "header");
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("this", "that");
        params.put("here", "there");
        HootDeserializer<DeleteWithHeadersAndParams> deserializer = new TestHootDeserializer<DeleteWithHeadersAndParams>(
        		DeleteWithHeadersAndParams.class);
        final HootRequest request = mHootRestClient.createRequest().delete()
                .setResource("headers.and.params").setHeaders(headers)
                .setQueryParameters(params).setDeserializer(deserializer)
                .bindListener(new TestHootListener(latch, true));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null);

        assertTrue(deserializer.getDeserializedResult().headers
                .equals("header")
                && deserializer.getDeserializedResult().params.thisString
                        .equals("that")
                && deserializer.getDeserializedResult().params.hereString
                        .equals("there"));
    }

    public void testDeleteFailure404() {
        final CountDownLatch latch = new CountDownLatch(1);
        final HootRequest request = mHootRestClient.createRequest().delete()
                .setResource("error/404")
                .bindListener(new TestHootListener(latch, true));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && !request.getResult().isSuccess()
                && request.getResult().getResponseCode() == 404);
    }

    public void testDeleteAcceptNotModified() {
        final CountDownLatch latch = new CountDownLatch(1);
        final List<Integer> successfulResults = new ArrayList<Integer>();
        successfulResults.add(HttpURLConnection.HTTP_NOT_MODIFIED);
        final HootRequest request = mHootRestClient.createRequest().delete()
                .setResource("error/304")
                .setSuccessfulResponseCodes(successfulResults)
                .bindListener(new TestHootListener(latch, false));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getResponseCode() == 304);
    }
    // end delete

    public void testPost() {
        final CountDownLatch latch = new CountDownLatch(1);
        InputStream is = new ByteArrayInputStream(
                new String("this is a post").getBytes());
        final HootDeserializer<Post> deserializer = new TestHootDeserializer<Post>(
                Post.class);
        final HootRequest request = mHootRestClient.createRequest().post(is)
                .setDeserializer(deserializer)
                .bindListener(new TestHootListener(latch, true));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null);

        assertTrue(deserializer.getDeserializedResult().post
                .equals("this is a post"));
    }

    public void testPostWithHeaders() {
        final CountDownLatch latch = new CountDownLatch(1);
        Properties headers = new Properties();
        headers.put("HOOT_TEST_HEADER", "some header");
        InputStream is = new ByteArrayInputStream(
                new String("this is a post").getBytes());
        final HootDeserializer<PostWithHeaders> deserializer = new TestHootDeserializer<PostWithHeaders>(
                PostWithHeaders.class);
        final HootRequest request = mHootRestClient.createRequest().post(is)
                .setResource("headers").setHeaders(headers)
                .setDeserializer(deserializer)
                .bindListener(new TestHootListener(latch, true));

        assertNotNull(request);

        executeTest(request, latch);

        assertTrue(request.getResult() != null
                && request.getResult().isSuccess()
                && request.getResult().getDeserializedResult() != null);
        assertTrue(deserializer.getDeserializedResult().headers
                .equals("some header")
                && deserializer.getDeserializedResult().post
                        .equals("this is a post"));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private <T> void executeTest(final HootRequest request, CountDownLatch latch) {
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
            latch.await(200, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class TestHootListener implements HootRequestListener {
        CountDownLatch mLatch = null;
        boolean mShouldHaveDeserializedResult;

        public TestHootListener(CountDownLatch latch,
                boolean shouldHaveDeserializedResult) {
            mShouldHaveDeserializedResult = shouldHaveDeserializedResult;
            mLatch = latch;
        }

        @Override
        public void onRequestStarted(HootRequest request) {
            Log.v(TAG, "onRequestStarted");
        }

        @Override
        public void onSuccess(HootRequest request, HootResult result) {
            Log.v(TAG, "onSuccess");
            if (mShouldHaveDeserializedResult) {
                assertNotNull(result.getDeserializedResult());
            }
            mLatch.countDown();
        }

        @Override
        public void onFailure(HootRequest request, HootResult result) {
            Log.v(TAG, "onFailure");
            mLatch.countDown();
        }

        @Override
        public void onCancelled(HootRequest request) {
            Log.v(TAG, "onCancelled");
            mLatch.countDown();
        }

        @Override
        public void onRequestCompleted(HootRequest request) {
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

    private class TestHootGlobalDeserializer extends HootGlobalDeserializer {
        private final String TAG = TestHootGlobalDeserializer.class
                .getSimpleName();

        public TestHootGlobalDeserializer() {
            super(true);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.twotoasters.android.hoot.HootGlobalDeserializer#deserialize(java
         * .io.InputStream, java.lang.Class)
         */
        @Override
        public <T> T deserialize(InputStream is, Class<T> clazz) {
            Log.v(TAG, "deserializing [" + clazz.getSimpleName() + "]");
            ObjectMapper mapper = new ObjectMapper();
            try {
                T object = mapper.readValue(is, clazz);
                return object;
            } catch (JsonParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JsonMappingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

    }
}
