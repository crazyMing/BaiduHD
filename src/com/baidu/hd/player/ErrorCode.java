package com.baidu.hd.player;

class ErrorCode {
	
	public static final int None = 0;

	/** 无效Intent */
	public static final int InvalidParam = 1;
	
	/** 内置播放内核不支持 */
	public static final int NotSupport = 2;
	
	/** 嗅探失败 */
	public static final int SnifferFail = 3;

	/** 网络不可用 */
	public static final int NetNotUseable = 4;
	
	/** sd卡不可用 */
	public static final int SDCardNotUseable = 5;
	
	/** 本地文件路径无效 */
	public static final int InvalidPath = 6;
	
	/** 播放内核错误 */
	public static final int PlayerCoreBase = 100;
	
	/** 下载任务错误 */
	public static final int TaskBase = 200;
	public static final int ZeroHandle = 250;
	
	/** 未知错误 */
	public static final int Unknown = 1000;
}
