 
package com.baidu.browser.explore;

import android.graphics.Bitmap;
import android.os.Message;
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback;

import com.baidu.browser.ui.BaseWebView;
import com.baidu.browser.webpool.BPWebPoolChromeClient;
import com.baidu.browser.webpool.BPWebPoolView;

/**
 * @ClassName: BPExploreChromeClient 
 * @Description: 辅助WebView处理Javascript的对话框，网站图标，网站title，加载进度等
 * @author LEIKANG 
 * @date 2012-12-6 下午2:17:29
 */
public class BPExploreChromeClient extends BPWebPoolChromeClient {

	@Override
	public void onProgressChanged(BPWebPoolView view, int newProgress) {
	}

	@Override
	public void onReceivedTitle(BPWebPoolView view, String title) {
	}

	@Override
	public void onReceivedIcon(BPWebPoolView view, Bitmap icon) {
	}

	@Override
	public void onReceivedTouchIconUrl(BPWebPoolView view, String url, boolean precomposed) {
	}

	@Override
	public void onShowCustomView(View view, CustomViewCallback callback) {
	}

	@Override
	public void onHideCustomView() {
	}

	@Override
	public boolean onCreateWindow(BPWebPoolView view, boolean dialog, boolean userGesture, Message resultMsg,
			BaseWebView.WebViewTransport bdTransport) {
		return false;
	}

	@Override
	public void onRequestFocus(BPWebPoolView view) {
	}

	@Override
	public void onCloseWindow(BPWebPoolView window) {
	}

}
