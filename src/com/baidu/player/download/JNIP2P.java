package com.baidu.player.download;

import android.util.Log;

public class JNIP2P {
	
	public class TASK_STATUS_CODE {
		public static final int TSC_NOITEM = 0; 	//
		public static final int TSC_ERROR = 1;      //����ʧ��
		public static final int TSC_PAUSE = 2;      //������ͣ
		public static final int TSC_CONNECT = 3;    //������
		public static final int TSC_DOWNLOAD = 4;   //������
		public static final int TSC_COMPLETE = 5;   //�������
		public static final int TSC_QUEUE = 6;      //�Ŷ���
	}
	
	public class TASK_FAIL_CODE {
		public static final int TFC_NOERROR = 0;
		public static final int TFC_TIME_OUT = 1;
		public static final int TFC_DISK_SPACE = 2;		// ���̿ռ䲻�㡣��ʱ nTotalSize ���ܼ���Ҫ���ֽ���
		public static final int TFC_FILE_ERROR = 3;		// �ļ�д��ʧ�ܡ������ļ����Ƿ����ȡ�
		public static final int TFC_SOURCE_FAIL = 4;	// ��ԴʧЧ
		public static final int TFC_ALREADY_EXIST = 5;	// p2p�����ظ�
		public static final int TFC_NOT_SUPPORT = 6;	// ��֧�ֵ�Э��
		public static final int TFC_RENAME_FAIL = 7;	// ����ʧ��
		public static final int TFC_FORBIDDEN = 8;		// �Ƿ���ֹ
	};
	
	public class APIErrorCode {
		public static final int API_SUCCESS         =  0;   //�ɹ�
		public static final int API_ERROR_PARAM     = -1;   //�������
		public static final int API_ERROR_HANDLE    = -2;   //��Ч������
		public static final int API_ERROR_UNKONWN   = -3;   //�ظ���ʼ��
		public static final int API_ERROR_LINK      = -4;   //�޷����Ӻ�̨����
		public static final int API_ERROR_BUFFER    = -5;   //����
		public static final int API_ERROR_NOT_FOUND = -6;   //����δ�ҵ�
		public static final int API_ERROR_CREATE_FAIL = -7; //����ʧ�ܣ���SDcard������
		public static final int API_ERROR_SHUTDOWN    = -8; //����������
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
	
	//�ں˳�ʼ��
	public native int netInit();
	
	//�ں��˳�
	public native int netQuit();
	
	//��������
	public native int netCreate(JNITaskCreateParam param);
	
	//ɾ������
	public native int netDelete(long nHandle);
	
	//��ʼ����
	public native int netStart(long nHandle);
	
	//ֹͣ����
	public native int netStop(long nHandle);
	
	//��ȡ������Ϣ
	public native int netQueryTaskInfo(long nHandle, JNITaskInfo jniTaskInfo);
	
	//����URL����ȡ�ļ������ļ���С
	public native int netParseURL(String strUrl, JNITaskInfo jniTaskInfo);
	
	//��������״̬
	public native int netSetNetworkStatus(boolean bCanUse);
	
	// ɾ���ļ�
	public native int netDeleteFile(String strPath, String strName, long nFileSize);
	
	// �ļ��Ƿ����
	public native int netFileExist(String strPath, String strFileName, long nFileSize);
	
	// ����
	public native int netSetSpeedLimit(int nValue);
	
	// ������صĿ���Ϣ
	public native int netGetBlockInfo(long nHandle, JNITaskBuffer taskBuffer, long nToRead);
	
	// ������־�ȼ�
	public native int netSetLogLevel(int level);
	
	// �������ڲ���
	public native int netSetPlaying(long nHandle, boolean bPlaying);
	
	// �����豸id
	public native int netSetDeviceID(String value);
	
	// ������Ƶʱ��
	public native int netSetMediaTime(long handle, int value);
	
	// ��ȡ��ת��url
	public native int netGetRedirectUrl(long nHandle, JNITaskInfo jniTaskInfo);
	
	// �ϱ���Ϣ
	public native int netStatReport(String strProduct, String strVersion, String strSubstat, String strChannel, String strBody);
	
	static{
        System.loadLibrary("stlport_shared");
        System.loadLibrary("p2p-jni");
	}
}
