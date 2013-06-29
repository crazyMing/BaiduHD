package com.baidu.hd.task;

import com.baidu.hd.module.Task;

/**
 * �����б���״̬
 * �����˵�Ĭ��״̬
 * ����pause��play״̬����Ϊ����״̬
 */
class ListRegularState extends ListBaseState {
	
	private BatchOperate mBatchOperate = BatchOperate.eNone;

	@Override
	public void reset() {		
	}

	@Override
	public void start(Task value) {
		startTask(value);
	}

	@Override
	public void stop(Task value) {
		stopTask(value);
	}

	@Override
	public void remove(Task value) {
		removeTask(value);
	}

	@Override
	public void error(Task value) {
		errorTask(value);
	}

	@Override
	public void onPrePostEvent(int eventId, Task value) {
		if(mBatchOperate == BatchOperate.eStop
				|| mBatchOperate == BatchOperate.eRemove) {
			return;
		}
		switch(eventId) {
		case CallbackEventId.OnStop:
		case CallbackEventId.OnRemove:
		case CallbackEventId.OnComplete:
		case CallbackEventId.OnError:
			if(!overMaxDownloadCount()) {
				startOneQueueTask();
			}
			break;
		}
	}

	@Override
	public void setBatching(BatchOperate op) {
		mBatchOperate = op;
		if(mBatchOperate == BatchOperate.eNone) {
			if(!overMaxDownloadCount()) {
				startOneQueueTask();
			}
		}
	}

	private void startOneQueueTask() {
		for(Task t: mAccessor.getAllTask()) {
			if(t.getState() == Task.State.Queue) {
				startTask(t);
				break;
			}
		}
	}
	
}
