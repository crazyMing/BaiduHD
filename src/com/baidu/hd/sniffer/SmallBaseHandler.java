package com.baidu.hd.sniffer;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import com.baidu.hd.log.Logger;
import com.baidu.hd.net.NewHttpComm.HttpAsyncTask;

/**
 * 小站嗅探处理器基类 
 */

abstract public class SmallBaseHandler
{
	private Logger logger = new Logger("SmallBaseHandler");
	private OnSmallCompleteListener mListener = null;
	protected Context mContext = null;
	protected WebView mWebView = null;
	
	protected String mRefer = "";
	protected SmallSiteUrl mSmallSiteUrl;
	
	protected boolean isComplete = true;
	
	protected boolean mCancel = false;
	
	protected HttpAsyncTask mHttpAsyncTask;
	
	abstract protected void start();
	abstract protected void stop();
	abstract protected String getType();
	
	protected void onCreate(OnSmallCompleteListener listener, Context context)
	{
		mListener = listener;
		mContext = context;
	}
	
	protected void onCreate(OnSmallCompleteListener listener, Context context, WebView webView)
	{
		onCreate(listener, context);
		this.mWebView = webView;
	}
	
	public void fetch(String refer)
	{
		mRefer = refer;
		isComplete = false;
		mCancel = false;
		
		if (mHttpAsyncTask != null)
		{
			mHttpAsyncTask.cancel(true);
		}
		
		start();
	}
	
	public void cancel()
	{
		mCancel = true;
		mRefer = "";
		mSmallSiteUrl = null;
		isComplete = true;
		stop();
	}
	
	public boolean isComplete()
	{
		return isComplete;
	}
	
	protected void snifferComplete()
	{
		log();
		isComplete = true;
		mCancel = false;
		if(!mCancel)
		{
			mListener.onComplete(mRefer, mSmallSiteUrl, this);
		}
		if (mHttpAsyncTask != null)
		{
			mHttpAsyncTask.cancel(true);
		}
	}
	
	private void log()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("sniffer by " + getType() + " "  + mRefer + "\r\n");
		if (mSmallSiteUrl != null && !mSmallSiteUrl.isSuccess())
		{
			sb.append("not found\r\n");
		}
		logger.i(sb.toString());
	}
	
	public boolean isCancel()
	{
		return this.mCancel;
	}
}
