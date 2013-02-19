package com.twotoasters.android.hoot;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

public abstract class HootThreadPoolAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	
	private static final boolean IS_SERIAL_EXECUTOR_BY_DEFAULT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2;
		
	@SuppressLint("NewApi")
	public void executeOnThreadPoolExecutor(Params...params) {
		if(IS_SERIAL_EXECUTOR_BY_DEFAULT) {
			executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		} else {
			execute(params);
		}
	}
}
