package com.baidu.hd.sniffer;

import com.baidu.hd.sniffer.Sniffer.BigSiteCallback;

import android.content.Context;
import android.webkit.WebView;

public class SnifferImpl implements Sniffer {
	
	private Context mContext = null;
		
	@Override
	public void setContext(Context ctx) {
		mContext = ctx;
	}

	@Override
	public void onCreate() {
	}

	@Override
	public void onDestory() {
		// TODO
		// cancel all
	}

	@Override
	public void onSave() {
	}

	@Override
	public SnifferEntity createBig(BigSiteCallback callback, WebView webView) {
		return new BigSnifferEntityImp(mContext, webView, callback);
	}
	
	@Override
	public SnifferEntity createM3U8Entity(BigSiteCallback callback, WebView webView) {
		return new M3U8SnifferEntityImpl(mContext, null, callback);
	}

	@Override
	public SnifferEntity createSmall(SmallSiteCallback callback, WebView webView) {
		return new SmallSnifferEntityImp(mContext, callback, webView);
	}

}
