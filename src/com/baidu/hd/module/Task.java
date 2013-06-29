package com.baidu.hd.module;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.util.StringUtil;

public abstract class Task {
	
	/**
	 * 获得任务类型
	 */
	abstract public int getType();
	
	/**
	 * 获得主键
	 */
	abstract public String getKey();
	
	abstract public SmallSiteTask toSmall();
	abstract public BigSiteTask toBig();
	
	/**
	 * 是否可以显示在下载列表
	 */
	abstract public boolean isVisible();
	
	/**
	 * 任务类型
	 */
	public class Type {
		public static final int Small = 1;
		public static final int Big = 2;
	}
	
	/**
	 * 状态类型
	 */
	public class State {
		public static final int Start = 1;
		public static final int Stop = 2;
		public static final int Complete = 3;
		public static final int Error = 4;
		public static final int Queue = 5;
	}

	/**
	 * 错误码
	 */
 	public class ErrorCode {
 		public static final int None = 0;

 		// 与JNIP2P的错误码同步
		public static final int Timeout = 1;
		public static final int DiskSpace = 2;		// 磁盘空间不足。此时 nTotalSize 是总计需要的字节数
		public static final int FileError = 3;		// 文件写入失败。例如文件名非法，等。
		public static final int SourceFail = 4;		// 资源失效
		public static final int AlreadyExist = 5;	// p2p任务重复
		public static final int NotSupport = 6;		// 不支持的协议
		public static final int RenameFail = 7;		// 改名失败
		public static final int Forbidden = 8;		// 非法禁止

		public static final int SnifferFail = 9;	// 嗅探失败
		public static final int ParseFail = 10;		// 解析文件失败
		public static final int ReplaceFail = 11;	// 替换ts链接失败

		public static final int Unknown = 100;		// 未知错误
	}

 	/** 数据库id	*/
	private long id = -1;
	
	/** 任务句柄 */
	private long handle = 0;
	
	/** 视频链接 */
	private String url = "";
	
	/** 引用页 */
	private String refer = "";
	
	/** 剧集ID */
	private long albumId = -1;
	
	/** 显示名 */
	private String name = "";
	
	/** 文件名 */
	private String fileName = "";
	
	/** 文件夹名 */
	private String folderName = "";
	
	/** 文件总大小 */
	private long totalSize = 0;
	
	/** 已下载大小 */
	private long downloadSize = 0;
	
	/** 当前速度 */
	private int speed = 0;
	
	/** 状态 */
	private int state = getDefaultState();
	
	/** 错误码 */
	private int errorCode = ErrorCode.None;
	
	/** 视频类型 */
	private int videoType = NetVideo.NetVideoType.NONE;
	
	/** 完成百分比 0-100 */
	private int percent = 0;
	
	/** 是否为正在观看 */
	private boolean playing = false;
	
	/** 任务块数 */
	private int diskFile = 0;
	
	/** patch， 速度为0的次数，目前针对m3u8的优化 */
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
	 * 是否为相同的对象
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
	 * 获得格式化的状态
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
	 * 获得格式化的错误码
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
	 * 获得格式化的对象信息
	 */
	public String getInfo() {
		return this.persist().toString();
	}
	
	/**
	 * 赋值对象所有变量
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
	 * 清空所有状态的变量
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
	 * 获得默认状态
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
