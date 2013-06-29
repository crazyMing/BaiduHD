package com.baidu.hd.stat;

import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.Video;
import com.baidu.hd.player.PlayerEventArgs;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.task.TaskEventArgs;

class EventStatListener implements EventListener {
	
	private ServiceFactory mServiceFactory = null;
	private Stat mStat = null;
	
	private LogDataHolder mData = null;
	private PlayDuration mPlayDuration = new PlayDuration();

	@Override
	public void onEvent(EventId id, EventArgs args) {
		switch(id) {
		case ePlayFail:
			{
				PlayerEventArgs playerArgs = (PlayerEventArgs)args;
				playFail(playerArgs.getVideo(), playerArgs.getErrorCode());
			}
			break;
		
		case eStartPlay:
			mPlayDuration.start();
			break;
		
		case eStopPlay:
			{
				PlayerEventArgs playerArgs = (PlayerEventArgs)args;
				endPlay(playerArgs.getVideo());
			}
			break;

		case eTaskError:
			{
				TaskEventArgs taskArgs = (TaskEventArgs)args;
				errorTask(taskArgs.getTask());
			}
			break;

		case eTaskComplete:
			{
				TaskEventArgs taskArgs = (TaskEventArgs)args;
				completeTask(taskArgs.getTask());
			}
			break;
		}
	}

	public void create(ServiceFactory serviceFactory, LogDataHolder data) {
		mServiceFactory = serviceFactory;
		mData = data;
		
		mStat = (Stat)mServiceFactory.getServiceProvider(Stat.class);
		
		EventCenter eventCenter = getEventCenter();
		eventCenter.addListener(EventId.ePlayFail, this);
		eventCenter.addListener(EventId.eStartPlay, this);
		eventCenter.addListener(EventId.eStopPlay, this);
		eventCenter.addListener(EventId.eTaskError, this);
		eventCenter.addListener(EventId.eTaskComplete, this);
	}
	
	public void destroy() {
		getEventCenter().removeListener(this);
	}
	
	private void playFail(Video video, int errorCode) {
		mData.addPlayFail(video, errorCode);
		try{
		if(!video.isLocal()) {
			int type = video.toNet().getType();
			if(type == NetVideo.NetVideoType.P2P_STREAM) {
				mData.incStat(StatId.P2PPlayFailCount);
				mStat.incEventCount(StatId.Play.Name, StatId.Play.P2PFailCount);
			} else {
				mData.incStat(StatId.BigSitePlayFailCount);
				mStat.incEventCount(StatId.Play.Name, StatId.Play.BigSiteFailCount);
			}
		}}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void endPlay(Video video) {
		mPlayDuration.end();
		
		if(mPlayDuration.getDuration() < 0 || mPlayDuration.getDuration() > 3600 * 4) {
			return;
		}
		
		int offset = getOffset(video);
		
		mData.addStat(StatId.TotalPlayTime, mPlayDuration.getDuration());
		mStat.incEventValue(StatId.Play.Name, StatId.Play.TotalTime, (int)mPlayDuration.getDuration());
		if(!video.isLocal()){
			if(video.toNet().getType()==NetVideo.NetVideoType.P2P_STREAM){
				mStat.incEventValue(StatId.Play.Name, StatId.Play.P2PTotalTime, (int)mPlayDuration.getDuration());
			}else{
				mStat.incEventValue(StatId.Play.Name, StatId.Play.BigTotalTime, (int)mPlayDuration.getDuration());
			}
		}
		if(offset != 0) {
			mData.addStat(StatId.BasePlayTime + offset, mPlayDuration.getDuration());
			mStat.incEventValue(StatId.Play.Name, StatId.Play.Time + video.toNet().formatType(), (int)mPlayDuration.getDuration());
			
			mData.incStat(StatId.BasePlayCount + offset);
			mStat.incEventCount(StatId.Play.Name, StatId.Play.Count);
			if(!video.isLocal()){
				if(video.toNet().getType()==NetVideo.NetVideoType.P2P_STREAM){
					mStat.incEventCount(StatId.Play.Name, StatId.Play.P2PTotalCount);
				}else{
					mStat.incEventCount(StatId.Play.Name, StatId.Play.BigTotalCount);
				}
			}
			if(mPlayDuration.isValid(getValidMinute(video))) {
				mData.incStat(StatId.BaseValidPlayCount + offset);
				mStat.incEventCount(StatId.Play.Name, StatId.Play.ValidCount + video.toNet().formatType());
				mStat.incEventAppValid();
			}
		}
		
		mPlayDuration.clear();
	}

	private void errorTask(Task task) {
		if(!isStreamTask(task)) {
			if(task.getVideoType() == NetVideo.NetVideoType.P2P_STREAM) {
				mData.incStat(StatId.P2PDownloadFailCount);
				mStat.incEventCount(StatId.Download.Name, StatId.Download.P2PFailCount);
			} else {
				mData.incStat(StatId.BigSiteDownloadFailCount);
				mStat.incEventCount(StatId.Download.Name, StatId.Download.BigSiteFailCount);
			}
		}
	}

	private void completeTask(Task task) {
		if(!isStreamTask(task)) {
			if(task.getVideoType() == NetVideo.NetVideoType.P2P_STREAM) {
				mData.incStat(StatId.P2PDownloadCompleteCount);
				mStat.incEventCount(StatId.Download.Name, StatId.Download.P2PCompleteCount);
			} else {
				mData.incStat(StatId.BigSiteDownloadCompleteCount);
				mStat.incEventCount(StatId.Download.Name, StatId.Download.BigSiteCompleteCount);
			}
		}
	}
	
	private int getOffset(Video video) {
		if(video.isLocal()) {
			return 0;
		} else {
			return video.toNet().getType();
		}
	}

	private int getValidMinute(Video video) {
		return 5;
	}

	private boolean isStreamTask(Task task) {
		if(task.getType() == Task.Type.Small) {
			return task.toSmall().isStreamMode();
		}
		return false;
	}

	private EventCenter getEventCenter() {
		return (EventCenter)mServiceFactory.getServiceProvider(EventCenter.class);
	}
	
}
