package com.baidu.hd.module;

import com.baidu.hd.module.album.Album;


/**
 * ������Ƶ ListView����
 */
public class HistoryItemPackage  extends ItemPackage
{
	private Album album = null;

	public Album getAlbum()
	{
		return this.album;
	}

	public void setAlbum(Album album)
	{
		this.album = album;
	}
}