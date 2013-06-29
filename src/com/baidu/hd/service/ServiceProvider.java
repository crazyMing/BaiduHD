package com.baidu.hd.service;

import android.content.Context;

public interface ServiceProvider {

	void setContext(Context ctx);
	void onCreate();
	void onDestory();
	void onSave();
}
