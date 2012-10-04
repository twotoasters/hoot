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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.jayway.android.robotium.solo.Solo;
import com.twotoasters.android.hoottestapplication.HootTestActivity;
import com.twotoasters.android.hoottestapplication.data.TestData;

import de.akquinet.android.marvin.ActivityTestCase;
import de.akquinet.android.marvin.matchers.Condition;

public class HootActivityTest extends ActivityTestCase<HootTestActivity> {

    public HootActivityTest() {
        super(HootTestActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        mSolo = new Solo(getInstrumentation(), getActivity());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRequestIsHandled() {
        mSolo.clickOnButton("Create Request 1");

        final HootTestActivity activity = getActivity();

        try {
            waitForCondition(new Condition() {

                @Override
                public boolean matches() {
                    return activity.mRequest.isComplete();
                }

            }, 5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        assertTrue(activity.mRequest != null
                && activity.mRequest.getResult() != null
                && activity.mRequest.getResult().isSuccess()
                && activity.mRequest.getResult().getDeserializedResult() != null
                && ((TestData) activity.mRequest.getResult()
                        .getDeserializedResult()).test.equals("This is a test"));
    }

    public void testOnPauseOnResume() {
        mSolo.clickOnButton("Create Request 2");

        final HootTestActivity activity = getActivity();

        getInstrumentation().callActivityOnPause(activity);
        getInstrumentation().callActivityOnResume(activity);

        try {
            waitForCondition(new Condition() {

                @Override
                public boolean matches() {
                    return activity.mRequest.isComplete();
                }

            }, 15, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertTrue(activity.mRequest != null
                && activity.mRequest.getResult() != null
                && activity.mRequest.getResult().isSuccess()
                && activity.mRequest.getResult().getDeserializedResult() != null
                && ((TestData) activity.mRequest.getResult()
                        .getDeserializedResult()).test.equals("This is a test"));
    }

    public void testOnPauseCompleteOnResume() {
        mSolo.clickOnButton("Create Request 1");

        final HootTestActivity activity = getActivity();

        getInstrumentation().callActivityOnPause(activity);

        sleep(12);

        getInstrumentation().callActivityOnResume(activity);

        try {
            waitForCondition(new Condition() {

                @Override
                public boolean matches() {
                    return activity.mRequest.isComplete();
                }

            }, 15, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertTrue(activity.mRequest != null
                && activity.mRequest.getResult() != null
                && activity.mRequest.getResult().isSuccess()
                && activity.mRequest.getResult().getDeserializedResult() != null
                && ((TestData) activity.mRequest.getResult()
                        .getDeserializedResult()).test.equals("This is a test"));
    }

    private Solo mSolo = null;
}
