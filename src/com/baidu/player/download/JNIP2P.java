package com.baidu.player.download;

import android.util.Log;

public class JNIP2P {
	
	public class TASK_STATUS_CODE {
		public static final int TSC_NOITEM = 0; 	//
		public static final int TSC_ERROR = 1;      //任务失败
		public static final int TSC_PAUSE = 2;      //任务暂停
		public static final int TSC_CONNECT = 3;    //连接中
		public static final int TSC_DOWNLOAD = 4;   //下载中
		public static final int TSC_COMPLETE = 5;   //下载完成
		public static final int TSC_QUEUE = 6;      //排队中
	}
	
	public class TASK_FAIL_CODE {
		public static final int TFC_NOERROR = 0;
		public static final int TFC_TIME_OUT = 1;
		public static final int TFC_DISK_SPACE = 2;		// 磁盘空间不足。此时 nTotalSize 是总计需要的字节数
		public static final int TFC_FILE_ERROR = 3;		// 文件写入失败。例如文件名非法，等。
		public static final int TFC_SOURCE_FAIL = 4;	// 资源失效
		public static final int TFC_ALREADY_EXIST = 5;	// p2p任务重复
		public static final int TFC_NOT_SUPPORT = 6;	// 不支持的协议
		public static final int TFC_RENAME_FAIL = 7;	// 改名失败
		public static final int TFC_FORBIDDEN = 8;		// 非法禁止
	};
	
	public class APIErrorCode {
		public static final int API_SUCCESS         =  0;   //成功
		public static final int API_ERROR_PARAM     = -1;   //错误参数
		public static final int API_ERROR_HANDLE    = -2;   //无效任务句柄
		public static final int API_ERROR_UNKONWN   = -3;   //重复初始化
		public static final int API_ERROR_LINK      = -4;   //无法连接后台进程
		public static final int API_ERROR_BUFFER    = -5;   //废弃
		public static final int API_ERROR_NOT_FOUND = -6;   //任务未找到
		public static final int API_ERROR_CREATE_FAIL = -7; //创建失败，如SDcard不可用
		public static final int API_ERROR_SHUTDOWN    = -8; //服务已运行
	};
	
	private static class LogWrapper {
		
		private String message = "";
		private boolean isVerbose = false;
		public LogWrapper(String message) {
			this(message, false);
		}
		public LogWrapper(String message, boolean isVerbose) {
			this.message = message;
			this.isVerbose = isVerbose;
			this.log(true);
		}
		public void release() {
			this.log(false);
		}
		
		private void log(boolean isStart) {
			if(this.isVerbose) {
				Log.v(":download", this.message + (isStart ? " start" : " end"));
			} else {
				Log.d(":download", this.message + (isStart ? " start" : " end"));
			}
		}
	}

	private static JNIP2P instance = new JNIP2P();
	public static JNIP2P getInstance() {
		return instance;
	}
	
	private JNIP2P() {
	}

	public int init() {
		LogWrapper log = new LogWrapper("netInit");
		try {
			return netInit();
		} finally {
			log.release();
		}
	}
	
	public int uninit() {
		LogWrapper log = new LogWrapper("netQuit");
		try {
			return netQuit();
		} finally {
			log.release();
		}
	}
	
	public int create(JNITaskCreateParam param) {
		LogWrapper log = new LogWrapper("create");
		try {
			return netCreate(param);
		} finally {
			log.release();
		}
	}
	
	public int start(long handle) {
		LogWrapper log = new LogWrapper("netStart");
		try {
			return netStart(handle);
		} finally {
			log.release();
		}
	}

	public int stop(long handle) {
		LogWrapper log = new LogWrapper("netStop");
		try {
			return netStop(handle);
		} finally {
			log.release();
		}
	}
	
	public int delete(long handle) {
		LogWrapper log = new LogWrapper("netDelete");
		try {
			return netDelete(handle);
		} finally {
			log.release();
		}
	}
	
	public int query(long handle, JNITaskInfo jniTaskInfo) {
		LogWrapper log = new LogWrapper("netQueryTaskInfo", true);
		try {
			return netQueryTaskInfo(handle, jniTaskInfo);
		} finally {
			log.release();
		}
	}
	
	public int parseUrl(String url, JNITaskInfo jniTaskInfo) {
		LogWrapper log = new LogWrapper("netParseURL");
		try {
			return netParseURL(url, jniTaskInfo);
		} finally {
			log.release();
		}
	}
	
	public int deleteFile(String path, String name, long fileSize) {
		LogWrapper log = new LogWrapper("netDeleteFile");
		try {
			return netDeleteFile(path, name, fileSize);
		} finally {
			log.release();
		}
	}
	
	public int isFileExist(String path, String fileFullName, long fileSize) {
		LogWrapper log = new LogWrapper("netFileExist");
		try {
			return netFileExist(path, fileFullName, fileSize);
		} finally {
			log.release();
		}
	}
	
	public int setSpeedLimit(int value) {
		LogWrapper log = new LogWrapper("netSetSpeedLimit", true);
		try {
			return netSetSpeedLimit(value);
		} finally {
			log.release();
		}
	}
	
	public int getBlock(long handle, JNITaskBuffer buffer) {
		int readedSize = JNITaskBuffer.BufferSize;
		LogWrapper log = new LogWrapper("netGetBlockInfo", true);
		try {
			return netGetBlockInfo(handle, buffer, readedSize);
		} finally {
			log.release();
		}
	}
	
	public int setLogLevel(int value) {
		LogWrapper log = new LogWrapper("netSetLogLevel", true);
		try {
			return netSetLogLevel(value);
		} finally {
			log.release();
		}
	}
	
	public int setPlaying(long handle, boolean playing) {
		LogWrapper log = new LogWrapper("netSetPlaying");
		try {
			return netSetPlaying(handle, playing);
		} finally {
			log.release();
		}
	}
	
	public int setDeviceId(String value) {
		LogWrapper log = new LogWrapper("setDeviceId");
		try {
			return netSetDeviceID(value);
		} finally {
			log.release();
		}
	}
	
	public int setMediaTime(long handle, int value) {
		LogWrapper log = new LogWrapper("setMediaTime");
		try {
			return netSetMediaTime(handle, value);
		} finally {
			log.release();
		}
	}
	
	public int getRedirectUrl(long handle, JNITaskInfo jniTaskInfo) {
		LogWrapper log = new LogWrapper("getRedirectUrl");
		try {
			return netGetRedirectUrl(handle, jniTaskInfo);
		} finally {
			log.release();
		}
	}
	
	public int statReport(String version, String channel, String key, String value) {
		LogWrapper log = new LogWrapper("statReport");
		try {
			return netStatReport("bdp_adr", version, key, channel, value);
		} finally {
			log.release();
		}
	}
	
	//内核初始化
	public native int netInit();
	
	//内核退出
	public native int netQuit();
	
	//创建任务
	public native int netCreate(JNITaskCreateParam param);
	
	//删除任务
	public native int netDelete(long nHandle);
	
	//开始下载
	public native int netStart(long nHandle);
	
	//停止下载
	public native int netStop(long nHandle);
	
	//获取任务信息
	public native int netQueryTaskInfo(long nHandle, JNITaskInfo jniTaskInfo);
	
	//解析URL，获取文件名和文件大小
	public native int netParseURL(String strUrl, JNITaskInfo jniTaskInfo);
	
	//设置网络状态
	public native int netSetNetworkStatus(boolean bCanUse);
	
	// 删除文件
	public native int netDeleteFile(String strPath, String strName, long nFileSize);
	
	// 文件是否存在
	public native int netFileExist(String strPath, String strFileName, long nFileSize);
	
	// 限速
	public native int netSetSpeedLimit(int nValue);
	
	// 获得下载的块信息
	public native int netGetBlockInfo(long nHandle, JNITaskBuffer taskBuffer, long nToRead);
	
	// 设置日志等级
	public native int netSetLogLevel(int level);
	
	// 设置正在播放
	public native int netSetPlaying(long nHandle, boolean bPlaying);
	
	// 设置设备id
	public native int netSetDeviceID(String value);
	
	// 设置视频时长
	public native int netSetMediaTime(long handle, int value);
	
	// 获取跳转的url
	public native int netGetRedirectUrl(long nHandle, JNITaskInfo jniTaskInfo);
	
	// 上报信息
	public native int netStatReport(String strProduct, String strVersion, String strSubstat, String strChannel, String strBody);
	
	static{
        System.loadLibrary("stlport_shared");
        System.loadLibrary("p2p-jni");
	}
}
