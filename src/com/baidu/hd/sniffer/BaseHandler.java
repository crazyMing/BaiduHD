package com.baidu.hd.sniffer;

import android.content.Context;

import com.baidu.hd.log.Logger;
import com.baidu.hd.util.StringUtil;

/**
 * 嗅探处理器基类
 */
abstract class BaseHandler {
	
	private Logger logger = new Logger("Sniffer");
	
	protected Context mContext = null;
	protected OnCompleteListener mListener = null;

	protected String mRefer = "";
	protected String mUrl = "";
	protected boolean isComplete = true;
	
	protected boolean mCancel = false;
	
	abstract protected void start();
	abstract protected void stop();
	abstract String getType();

	protected void onCreate(OnCompleteListener listener, Context context) {
		mListener = listener;
		mContext = context;
	}
	
	public void fetch(String refer) {
		mRefer = refer;
		isComplete = false;
		start();
	}
	
	public void cancel() {
		mCancel = true;
    	mRefer = "";
    	mUrl = "";
    	isComplete = true;
    	stop();
	}
	
	public boolean isComplete() {
		return isComplete;
	}

    protected void snifferComplete() {
    	log();
    	isComplete = true;
    	if(!mCancel) {
    		mListener.onComplete(mRefer, mUrl, null, this);
    	}
    }

    protected void log() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("sniffer by " + getType() + " " + mRefer + "\r\n");
    	if(StringUtil.isEmpty(mUrl)) {
    		sb.append("error not found\r\n");
    	} else {
        	sb.append(mUrl + "\r\n");
    	}
    	logger.i(sb.toString());
    }
}
