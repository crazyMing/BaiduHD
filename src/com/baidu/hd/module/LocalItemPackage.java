package com.baidu.hd.module;

import com.baidu.hd.module.album.LocalVideo;


/**
 * 本地视频 ListView数据
 */
public class LocalItemPackage  extends ItemPackage
{
	private LocalVideo mVideo = null;

	public LocalVideo getVideo()
	{
		return mVideo;
	}

	public void setVideo(LocalVideo video)
	{
		this.mVideo = video;
	}
}