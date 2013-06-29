package com.baidu.hd.module;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.hd.module.album.NetVideo;

/**
 * 小站任务
 */
public class SmallSiteTask extends Task{

	/**
	 * 是否为流式模式
	 */
	private boolean streamMode = false;

	SmallSiteTask() {
		setVideoType(NetVideo.NetVideoType.P2P_STREAM);
	}

	@Override
	public int getType() {
		return Type.Small;
	}

	@Override
	public String getKey() {
		return getUrl();
	}

	@Override
	public SmallSiteTask toSmall() {
		return this;
	}

	@Override
	public BigSiteTask toBig() {
		return null;
	}

	@Override
	public boolean isVisible() {
		return !streamMode;
	}

	@Override
	public void copyFrom(Task value) {
		
		super.copyFrom(value);
		
		if(value.getType() == Type.Small) {
			streamMode = value.toSmall().streamMode;
		}
	}
	
	@Override
	protected JSONObject persist() {
		
		JSONObject o = super.persist();
		try {			
			o.put("streamMode", streamMode);
		} catch(JSONException e) {
			e.printStackTrace();
		}
		return o;
	}

	public boolean isStreamMode() {
		return streamMode;
	}
	public void setStreamMode(boolean value) {
		streamMode = value;
	}
}


