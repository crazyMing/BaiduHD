package com.baidu.hd.module;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 大站单一视频
 * 
 * 三种类型：虚拟总任务whole，一级任务和二级任务
 * 子任务状态不可能是Queue
 * 状态策略：
 * 		anyone sub start, whole start
 * 		all sub stop or complete, whole stop
 * 		anyone sub error, whole error
 * 		all sub complete, whole complete
 * 
 * 		whole start, sub be start, stop, complete
 * 		whole stop, sub be stop, complete
 * 		whole complete, all sub complete
 * 		whole error, sub be stop, complete, error
 * 		whole queue, sub be stop, complete
 * 大小：
 * 		仅first时，download = 0,total = 0
 * 		都存在时
 * 			download为所有second之和
 * 			total为所有second之和，任意一个second total为0，则whole total为0
 * 百分比：
 * 		仅first时，percent = download / total
 * 		有second时，percent = downloadDuration / totalDuration
 * 当whole.getUrl() 不为空时，表示已经拥有了嗅探后的地址
 */
public class BigSiteTask extends Task {
	
	public class SubType {
		public static final int Whole = 1;
		public static final int First = 2;
		public static final int Second = 3;
	}
	
	/** 播放方式 */
	public class PlayType {
		
		/** 未知 */
		public static final int Unknown = 1;
		
		/** p2p */
		public static final int P2P = 2;
		
		/** 文件播放方式 */
		public static final int File = 3;
	}
	
	private int mSubType = SubType.Whole;
	
	/** 播放方式，whole有效 */
	private int mPlayType = PlayType.Unknown;
	
	/** 时长，first为总时长，second为自身时长 */
	private int mDuration = 0;
	
	/** 是否已经解析完成，whole有效 */
	private boolean mParseComplete = false;
	
	/** 是否已经估算完成m3u8任务总大小，whole有效 */
	private boolean mComputeTotalSize = false;
	
	/** 所属whole任务 */
	private BigSiteTask mParent = null;

	/**
	 * 一级任务
	 * 对于M3U8任务，即列表任务
	 * 对于MP4任务，即视频任务
	 */
	private BigSiteTask mFirstTask = null;

	/**
	 * 二级任务
	 * 对与M3U8任务，即视频任务
	 */
	private List<BigSiteTask> mSecondTasks = new ArrayList<BigSiteTask>();

	BigSiteTask() {
	}

	@Override
	public int getType() {
		return Type.Big;
	}

	@Override
	public String getKey() {
		
		if(mSubType == SubType.Whole) {
			return getRefer();
		} else {
			return mParent.getRefer();
		}
	}

	@Override
	public SmallSiteTask toSmall() {
		return null;
	}

	@Override
	public BigSiteTask toBig() {
		return this;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public void copyFrom(Task value) {
		
		switch(value.getType()) {
		case Type.Small:
			{
				super.copyFrom(value);
			}
			break;
		case Type.Big:
			{
				super.copyFrom(value);
				
				BigSiteTask bigTask = value.toBig();
				mDuration = bigTask.mDuration;
				mParent = bigTask.mParent;
				mFirstTask = bigTask.mFirstTask;
				mSecondTasks = bigTask.mSecondTasks;
				mSubType = bigTask.mSubType;
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public void clearState() {
		
		super.clearState();
		mDuration = 0;
		if(mFirstTask != null) {
			mFirstTask.clearState();
		}
		for(BigSiteTask t: mSecondTasks) {
			t.clearState();
		}
	}
	
	@Override
	protected JSONObject persist() {
		
		JSONObject o = super.persist();
		try {
			o.put("bigSiteType", getFormatBigSiteType());
			
			switch(getSubType()) {
			case SubType.Whole:
				{
					o.put("duration", mDuration);
					o.put("first", 
							mFirstTask == null ? new JSONObject() : mFirstTask.persist());
					
					JSONArray array = new JSONArray();
					for(BigSiteTask t: mSecondTasks) {
						array.put(t.persist());
					}
					o.put("second", array);
				}
				break;
			case SubType.First:
				o.put("duration", mDuration);
				break;
			case SubType.Second:
				o.put("duration", mDuration);
				break;
			}
			
		} catch(JSONException e) {
			e.printStackTrace();
		}
		return o;
	}

	public int getSubType() {
		return mSubType;
	}
	public void setSubType(int value) {
		mSubType = value;
	}
	public int getDuration() {
		return mDuration;
	}
	public void setDuration(int duration) {
		mDuration = duration;
	}
	public boolean isParseComplete() {
		return mParseComplete;
	}
	public void setParseComplete(boolean value) {
		mParseComplete = value;
	}
	public boolean isComputeTotalSize() {
		return mComputeTotalSize;
	}
	public void setComputeTotalSize(boolean computeTotalSize) {
		mComputeTotalSize = computeTotalSize;
	}
	public int getPlayType() {
		return mPlayType;
	}
	public void setPlayType(int value) {
		mPlayType = value;
	}

	public List<BigSiteTask> getSecondTasks() {
		return mSecondTasks;
	}

	public void setSecondTasks(List<BigSiteTask> value) {
		mSecondTasks = value;
	}

	public BigSiteTask getFirstTask() {
		return mFirstTask;
	}

	public void setFirstTask(BigSiteTask value) {
		mFirstTask = value;
	}

	public BigSiteTask getParent() {
		return mParent;
	}

	public void setParent(BigSiteTask value) {
		mParent = value;
	}

	private String getFormatBigSiteType() {
		
		switch(mSubType) {
		case SubType.Whole:
			return "Whole";
		case SubType.First:
			return "First";
		case SubType.Second:
			return "Second";
		}
		return "Unknown";
	}
}
