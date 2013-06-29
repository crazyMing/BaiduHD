package com.baidu.hd.sniffer;

/**
 * 嗅探实体，负责真正的嗅探工作
 * 实现分为大站实体和小站实体
 * 
 * 大站嗅探出m3u8视频链接
 * 小站嗅探出播放列表和当前页的视频链接
 */
public interface SnifferEntity {

	/**
	 * 开始嗅探
	 * @param refer 视频引用页
	 */
	void request(String refer);
	
	/**
	 * 停止嗅探，不会再收到结果
	 */
	void cancel();
}
