package com.baidu.hd.stat;

import java.util.HashMap;
import java.util.Map;

class UdpDataHolder {

	/** 统计 */
	private Map<Integer, Long> mStat = new HashMap<Integer, Long>();
	private Map<Integer, String> mStatString = new HashMap<Integer, String>();

	/** 增加统计项计数 */
	public void incStat(int id) {
		if(this.mStat.containsKey(id)) {
			long value = this.mStat.get(id);
			this.mStat.put(id, ++value);
		} else {
			this.mStat.put(id, (long)1);
		}
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
	
	public void setStatString(int id, String value) {
		this.mStatString.put(id, value);
	}
	
	/** 清空数据项 */
	public void clear() {
		mStat.clear();
		mStatString.clear();
	}
	
	public String getStat() {
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<Integer, Long> entry: this.mStat.entrySet()) {
			sb.append(entry.getKey()).
				append("=").
				append(entry.getValue()).
				append(StatConst.UdpSep);
		}
		for(Map.Entry<Integer, String> entry: this.mStatString.entrySet()) {
			sb.append(entry.getKey()).
				append("=").
				append(entry.getValue()).
				append(StatConst.UdpSep);
		}
		String result = sb.toString();
		if(result.length() > 1) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}
}
