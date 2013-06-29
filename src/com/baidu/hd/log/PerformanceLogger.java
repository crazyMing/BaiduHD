package com.baidu.hd.log;

import java.util.Date;

import android.util.Log;

public class PerformanceLogger {

	private static final boolean enableLogger = true;
	private static final String TAG = "Performance";
	
	private static long lastTick = 0;
	
	public static void write(String msg) {
		
		if(enableLogger) {
			long curTick = new Date().getTime();
			Log.v(TAG, "" + (curTick - lastTick) + " " + msg);
			lastTick = curTick;
		}
	}
}
