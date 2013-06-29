package com.baidu.hd.task;

import java.io.File;

import com.baidu.hd.conf.Configuration;
import com.baidu.hd.db.DBWriter;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.P2PBlock;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.album.NetVideo.NetVideoType;
import com.baidu.hd.util.FileUtil;
import com.baidu.hd.util.StringUtil;

/**
 * ������������
 */
abstract class BaseTaskHandler implements TaskHandler {

	private Logger logger = new Logger("TaskHandler");

	private boolean mPostEventEnable = false;

	/**
	 * ���������������
	 */
	protected TaskManagerAccessor mAccessor = null;
	
	public void create(TaskManagerAccessor accessor) {
		mAccessor = accessor;
	}

	@Override
	public void create(Task aTask) {
		if(needSaveDatabase(aTask)) {
			createFolder(aTask);
			getDBWriter().addTask(aTask);
		}
		mAccessor.postEvent(CallbackEventId.OnCreate, aTask);
	}

	@Override
	public void remove(Task aTask) {
		if (needSaveDatabase(aTask)) {
			getDBWriter().removeTask(aTask);
		}
		logger.d("remove folder " + aTask.getFolderName());
		FileUtil.removePathAsync(getSavePath() + aTask.getFolderName());
		mAccessor.postEvent(CallbackEventId.OnRemove, aTask);
	}

	@Override
	public void start(Task task) {
	}

	@Override
	public void stop(Task task) {
	}

	@Override
	public void query(Task task) {
	}

	@Override
	public void queue(Task task) {
		setQueue(task);
		mAccessor.postEvent(CallbackEventId.OnQueue, task);
	}

	@Override
	public void error(Task task) {
		setError(task);
		mAccessor.postEvent(CallbackEventId.OnError, task);
	}

	@Override
	public boolean needRealStart(Task task) {
		switch(task.getState()) {
		case Task.State.Start:
			return false;
		case Task.State.Queue:
		case Task.State.Stop:
		case Task.State.Error:
			return true;
		case Task.State.Complete:
			return !isFileExist(task);
		}
		return false;
	}

	@Override
	public boolean isFileExist(Task task) {
		return false;
	}

	@Override
	public P2PBlock getBlock(Task task) {
		return null;
	}

	@Override
	public void startup(Task task) {
	}

	@Override
	public void shutdown(Task task) {
	}

	@Override
	public void setProperty(Task inTask, Task paramTask) {

		boolean dirty = false;
		if (!"".equals(paramTask.getUrl()) && "".equals(inTask.getUrl())) {
			inTask.setUrl(paramTask.getUrl());
			dirty = true;
		}
		if (!"".equals(paramTask.getName()) && "".equals(inTask.getName())) {
			inTask.setName(paramTask.getName());
			dirty = true;
		}
		if (paramTask.getVideoType() != NetVideoType.NONE
				&& inTask.getVideoType() == NetVideoType.NONE) {
			inTask.setVideoType(paramTask.getVideoType());
			dirty = true;
		}
		if (paramTask.getErrorCode() != Task.ErrorCode.None) {
			inTask.setErrorCode(paramTask.getErrorCode());
			dirty = true;
		}

		if (dirty) {
			getDBWriter().updateTask(paramTask);
		}
	}

	@Override
	public void setPostEventEnable(boolean value) {
		mPostEventEnable = value;
	}

	// ///////////////////////////////////////////////////////////////////
	// abstract
	/** �Ƿ�Ҫ���������ݿ� */
	protected boolean needSaveDatabase(Task aTask) {
		return true;
	}

	/** �Ƿ�Ҫ���ⷢ���¼� */
	protected boolean needPostEvent(Task aTask) {
		return true;
	}
	
	protected boolean needRemoveOnComplete(Task aTask) {
		return true;
	}

	// ///////////////////////////////////////////////////////////////////
	// helper
	protected DBWriter getDBWriter() {
		return (DBWriter) mAccessor.getServiceFactory()
				.getServiceProvider(DBWriter.class);
	}

	protected Task findTask(Task value) {
		if(value == null) {
			return null;
		}
		for (Task t : mAccessor.getAllTask()) {
			if (t.getKey().equalsIgnoreCase(value.getKey())) {
				return t;
			}
		}
		return null;
	}

	protected void setStart(Task aTask) {

		aTask.setState(Task.State.Start);
		if (needSaveDatabase(aTask)) {
			getDBWriter().updateTask(aTask);
		}
		if (needPostEvent(aTask) && mPostEventEnable) {
			mAccessor.postEvent(CallbackEventId.OnStart, aTask);
		}
	}

	protected void setQueue(Task aTask) {
		aTask.setState(Task.State.Queue);
		aTask.setSpeed(0);
		if (needSaveDatabase(aTask)) {
			getDBWriter().updateTask(aTask);
		}
		if (needPostEvent(aTask) && mPostEventEnable) {
			mAccessor.postEvent(CallbackEventId.OnQueue, aTask);
		}
	}

	protected void setError(Task aTask) {
		aTask.setState(Task.State.Error);
		aTask.setSpeed(0);
		if (needSaveDatabase(aTask)) {
			getDBWriter().updateTask(aTask);
		}
		if (needPostEvent(aTask) && mPostEventEnable) {
			mAccessor.postEvent(CallbackEventId.OnError, aTask);
		}
	}

	protected void setError(Task aTask, int errorCode) {
		aTask.setErrorCode(errorCode);
		setError(aTask);
	}

	protected void setStop(Task aTask) {
		aTask.setState(Task.State.Stop);
		aTask.setSpeed(0);
		if (needSaveDatabase(aTask)) {
			getDBWriter().updateTask(aTask);
		}
		if (needPostEvent(aTask) && mPostEventEnable) {
			mAccessor.postEvent(CallbackEventId.OnStop, aTask);
		}
	}

	protected void setComplete(Task aTask) {
		aTask.setDownloadSize(aTask.getTotalSize());
		aTask.setPercent(100);
		aTask.setState(Task.State.Complete);
		aTask.setSpeed(0);
		if(needRemoveOnComplete(aTask)) {
			// ������ɾ�ɾ��
			mAccessor.getExeAdpater().remove(aTask);
		}
		if (needSaveDatabase(aTask)) {
			getDBWriter().updateTask(aTask);
		}
		if (needPostEvent(aTask) && mPostEventEnable) {
			mAccessor.postEvent(CallbackEventId.OnComplete, aTask);
		}
	}

	protected String getSavePath() {
		Configuration conf = (Configuration)mAccessor.getServiceFactory().getServiceProvider(Configuration.class);
		return conf.getTaskSavePath();
	}
	
	protected void debugLog(String msg, Task aTask) {
		if (aTask.getType() == Task.Type.Small) {
			logger.d(msg + " " + aTask.getUrl() + " stream:"
					+ aTask.toSmall().isStreamMode());
		} else {
			logger.d(msg + " " + aTask.getUrl());
		}
	}
	
	private void createFolder(Task task) {
		if(StringUtil.isEmpty(task.getFolderName())) {
			task.setFolderName(FileUtil.filterName(task.getName() + "_" + StringUtil.createUUID()));
		}
		File path = new File(getSavePath() + task.getFolderName());
		if(!path.exists()) {
			path.mkdirs();
		}
	}
}
