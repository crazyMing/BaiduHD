package com.baidu.hd.module.album;

/**
 * 本地嗅探得到的剧集类
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
