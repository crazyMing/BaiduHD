package com.baidu.hd.upgrade;

import com.baidu.hd.service.ServiceProvider;

public interface Upgrade extends ServiceProvider {

	/**
	 * �ֶ����App
	 */
	void manualAppCheck(boolean isForce, boolean isSilence);
	
	/**
	 * ȡ�����App
	 */
	void cancelAppCheck();
	
	/**
	 * ���App����״̬
	 */
	int upgradeAppStatus();	
	
	/**
	 * �ֶ���ⲥ���ں�
	 */
	void manualPlayerCoreCheck(boolean isForce, boolean isSilence);
	
	/**
	 * ȡ����ⲥ���ں�
	 */
	void cancelPlayerCoreCheck();	
	
	/**
	 * ��ò����ں˸���״̬
	 */
	int upgradePlayerCoreStatus();	
}
