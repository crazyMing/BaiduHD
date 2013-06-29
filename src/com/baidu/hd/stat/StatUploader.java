package com.baidu.hd.stat;

import java.util.Hashtable;
import java.util.Map;

import android.util.Log;

import com.baidu.hd.conf.Configuration;
import com.baidu.hd.net.HttpComm;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.util.StringUtil;

class StatUploader {
	
	private static final String TAG = "StatUploader";

	private ServiceFactory mServiceFactory = null;

	public void create(ServiceFactory serviceFactory) {
		this.mServiceFactory = serviceFactory;
	}
	
	public void postLog(String message) {
		Log.d(TAG, "post log");
		this.postToServer("log", message);
	}
	
	public void postSnifferFail(String message) {
		Log.d(TAG, "post sniffer fail");
		this.postToServer("sniffer", message);
	}
	
	public void postPlayFail(String message) {
		Log.d(TAG, "post play fail");
		this.postToServer("play", message);
	}
	
	public void postStat(String message) {
		// TODO
		Log.d(TAG, "post stat" + message);
	}
	
	private void postToServer(String tag, String msg) {
		
		if(!StringUtil.isEmpty(msg)) {
			
			Configuration conf = (Configuration)this.mServiceFactory.getServiceProvider(Configuration.class);
			String serverFormat = conf.getUploadLogUrl();

			Map<String, String> param = new Hashtable<String, String>();
			param.put("postdata", "\r\n" + msg);
			String logServer = String.format(serverFormat, tag);
			new HttpComm().post(logServer, param, true, null);
		}
	}
}
