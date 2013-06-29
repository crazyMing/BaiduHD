package com.baidu.hd.log;

import android.util.Log;

public class DebugLogger {

	private static final boolean enable = true;
	private static final String TAG = "Debug";
	
	public static void write(String msg) {
		if(enable) {
			Log.w(TAG, msg);
		}
	}
}
