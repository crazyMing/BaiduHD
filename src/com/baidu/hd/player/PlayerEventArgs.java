package com.baidu.hd.player;

import com.baidu.hd.event.EventArgs;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.album.Video;

public class PlayerEventArgs extends EventArgs {

	private Video mVideo = null;
	private Task mTask = null;
	private int mErrorCode = ErrorCode.None;
	
	public PlayerEventArgs(Video video, Task task) {
		this.mVideo = video;
		this.mTask = task;
	}
	public PlayerEventArgs(Video video, int errorCode) {
		this.mVideo = video;
		this.mErrorCode = errorCode;
	}

	public Video getVideo() {
		return mVideo;
	}
	public Task getTask() {
		return mTask;
	}
	public int getErrorCode() {
		return mErrorCode;
	}
}
