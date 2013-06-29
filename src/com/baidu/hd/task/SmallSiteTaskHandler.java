package com.baidu.hd.task;

import com.baidu.hd.log.Logger;
import com.baidu.hd.module.P2PBlock;
import com.baidu.hd.module.SmallSiteTask;
import com.baidu.hd.module.Task;
import com.baidu.hd.util.StringUtil;

/**
 * Сվ��������
 */
class SmallSiteTaskHandler extends BaseTaskHandler {
	
	private Logger logger = new Logger("TaskHandler");

	@Override
	public void create(TaskManagerAccessor accessor) {
		super.create(accessor);
	}

	@Override
	public void destroy() {
	}

	@Override
	public void clearHandle(Task task) {
		task.setHandle(0);
	}

	@Override
	public void forceStart(Task task) {
		mAccessor.getExeAdpater().start(task);
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// operate
	@Override
	public void create(Task aTask) {

		if(StringUtil.isEmpty(aTask.getFileName()) || aTask.getTotalSize() == 0) {
			mAccessor.getQueryAdapter().setDefaultInfo(aTask);
		}
		super.create(aTask);
	}

	@Override
	public void remove(Task aTask) {
		mAccessor.getExeAdpater().remove(aTask);
		super.remove(aTask);
	}

	/**
	 * ���������ⷢ�¼�����������״̬
	 */
	@Override
	public void start(Task task) {
		
		switch(task.getState()) {

		case Task.State.Start:
			debugLog("started, need not start", task);
			mAccessor.postEvent(CallbackEventId.OnStart, task);
			if(task.getHandle() != 0) {
				// ֮ǰ�ոնԵײ�start�����ǻ�û�ص������������¼����ȴ��ص�ʱ�ٷ��¼�
				mAccessor.postEvent(CallbackEventId.OnPlay, task);
			}
			break;

		case Task.State.Queue:
		case Task.State.Stop:
		case Task.State.Error:
			debugLog("need start", task);
			setStart(task);
			mAccessor.getExeAdpater().start(task);
			break;

		case Task.State.Complete:
			if(isFileExist(task)) {
				debugLog("complete, need not start", task);
				mAccessor.postEvent(CallbackEventId.OnStart, task);
			} else {
				
				debugLog("complete, but file not exist", task);
				setStart(task);
				mAccessor.getExeAdpater().remove(task);
				task.clearState();
				mAccessor.getExeAdpater().start(task);
			}
			break;
		
		default:
			break;
		}
	}

	@Override
	public void stop(Task task) {
		
		switch(task.getState()) {
		
		case Task.State.Start:
			debugLog("need stop", task);
			setStop(task);
			mAccessor.getExeAdpater().stop(task);
			break;

		case Task.State.Queue:
			debugLog("need only set stop", task);
			setStop(task);
			break;
			
		case Task.State.Stop:
		case Task.State.Error:
		case Task.State.Complete:
			debugLog("not started, need not stop", task);
			mAccessor.postEvent(CallbackEventId.OnStop, task);
			break;
		}
	}

	@Override
	public void query(Task task) {
		if(task.getState() == Task.State.Start) {
			mAccessor.getExeAdpater().query(task);
		}
	}

	@Override
	public void startPlay(Task task) {
		task.setPlaying(true);
		if(task.getHandle() != 0) {
			mAccessor.getQueryAdapter().setPlaying(task);
		}
	}

	@Override
	public void endPlay(Task task) {
		task.setPlaying(false);
		mAccessor.getQueryAdapter().setPlaying(task);
	}

	@Override
	public boolean isFileExist(Task task) {
		return mAccessor.getQueryAdapter().isFileExist(task.toSmall());
	}

	@Override
	public P2PBlock getBlock(Task task) {
		return mAccessor.getQueryAdapter().getBlock(task);
	}

	@Override
	public void startup(Task task) {
		if(task.getState() == Task.State.Start || task.getState() == Task.State.Queue) {
			debugLog("set stop", task);
			setStop(task);
		}
	}

	@Override
	public void shutdown(Task task) {
		if(task.getState() == Task.State.Start) {
			getDBWriter().updateTask(task);
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// callback
	@Override
	public void onSuccess(int eventId, Task oTask) {
		
		// �ڼ������Ҳ����������ˣ������κδ���
		if(eventId == CallbackEventId.OnRemove) {
			return;
		}
		SmallSiteTask iTask = findTaskByCallback(oTask);
		if(iTask == null) {
			logger.w("have not callback task in onSuccess " + (iTask == null ? "null task" : iTask.getUrl()));
			return;
		}
		switch(eventId) {
		
		case CallbackEventId.OnCreate:
			onCreate(iTask, oTask);
			break;

		case CallbackEventId.OnQuery:
			onQuery(iTask, oTask);
			break;
			
		case CallbackEventId.OnStart:
			onStart(iTask, oTask);
			break;
			
		case CallbackEventId.OnStop:
			// �����κ�����
			break;
		}
	}
	
	@Override
	public void onError(int eventId, Task task, int errorCode) {
		if(eventId == CallbackEventId.OnCreate) {
			SmallSiteTask inTask = findTaskByCallback(task);
			if(inTask == null) {
				logger.w("have not callback task in onError " + (task == null ? "null task" : task.getRefer()));
				return;
			}
			setError(inTask);
		}
	}
	
	private void onCreate(SmallSiteTask iTask, Task oTask) {
		debugLog("onCreate", oTask);
		iTask.setHandle(oTask.getHandle());
	}

	private void onQuery(SmallSiteTask iTask, Task oTask) {
		logger.v("onQuery " + oTask.getInfo());
		
		int oldState = iTask.getState();
		int newState = oTask.getState();
		
		// ��ֵ�������г�Ա
		iTask.copyFrom(oTask);
		if(iTask.getTotalSize() != 0) {
			iTask.setPercent((int)(100 * iTask.getDownloadSize() / iTask.getTotalSize()));
		}
		
		if(newState == Task.State.Stop || newState == Task.State.Start) {
			// Сվ����ע��ʼ�ͽ����ص�����Ϊ����������ʱ������״̬
			// ���ǡ�ø�������ײ㻹δ��Ч����ʱ��һ�ε�query������
			// newState����ԭ����״̬��oldState���������������õ�״̬��״̬��ͬ�ˣ��������û�ȥ��
			// ��վ����������������
			iTask.setState(oldState);
			newState = oldState;
		}
		
		if(oldState != newState) {
			logger.d("convert to new state " + oTask.getUrl() + " " + oTask.getFormatState());
			if(newState == Task.State.Error) {
				setError(iTask);
			}
			if(newState == Task.State.Complete) {
				setComplete(iTask);
			}
		}
	}

	private void onStart(SmallSiteTask iTask, Task oTask) {
		debugLog("onStart", oTask);
		if(iTask.isPlaying()) {
			mAccessor.getQueryAdapter().setPlaying(iTask);
			mAccessor.postEvent(CallbackEventId.OnPlay, iTask);
		}		
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// utility	
	@Override
	protected boolean needSaveDatabase(Task value) {
		return !value.toSmall().isStreamMode();
	}
	
	@Override
	protected boolean needRemoveOnComplete(Task aTask) {
		return false;
	}
	
	private SmallSiteTask findTaskByCallback(Task value) {
		if(value == null) {
			logger.e("findTaskByCallback null param");
			return null;
		}
		if(value.getType() != Task.Type.Small) {
			logger.e("findTaskByCallback param is not big " + value.getUrl());
			return null;
		}
		Task task = findTask(value);
		if(task == null) {
			logger.w("findTaskByCallback not found task " + value.getUrl());
			return null;
		}
		if(task.getType() != Task.Type.Small) {
			logger.e("findTaskByCallback task is not big " + value.getUrl());
			return null;
		}
		return task.toSmall();
	}
}
