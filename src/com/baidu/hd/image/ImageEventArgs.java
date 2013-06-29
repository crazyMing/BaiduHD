package com.baidu.hd.image;

import com.baidu.hd.event.EventArgs;
import com.baidu.hd.module.Image;

public class ImageEventArgs extends EventArgs {

	private String url = "";
	private String path = "";
	private int type = Image.Type.Unknown;
	
	public String getUrl() {
		return url;
	}
	public String getPath() {
		return path;
	}
	public int getType() {
		return type;
	}

	public ImageEventArgs(String url, String path, int type) {
		this.url = url;
		this.path = path;
		this.type = type;
	}
}
