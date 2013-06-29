package com.baidu.hd.player;

import android.app.Activity;
import android.view.View;

import com.baidu.hd.module.Task;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.Video;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.util.Turple;

/**
 * ������������
 */
public interface PlayerAccessor {

	/** �����Ļ�ߴ� */
	Turple<Integer, Integer> getScreenSize();
	
	/** �����Ƶ�ߴ� */
	Turple<Integer, Integer> getVideoSize();
	
	/**
	 * ����ָ������Ƶ
	 * ʹ��ͬһ��Album
	 * @param video
	 */
	void playMedia(Video video);
	
	/** ��õ�ǰר�� */
	Album getAlbum();
	
	/** ��õ�ǰVideo */
	Video getVideo();
	
	/** ��õ�ǰ���� */
	Task getTask();
	
	/** �����Ϊ�������Activity */
	Activity getHost();
	
	/** ��ÿؼ���������׼ */
	View getControlHolder();
	
	/** ��÷��񹤳� */
	ServiceFactory getServiceFactory();
	
	/** ��ʼ�������пؼ��ļ�ʱ�� */
	void startHideControl();
	
	/** ֹͣ�������пؼ��ļ�ʱ�� */
	void stopHideControl();

	void stopHideRightBar();

	void startHideRightBar();
}
