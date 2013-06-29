package com.baidu.player.download;

import com.baidu.player.download.JNITaskCreateParam;
import com.baidu.player.download.JNITaskInfo;
import com.baidu.player.download.JNITaskBuffer;

interface DownloadServiceInterface {

	int init();
	int uninit();
	int setDeviceId(String value);
	int create(inout JNITaskCreateParam param);
	int start(long handle);
	int stop(long handle);
	int delete(long handle);
	int query(long handle, inout JNITaskInfo jniTaskInfo);
	int parseUrl(String url, inout JNITaskInfo jniTaskInfo);
	int isFileExist(String path, String fileFullName, long fileSize);
	int setSpeedLimit(int value);
	int getBlock(long handle, out JNITaskBuffer buffer);
	int setLogLevel(int value);
	int setPlaying(long handle, boolean playing);
	int setMediaTime(long handle, int value);
	int getRedirectUrl(long handle, inout JNITaskInfo jniTaskInfo);
	int statReport(String key, String value);
}