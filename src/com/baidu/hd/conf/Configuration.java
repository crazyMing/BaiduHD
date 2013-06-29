package com.baidu.hd.conf;

import com.baidu.hd.service.ServiceProvider;

public interface Configuration extends ServiceProvider {

	/**
	 * 获得图片的保存路径
	 */
	String getImagePath();
 
	/**
	 * 获得反馈Url
	 */
	String getFeedbackUrl();
	
	/**
	 * 获得上传日志的Url
	 * 使用String.format
	 */
	String getUploadLogUrl();
	
	// 任务
	/**
	 * 下载任务保存地址
	 */
	String getTaskSavePath();
	
	/**
	 * 获得任务最大下载数
	 */
	int getTaskMaxDownload();

	/**
	 * 获得缓存任务文件的后缀名
	 * @return
	 */
	String getTaskFileNameExt();
	
	// 推荐
	/**
	 * 地址栏suggest服务器地址
	 */
	String getAdressleisteSuggestServer();
	
	// 升级
	/**
	 * 获得升级用url
	 * 使用String.format: deviceId,version,channel
	 */
	String getAppUpgradeUrl();

	/**
	 * 获得播放内核升级用url
	 */
	String getPlayerCoreUpgradeUrl();


	/** 统计 */
	String getStartActivateUrl();

	// 嗅探
	/**
	 * 获得大站嗅探用url
	 * 使用String.format url
	 */
	String getBigSiteSnifferUrl();
	
	/**
	 * 获得大站AlbumInfo
	 */
	String getAlbumInfoUrl();
	
	/**
	 * 获得大站PlayInfo
	 */
	String getPlayInfoUrl();
	
	/**
	 * 获得大站UpdateInfo
	 */
	String getUpdateInfoUrl();
	
	/**
	 * 获得小站嗅探用url
	 * 使用String.format version, emid, url
	 */
	String getSmallSiteSnifferUrl();
	
	// UA
	/** 获得嗅探UA */
	String getSnifferUA();
	
	/** 获得播放UA */
	String getPlayUA();
	
	/**
	 * 获得搜索地址
	 * */
	String getSearchUrl();
}
