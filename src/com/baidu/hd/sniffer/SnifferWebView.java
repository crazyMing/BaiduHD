package com.baidu.hd.sniffer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.conf.Configuration;
import com.baidu.hd.log.Logger;

/**
 * 嗅探使用的WebView
 */
@TargetApi(8)
class SnifferWebView {

	interface Callback {
		/**
		 * 网页开始加载
		 */
		void onPageStart(String refer);

		/**
		 * 网页加载完成
		 */
		void onPageFinished(String refer);

		/**
		 * 嗅探完成
		 */
		void onComplete(String refer, String url);
	}

	private Logger logger = new Logger("Sniffer");

	/**
	 * 浏览器组件
	 */
	private WebView mWebView = null;

	/**
	 * 跳转组件
	 */
	private RediretPlugin mRedirect = null;

	private RediretPlugin.Callback mRedirectCallback = new RediretPlugin.Callback() {

		@Override
		public void onFinished(String url) {
			logger.d("RediretPlugin.Callback onFinish url=" + url);
			mWebView.loadUrl("javascript:sniffer.setRedirect('" + url + "')");
		}
	};

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			ResultPackage pack = (ResultPackage) msg.obj;
			mCallback.onComplete(pack.getRefer(), pack.getUrl());
		}
	};

	/** 外界回调 */
	private Callback mCallback = null;
	
	public SnifferWebView(WebView webView) {
		mWebView = webView;
		
	}

	/**
	 * 加载url
	 */
	public void loadUrl(String refer) {
		// this.mWebView.resumeTimers();
		this.mWebView.loadUrl(refer);
	}

	/**
	 * 停止加载
	 */
	public void stop() {
		this.mWebView.stopLoading();
		// this.mWebView.pauseTimers();
	}

	/**
	 * 创建实例
	 */
	public void create(Context context, Callback callback) {

		this.mCallback = callback;
//		this.mWebView = new WebView(context);

		WebSettings settings = this.mWebView.getSettings();

		Configuration conf = (Configuration) BaiduHD.cast(context)
				.getServiceFactory().getServiceProvider(Configuration.class);
		settings.setUserAgentString(conf.getSnifferUA());
//		settings.setUserAgentString("Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_2 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8H7 Safari/6533.18.5");

		// js
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(false);

		// cache
		settings.setAppCacheEnabled(false);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

		// plugin
		settings.setPluginsEnabled(false);
		settings.setPluginState(PluginState.OFF);

		// view
		settings.setLoadWithOverviewMode(false);

		// network
		settings.setLoadsImagesAutomatically(false);
		settings.setBlockNetworkImage(true);
		settings.setBlockNetworkLoads(false);

		// data base
		settings.setDatabaseEnabled(true);
		settings.setDomStorageEnabled(true);

		this.mWebView.addJavascriptInterface(new JsInterface(), "Sniffer");

		this.mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String refer) {

				if (mCallback != null) {
					mCallback.onPageFinished(refer);
				}
				super.onPageFinished(view, refer);
			}

			@Override
			public void onPageStarted(WebView view, String refer, Bitmap favicon) {

				if (mCallback != null) {
					mCallback.onPageStart(refer);
				}
				super.onPageStarted(view, refer, favicon);
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
				// logger.d(consoleMessage.message());
				return super.onConsoleMessage(consoleMessage);
			}

			@Override
			public boolean onCreateWindow(WebView view, boolean isDialog,
					boolean isUserGesture, Message resultMsg) {
				// DebugLogger.write("onCreateWindow");
				return true;
			}

			@Override
			public void onGeolocationPermissionsShowPrompt(String origin,
					GeolocationPermissions.Callback callback) {
				// DebugLogger.write("onGeolocationPermissionsShowPrompt");
			}

			@Override
			public boolean onJsBeforeUnload(WebView view, String url,
					String message, JsResult result) {
				// DebugLogger.write("onJsBeforeUnload");
				return true;
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					JsResult result) {
				// DebugLogger.write("onJsAlert");
				return true;
			}

			@Override
			public boolean onJsConfirm(WebView view, String url,
					String message, JsResult result) {
				// DebugLogger.write("onJsConfirm");
				return true;
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message,
					String defaultValue, JsPromptResult result) {
				// DebugLogger.write("onJsPrompt");
				return true;
			}
		});

		this.mRedirect = new RediretPlugin(context);
		this.mRedirect.setCallback(this.mRedirectCallback);
	}

	// ////////////////////js interface////////////////////////
	private class JsInterface {

		@SuppressWarnings("unused")
		public void callback(String refer, String url) {

			if (mCallback != null) {
				mHandler.sendMessage(mHandler.obtainMessage(0,
						new ResultPackage(refer, url)));
			}
		}

		@SuppressWarnings("unused")
		public void debug(String message) {
			logger.d(message);
		}

		@SuppressWarnings("unused")
		public void error(String message) {
			logger.e(message);
		}
		
		@SuppressWarnings("unused")
		public void write(String message) {
			logger.d("write");
			String path = Environment.getExternalStorageDirectory() + "/baidu/test_url/";
			String fileName = "sohu.html";
			FileWriter fileWriter;
			try {
				if (!new File(path).exists()) {
					new File(path).mkdirs();
				}
				File file = new File(path + fileName);
				fileWriter = new FileWriter(file);
				fileWriter.write(message);
				fileWriter.flush();
				fileWriter.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@SuppressWarnings("unused")
		public void fetchRedirect(String url) {
			logger.d("fetchRedirect url=" + url);
			mRedirect.run(url);
		}

		@SuppressWarnings("unused")
		public void stopRedirect() {
			mRedirect.stop();
		}
	}

	private class ResultPackage {

		private String mRefer = "";

		public String getRefer() {
			return mRefer;
		}

		private String mUrl = null;

		public String getUrl() {
			return mUrl;
		}

		public ResultPackage(String url, String value) {
			this.mRefer = url;
			this.mUrl = value;
		}
	}
}
