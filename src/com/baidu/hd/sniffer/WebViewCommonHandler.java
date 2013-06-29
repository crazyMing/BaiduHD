package com.baidu.hd.sniffer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Handler;
import android.os.Message;

import com.baidu.hd.log.Logger;

/**
 * 大站常规支持处理器
 */
class WebViewCommonHandler extends WebViewBaseHandler {

	private static String mDefaultJs = "";
	
	private Logger logger = new Logger("Sniffer");
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			mSnifferWebView.loadUrl("javascript:" + getDefaultSiteJs().replace("$currentUrl$", mRefer));
			this.sendEmptyMessageDelayed(0, 1000);
		}
	};

	protected String getShouldLoadUrl() {
		return mRefer;
	}
	
    protected void createWebView() {

    	this.mSnifferWebView.create(mContext, new SnifferWebView.Callback() {
			
			@Override
			public void onPageStart(String refer) {
				if(refer.equalsIgnoreCase(mRefer) && !"".equals(mRefer)) {
					mHandler.sendEmptyMessageDelayed(0, 1000);
				}
			}

			@Override
			public void onPageFinished(String refer) {
				if(refer.equalsIgnoreCase(mRefer) && !"".equals(mRefer)) {
					if(!mHandler.hasMessages(0)) {
						mHandler.sendEmptyMessageDelayed(0, 1000);
					}
				}
			}

			@Override
			public void onComplete(String refer, String url) {
				if(refer.equalsIgnoreCase(mRefer) && !"".equals(mRefer)) {
					mUrl = url;
					snifferComplete();
				}
				stop();
			}
		});
    }

    @Override
	protected void stop() {
    	this.mHandler.removeMessages(0);
		super.stop();
	}

    private String getDefaultSiteJs() {
    	
    	if("".equals(mDefaultJs)) {

            try {
            	InputStream is = this.mContext.getAssets().open("sniffer/default.js");
            	BufferedReader br = new BufferedReader(new InputStreamReader(is));
            	String data = null;
            	StringBuilder sb = new StringBuilder();
            	while((data = br.readLine()) != null)
            	{
            		sb.append(data);
            	}
            	mDefaultJs = sb.toString();
            	
            } catch(Exception e) {
            	e.printStackTrace();
            	this.logger.e(e.getMessage());
            }
    	}
    	return mDefaultJs;
    }
    
	@Override
	String getType() {
		return "WebViewCommon";
	}
}
