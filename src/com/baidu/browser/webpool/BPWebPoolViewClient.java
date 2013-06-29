package com.baidu.browser.webpool;

import android.graphics.Bitmap;
import android.webkit.WebView;

/**
 * @ClassName: BPWebPoolViewClient 
 * @Description: WebPoolViewClient
 * @author LEIKANG 
 * @date 2012-12-6 ÏÂÎç2:26:41
 */
public class BPWebPoolViewClient {

	/**
	 * Give the host application a chance to take over the control when a new
	 * url is about to be loaded in the current WebView. If WebViewClient is not
	 * provided, by default WebView will ask Activity Manager to choose the
	 * proper handler for the url. If WebViewClient is provided, return true
	 * means the host application handles the url, while return false means the
	 * current WebView handles the url.
	 * 
	 * @param view
	 *            The WebView that is initiating the callback.
	 * @param url
	 *            The url to be loaded.
	 * @return True if the host application wants to leave the current WebView
	 *         and handle the url itself, otherwise return false.
	 */
	public boolean shouldOverrideUrlLoading(BPWebPoolView view, String url) {
		return false;
	}

	/**
	 * Notify the host application that a page has started loading. This method
	 * is called once for each main frame load so a page with iframes or
	 * framesets will call onPageStarted one time for the main frame. This also
	 * means that onPageStarted will not be called when the contents of an
	 * embedded frame changes, i.e. clicking a link whose target is an iframe.
	 * 
	 * @param view
	 *            The WebView that is initiating the callback.
	 * @param url
	 *            The url to be loaded.
	 * @param favicon
	 *            The favicon for this page if it already exists in the
	 *            database.
	 */
	public void onPageStarted(BPWebPoolView view, String url, Bitmap favicon) {
	}

	/**
	 * Notify the host application that a page has finished loading. This method
	 * is called only for main frame. When onPageFinished() is called, the
	 * rendering picture may not be updated yet. To get the notification for the
	 * new Picture, use {@link WebView.PictureListener#onNewPicture}.
	 * 
	 * @param view
	 *            The WebView that is initiating the callback.
	 * @param url
	 *            The url of the page.
	 */
	public void onPageFinished(BPWebPoolView view, String url) {
	}

	/**
	 * Notify the host application that the WebView will load the resource
	 * specified by the given url.
	 * 
	 * @param view
	 *            The WebView that is initiating the callback.
	 * @param url
	 *            The url of the resource the WebView will load.
	 */
	public void onLoadResource(BPWebPoolView view, String url) {
	}

	/**
	 * Report an error to the host application. These errors are unrecoverable
	 * (i.e. the main resource is unavailable). The errorCode parameter
	 * corresponds to one of the ERROR_* constants.
	 * 
	 * @param view
	 *            The WebView that is initiating the callback.
	 * @param errorCode
	 *            The error code corresponding to an ERROR_* value.
	 * @param description
	 *            A String describing the error.
	 * @param failingUrl
	 *            The url that failed to load.
	 */
	public void onReceivedError(BPWebPoolView view, int errorCode, String description, String failingUrl) {
	}

	/**
	 * Notify the host application that the WebView has go back
	 * 
	 * @param aItem
	 *            BdWebPoolBackForwardListItem
	 */
	public void onGoBack(BPWebPoolBackForwardListItem aItem) {

	}

	/**
	 * Notify the host application that the WebView has go back
	 * 
	 * @param aItem
	 *            BdWebPoolBackForwardListItem
	 */
	public void onGoForward(BPWebPoolBackForwardListItem aItem) {

	}

	/**
	 * @param view
	 *            BdWebPoolViewÊµÀý
	 */
	public void onReload(BPWebPoolView view) {
	}

	/**
	 * @param view
	 *            BdWebPoolViewÊµÀý
	 */
	public void onWebViewChanged(BPWebPoolView view) {
	}
	
}
