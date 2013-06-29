package com.baidu.hd.service;

import android.content.Context;

public interface ServiceFactory {

	ServiceProvider getServiceProvider(Class<? extends ServiceProvider> type);

	void setContext(Context ctx);
	void create();
	void destory();
	void save();
}
