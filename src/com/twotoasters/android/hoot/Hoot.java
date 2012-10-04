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

import android.util.Base64;

import com.twotoasters.android.hoot.HootRequest.HootRequestListener;

public class Hoot {

    public static Hoot createInstanceWithBaseUrl(String baseUrl) {
        return new Hoot(baseUrl);
    }

    public HootRequest createRequest(HootRequestListener listener) {
        return new HootRequest(this, listener);
    }

    public void setBasicAuth(String username, String password) {
        mBasicAuthUsername = username;
        mBasicAuthPassword = password;
    }

    // -------------------------------------------------------------------------
    // END OF PUBLIC INTERFACE
    // -------------------------------------------------------------------------
    private String mBasicAuthUsername = null;
    private String mBasicAuthPassword = null;
    private String mBaseUrl;

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

    String calculateBasicAuthHeader() {
        return "Basic "
                + Base64.encodeToString(new String(getBasicAuthUsername() + ":"
                        + getBasicAuthPassword()).getBytes(), Base64.NO_WRAP);
    }

}
