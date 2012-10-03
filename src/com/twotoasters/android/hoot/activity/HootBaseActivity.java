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

package com.twotoasters.android.hoot.activity;

import android.app.Activity;
import android.os.Bundle;

import com.twotoasters.android.hoot.Hoot;
import com.twotoasters.android.hoot.HootRequest;
import com.twotoasters.android.hoot.HootRequest.HootRequestListener;

public abstract class HootBaseActivity extends Activity implements
        HootActivityHelper.OnHootRequestReconnect {

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHootActivityHelper = new HootActivityHelper(this, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
    @Override
    final public Object onRetainNonConfigurationInstance() {
        if (mHootActivityHelper != null) {
            onAddNonConfigurationData();
            return mHootActivityHelper.onRetainNonConfigurationInstance();
        }
        return null;
    }

    /**
     * Subclasses override if they wish to add data to the non-configuration
     * instance
     */
    protected void onAddNonConfigurationData() {
        // do nothing
    }

    /**
     * Helper method to add data to the non-configuration instance. Should only
     * be called from within the <code>onAddNonConfigurationData()</code>
     * method.
     * 
     * @param key
     *            a key to access the data later
     * @param data
     *            the data to store. Can be anything, really.
     */
    protected void addDataToNonConfigurationInstance(String key, Object data) {
        if (mHootActivityHelper != null) {
            mHootActivityHelper.addDataToNonConfigurationInstance(key, data);
        }
    }

    /**
     * Helper method to fetch previously stored data by the
     * <code>addDataToNonConfigurationInstance()</code> method.
     * 
     * @param key
     *            the key that the data was saved under
     * @return the object or <code>null</code> if no instance was found or the
     *         key wasn't found.
     */
    protected Object fetchDataFromNonConfigurationInstance(String key) {
        if (mHootActivityHelper != null) {
            return mHootActivityHelper
                    .fetchDataFromNonConfigurationInstance(key);
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (mHootActivityHelper != null) {
            // if we're finishing, we want to cancel any requests in progress.
            // otherwise, let the requests finish and we'll get the results
            // when we return.
            if (isFinishing()) {
                mHootActivityHelper.detachAll(true);
                mHootActivityHelper = null;
            } else {
                mHootActivityHelper.detachAll(false);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (mHootActivityHelper != null) {
            mHootActivityHelper.reattachAll();
        }
    }

    /**
     * The method for subclasses to create Hoot requests with the help of the
     * Activity lifecycle management.
     * 
     * @param kit
     *            the Hoot instance
     * @param listener
     *            the listener to call
     * @param clazz
     *            the class of the object expected (null if void).
     * @return the request
     */
    protected <T> HootRequest<T> createRequest(Hoot kit,
            HootRequestListener<T> listener, Class<T> clazz) {
        return mHootActivityHelper.createRequest(kit, listener, clazz);
    }

    /**
     * The method subclasses should call from their
     * <code>onRequestReconnect()</code> method to reconnect to existing
     * requests when queried.
     * 
     * @param request
     *            the request to reconnect to
     * @param listener
     *            the listener to call
     * @return the request
     */
    public <T> HootRequest<T> connectToRequest(HootRequest<T> request,
            HootRequestListener<T> listener) {
        return mHootActivityHelper.connectToRequest(request, listener);
    }

    // -------------------------------------------------------------------------
    // End of public interface
    // -------------------------------------------------------------------------
    private HootActivityHelper mHootActivityHelper = null;

}
