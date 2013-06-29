package com.baidu.hd.sniffer;

import android.content.Context;
import android.webkit.WebView;

/**
 * 大站特殊支持处理器
 */
class WebViewSpecHandler extends WebViewBaseHandler {

	private static final String mDefaultUrl = "file:///android_asset/sniffer/sites/";

	private Spec mSpec = null;
	
	public void onCreate(OnCompleteListener listener, Context context, WebView webView, Spec spec) {
		super.onCreate(listener, context, webView);
		mSpec = spec;
	}
	
	@Override
	protected String getShouldLoadUrl() {
		String fileName = mSpec.getSpec(mRefer);
		return mDefaultUrl + fileName;
	}

	@Override
	protected void createWebView() {

    	this.mSnifferWebView.create(this.mContext, new SnifferWebView.Callback() {
			
			@Override
			public void onPageStart(String refer) {
			}

			@Override
			public void onPageFinished(String refer) {
				mSnifferWebView.loadUrl("javascript:sniffer.setRawUrl('" + mRefer + "')");
			}

			@Override
			public void onComplete(String refer, String url) {
				if(refer.equals(mRefer) && !"".equals(mRefer)) {
					mUrl = url;
					snifferComplete();
				}
			}
		});
	}

	@Override
	String getType() {
		return "WebViewSpec";
	}
}
