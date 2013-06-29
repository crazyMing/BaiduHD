package com.baidu.hd.module.album;

/**
 * ��վ���� �����缯
 * @author sunjianshun
 *
 */
public class BigNativeAlbum extends Album {
	
	private NetVideo mVideo = null;

	public BigNativeAlbum() {
		super();
		this.setFromType(1);
		this.setPersonalHistory(false);
	}
	
	public NetVideo getVideo() {
		return mVideo;
	}

	public void setVideo(NetVideo video) {
		this.mVideo = video;
	}

	@Override
	public BigNativeAlbum asBigNativeAlbum() {
		return this;
	}
}
