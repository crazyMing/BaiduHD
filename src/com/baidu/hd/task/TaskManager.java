package com.baidu.hd.task;

import java.util.List;

import com.baidu.hd.module.P2PBlock;
import com.baidu.hd.module.Task;
import com.baidu.hd.service.ServiceProvider;

/**
 * 任务管理器对外接口
 */
public interface TaskManager extends ServiceProvider {

	/////////////////////////////////////////////////////////
	// 单个任务
	/**
	 * 删除任务
	 * 当前允许任务状态：所有
	 */
	void remove(Task value);
	
	/**
	 * 开始任务
	 * 当前允许任务状态：开始，停止，错误
	 * 如果不存在则创建
	 */
	void start(Task value);
	
	/**
	 * 停止任务
	 * 当前允许任务状态：开始，停止
	 */
	void stop(Task value);
	
	/** 任务出错 */
	void error(Task value);
	
	/** 查找任务 */
	Task find(String value);
	

	/////////////////////////////////////////////////////////
	// 批量任务
	/** 查询多个 */
	List<Task> multiQuery(List<String> keys);
	
	/** 移除多个 */
	void multiRemove(List<String> keys);
	
	/**
	 * 开始所有可见的任务
	 * 可见即为在UI上可以展示的
	 */
	void startAllVisible();
	
	/** 停止所有可见的任务 */
	void stopAllVisible();

	/** 获得所有可见任务 */
	List<Task> getAllVisible();
	
	/** 获得所有任务 */
	List<Task> getAll();
	
	/////////////////////////////////////////////////////////
	// 其他
	/** 任务完成且文件存在 */
	boolean isFileExist(Task value);
	
	/** 设置任务视频时长 */
	void setMediaTime(Task task, int value);

	/** 获得任务块 */
	P2PBlock getBlock(Task value);
	
	/** 清楚垃圾 */
	public interface clearGarbageEvent
	{
		void clearSize(long size);
		boolean isCancel();
	}
	
	void clearGarbage(clearGarbageEvent event);
}
