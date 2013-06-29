package com.baidu.hd.task;

import com.baidu.hd.detect.Detect;
import com.baidu.hd.detect.FilterCallback;
import com.baidu.hd.module.Task;

/**
 * 任务列表暂停状态
 * 网络状态变化可能会触发到此状态
 * 只有希望恢复任务的时候才会进入这个状态
 */
class ListPauseState extends ListBaseState {

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

	public void enter() {
		
		mChanger.change(ListStateChanger.Type.ePause);
		pauseAll();
		
		Detect detect = (Detect)mAccessor.getServiceFactory().getServiceProvider(Detect.class);
		detect.filterByNet(new FilterCallback() {
			
			@Override
			public void onDetectPromptReturn(DetectPromptReturn canUse) {
				switch(canUse) {
				case eTrue:
				case eNoNetAvailable:
					resumeAll();
					mChanger.change(ListStateChanger.Type.eRegular);
					break;
				default:
					break;
				}
			}
		});
	}
	
	public void leave() {
		resumeAll();
		mChanger.change(ListStateChanger.Type.eRegular);
	}
	
	private void pauseAll() {
		for(Task t: mAccessor.getAllTask()) {
			if(mCache.contain(t)) {
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
