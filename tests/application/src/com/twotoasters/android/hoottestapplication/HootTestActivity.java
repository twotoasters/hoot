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

package com.twotoasters.android.hoottestapplication;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.twotoasters.android.hoot.Hoot;
import com.twotoasters.android.hoot.HootDeserializer;
import com.twotoasters.android.hoot.HootRequest;
import com.twotoasters.android.hoot.HootRequest.HootRequestListener;
import com.twotoasters.android.hoot.HootResult;
import com.twotoasters.android.hoot.activity.HootBaseActivity;
import com.twotoasters.android.hoottestapplication.data.TestData;

public class HootTestActivity extends HootBaseActivity {

    public HootRequest<TestData> mRequest;

    /*
     * (non-Javadoc)
     * 
     * @see com.twotoasters.android.hoot.activity.HootBaseActivity#onCreate
     * (android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hoot_test_activity);

        mHootRestClient = Hoot
                .createInstanceWithBaseUrl("http://10.0.2.2:4567");
        Button button = (Button) findViewById(R.id.create_request_1);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mRequest = createRequest(mHootRestClient,
                        testDataRequestListener, TestData.class).get()
                        .setDeserializer(testDataDeserializer);
                mRequest.execute();
            }
        });
        button = (Button) findViewById(R.id.create_request_2);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mRequest = createRequest(mHootRestClient,
                        testDataRequestListener, TestData.class).get()
                        .setResource("wait")
                        .setDeserializer(testDataDeserializer);
                mRequest.execute();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onRequestReconnect(HootRequest<?> request) {
        mRequest = (HootRequest<TestData>) request;
        connectToRequest(mRequest, testDataRequestListener);
        return true;
    }

    // -------------------------------------------------------------------------
    // END OF PUBLIC INTERFACE
    // -------------------------------------------------------------------------
    private static final String TAG = HootTestActivity.class.getSimpleName();
    private Hoot mHootRestClient;

    private HootRequestListener<TestData> testDataRequestListener = new HootRequestListener<TestData>() {

        @Override
        public void onRequestStarted(HootRequest<TestData> request) {
            Log.v(TAG, "onRequestStarted");
        }

        @Override
        public void onSuccess(HootRequest<TestData> request,
                HootResult<TestData> result) {
            Log.v(TAG, "onSuccess");
        }

        @Override
        public void onFailure(HootRequest<TestData> request,
                HootResult<TestData> result) {
            Log.v(TAG, "onFailure");
        }

        @Override
        public void onCancelled(HootRequest<TestData> request) {
            Log.v(TAG, "onCancelled");
        }

        @Override
        public void onRequestCompleted(HootRequest<TestData> request) {
            Log.v(TAG, "onRequestCompleted");
        }
    };

    protected HootDeserializer<TestData> testDataDeserializer = new HootDeserializer<TestData>(
            false) {

        @Override
        public TestData deserialize(String string) {
            try {
                return TestData.fromJson(new JSONObject(string));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    };

}
