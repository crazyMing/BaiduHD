package com.baidu.hd.util;


import android.os.Environment;

public class Const {
	
	public static final boolean isDebug = false;
	
	// 据总时长大于n秒才会记录最后播放时间
	public static final int PlayerListRecordStopPos = 5;
	
	// 0速度最大时间
	public static final int ZeroMaxTime = 3;
	
	// m3u8文件有效块的最小时长
	public static final int M3U8ValidBlockDuration = 5;

	// extra key
	public static class IntentExtraKey {

		public static final String AutoHideFloatSearchBox = "isautohide";

		// 特性页
		public static final String SpecName = "SpecName";
		public static final String SpecUrl = "SpecUrl";

		// 语音搜索Activity ID
		public static final String VoiceSearchActivityID = "ActivityID";
		public static final String HomeActivityID = "HomeActivity";
		public static final String SearchResultActivityID = "SearchResultActivity";
		
		// 搜索结果页
		public static final String SearchResultKeyword = "keyword";
		public static final String SearchResultSource = "source";
		public static final String SearchResultVoiceList = "voicelist";

		// 播放页数据
		public static final String VideoAlbum = "album";
		public static final String VideoVideo = "video";
		
		public static final String PersonalActivity = "PersonalActivity";
		public static final String SettingsActivity = "SettingsActivity";

		// 第三方页Url
		public static final String ThirdPartAlbum = "ThirdPartAlbum";
		public static final String ThirdPartVideo = "ThirdPartVideo";
		
		// 下载页
		public static final String DownloadPageName = "DownloadPageName";
		public static final String DownloadPageUrl = "DownloadPageUrl";
		public static final String DownloadPageType = "DownloadPageType";
		
		// 浏览器模式
		public static final String BrowSnifferResult = "BrowSnifferResult";
		public static final String BrowSnifferNumber = "BrowSnifferNumber";
		
		// 浏览器模式特型页
		public static final String BrowSpecIsFromBdFrameView = "BrowSpecIsFromBdFrameView";
		public static final String BrowSpecTitle = "BrowSpecTitle";
		public static final String BrowSpecRefer = "BrowSpecRefer";
		public static final String BrowSpecName ="BrowSpecName";
		public static final String BrowSpecIndex = "BrowSpecIndex";
		public static final String BrowSpecAlbumId = "BrowSpecAlbumId";
		
		public static final String BrowSpecSmallSiteUrl="BrowSpecSmallSiteUrl";
		
		// 浏览器模式缓存选择页
		public static final String BrowSpecSelect = "BrowSpecSelect";
		  

	}

	public static class Elapse {

		// 任务列表刷新
		public static final int TaskRefresh = 2000;

		// 播放器界面刷新
		public static final int PlayerRefresh = 200; // 毫秒

		// 程序启动后检测
		public static final int PartnerApp = 10000;
		public static final int AppUpgrade = 2000;
		public static final int PlayerCore = 2000;
		
		public static final int DownloadServiceCheck = 2000;
		
		/** 统计模块网络可用性检测 */
		public static final int StatNetAvailabeCheck = 5000;
	}
	
	public static class Timeout {
		// 5min
		public static final int PlayerEventLoop = 300000;
		public static final int PlayerComplete = 300000;
		
//		public static final int SnifferComplete = 20000;
		public static final int SnifferComplete = 5000;
		public static final int SnifferWaitNet = 5000;
		public static final int SnifferWaitAlbum = 1000;
	}

	public static class Path {
		public static final String AppUpgradeFilePath = Environment	.getExternalStorageDirectory() + "/baidu/baiduplayer/upgrade/app/";
		public static final String AppUpgradeFileName = AppUpgradeFilePath	+ "bdvideo-" + StringUtil.createUUID() + ".apk";
		public static final String PlayerCoreUpgradeFilePath = Environment.getExternalStorageDirectory() + "/baidu/baiduplayer/upgrade/core/";
		public static final String NetVideoBufferFilePath = Environment.getExternalStorageDirectory() + "/baidu/baiduplayer/file/";
	}

	public static final String PlayerCoreTmpFileNameSubfix = "tmp";
	
	/*
	 * 亮度，声音常量
	 * */
	
	public static class BrightVolume {
		public static final int GestureVoiceMax = 150;
		public static final int GestureVoiceRatio = 10;
		public static final int BrightMax = 225;
	}
	
	/**
	 * 个人面板相关常量
	 */
	public static class PersonalConst {
		public static final int SilderDuration = 200;
	}
	
	/**
	 * SharedPreferences
	 */
	public static class SharedPreferences {
		/** 缓存路径 */
		public static final String NAME_NetVideoBufferPath = "NAME_NetVideoBufferPath";
		public static final String KEY_NetVideoBufferPath = "KEY_NetVideoBufferPath";
	}
}
