package com.baidu.hd;

import android.app.ActivityManager;
import android.content.Context;

public class Product {

	private static Context sContext = null;

	public enum ProcessType {
		Unknown, Main, Task, Stat,
	}

	public static void init(Context context) {
		sContext = context;
	}

	private static ProcessType mType = ProcessType.Unknown;

	public static ProcessType getProcessType() {

		if (mType == ProcessType.Unknown) {

			int pid = android.os.Process.myPid();
			ActivityManager mActivityManager = (ActivityManager) sContext
					.getSystemService(Context.ACTIVITY_SERVICE);
			for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
					.getRunningAppProcesses()) {
				if (appProcess.pid == pid) {
					if (appProcess.processName
							.equalsIgnoreCase("com.baidu.hd")) {
						mType = ProcessType.Main;
						break;
					}
					if (appProcess.processName
							.equalsIgnoreCase("com.baidu.hd:task")) {
						mType = ProcessType.Task;
						break;
					}
				}
			}
		}
		return mType;
	}
}
