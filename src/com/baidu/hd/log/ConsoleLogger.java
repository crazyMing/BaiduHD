package com.baidu.hd.log;

import android.util.Log;

class ConsoleLogger {

	private static final boolean enable = true;
	
	public void write(int level, String tag, String msg) {
		
		if(!enable) {
			return;
		}
		
		switch(level) {
		case Logger.Level.verbose:
			Log.v(tag, msg);
			break;
		case Logger.Level.debug:
			Log.d(tag, msg);
			break;
		case Logger.Level.info:
			Log.i(tag, msg);
			break;
		case Logger.Level.warn:
			Log.w(tag, msg);
			break;
		case Logger.Level.error:
			Log.e(tag, msg);
			break;
		}
	}
}
