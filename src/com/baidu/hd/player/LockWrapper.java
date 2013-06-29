package com.baidu.hd.player;

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

class LockWrapper {

	/**
	 * »½ÐÑËø
	 */
	private WakeLock mWakeLock = null;
	private static final String POWER_LOCK = "BAIDUSDLPLAYER";

	private Activity mActivity = null;

	LockWrapper(Activity activity) {
		this.mActivity = activity;
	}

	void onCreate() {
		PowerManager pm = (PowerManager) this.mActivity
				.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, POWER_LOCK);
	}

	void onResume() {
		if (null != mWakeLock && (!mWakeLock.isHeld())) {
			mWakeLock.acquire();
		}
	}

	void onPause() {
		if (null != mWakeLock && (mWakeLock.isHeld())) {
			mWakeLock.release();
		}
	}
}