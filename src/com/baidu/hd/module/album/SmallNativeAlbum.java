package com.baidu.hd.module.album;

/**
 * ������̽�õ��ľ缯��
 * @author sunjianshun
 *
 */
public class SmallNativeAlbum extends Album {
	
	private NetVideo mVideo = null;
	
	public SmallNativeAlbum() {
		super();
		this.setFromType(3);
		this.setPersonalHistory(false);
	}

	public NetVideo getVideo() {
		return mVideo;
	}
	
	public void setNetVideo(NetVideo video) {
		this.mVideo = video;
	}
	
	@Override
	public SmallNativeAlbum asSmallNativeAlbum() {
		return this;
	}

}
