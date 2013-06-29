package com.baidu.hd.stat;

import java.util.Date;

/**
 * 播放时长帮助类
 */
class PlayDuration {
	
	private long mStartTime = 0;
	private long mEndTime = 0;

	public void start() {
		this.mStartTime = new Date().getTime();
		this.mEndTime = 0;
	}
	
	public void end() {
		this.mEndTime = new Date().getTime();
	}
	
	public boolean isValid(int minute) {
		return this.mEndTime - this.mStartTime > minute * 60 * 1000;
	}
	
	public long getDuration() {
		return (this.mEndTime - this.mStartTime) / 1000;
	}
	
	public void clear() {
		mStartTime = 0;
		mEndTime = 0;
	}
}
