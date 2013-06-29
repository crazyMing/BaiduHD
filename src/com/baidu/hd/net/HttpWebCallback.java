package com.baidu.hd.net;

import android.graphics.Bitmap;

public interface HttpWebCallback {

	void onTitle(String url, String title);
	void onImage(String url, Bitmap image);
	void onStop(String url);
}
