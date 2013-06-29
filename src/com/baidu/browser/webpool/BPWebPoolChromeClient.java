package com.baidu.browser.webpool;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient.CustomViewCallback;

import com.baidu.browser.ui.BaseWebView;

/**
 * @ClassName: BPWebPoolChromeClient 
 * @Description: 辅助WebView处理Javascript的对话框，网站图标，网站title，加载进度等 
 * @author LEIKANG 
 * @date 2012-12-6 下午1:00:30
 */
public class BPWebPoolChromeClient {

	/**
	 * Tell the host application the current progress of loading a page.
	 * 
	 * @param view
	 *            The WebView that initiated the callback.
	 * @param newProgress
	 *            Current page loading progress, represented by an integer
	 *            between 0 and 100.
	 */
	public void onProgressChanged(BPWebPoolView view, int newProgress) {
	}

	/**
	 * Notify the host application of a change in the document title.
	 * 
	 * @param view
	 *            The WebView that initiated the callback.
	 * @param title
	 *            A String containing the new title of the document.
	 */
	public void onReceivedTitle(BPWebPoolView view, String title) {
	}

	/**
	 * Notify the host application of a new favicon for the current page.
	 * 
	 * @param view
	 *            The WebView that initiated the callback.
	 * @param icon
	 *            A Bitmap containing the favicon for the current page.
	 */
	public void onReceivedIcon(BPWebPoolView view, Bitmap icon) {
	}

	/**
	 * Notify the host application of the url for an apple-touch-icon.
	 * 
	 * @param view
	 *            The WebView that initiated the callback.
	 * @param url
	 *            The icon url.
	 * @param precomposed
	 *            True if the url is for a precomposed touch icon.
	 */
	public void onReceivedTouchIconUrl(BPWebPoolView view, String url, boolean precomposed) {
	}

	/**
	 * Notify the host application that the current page would like to show a
	 * custom View.
	 * 
	 * @param view
	 *            is the View object to be shown.
	 * @param callback
	 *            is the callback to be invoked if and when the view is
	 *            dismissed.
	 */
	public void onShowCustomView(View view, CustomViewCallback callback) {
	}

	/**
	 * Notify the host application that the current page would like to hide its
	 * custom view.
	 */
	public void onHideCustomView() {
	}

	/**
	 * Request the host application to create a new Webview. The host
	 * application should handle placement of the new WebView in the view
	 * system. The default behavior returns null.
	 * 
	 * @param view
	 *            The WebView that initiated the callback.
	 * @param dialog
	 *            True if the new window is meant to be a small dialog window.
	 * @param userGesture
	 *            True if the request was initiated by a user gesture such as
	 *            clicking a link.
	 * @param resultMsg
	 *            The message to send when done creating a new WebView. Set the
	 *            new WebView through resultMsg.obj which is
	 *            WebView.WebViewTransport() and then call
	 *            resultMsg.sendToTarget();
	 * @param bdTransport
	 *            BdWebViewTransport
	 * @return Similar to javscript dialogs, this method should return true if
	 *         the client is going to handle creating a new WebView. Note that
	 *         the WebView will halt processing if this method returns true so
	 *         make sure to call resultMsg.sendToTarget(). It is undefined
	 *         behavior to call resultMsg.sendToTarget() after returning false
	 *         from this method.
	 */
	public boolean onCreateWindow(BPWebPoolView view, boolean dialog, boolean userGesture, Message resultMsg,
			BaseWebView.WebViewTransport bdTransport) {
		return false;
	}

	/**
	 * Request display and focus for this WebView. This may happen due to
	 * another WebView opening a link in this WebView and requesting that this
	 * WebView be displayed.
	 * 
	 * @param view
	 *            The WebView that needs to be focused.
	 */
	public void onRequestFocus(BPWebPoolView view) {
	}

	/**
	 * Notify the host application to close the given WebView and remove it from
	 * the view system if necessary. At this point, WebCore has stopped any
	 * loading in this window and has removed any cross-scripting ability in
	 * javascript.
	 * 
	 * @param window
	 *            The WebView that needs to be closed.
	 */
	public void onCloseWindow(BPWebPoolView window) {
	}

	// For Android 3.0+
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) { 
	}

	// For Android < 3.0
	public void openFileChooser(ValueCallback<Uri> uploadMsg) { 
	}
	
	/**
	 * Instructs the client to show a prompt to ask the user to set the
	 * Geolocation permission state for the specified origin.
	 */
	public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) { 
		
	}
	
	/**
	 * Instructs the client to hide the Geolocation permissions prompt.
	 */
	public void onGeolocationPermissionsHidePrompt() {
		
	}

	
	public void getDefaultVideoPoster() {
		
	}
}
