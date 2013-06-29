package com.baidu.hd.module.album;

/**
 * 小站服务器嗅探剧集类
 * @author sunjianshun
 *
 */
public class SmallServerAlbum extends Album {

	public SmallServerAlbum() {
		super();
		this.setFromType(2);
	}

	@Override
	public SmallServerAlbum asSmallServerAlbum() {
		return this;
	}

}
