package com.baidu.hd.stat;

import com.baidu.hd.service.ServiceProvider;

public interface Stat extends ServiceProvider {
	
	public enum NetState {
		eWifi,
		eNoWifi,
		eDisable,
	}

	/** 上报日志	 */
	void postLog();
	
	/** 嗅探失败 */
	void addSnifferFail(String refer);

	/** 设置本地视频数量 */
	void setLocalVideoCount(int value);
	
	void setNetState(NetState state);
	void setHaveSDCard(boolean value);

	// log平台统计
	void incLogCount(int id);
	
	// 移动统计
	void incEventCount(String type, String subType);
	void incEventValue(String type, String subType, int value);
	void incEventAppValid();
	
	void incUdpCount(int id);
	void incUdpValue(int id, long value);
	void setUdpString(int id, String value);
	void sendUdp();
}
