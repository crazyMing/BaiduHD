package com.baidu.hd.task;

import com.baidu.hd.conf.Configuration;
import com.baidu.hd.module.Task;

/**
 * 任务列表状态的基类
 */
abstract class ListBaseState implements ListState {

	/** 任务最大下载数 */
	private int mMaxDownloadCount = 1;
	
	protected TaskManagerAccessor mAccessor = null;
	protected ListStateChanger mChanger = null;

	public void create(TaskManagerAccessor accessor, ListStateChanger changer) {
		mAccessor = accessor;
		mChanger = changer;
		Configuration conf = (Configuration)
				mAccessor.getServiceFactory().getServiceProvider(Configuration.class);
		mMaxDownloadCount = conf.getTaskMaxDownload();
	}
	
	protected void startTask(Task aTask) {
		
		TaskHandler taskHandler = mAccessor.getTaskHandler(aTask.getType()); 
		if(!taskHandler.needRealStart(aTask)) {
			taskHandler.start(aTask);
		} else {
			if(overMaxDownloadCount()) {
				mAccessor.getTaskHandler(aTask.getType()).queue(aTask);
			} else {
				mAccessor.getTaskHandler(aTask.getType()).start(aTask);
			}
		}
	}
	
	protected void stopTask(Task aTask) {
		mAccessor.getTaskHandler(aTask.getType()).stop(aTask);
	}
	
	protected void removeTask(Task aTask) {
		mAccessor.getTaskHandler(aTask.getType()).remove(aTask);
	}
	
	protected void queueTak(Task aTask) {
		mAccessor.getTaskHandler(aTask.getType()).queue(aTask);
	}
	
	protected void errorTask(Task aTask) {
		mAccessor.getTaskHandler(aTask.getType()).error(aTask);
	}
	
	protected boolean overMaxDownloadCount() {
		int startCount = 0;
		for(Task t: mAccessor.getAllTask()) {
			if(t.getState() == Task.State.Start) {
				++startCount;
			}
		}
		return startCount >= mMaxDownloadCount;
	}

}
