package com.baidu.hd.task;

import com.baidu.hd.module.P2PBlock;
import com.baidu.hd.module.Task;

/**
 * 不同任务类型的处理器接口
 * 
 * 两个间接派生类：
 * BigSiteTaskHandler
 * SmallSiteTaskHandler
 * 
 * 主动操作的方面：
 * 通知两个Adapter去执行某操作或获取某数据
 * 该对象被TaskManagerForService使用
 * 该对象使用两个Adapter
 * 
 * 被动接受的方面：
 * 接收下载服务的回调
 * 该对象被ExeAdapter使用
 * 
 * 参数都是容器内对象
 * 
 * 因为此类为内部类，不检查参数有效性，包括主动和被动的
 * 
 * Api调用失败不回调，发生的几率比较低，另外也不知道如何展示
 * 只有状态变成错误才回调OnError
 */
interface TaskHandler {
	
	/** 创建处理器 */
	void create(TaskManagerAccessor accessor);
	
	/** 销毁处理器 */
	void destroy();
	
	/** 请空任务句柄 */
	void clearHandle(Task task);
	
	/** 前置开启任务 */
	void forceStart(Task task);
	
	/** 创建任务 */
	void create(Task task);
	
	/**
	 * 删除任务
	 * 当前允许任务状态：所有
	 */
	void remove(Task task);
	
	/**
	 * 开始任务
	 * 当前允许任务状态：开始，停止，错误
	 */
	void start(Task task);
	
	/**
	 * 停止任务
	 * 当前允许任务状态：开始，停止
	 */
	void stop(Task task);

	/**
	 * 查询任务
	 * 当前允许任务状态：所有
	 */
	void query(Task task);
	
	/** 任务排队  */
	void queue(Task task);
	
	/** 任务出错  */
	void error(Task task);
	
	/** 开始播放任务  */
	void startPlay(Task task);
	
	/** 结束播放任务 */
	void endPlay(Task task);
	
	/** 是否需要真正的开始任务 */
	boolean needRealStart(Task task);
	
	/** 任务完成且文件存在 */
	boolean isFileExist(Task task);
	
	/** 获得p2p block  */
	P2PBlock getBlock(Task task);
	
	/** 启动时的操作 */
	void startup(Task task);
	
	/** 结束时的操作 */
	void shutdown(Task task);
	
	/** 成功回调 */
	void onSuccess(int eventId, Task task);
	
	/** 失败回调 */
	void onError(int eventId, Task task, int errorCode);
	
	/**
	 * 把参数对象的一些属性赋值到容器内对象，在start,error时使用
	 * 目前只有BigSiteTaskHandler使用
	 * @param inTask 容器内对象
	 * @param paramTask 参数对象
	 */
	void setProperty(Task inTask, Task paramTask);
	
	/** 设置是否可以向外发送事件 */
	void setPostEventEnable(boolean value);
}
