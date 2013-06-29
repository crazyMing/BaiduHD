package com.baidu.hd.module.album;

import android.os.Bundle;


abstract public class Video {
	
	abstract public boolean isLocal();
	abstract public LocalVideo toLocal();
	abstract public NetVideo toNet();
	abstract public boolean isSame(Video v);
	abstract protected void fillFromBundle(Bundle b);
	
	Video() {
	}

	/**
	 * 最后播放位置
	 */
	private int position = 0;

	/**
	 * 视频名称
	 */
	private String name = "";

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Bundle toBundle() {
		
		Bundle b = new Bundle();
		b.putInt("position", this.position);
		b.putString("name", this.name);
		b.putBoolean("islocal", this.isLocal());
		return b;
	}
	
	public static Video fromBundle(Bundle b) {
		
		if(b == null) {
			return null;
		}
		
		Video v = VideoFactory.create(b.getBoolean("islocal"));
		v.setName(b.getString("name"));
		v.setPosition(b.getInt("position"));
		v.fillFromBundle(b);
		return v;
	}
}


