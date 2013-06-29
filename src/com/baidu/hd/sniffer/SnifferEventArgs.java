package com.baidu.hd.sniffer;

import java.util.List;

import com.baidu.hd.event.EventArgs;

public class SnifferEventArgs extends EventArgs {

	private String mUrl = null;
	private List<String> mSrcs = null;
	private boolean mSuccess = false;

	public String getUrl() {
		return mUrl;
	}

	public List<String> getSrcs() {
		return this.mSrcs;
	}
	
	public boolean isSuccess() {
		return mSuccess;
	}

	public SnifferEventArgs(String url) {
		this.mUrl = url;
	}
	
	public SnifferEventArgs(String url, List<String> srcs) {
		this.mUrl = url;
		this.mSrcs = srcs;
		this.mSuccess = true;
	}
	
}
