package com.baidu.hd.conf;

import com.baidu.hd.service.ServiceProvider;

public interface Configuration extends ServiceProvider {

	/**
	 * ���ͼƬ�ı���·��
	 */
	String getImagePath();
 
	/**
	 * ��÷���Url
	 */
	String getFeedbackUrl();
	
	/**
	 * ����ϴ���־��Url
	 * ʹ��String.format
	 */
	String getUploadLogUrl();
	
	// ����
	/**
	 * �������񱣴��ַ
	 */
	String getTaskSavePath();
	
	/**
	 * ����������������
	 */
	int getTaskMaxDownload();

	/**
	 * ��û��������ļ��ĺ�׺��
	 * @return
	 */
	String getTaskFileNameExt();
	
	// �Ƽ�
	/**
	 * ��ַ��suggest��������ַ
	 */
	String getAdressleisteSuggestServer();
	
	// ����
	/**
	 * ���������url
	 * ʹ��String.format: deviceId,version,channel
	 */
	String getAppUpgradeUrl();

	/**
	 * ��ò����ں�������url
	 */
	String getPlayerCoreUpgradeUrl();


	/** ͳ�� */
	String getStartActivateUrl();

	// ��̽
	/**
	 * ��ô�վ��̽��url
	 * ʹ��String.format url
	 */
	String getBigSiteSnifferUrl();
	
	/**
	 * ��ô�վAlbumInfo
	 */
	String getAlbumInfoUrl();
	
	/**
	 * ��ô�վPlayInfo
	 */
	String getPlayInfoUrl();
	
	/**
	 * ��ô�վUpdateInfo
	 */
	String getUpdateInfoUrl();
	
	/**
	 * ���Сվ��̽��url
	 * ʹ��String.format version, emid, url
	 */
	String getSmallSiteSnifferUrl();
	
	// UA
	/** �����̽UA */
	String getSnifferUA();
	
	/** ��ò���UA */
	String getPlayUA();
	
	/**
	 * ���������ַ
	 * */
	String getSearchUrl();
}
