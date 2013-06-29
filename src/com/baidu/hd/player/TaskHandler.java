package com.baidu.hd.player;

import java.util.ArrayList;

import android.util.Log;

import com.baidu.hd.conf.Configuration;
import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.BigSiteTask;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.TaskFactory;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId;
import com.baidu.hd.task.TaskEventArgs;
import com.baidu.hd.task.TaskManager;

class TaskHandler {
	
	interface Callback {
		void onComplete(String url, int errorCode);
		void onSetTitle(String title);
		Album getAlbum();
	}
	
	public enum State {
		None,
		BigSiteStream,		/** ��վhttpֱ�Ӳ���*/
		BigSitePart,		/** ��վ�ֿ���±߲� */
		BigSiteLocal,		/** ��վ���� */
		SmallSiteStream,	/** Сվ��ʽ���� */
		SmallSitePart,		/** Сվ�ֿ���±߲� */
		SmallSiteLocal,		/** Сվ���� */
	}

	private Logger logger = new Logger("TaskHandler");
	private ServiceFactory mServiceFactory = null;
	
	/**
	 * �ص�����
	 */
	private Callback mCallback = null;
	
	/**
	 * ��ǰ״̬
	 */
	private State mState = State.None;
	
	/**
	 * ��ǰ���ŵ���Ƶ
	 */
	private NetVideo mVideo = null;
	
	/**
	 * ��ǰ������������
	 */
	private Task mTask = null;
	
	/**
	 * �Ƿ�ѡ��������
	 */
	private boolean mDownload = false;

	private EventListener mEventListener = new EventListener() {
		
		@Override
		public void onEvent(EventId id, EventArgs args) {
			
			switch(id) {
			case eTaskPlay:
				{
					TaskEventArgs taskEventArgs = (TaskEventArgs)args;
					onTaskPlay(taskEventArgs.getTask());
				}
				break;
			case eTaskError:
				{
					TaskEventArgs taskEventArgs = (TaskEventArgs)args;
					onTaskError(taskEventArgs.getTask());
				}
				break;
			case eTaskStart:
				{
					TaskEventArgs taskEventArgs = (TaskEventArgs)args;
					onTaskCreate(taskEventArgs.getTask());
				}
				break;
			}
		}
	};
	
	TaskHandler(ServiceFactory factory, Callback callback) {
		mServiceFactory = factory;
		mCallback = callback;
	}
	
	public Task getTask() {
		return mTask;
	}
	
	public State getState() {
		return mState;
	}
	
	public void startTask() {
		if(mTask != null) {
			getManager().start(mTask);
		}
	}
	
	/** ��ʼ���� */
	public void request(NetVideo video) {
		
		mVideo = video;
		
		if(video.isBdhd()) {
			playSmallSiteVideo();
		} else {
			playBigSiteVideo();
		}
	}
	
	/** ֹͣ���� */
	public void destroy(ArrayList<Task> waitToDownload) {
		
		EventCenter eventCenter = (EventCenter)mServiceFactory.getServiceProvider(EventCenter.class);
		eventCenter.removeListener(mEventListener);

		switch(mState) {
		case SmallSiteStream:
			{
				getManager().remove(mTask);
				downloadTask();
			}
			break;
		case BigSiteStream:
			{
				downloadTask();
			}
			break;
		}
		for(Task task:waitToDownload){
			TaskManager taskManager = getManager();
			if (taskManager.find(task.getKey()) == null) {
				Stat stat = (Stat)mServiceFactory.getServiceProvider(Stat.class);
				stat.incLogCount(StatId.BaseDownloadCount + task.getVideoType());
				stat.incEventCount(StatId.Download.Name, StatId.Download.Count);
				stat.incEventCount(StatId.Download.Name, StatId.Download.Count
						+ task.getFormatVideoType());
			}
			taskManager.start(task);
		}
	}
	
	public boolean download() {
		logger.d("toDownload");
		if(mDownload) {
			return false;
		}
		
		String key = "";
		if(mVideo.isBdhd()) {
			key = mVideo.getUrl();
		} else {
			key = mVideo.getRefer();
		}
		Task task = getManager().find(key);
		if(task != null && task.isVisible()) {
			// �ж�visible��ȥ����ʽ����
			return false;
		}
		mDownload = true;
		return true;
	}
	
	private boolean alreadyDownLoading=false;//TODO by juqiang �ж��Ƿ�������ʱ���Ѿ�downloading
	public boolean isDownLoading(){
		return mDownload||alreadyDownLoading;
	}
	public void setVideoDuration(int value) {
		if(mTask != null) {
			getManager().setMediaTime(mTask, value);
		}
	}

	/** ���Ŵ�վ��Ƶ */
	private void playBigSiteVideo() {

		TaskManager taskManager = getManager();
		Task task = taskManager.find(mVideo.getRefer());
		if(task != null) {
			switch(task.toBig().getPlayType()) {
			
			case BigSiteTask.PlayType.Unknown:
				mState = State.BigSiteStream;
				logger.d("state:BigSiteStream " + mVideo.getRefer());
				break;
				
			case BigSiteTask.PlayType.P2P:
				if(taskManager.isFileExist(task)) {
					
					mState = State.BigSiteLocal;
					logger.d("state:BigSiteLocal " + task.getFileName());
					onPlay(String.format("p2p://%d|%s%s/%s", 
							task.getTotalSize(), getConf().getTaskSavePath(), task.getFolderName(), task.getFileName()));
				} else {
					
					mState = State.BigSitePart;
					registerEvent();
					mTask = task;
					logger.d("state:BigSitePart " + task.getRefer());
				}
				break;
				
			case BigSiteTask.PlayType.File:
				if(taskManager.isFileExist(task)) {
					
					mState = State.BigSiteLocal;
					logger.d("state:BigSiteLocal " + task.getFileName());
					onPlay(String.format("file://%s%s/%s", 
							getConf().getTaskSavePath(), task.getFolderName(), task.getFileName()));
				} else {
					
					mState = State.BigSiteStream;
					logger.d("state:BigSiteStream " + task.getRefer());
				}
				break;
			}
			
		} else {
			mState = State.BigSiteStream;
			logger.d("state:BigSiteStream");
		}
	}

	/**
	 * ����Сվ��Ƶ
	 */
	private void playSmallSiteVideo() {

		TaskManager taskManager = getManager();
		Task task = taskManager.find(mVideo.getUrl());
		if(task != null) {
			alreadyDownLoading=true;
			if(taskManager.isFileExist(task)) {
			
				mState = State.SmallSiteLocal;
				logger.d("state:SmallSiteLocal " + task.getFileName());
				onPlay(String.format("p2p://%d|%s%s/%s", 
						task.getTotalSize(), getConf().getTaskSavePath(), task.getFolderName(), task.getFileName()));
			} else {
				
				mState = State.SmallSitePart;
				registerEvent();
				mTask = task;
				logger.d("state:SmallSitePart " + mTask.getUrl());
			}
		} else {
			
			mState = State.SmallSiteStream;
			registerEvent();
			mTask = createTask();
			mTask.toSmall().setStreamMode(true);
			logger.d("state:SmallSiteStream " + mTask.getUrl());
		}
	}
	
	private Task createTask() {
		Task task = TaskFactory.create(mVideo.isBdhd() ? Task.Type.Small : Task.Type.Big);
		task.setName(mVideo.getName());
		task.setVideoType(mVideo.getType());
		task.setRefer(mVideo.getRefer());
		task.setAlbumId(mVideo.getAlbumId());
		task.setUrl(mVideo.getUrl());
		if(mCallback.getAlbum() != null) {
			task.setAlbumId(mCallback.getAlbum().getId());
		}
		return task;
	}

	/*
	 * �¼��������Ļص�
	 * ��������ʼ���¼�
	 */
	private void onTaskPlay(Task aTask) {
		if(aTask == null || mTask == null) {
			return;
		}
		if(!aTask.isSame(mTask)) {
			logger.d("this is not my task " + aTask.getUrl());
			return;
		}
		mTask = aTask;

		if(mTask.getHandle() != 0) {
			onPlay(String.format("p2p://%d", mTask.getHandle()));
		} else {
			if(mCallback != null) {
				mCallback.onComplete("", ErrorCode.ZeroHandle);
			}
		}
	}

	private void onTaskError(Task aTask) {
		if(aTask == null || mTask == null) {
			return;
		}
		if(!aTask.isSame(mTask)) {
			logger.d("this is not my task " + aTask.getUrl());
			return;
		}
		mTask = aTask;
		onError(aTask.getErrorCode());
	}
	
	private void onTaskCreate(Task aTask) {
		if(aTask == null || mTask == null) {
			return;
		}
		if(!aTask.isSame(mTask)) {
			return;
		}
		if(mCallback != null) {
			mCallback.onSetTitle(aTask.getName());
		}
	}

	/*
	 * ֪ͨ���±߿���Э��
	 * ����ʼ���¼���Ӧ�е���
	 */
	private void onPlay(String dataSource) {
		if(mCallback != null) {
			mCallback.onComplete(dataSource, ErrorCode.None);
		}
	}
	
	/*
	 * ֪ͨ�����Э��
	 * ����ʼ���¼���Ӧ�е���
	 */
	private void onError(int errorCode) {
		if(mCallback != null) {
			mCallback.onComplete("", ErrorCode.TaskBase + errorCode);
		}
	}
	
	private void downloadTask() {
		if(mDownload) {
			TaskManager taskManager = getManager();
			Task task = createTask();
			if(taskManager.find(task.getKey()) == null) {
				Stat stat = (Stat)mServiceFactory.getServiceProvider(Stat.class);
				stat.incLogCount(StatId.BaseDownloadCount + task.getVideoType());
				stat.incEventCount(StatId.Download.Name, StatId.Download.Count);
				stat.incEventCount(StatId.Download.Name, StatId.Download.Count + task.getFormatVideoType());
				stat.incEventAppValid();
			}
			taskManager.start(task);
		}
	}
	
	private void registerEvent() {
		EventCenter eventCenter = (EventCenter)mServiceFactory.getServiceProvider(EventCenter.class);
		eventCenter.addListener(EventId.eTaskPlay, mEventListener);
		eventCenter.addListener(EventId.eTaskError, mEventListener);
		eventCenter.addListener(EventId.eTaskStart, mEventListener);
	}
	
	private TaskManager getManager() {
		return (TaskManager)mServiceFactory.getServiceProvider(TaskManager.class);
	}
	
	private Configuration getConf() {
		return (Configuration)mServiceFactory.getServiceProvider(Configuration.class);
	}
	
	public void setDownload(boolean mDownload){
		this.mDownload=mDownload;
	}
}
