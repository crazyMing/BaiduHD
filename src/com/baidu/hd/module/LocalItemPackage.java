package com.baidu.hd.module;

import com.baidu.hd.module.album.LocalVideo;


/**
 * ������Ƶ ListView����
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