package com.baidu.hd.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 */
public class HttpWeb {

	public void fetch(String url, Context context, HttpWebCallback callback) {
		
		final String fUrl = url;
		final HttpWebCallback fCallback = callback;
		WebView webView = new WebView(context);
		WebSettings settings = webView.getSettings();
		
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

		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				fCallback.onImage(fUrl, favicon);
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				fCallback.onStop(fUrl);
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
		});

		webView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onReceivedIcon(WebView view, Bitmap icon) {
				fCallback.onImage(fUrl, icon);
				super.onReceivedIcon(view, icon);
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				fCallback.onTitle(fUrl, title);
				super.onReceivedTitle(view, title);
			}

			@Override
			public boolean onCreateWindow(WebView view, boolean isDialog,
					boolean isUserGesture, Message resultMsg) {
				return true;
			}

			@Override
			public void onGeolocationPermissionsShowPrompt(String origin,
					GeolocationPermissions.Callback callback) {
			}

			@Override
			public boolean onJsBeforeUnload(WebView view, String url,
					String message, JsResult result) {
				return true;
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					JsResult result) {
				return true;
			}

			@Override
			public boolean onJsConfirm(WebView view, String url,
					String message, JsResult result) {
				return true;
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message,
					String defaultValue, JsPromptResult result) {
				return true;
			}
		});

		webView.loadUrl(url);

		new Handler() {

			@Override
			public void handleMessage(Message msg) {
				fCallback.onStop(fUrl);
			}
			
		}.sendEmptyMessageDelayed(0, 20000);
	}
}
