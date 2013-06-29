package com.baidu.hd.task;

import com.baidu.hd.detect.Detect;
import com.baidu.hd.detect.NetUsable;
import com.baidu.hd.module.Task;

/**
 * 任务列表播放状态
 * 播放视频时会触发到此状态
 */
class ListPlayState extends ListBaseState {

	private Task mTask = null;
	
	private ListStateCache mCache = new ListStateCache();

	@Override
	public void reset() {
		mCache.clear();
	}

	@Override
	public void start(Task value) {
		startTask(value);
	}

	@Override
	public void stop(Task value) {
		mCache.remove(value);
		stopTask(value);
	}

	@Override
	public void remove(Task value) {
		mCache.remove(value);
		removeTask(value);
	}

	@Override
	public void error(Task value) {
		mCache.remove(value);
		errorTask(value);
	}

	@Override
	public void onPrePostEvent(int eventId, Task value) {
	}

	@Override
	public void setBatching(BatchOperate op) {
	}

	public void enter(Task value) {
		mChanger.change(ListStateChanger.Type.ePlay);
		pauseAll(value);
		if(value != null) {
			mTask = value;
			startTask(value);
			mAccessor.getTaskHandler(value.getType()).startPlay(value);
		}
	}
	
	public void leave(Task value) {
		if(value != null) {
			mAccessor.getTaskHandler(value.getType()).endPlay(value);
			mTask = null;
		}
		Detect detect = (Detect)mAccessor.getServiceFactory().getServiceProvider(Detect.class);
		if(detect.getNetUsable() == NetUsable.ePrompt) {
			// 为了停止此播放的下载任务，包括新创建的下载任务和本就存在的任务
			pauseAll(null);
		} else {
			resumeAll();
		}
		mChanger.change(ListStateChanger.Type.eRegular);
	}
	
	public Task getPlayingTask() {
		return mTask;
	}
	
	private void pauseAll(Task except) {
		for(Task t: mAccessor.getAllTask()) {
			if(mCache.contain(t)) {
				continue;
			}
			if(except != null && t.isSame(except)) {
				continue;
			}
			if(t.getState() == Task.State.Start || t.getState() == Task.State.Queue) {
				mCache.add(t);
				stopTask(t);
			}
		}
	}
	
	private void resumeAll() {
		for(ListStateCache.Package pack: mCache.getAll()) {
			if(pack.getState() == Task.State.Start) {
				startTask(pack.getTask());
			}
		}
		for(ListStateCache.Package pack: mCache.getAll()) {
			if(pack.getState() == Task.State.Queue) {
				startTask(pack.getTask());
			}
		}
		mCache.clear();
	}
}
