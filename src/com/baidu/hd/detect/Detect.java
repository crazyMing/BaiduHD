package com.baidu.hd.detect;

import com.baidu.hd.service.ServiceProvider;

public interface Detect extends ServiceProvider {

	NetUsable getNetUsable();
	boolean isNetAvailabe();
	boolean isNetAvailabeWithWifi();
	boolean isSDCardAvailable();

	boolean isNetPrompt();
	void setNetPrompt(boolean value);
	
	void filterByNet(FilterCallback callback);
	void SDCardPrompt();
}
