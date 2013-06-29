package com.baidu.hd.module.album;

import android.os.Bundle;

public class NetVideo extends Video {
	
	public class NetVideoType {
		public static final int NONE = -1;				 //
		public static final int P2P_STREAM = 1;    // 服务器嗅探得到的视频
		public static final int BIGSITE = 2;			 // 大站视频
	}

	@Override
	public boolean isLocal() {
		return false;
	}

	@Override
	public LocalVideo toLocal() {
		return null;
	}

	@Override
	public NetVideo toNet() {
		return this;
	}

	NetVideo() {
		
	}
	
	private long id = -1;
	private String url = "";			  // bdhd地址
	private String refer = "";            // 视频页的refer
	private String albumRefer = "";	  // 视频列表页refer
	private String episode = "";  // 第几集，将该字段保存到DBNetVideo中的playId
	private int type = NetVideoType.NONE;
	private Album album = null;
	
	//<add by sunjianshun 2012.10.31 BEGIN
	private long albumId = -1;
	private int grade = 0; // 视频清晰度等级
	private String image = "";
	//add by sunjianshun 2012.10.31 END>

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRefer() {
		return refer;
	}

	public void setRefer(String refer) {
		this.refer = refer;
	}
	
	public String getAlbumRefer() {
		return albumRefer;
	}
	
	public void setAlbumRefer(String albumRefer) {
		this.albumRefer = albumRefer;
	}

	public String getEpisode() {
		return episode;
	}

	public void setEpisode(String episode) {
		this.episode = episode;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Album getAlbum() {
		return album;
	}

	public void setAlbum(Album album) {
		this.album = album;
	}

	public long getAlbumId() {
		return albumId;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public boolean isBdhd() {
		return this.type == NetVideoType.P2P_STREAM;
	}

	public Bundle toBundle() {
		
		Bundle b = super.toBundle();
		b.putLong("id", this.id);
		b.putString("url", this.url);
		b.putString("refer", this.refer);
		b.putString("episode", this.episode);
		b.putInt("type", this.type);
		b.putLong("albumId", this.albumId);
		b.putString("albumRefer", this.albumRefer);
		return b;
	}
	
	@Override
	public boolean isSame(Video v) {
		if (refer == null) return false;
		
		if(v.isLocal()) {
			return false;
		}
		return this.refer.equalsIgnoreCase(v.toNet().refer);
	}

	@Override
	protected void fillFromBundle(Bundle b) {

		this.id = b.getLong("id");
		this.url = b.getString("url");
		this.refer = b.getString("refer");
		this.episode = b.getString("episode");
		this.type = b.getInt("type");
		this.albumId = b.getLong("albumId");
		this.albumRefer = b.getString("albumRefer");
	}
	
	public String formatType() {
		return getFormatType(type);
	}
	
	public static String getFormatType(int value) {
		switch(value) {
		case NetVideoType.P2P_STREAM:
			return "P2pstream";
		case NetVideoType.BIGSITE:
			return "Bigsite";
		default:
			return "Unknown";
		}
	}

	@Override
	public String toString() {
		return "NetVideo [id=" + id + ", url=" + url + ", refer=" + refer
				+ ", albumRefer=" + albumRefer + ", episode=" + episode
				+ ", type=" + type + ", album=" + album + ", albumId="
				+ albumId + ", grade=" + grade + ", image=" + image + "]";
	}
}



