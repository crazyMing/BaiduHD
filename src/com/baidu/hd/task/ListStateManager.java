package com.baidu.hd.task;

import com.baidu.hd.detect.Detect;
import com.baidu.hd.detect.NetUsable;
import com.baidu.hd.detect.NetUsableChangedEventArgs;
import com.baidu.hd.detect.SDCardStateChangedEventArgs;
import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.Task;
import com.baidu.hd.player.PlayerEventArgs;

/**
 * 任务列表状态管理器
 * 通过监听事件来切换状态
 */
class ListStateManager implements EventListener, ListStateChanger {
	
	private Logger logger = new Logger("ListStateManager");
	
	private TaskManagerAccessor mAccessor = null;
	
	private ListRegularState mRegularState = new ListRegularState();
	private ListPlayState mPlayState = new ListPlayState();
	private ListPauseState mPauseState = new ListPauseState();

	/** 当前状态 */
	private ListState mCurrentState = null;
	
	@Override
	public void onEvent(EventId id, EventArgs args) {
		
		switch(id) {
		case eStartPlay:
			{
				PlayerEventArgs playerArgs = (PlayerEventArgs)args;
				mPlayState.enter(playerArgs.getTask());
			}
			break;
		
		case eStopPlay:
			{
				PlayerEventArgs playerArgs = (PlayerEventArgs)args;
				mPlayState.leave(playerArgs.getTask());
			}
			break;
			
		case eNetStateChanged:
			{
				if(mCurrentState == mPlayState) {
					return;
				}
				NetUsableChangedEventArgs netArgs = (NetUsableChangedEventArgs)args;
				if(netArgs.getNew() == NetUsable.ePrompt) {
					// 有开始的任务进入停止状态
					if(haveStartTask()) {
						mPauseState.enter();
					}
				}
				if(netArgs.getOld() == NetUsable.ePrompt) {
					// 当前是暂停状态才会去恢复
					if(mCurrentState == mPauseState) {
						mPauseState.leave();
					}
				}
			}
			break;
		
		case eSDCardStateChanged:
			{
				SDCardStateChangedEventArgs sdArgs = (SDCardStateChangedEventArgs)args;
				if(sdArgs.isCanUse()) {
					return;
				}
				if(haveStartTask()) {
					Detect detect = (Detect)mAccessor.getServiceFactory().getServiceProvider(Detect.class);
					detect.SDCardPrompt();
				}
			}
			break;
		}
	}

	public void create(TaskManagerAccessor accessor) {
		mAccessor = accessor;
		
		mRegularState.create(accessor, this);
		mPlayState.create(accessor, this);
		mPauseState.create(accessor, this);
		
		mCurrentState = mRegularState;
		
		EventCenter eventCenter = getEventCenter();
		eventCenter.addListener(EventId.eNetStateChanged, this);
		eventCenter.addListener(EventId.eStartPlay, this);
		eventCenter.addListener(EventId.eStopPlay, this);
		eventCenter.addListener(EventId.eSDCardStateChanged, this);
	}
	
	public void destory() {
		getEventCenter().removeListener(this);
	}
	
	public ListState getCurrentState() {
		return mCurrentState;
	}
	
	public void restart() {
		logger.d("recv restart msg");
		for(Task t : mAccessor.getAllTask()) {
			mAccessor.getTaskHandler(t.getType()).clearHandle(t);
		}
		if(mCurrentState == mRegularState) {
			logger.d("current is regular state in restart");
			for(Task t: mAccessor.getAllTask()) {
				if(t.getState() == Task.State.Start) {
					logger.d("start task in restart");
					mAccessor.getTaskHandler(t.getType()).forceStart(t);
				}
			}
		} else if(mCurrentState == mPlayState) {
			logger.d("current is playing state in restart");
			Task playingTask = mPlayState.getPlayingTask();
			if(playingTask != null) {
				mAccessor.getTaskHandler(playingTask.getType()).stop(playingTask);
				mAccessor.postEvent(CallbackEventId.OnError, playingTask);
			}
		}
	}
	
	@Override
	public void change(Type type) {
		logger.d("change state to " + type);
		switch(type) {
		case eRegular:
			mCurrentState = mRegularState;
			mPlayState.reset();
			mPauseState.reset();
			break;
		case ePlay:
			mCurrentState = mPlayState;
			mRegularState.reset();
			mPauseState.reset();
			break;
		case ePause:
			mCurrentState = mPauseState;
			mRegularState.reset();
			mPlayState.reset();
			break;
		}
	}
	
	private boolean haveStartTask() {
		for(Task t: mAccessor.getAllTask()) {
			if(t.getState() == Task.State.Start || t.getState() == Task.State.Queue) {
				return true;
			}
		}
		return false;
	}
	
	private EventCenter getEventCenter() {
		return (EventCenter)mAccessor.getServiceFactory().getServiceProvider(EventCenter.class);
	}
}
