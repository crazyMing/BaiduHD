package com.baidu.hd.upgrade;

import com.baidu.hd.service.ServiceProvider;

public interface Upgrade extends ServiceProvider {

	/**
	 * 手动检测App
	 */
	void manualAppCheck(boolean isForce, boolean isSilence);
	
	/**
	 * 取消检测App
	 */
	void cancelAppCheck();
	
	/**
	 * 获得App更新状态
	 */
	int upgradeAppStatus();	
	
	/**
	 * 手动检测播放内核
	 */
	void manualPlayerCoreCheck(boolean isForce, boolean isSilence);
	
	/**
	 * 取消检测播放内核
	 */
	void cancelPlayerCoreCheck();	
	
	/**
	 * 获得播放内核更新状态
	 */
	int upgradePlayerCoreStatus();	
}
