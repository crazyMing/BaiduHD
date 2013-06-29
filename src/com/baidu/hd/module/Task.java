package com.baidu.hd.module;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.util.StringUtil;

public abstract class Task {
	
	/**
	 * �����������
	 */
	abstract public int getType();
	
	/**
	 * �������
	 */
	abstract public String getKey();
	
	abstract public SmallSiteTask toSmall();
	abstract public BigSiteTask toBig();
	
	/**
	 * �Ƿ������ʾ�������б�
	 */
	abstract public boolean isVisible();
	
	/**
	 * ��������
	 */
	public class Type {
		public static final int Small = 1;
		public static final int Big = 2;
	}
	
	/**
	 * ״̬����
	 */
	public class State {
		public static final int Start = 1;
		public static final int Stop = 2;
		public static final int Complete = 3;
		public static final int Error = 4;
		public static final int Queue = 5;
	}

	/**
	 * ������
	 */
 	public class ErrorCode {
 		public static final int None = 0;

 		// ��JNIP2P�Ĵ�����ͬ��
		public static final int Timeout = 1;
		public static final int DiskSpace = 2;		// ���̿ռ䲻�㡣��ʱ nTotalSize ���ܼ���Ҫ���ֽ���
		public static final int FileError = 3;		// �ļ�д��ʧ�ܡ������ļ����Ƿ����ȡ�
		public static final int SourceFail = 4;		// ��ԴʧЧ
		public static final int AlreadyExist = 5;	// p2p�����ظ�
		public static final int NotSupport = 6;		// ��֧�ֵ�Э��
		public static final int RenameFail = 7;		// ����ʧ��
		public static final int Forbidden = 8;		// �Ƿ���ֹ

		public static final int SnifferFail = 9;	// ��̽ʧ��
		public static final int ParseFail = 10;		// �����ļ�ʧ��
		public static final int ReplaceFail = 11;	// �滻ts����ʧ��

		public static final int Unknown = 100;		// δ֪����
	}

 	/** ���ݿ�id	*/
	private long id = -1;
	
	/** ������ */
	private long handle = 0;
	
	/** ��Ƶ���� */
	private String url = "";
	
	/** ����ҳ */
	private String refer = "";
	
	/** �缯ID */
	private long albumId = -1;
	
	/** ��ʾ�� */
	private String name = "";
	
	/** �ļ��� */
	private String fileName = "";
	
	/** �ļ����� */
	private String folderName = "";
	
	/** �ļ��ܴ�С */
	private long totalSize = 0;
	
	/** �����ش�С */
	private long downloadSize = 0;
	
	/** ��ǰ�ٶ� */
	private int speed = 0;
	
	/** ״̬ */
	private int state = getDefaultState();
	
	/** ������ */
	private int errorCode = ErrorCode.None;
	
	/** ��Ƶ���� */
	private int videoType = NetVideo.NetVideoType.NONE;
	
	/** ��ɰٷֱ� 0-100 */
	private int percent = 0;
	
	/** �Ƿ�Ϊ���ڹۿ� */
	private boolean playing = false;
	
	/** ������� */
	private int diskFile = 0;
	
	/** patch�� �ٶ�Ϊ0�Ĵ�����Ŀǰ���m3u8���Ż� */
	private int zeroTime = 0;
	

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getHandle() {
		return handle;
	}
	public void setHandle(long handle) {
		this.handle = handle;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getRefer() {
		return refer;
	}
	public void setRefer(String refer) {
		this.refer = refer;
	}
	public long getAlbumId() {
		return albumId;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	public int getState() {
		return state;
	}
	public void setState(int state) {
		
		if(state != State.Error) {
			this.errorCode = ErrorCode.None;
		}
		this.state = state;
	}
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		
		if(errorCode != ErrorCode.None) {
			this.state = State.Error;
		}
		this.errorCode = errorCode;
	}
	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}
	public long getTotalSize() {
		return this.totalSize;
	}
	public void setDownloadSize(long downloadSize) {
		this.downloadSize = downloadSize;
	}
	public long getDownloadSize() {
		return this.downloadSize;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
		this.zeroTime = 0;
	}
	public int getSpeed() {
		return this.speed;
	}
	public int getVideoType() {
		return videoType;
	}
	public void setVideoType(int videoType) {
		this.videoType = videoType;
	}
	public int getPercent() {
		return percent;
	}
	public void setPercent(int percent) {
		this.percent = percent;
	}
	public boolean isPlaying() {
		return playing;
	}
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}
	public int getDiskFile() {
		return diskFile;
	}
	public void setDiskFile(int value) {
		diskFile = value;
	}
	public int getZeroTime() {
		return zeroTime;
	}
	public void incZeroTime() {
		++this.zeroTime;
	}

	/**
	 * �Ƿ�Ϊ��ͬ�Ķ���
	 */
	public boolean isSame(Task task) {

		if(task == null || task.getKey() == null) {
			return false;
		}
		return this.getKey().equalsIgnoreCase(task.getKey());
	}

	public String getFormatType() {
		
		switch(this.getType()) {
		case Type.Big:
			return "Big";
		case Type.Small:
			return "Small";
		}
		return "Unknown";
	}
	
	/**
	 * ��ø�ʽ����״̬
	 */
	public String getFormatState() {
		
		switch(this.state) {
		case State.Start:
			return "Start";
		case State.Stop:
			return "Stop";
		case State.Complete:
			return "Complete";
		case State.Error:
			return "Error";
		case State.Queue:
			return "Queue";
		}
		return "Unknown";
	}

	public String getFormatVideoType() {
		return NetVideo.getFormatType(videoType);
	}
	
	/**
	 * ��ø�ʽ���Ĵ�����
	 */
	public String getFormatErrorCode() {
		
		switch(this.errorCode) {
 		case ErrorCode.None:
 			return "None";
 		case ErrorCode.Unknown:
 			return "Unknown";
		}
		return "Exception";
	}
	
	/**
	 * ��ø�ʽ���Ķ�����Ϣ
	 */
	public String getInfo() {
		return this.persist().toString();
	}
	
	/**
	 * ��ֵ�������б���
	 */
	public void copyFrom(Task value) {
		
		this.id = value.id;
		this.handle = value.handle;
		this.url = value.url;

		if(!StringUtil.isEmpty(value.fileName)) {
			this.fileName = value.fileName;
		}
		if(value.totalSize != 0) {
			this.totalSize = value.totalSize;
		}
		
		this.refer = value.refer;
		this.albumId = value.albumId;
		this.name = value.name;
		this.folderName = value.folderName;
		this.downloadSize = value.downloadSize;
		this.state = value.state;
		this.errorCode = value.errorCode;
		this.speed = value.speed;
		this.videoType = value.videoType;
		this.percent = value.percent;
		this.diskFile = value.diskFile;
	}
	
	/**
	 * �������״̬�ı���
	 */
	public void clearState() {
		
		this.handle = 0;
		this.state = Task.getDefaultState();
		this.downloadSize = 0;
		this.percent = 0;
		this.errorCode = ErrorCode.None;
		this.speed = 0;
	}
	
	/**
	 * ���Ĭ��״̬
	 */
	public static int getDefaultState() {
		return State.Stop;
	}
	
	protected JSONObject persist() {

		JSONObject o = new JSONObject();
		try {
			o.put("id", this.id);
			o.put("type", this.getFormatType());
			o.put("handle", this.handle);
			o.put("url", this.url);
			o.put("refer", this.refer);
			o.put("albumId", this.albumId);
			o.put("name", this.name);
			o.put("fileName", this.fileName);
			o.put("folderName", this.folderName);
			o.put("totalSize", this.totalSize);
			o.put("downloadSize", this.downloadSize);
			o.put("state", this.getFormatState());
			o.put("errorCode", this.getFormatErrorCode());
			o.put("speed", this.speed);
			o.put("videotype", this.getFormatVideoType());
			o.put("percent", this.percent);
			o.put("visible", this.isVisible());
			o.put("diskFile", diskFile);
			
		} catch(JSONException e) {
			e.printStackTrace();
		}
		return o;
	}
}
