package com.baidu.hd.stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.hd.module.album.Video;
import com.baidu.hd.util.StringUtil;

class LogDataHolder {

	/** 嗅探失败  */
	private List<String> mSnifferFail = new ArrayList<String>();
	
	/** 播放失败  */
	private List<String> mPlayFail = new ArrayList<String>();
	
	/** 统计 */
	private Map<Integer, Long> mStat = new HashMap<Integer, Long>();

	/**
	 * 添加嗅探失败信息
	 */
	public void addSnifferFail(String url) {
		this.mSnifferFail.add(url);
	}
	
	/**
	 * 添加播放失败信息
	 * @param video 播放的视频
	 * @param errorCode 错误码
	 */
	public void addPlayFail(Video video, int errorCode) {
		String key = "";
		try{
		if (video.isLocal()) {
			key = video.toLocal().getFullName();
		} else {
			if (video.toNet().isBdhd()) {
				key = video.toNet().getUrl();
			} else {
				key = video.toNet().getRefer();
			}
		}}catch(Exception e){
			e.printStackTrace();
		}
		this.mPlayFail.add(String.format("key:%s errorCode:%d", key, errorCode));
	}
	
	/** 增加统计项计数 */
	public void incStat(int id) {
		if(this.mStat.containsKey(id)) {
			long value = this.mStat.get(id);
			this.mStat.put(id, ++value);
		} else {
			this.mStat.put(id, (long)1);
		}
	}
	
	/** 设置统计项数量 */
	public void setStat(int id, long value) {
		this.mStat.put(id, value);
	}
	
	/** 增加统计项数量 */
	public void addStat(int id, long value) {
		if(this.mStat.containsKey(id)) {
			long v = this.mStat.get(id);
			this.mStat.put(id, v + value);
		} else {
			this.mStat.put(id, value);
		}
	}
	
	public String getSnifferFail() {
		StringBuilder sb = new StringBuilder();
		for(String item: this.mSnifferFail) {
			sb.append(item).append("\n");
		}
		return sb.toString();
	}
	
	public String getPlayFail() {
		StringBuilder sb = new StringBuilder();
		for(String item: this.mPlayFail) {
			sb.append(item).append("\n");
		}
		return sb.toString();
	}
	
	public String getStat() {
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<Integer, Long> entry: this.mStat.entrySet()) {
			sb.append(entry.getKey()).
				append("=").
				append(entry.getValue()).
				append(StatConst.LogSep);
		}
		String result = sb.toString();
		if(result.length() > 1) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}
	
	public void appendStat(String message) {
		try {
			String[] entries = message.split(StatConst.LogSep);
			for(String entry: entries) {
				String[] keyvalue = entry.split("=");
				if(keyvalue.length != 2) {
					continue;
				}
				if(StringUtil.isNumeric(keyvalue[1])) {
					this.appendStat(Integer.parseInt(keyvalue[0]), Integer.parseInt(keyvalue[1]));
				}
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void appendStat(int key, int value) {
		switch(key) {
		case StatId.Wifi:
		case StatId.NoSDCard:
		case StatId.LocalVideoCount:
			break;
		default:
			this.addStat(key, value);
			break;
		}
	}
}
