package com.baidu.hd.test;

import android.os.Bundle;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.web.UrlHandler;
import com.baidu.hd.R;

public class HomeActivity extends BaseActivity {

	private UrlHandler mUrlHandler = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_home);

		super.setBackExitFlag(false);
		
		mUrlHandler = new UrlHandler(this, getPlayerApp().getServiceFactory());

		final WebView webView = (WebView) this.findViewById(R.id.web_view);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebChromeClient(new WebChromeClient() {

			@Override
			public boolean onJsPrompt(WebView view, String url, String message,
					String defaultValue, JsPromptResult result) {

				UrlHandler.Result jsResult = new UrlHandler.Result();
				if (mUrlHandler.handleUrl(message, defaultValue, jsResult)) {
					result.confirm(jsResult.getResult());
					mUrlHandler.reset();
					return true;
				}
				return super.onJsPrompt(view, url, message, defaultValue,
						result);
			}
		});
		webView.loadUrl("http://fsreport.p2sp.baidu.com/android/home.html");
		//webView.loadUrl("file:///android_asset/test/home.html");
	}
}
