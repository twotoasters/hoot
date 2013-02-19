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

import android.os.AsyncTask;
import android.util.Log;

public class HootTask extends HootThreadPoolAsyncTask<HootRequest, HootRequest, HootRequest> {

    @Override
    protected HootRequest doInBackground(HootRequest... params) {
        HootRequest request = params[0];
        if (isCancelled()) {
            return request;
        }

        // let the UI thread get an update so we know we've started the request
        publishProgress(params);

        do {
            HootResult result = request.getHoot().executeRequestSynchronously(
                    request);
            if (result.isSuccess()) {
                return request;
            }
        } while (request.shouldRetry() && !isCancelled());

        return request;
    }

    @Override
    protected void onPostExecute(HootRequest request) {
        if (request != null) {
            request.setComplete(true);
        }

        if (request.getListener() != null) {
            request.getListener().onRequestCompleted(request);
            HootResult result = request.getResult();
            if (result != null && result.isSuccess()) {
                request.getListener().onSuccess(request, result);
            } else {
                request.getListener().onFailure(request, result);
            }
        }
    }

    @Override
    protected void onProgressUpdate(HootRequest... values) {
        HootRequest request = values[0];

        if (request != null && request.getListener() != null) {
            request.getListener().onRequestStarted(request);
        }
    }

    void cancel() {
        cancel(true);
    }
}
