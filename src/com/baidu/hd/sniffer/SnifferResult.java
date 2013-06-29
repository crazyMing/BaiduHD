package com.baidu.hd.sniffer;

import com.baidu.hd.sniffer.SnifferHandler.SnifferType;

public class SnifferResult {
	
	// ��̽����
	private SnifferType mType = SnifferType.UNKNOWN;
	// �ɹ����
	private boolean mSucceed = false;
	// ҳ���ַ
	private String mRefer = "";
	// ��վ��̽���
	private BigSiteSnifferResult mBigSiteResult = null;
	// Сվ��̽���
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
