package com.baidu.hd.stat;

import android.os.Environment;


public class StatConst {
	
	public static final String path = 
			Environment.getExternalStorageDirectory() + "/baidu/baiduplayer/stat/";
	public static final String statPath = 
			Environment.getExternalStorageDirectory() + "/baidu/baiduplayer/stat/stat";
	public static final String snifferPath = 
			Environment.getExternalStorageDirectory() + "/baidu/baiduplayer/stat/sniffer";
	public static final String playPath = 
			Environment.getExternalStorageDirectory() + "/baidu/baiduplayer/stat/play";
	public static final String crashPath = 
			Environment.getExternalStorageDirectory() + "/baidu/baiduplayer/stat/crash";
	
	public static final String LogSep = "&";
	public static final String UdpSep = "&";
}
