package com.baidu.hd.module.album;

import java.io.File;

import android.os.Bundle;

public class LocalVideo extends Video {

	@Override
	public boolean isLocal() {
		return true;
	}

	@Override
	public LocalVideo toLocal() {
		return this;
	}

	@Override
	public NetVideo toNet() {
		return null;
	}
	
	LocalVideo() {
		
	}

	private long id = -1;
	private String fullName = "";
	private long totalSize = 0;
	private int duration = 0;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getName() {
		return new File(this.fullName).getName();
	}
	public String getPath() {
		return new File(this.fullName).getPath();
	}
	public long getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public Bundle toBundle() {
		
		Bundle b = super.toBundle();
		b.putLong("id", this.getId());
		b.putString("fullname", this.getFullName());
		return b;
	}

	@Override
	public boolean isSame(Video value) {
		if(!value.isLocal()) {
			return false;
		}
		return this.fullName.equalsIgnoreCase(value.toLocal().fullName);
	}
	
	@Override
	protected void fillFromBundle(Bundle b) {

		this.setId(b.getLong("id"));
		this.setFullName(b.getString("fullname"));
	}
}
