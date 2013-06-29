package com.baidu.hd.module;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ��վ��һ��Ƶ
 * 
 * �������ͣ�����������whole��һ������Ͷ�������
 * ������״̬��������Queue
 * ״̬���ԣ�
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
 * ��С��
 * 		��firstʱ��download = 0,total = 0
 * 		������ʱ
 * 			downloadΪ����second֮��
 * 			totalΪ����second֮�ͣ�����һ��second totalΪ0����whole totalΪ0
 * �ٷֱȣ�
 * 		��firstʱ��percent = download / total
 * 		��secondʱ��percent = downloadDuration / totalDuration
 * ��whole.getUrl() ��Ϊ��ʱ����ʾ�Ѿ�ӵ������̽��ĵ�ַ
 */
public class BigSiteTask extends Task {
	
	public class SubType {
		public static final int Whole = 1;
		public static final int First = 2;
		public static final int Second = 3;
	}
	
	/** ���ŷ�ʽ */
	public class PlayType {
		
		/** δ֪ */
		public static final int Unknown = 1;
		
		/** p2p */
		public static final int P2P = 2;
		
		/** �ļ����ŷ�ʽ */
		public static final int File = 3;
	}
	
	private int mSubType = SubType.Whole;
	
	/** ���ŷ�ʽ��whole��Ч */
	private int mPlayType = PlayType.Unknown;
	
	/** ʱ����firstΪ��ʱ����secondΪ����ʱ�� */
	private int mDuration = 0;
	
	/** �Ƿ��Ѿ�������ɣ�whole��Ч */
	private boolean mParseComplete = false;
	
	/** �Ƿ��Ѿ��������m3u8�����ܴ�С��whole��Ч */
	private boolean mComputeTotalSize = false;
	
	/** ����whole���� */
	private BigSiteTask mParent = null;

	/**
	 * һ������
	 * ����M3U8���񣬼��б�����
	 * ����MP4���񣬼���Ƶ����
	 */
	private BigSiteTask mFirstTask = null;

	/**
	 * ��������
	 * ����M3U8���񣬼���Ƶ����
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
