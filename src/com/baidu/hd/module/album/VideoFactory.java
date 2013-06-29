package com.baidu.hd.module.album;

public class VideoFactory {

	public static Video create(boolean isLocal) {
		return isLocal ? new LocalVideo() : new NetVideo();
	}
}
