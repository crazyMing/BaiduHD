package com.baidu.hd.task;

import com.baidu.hd.module.Task;

/**
 * 任务列表状态：常规，暂停，播放
 * 网络状态不可用导致暂停全部
 * 播放状态导致暂停其他
 * 
 * 参数都是容器内对象
 */
interface ListState {
	
	public enum BatchOperate {
		eNone,
		eStart,
		eStop,
		eRemove,
	}
	
	void create(TaskManagerAccessor accessor, ListStateChanger changer);
	void reset();
	
	//////////////////////////////////////////
	// 常规操作 
	void start(Task value);
	void stop(Task value);
	void remove(Task value);
	void error(Task value);
	void onPrePostEvent(int eventId, Task value);
	void setBatching(BatchOperate op);
}
