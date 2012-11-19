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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;

import com.twotoasters.android.hoot.Hoot;
import com.twotoasters.android.hoot.HootRequest;
import com.twotoasters.android.hoot.HootRequest.HootRequestListener;
import com.twotoasters.android.hoot.HootResult;

public class HootActivityHelper {

    public interface OnHootRequestReconnect {
        public boolean onRequestReconnect(HootRequest request);
    }

    @SuppressWarnings("unchecked")
    public HootActivityHelper(Activity activity,
            OnHootRequestReconnect reconnectListener) {
        mHootRequestReconnectListener = reconnectListener;
        mRetainedInstanceMap = (Map<String, Object>) getLastNonConfigurationInstanceFromActivity(activity);
        if (mRetainedInstanceMap == null) {
            mRetainedInstanceMap = new HashMap<String, Object>();
            mRequests = new ArrayList<HootRequest>();
        } else {
            mRequests = (List<HootRequest>) mRetainedInstanceMap.get(REQUESTS);
        }
    }

    public HootRequest connectToRequest(HootRequest request,
            HootRequestListener listener) {
        if (!mRequests.contains(request)) {
            mRequests.add(request);
        }
        request.bindListener(new HootActivityHelperRequestListener(listener));
        return request;
    }

    public Object onRetainNonConfigurationInstance() {
        detachAll(false);
        mRetainedInstanceMap.remove(REQUESTS);
        if (mRequests != null) {
            mRetainedInstanceMap.put(REQUESTS, mRequests);
        }
        return mRetainedInstanceMap;
    }

    public void detachAll(boolean cancel) {
        for (HootRequest request : mRequests) {
            if (request.getListener() instanceof HootActivityHelperRequestListener) {
                HootActivityHelperRequestListener listener = (HootActivityHelperRequestListener) request
                        .getListener();
                listener.detach();
            }
            request.unbindListener(cancel);
        }
    }

    public void reattachAll() {
        if (mHootRequestReconnectListener != null) {
            List<HootRequest> toAttach = new ArrayList<HootRequest>(mRequests);
            List<HootRequest> toRemove = new ArrayList<HootRequest>();
            for (HootRequest request : toAttach) {
                if (!mHootRequestReconnectListener.onRequestReconnect(request)) {
                    toRemove.add(request);
                }
            }

            for (HootRequest request : toRemove) {
                mRequests.remove(request);
            }
        }
    }

    /**
     * This is the way to save non-configuration data for activities.
     * 
     * @param key
     * @param data
     */
    public void addDataToNonConfigurationInstance(String key, Object data) {
        mRetainedInstanceMap.put(key, data);
    }

    /**
     * 
     * @param key
     * @return
     */
    public Object fetchDataFromNonConfigurationInstance(String key) {
        return mRetainedInstanceMap.get(key);
    }

    public HootRequest createRequest(Hoot kit, HootRequestListener listener) {

        HootRequest request = kit
                .createRequest();
        return connectToRequest(request, listener);
    }

    // -------------------------------------------------------------------------
    // End of public interface
    // -------------------------------------------------------------------------
    private static final String REQUESTS = "@!(R)!@";
    private OnHootRequestReconnect mHootRequestReconnectListener;
    private List<HootRequest> mRequests;
    private Map<String, Object> mRetainedInstanceMap;

    private Object getLastNonConfigurationInstanceFromActivity(Activity activity) {
        if (activity instanceof FragmentActivity) {
            return ((FragmentActivity) activity)
                    .getLastCustomNonConfigurationInstance();
        } else {
            return activity.getLastNonConfigurationInstance();
        }
    }

    private class HootActivityHelperRequestListener implements
            HootRequestListener {
        HootRequestListener _listener = null;

        public HootActivityHelperRequestListener(HootRequestListener listener) {
            _listener = listener;
        }

        public void detach() {
            _listener = null;
        }

        @Override
        public void onRequestStarted(HootRequest request) {
            if (_listener != null) {
                _listener.onRequestStarted(request);
            }
        }

        @Override
        public void onRequestCompleted(HootRequest request) {
            if (_listener != null) {
                _listener.onRequestCompleted(request);
            }
            requestFinished(request);
        }

        @Override
        public void onSuccess(HootRequest request, HootResult result) {
            if (_listener != null) {
                _listener.onSuccess(request, result);
            }
        }

        @Override
        public void onFailure(HootRequest request, HootResult result) {
            if (_listener != null) {
                _listener.onFailure(request, result);
            }
        }

        @Override
        public void onCancelled(HootRequest request) {
            if (_listener != null) {
                _listener.onCancelled(request);
            }
        }

    }

    private void requestFinished(HootRequest request) {
        mRequests.remove(request);
    }

}
