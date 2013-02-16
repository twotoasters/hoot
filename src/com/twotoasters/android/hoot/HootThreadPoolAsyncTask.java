package com.twotoasters.android.hoot;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

public abstract class HootThreadPoolAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	@SuppressLint("NewApi")
	public void executeOnThreadPoolExecutor(Params...params) {
		if(isSerialExecutorByDefault()) {
			executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		} else {
			execute(params);
		}
	}
	
	private static boolean isSerialExecutorByDefault() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2;
	}
}
