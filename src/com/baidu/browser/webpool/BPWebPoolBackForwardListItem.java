package com.baidu.browser.webpool;

import android.os.Bundle;
import android.webkit.WebView;

import com.baidu.browser.ui.BaseWebView;

/**
 * @ClassName: BPWebPoolBackForwardListItem 
 * @Description: 前进后退列表元素 
 * @author LEIKANG 
 * @date 2012-12-6 下午2:24:50
 */
public class BPWebPoolBackForwardListItem {

	/** Url地址 */
	private String mUrl;
	/** 标题 */
	private String mTitle;
	/** 元素所在WebView */
	private BaseWebView mWebView;
	/** 元素在前进后退列表中的位置 */
	private int mIndex;
	/** 页面加载状态 */
	private LoadStatus mLoadStatus;
	/** 用户储存数据 */
	private Bundle mBundle;

	/** Load Status */
	public enum LoadStatus {
		/** Normal Status */
		STATUS_NORMAL,
		/** Error Status */
		STATUS_ERROR,
	}
	
	/**
	 * Constructor
	 */
	public BPWebPoolBackForwardListItem() {
	}

	/**
	 * @return 链接地址
	 */
	public String getUrl() {
		return mUrl;
	}

	/**
	 * @param aUrl
	 *            设置链接地址
	 */
	public void setUrl(String aUrl) {
		mUrl = aUrl;
	}

	/**
	 * @return 标题
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * @param aTitle
	 *            设置标题
	 */
	public void setTitle(String aTitle) {
		mTitle = aTitle;
	}

	/**
	 * @return 所在WebView
	 */
	public WebView getWebView() {
		return mWebView;
	}

	/**
	 * @param aWebView
	 *            设置所在WebView
	 */
	public void setWebView(BaseWebView aWebView) {
		mWebView = aWebView;
	}

	/**
	 * @return 获取该元素对应的前进后退列表位置
	 */
	public int getIndex() {
		return mIndex;
	}

	/**
	 * @param aIndex
	 *            设置该元素对应的前进后退列表位置
	 */
	public void setIndex(int aIndex) {
		mIndex = aIndex;
	}

	/**
	 * @return 获取用户数据
	 */
	public Bundle getExtras() {
		return mBundle;
	}

	/**
	 * @param aBundle
	 *            设置用户数据
	 */
	public void putExtras(Bundle aBundle) {
		mBundle = aBundle;
	}

	/**
	 * @return the mLoadStatus
	 */
	public LoadStatus getLoadStatus() {
		return mLoadStatus;
	}

	/**
	 * @param mLoadStatus the mLoadStatus to set
	 */
	public void setLoadStatus(LoadStatus aLoadStatus) {
		mLoadStatus = aLoadStatus;
	}

}
