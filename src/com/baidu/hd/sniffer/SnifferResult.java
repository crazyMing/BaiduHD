package com.baidu.hd.sniffer;

import com.baidu.hd.sniffer.SnifferHandler.SnifferType;

public class SnifferResult {
	
	// 嗅探类型
	private SnifferType mType = SnifferType.UNKNOWN;
	// 成功标记
	private boolean mSucceed = false;
	// 页面地址
	private String mRefer = "";
	// 大站嗅探结果
	private BigSiteSnifferResult mBigSiteResult = null;
	// 小站嗅探结果
	private SmallSiteUrl mSmallSiteUrl = null;
	
	public BigSiteSnifferResult getBigSiteResult() {
		return mBigSiteResult;
	}
	
	public void setBigSiteResult(BigSiteSnifferResult mBigSiteResult) {
		this.mBigSiteResult = mBigSiteResult;
	}
	
	public SmallSiteUrl getSmallSiteUrl() {
		return mSmallSiteUrl;
	}
	
	public void setSmallSiteUrl(SmallSiteUrl mSmallSiteUrl) {
		this.mSmallSiteUrl = mSmallSiteUrl;
	}

	public SnifferType getType() {
		return mType;
	}

	public void setType(SnifferType mType) {
		this.mType = mType;
	}

	public boolean isSucceed() {
		return mSucceed;
	}

	public void setSucceed(boolean mSucceed) {
		this.mSucceed = mSucceed;
	}

	public String getRefer() {
		return mRefer;
	}

	public void setRefer(String mRefer) {
		this.mRefer = mRefer;
	}
}
