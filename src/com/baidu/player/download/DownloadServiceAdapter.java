package com.baidu.player.download;

import com.baidu.hd.service.ServiceProvider;

public interface DownloadServiceAdapter extends ServiceProvider {

	int create(JNITaskCreateParam param);
	int start(long handle);
	int stop(long handle);
	int delete(long handle);
	int query(long handle, JNITaskInfo jniTaskInfo);
	int parseUrl(String url, JNITaskInfo jniTaskInfo);
	boolean isFileExist(String path, String fileFullName, long fileSize);
	int setSpeedLimit(int value);
	int getBlock(long handle, JNITaskBuffer buffer);
	int setLogLevel(int value);
	int setPlaying(long handle, boolean playing);
	int setMediaTime(long handle, int value);
	int getRedirectUrl(long handle, JNITaskInfo jniTaskInfo);
	int statReport(String key, String value);
	boolean exist(long handle);
	
	// for test
	int destroy();
}
