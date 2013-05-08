package com.twotoasters.android.hoot;

class Log {
	
	private static int minPriority = android.util.Log.VERBOSE;
	
	static void setMinPriority(int minPriority) {
		Log.minPriority = minPriority;
	}
	
	static void v(String tag, String msg) {
		if (minPriority >= android.util.Log.VERBOSE) {
			android.util.Log.v(tag, msg);
		}
	}
	
	static void d(String tag, String msg) {
		if (minPriority >= android.util.Log.DEBUG) {
			android.util.Log.d(tag, msg);
		}
	}

}
