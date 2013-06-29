package com.baidu.hd.module.album;


import android.os.Bundle;

public class AlbumFactory {
	
	public enum AlbumType {
		BIG_SERVER,
		BIG_NATIVE,
		SMALL_SERVER,
		SMALL_NATIVE;
	}
	
	private static AlbumFactory mInstance = null;
	
	public static AlbumFactory getInstance() {
		if (mInstance == null) {
			mInstance = new AlbumFactory();
		}
		return mInstance;
	}
	
	private AlbumFactory() {
		//
	}
	
	public Album createAlbum(AlbumType type) {
		switch (type) {
		case SMALL_SERVER:
			return new SmallServerAlbum();
		case SMALL_NATIVE:
			return new SmallNativeAlbum();
		case BIG_SERVER:
			return new BigServerAlbum();
		case BIG_NATIVE:
			return new BigNativeAlbum();
		}
		return null;
	}
	
	public Album fromBundle(Bundle b) {
		if(b == null) {
			return null;
		}
		// TODO 先只建来自服务器嗅探的剧集
		Album a = new SmallServerAlbum();
	/*	a.id = b.getLong("id");
		a.refer = b.getString("refer");
		a.listId = b.getString("listid");
		a.listName = b.getString("listname");
		a.image = b.getString("image");
		a.site = b.getString("site");
		a.year = b.getString("year");
		a.type = b.getInt("type");
		a.current = NetVideo.fromBundle(b.getBundle("current")).toNet();
		a.newestId = b.getString("newest");
		a.haveNew = b.getBoolean("haveNew");
		a.isFinished = b.getBoolean("isfinish");
		a.push = b.getBoolean("push");
		a.push = b.getBoolean("personalHistory");
		a.push = b.getBoolean("favorite");*/
		return a;
	}
}
 