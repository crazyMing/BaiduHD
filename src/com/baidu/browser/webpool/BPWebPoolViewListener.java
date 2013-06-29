package com.baidu.browser.webpool;

import android.graphics.Bitmap;

/**
 * @ClassName: BPWebPoolViewListener 
 * @Description: BdWebPoolView监听类
 * @author LEIKANG 
 * @date 2012-12-6 下午2:26:56
 */
public class BPWebPoolViewListener {

	/**
	 * 是否拦截页面加载
	 * 
	 * @param view
	 *            当前加载的BdWebPoolView实例
	 * @param url
	 *            要拦截的Url
	 * @return true表示拦截页面加载，自行处理；false表示不拦截，由内核处理
	 */
	public boolean shouldOverrideUrlLoading(BPWebPoolView view, String url) {
		return false;
	}

	/**
	 * 开始加载页面
	 * 
	 * @param view
	 *            当前加载的BdWebPoolView实例
	 * @param url
	 *            开始加载的Url
	 * @param favicon
	 *            网站favicon图标
	 */
	public void onPageStarted(BPWebPoolView view, String url, Bitmap favicon) {
	}

	/**
	 * 页面加载中
	 * 
	 * @param view
	 *            当前加载的BdWebPoolView实例
	 * @param newProgress
	 *            加载进度
	 */
	public void onProgressChanged(BPWebPoolView view, int newProgress) {
	}

	/**
	 * 收到页面标题
	 * 
	 * @param view
	 *            当前加载的BdWebPoolView实例
	 * @param title
	 *            收到的标题
	 */
	public void onReceivedTitle(BPWebPoolView view, String title) {
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
	 * 页面加载结束
	 * 
	 * @param view
	 *            当前加载的BdWebPoolView实例
	 * @param url
	 *            加载结束的Url
	 */
	public void onPageFinished(BPWebPoolView view, String url) {
	}

	/**
	 * 开始WebView切换
	 * 
	 * @param view
	 *            当前使用的BdWebPoolView实例
	 */
	public void onWebViewSwitchStarted(BPWebPoolView view) {
	}

	/**
	 * 完成WebView切换
	 * 
	 * @param view
	 *            当前使用的BdWebPoolView实例
	 */
	public void onWebViewSwitchFinish(BPWebPoolView view) {
	}

	/**
	 * 后退
	 * 
	 * @param aBackItem
	 *            后退的BdWebPoolBackForwardListItem数据
	 */
	public void onGoBack(BPWebPoolBackForwardListItem aBackItem) {

	}

	/**
	 * 前进
	 * 
	 * @param aForwardItem
	 *            前进的BdWebPoolBackForwardListItem数据
	 */
	public void onGoForward(BPWebPoolBackForwardListItem aForwardItem) {

	}

	/**
	 * 当浏览器状态发生改变时，触发此方法。
	 * 
	 * @param view
	 *            发生改变的主View
	 * @param changeStateMask
	 *            状态改变标志位
	 * @param newValue
	 *            新值
	 * */
	public void onStateChanged(BPWebPoolView view, int changeStateMask, Object newValue) {

	}
}
