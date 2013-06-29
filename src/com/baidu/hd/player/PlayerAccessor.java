package com.baidu.hd.player;

import android.app.Activity;
import android.view.View;

import com.baidu.hd.module.Task;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.Video;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.util.Turple;

/**
 * 播放器访问器
 */
public interface PlayerAccessor {

	/** 获得屏幕尺寸 */
	Turple<Integer, Integer> getScreenSize();
	
	/** 获得视频尺寸 */
	Turple<Integer, Integer> getVideoSize();
	
	/**
	 * 播放指定的视频
	 * 使用同一个Album
	 * @param video
	 */
	void playMedia(Video video);
	
	/** 获得当前专辑 */
	Album getAlbum();
	
	/** 获得当前Video */
	Video getVideo();
	
	/** 获得当前任务 */
	Task getTask();
	
	/** 获得作为父窗体的Activity */
	Activity getHost();
	
	/** 获得控件的容器基准 */
	View getControlHolder();
	
	/** 获得服务工厂 */
	ServiceFactory getServiceFactory();
	
	/** 开始隐藏所有控件的计时器 */
	void startHideControl();
	
	/** 停止隐藏所有控件的计时器 */
	void stopHideControl();

	void stopHideRightBar();

	void startHideRightBar();
}
