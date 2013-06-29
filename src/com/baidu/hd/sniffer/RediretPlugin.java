package com.baidu.hd.sniffer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.conf.Configuration;
import com.baidu.hd.log.Logger;

/**
 * webview用于跳转的组件
 */
@TargetApi(8)
class RediretPlugin {
	
	interface Callback {
		void onFinished(String url);
	}
	
	private static final int TimeoutTick = 5 * 1000;
	private static final int DetectTimeout = 1000;
	
	private static final int RunId = 10000;
	private static final int FoundId = 10001;
	
	private Logger logger = new Logger("RediretPlugin");
	
	private Context mContext = null;
	private WebView mWebView = null;
	
	private long mStartTick = 0;
	private Callback mCallback = null;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			
			if(msg.what == RunId) {
				
				createWebView();
				mWebView.loadUrl(msg.obj.toString());
				
				mStartTick = System.currentTimeMillis();
				mHandler.sendEmptyMessageDelayed(DetectTimeout, 1000);
				return;
			}
			if(msg.what == FoundId) {
				
				if(mCallback != null) {
					mCallback.onFinished(msg.obj.toString());
				}
			}
			if(msg.what == DetectTimeout) {
				
				if(System.currentTimeMillis() - mStartTick >= TimeoutTick) {
					if(mCallback != null) {
						mCallback.onFinished("");
					}
				} else {
					this.sendEmptyMessageDelayed(DetectTimeout, 1000);
				}
				return;
			}
			super.handleMessage(msg);
		}
	};
	
	public RediretPlugin(Context context) {
		this.mContext = context;
	}
	
	public void run(String url) {
		this.mHandler.sendMessage(this.mHandler.obtainMessage(RunId, url));
	}
	
	public void stop() {
		this.mWebView.stopLoading();
	}
	
	public void setCallback(Callback callback) {
		this.mCallback = callback;
	}

	@TargetApi(8)
	protected void createWebView() {
		
		if(this.mWebView != null) {
			return;
		}
	  	this.mWebView = new WebView(this.mContext);

		WebSettings settings = this.mWebView.getSettings();
		Configuration conf = (Configuration) BaiduHD.cast(mContext)
				.getServiceFactory().getServiceProvider(Configuration.class);
		settings.setUserAgentString(conf.getSnifferUA());

		// js
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(false);
		
		// cache
		settings.setAppCacheEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_DEFAULT);
		
		// plugin
		settings.setPluginsEnabled(false);
		
		// view
		settings.setLoadWithOverviewMode(false);
		
		// network
		settings.setLoadsImagesAutomatically(false);
		settings.setBlockNetworkImage(true);
		settings.setBlockNetworkLoads(false);
		
		// data base
		settings.setDatabaseEnabled(true);
		settings.setDomStorageEnabled(true);
		
		this.mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				
				logger.d("Page Finished " + url);
				super.onPageFinished(view, url);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				
				mHandler.sendMessage(mHandler.obtainMessage(FoundId, url));
				logger.d("Page Started " + url);
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				
				logger.d("Received Error. " + errorCode + " " + description);
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
			
		});

		this.mWebView.setWebChromeClient(new WebChromeClient() {

			@Override
			public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
				//logger.d(consoleMessage.message());
				return super.onConsoleMessage(consoleMessage);
			}
		});
	}
}
