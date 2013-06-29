package com.baidu.hd.sniffer;


import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import com.baidu.hd.log.Logger;
import com.baidu.hd.sniffer.smallsniffer.NativeSniffer;

public class SmallNativeHandler extends SmallBaseHandler
{
	
	private Logger logger = new Logger("SmallNativeHandler");
	private NativeSniffer mNativeSniffer;
	
	public void onCreate(OnSmallCompleteListener listener, Context context, WebView webView)
	{
		super.onCreate(listener, context, webView);
	}

	@Override
	protected void start()
	{
		
		logger.d("native sniffer start");
		if ("".equals(mRefer))
		{
			snifferComplete();
			return ;
		}
		
		final String refer = mRefer;
		
		mNativeSniffer = new NativeSniffer(new NativeSniffer.CallBack()
		{
			@Override
			public void onResult(String pageUrl, String bdhd)
			{
				logger.d("bdhd : " + bdhd);
				mSmallSiteUrl = new SmallSiteUrl();
				mSmallSiteUrl.setBdhd(bdhd);
				mSmallSiteUrl.setSnifferType(true);
				snifferComplete();
			}
		});
		mNativeSniffer.sniffer(mWebView, refer);
	}

	@Override
	protected void stop()
	{
		if (mNativeSniffer != null)
		{
			mNativeSniffer.cancle();
		}
	}

	@Override
	protected String getType()
	{
		return "NativeSniffer";
	}

	@Override
	public void cancel()
	{
		super.cancel();
	}
}
