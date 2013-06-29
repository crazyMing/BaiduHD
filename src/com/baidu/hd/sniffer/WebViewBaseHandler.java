package com.baidu.hd.sniffer;

import android.content.Context;
import android.webkit.WebView;


/**
 * ��վ��������֧�ִ������Ļ���
 * ��������WebView
 */
abstract class WebViewBaseHandler extends BaseHandler {

	/**
	 * ��̽��WebView
	 */
	protected SnifferWebView mSnifferWebView = null;
	protected WebView mWebView = null;

	abstract protected String getShouldLoadUrl();
	abstract protected void createWebView();

	protected void onCreate(OnCompleteListener listener, Context context, WebView webView) {
		mWebView = webView;
		super.onCreate(listener, context);
	}
	
	@Override
	protected void start() {
		if(this.mSnifferWebView == null) {
			this.mSnifferWebView = new SnifferWebView(mWebView);
			this.createWebView();
		}
		
		this.mSnifferWebView.loadUrl(this.getShouldLoadUrl());
	}
	
	@Override
	protected void stop() {
		if(mSnifferWebView != null) {
			this.mSnifferWebView.stop();
		}
	}
}
