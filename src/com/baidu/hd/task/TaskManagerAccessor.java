package com.baidu.hd.task;

import java.util.List;

import com.baidu.hd.module.Task;
import com.baidu.hd.service.ServiceFactory;

/**
 * 任务管理器访问器
 * 获得任务相关的数据，主要供taskhandler使用
 */
interface TaskManagerAccessor {

	/**
	 * 服务工厂
	 */
	ServiceFactory getServiceFactory();
	
	/**
	 * 获得任务处理器，以回调继续处理任务
	 */
	TaskHandler getTaskHandler(int type);

	/**
	 * 获得所有任务
	 */
	List<Task> getAllTask();
	
	/**
	 * 获得任务处理器，用以操作任务
	 */
	ExecAdapter getExeAdpater();
	
	/**
	 * 获得查询处理器
	 */
	QueryAdapter getQueryAdapter();

	/**
	 * 向外通知
	 */
	void postEvent(int eventId, Task task);
}
