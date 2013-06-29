package com.baidu.browser.webpool;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.baidu.browser.ui.BaseWebView;

/**
 * @ClassName: BPWebPoolCustomView 
 * @Description: Custom WebView 该函数实现浏览器数据缓存，保证在离线情况下保持前进后退记录
 * @author LEIKANG 
 * @date 2012-12-6 下午2:25:24
 */
public class BPWebPoolCustomView extends BaseWebView {

	/** Load Mode */
	public enum LoadMode {
		/** Normal Load */
		LOAD_NORMAL,
		/** Back or forward Load */
		LOAD_BACKORFORWARD,
		/** Refresh Load */
		LOAD_RELOAD
	}

	/** Page Type */
	public enum PageType {
		/** Web Type */
		PAGETYPE_WEB,
		/** Wap Type */
		PAGETYPE_WAP
	}

	/** History Count */
	private int mHistoryCount;
	/** Last Current Index */
	private int mLastIndex;
	/** Load Url */
	private String mLoadUrl;
	/** Load Mode */
	private LoadMode mLoadMode;
	/** Page Type */
	private PageType mPageType;

	/**
	 * Constructor
	 * @param aWebPoolView
	 * @param aContext
	 */
	public BPWebPoolCustomView(BPWebPoolView aWebPoolView, Context aContext) {
		super(aContext);
	}

	/**
	 * Constructor
	 * @param aWebPoolView
	 * @param aContext
	 * @param aAttrs
	 */
	public BPWebPoolCustomView(BPWebPoolView aWebPoolView, Context aContext, AttributeSet aAttrs) {
		this(aWebPoolView, aContext, aAttrs, 0);
	}

	/**
	 * Constructor
	 * @param aWebPoolView
	 * @param aContext
	 * @param aAttrs
	 * @param aDefStyle
	 */
	public BPWebPoolCustomView(BPWebPoolView aWebPoolView, Context aContext, AttributeSet aAttrs,
			int aDefStyle) {
		super(aContext, aAttrs, aDefStyle);
	}

	@Override
	public boolean drawChild(Canvas canvas, View child, long drawingTime) {
		return super.drawChild(canvas, child, drawingTime);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	/**
	 * Set the history count
	 * @param aHistoryCount
	 */
	public void setHistoryCount(int aHistoryCount) {
		mHistoryCount = aHistoryCount;
	}

	/**
	 * Get the history count
	 * @return History count
	 */
	public int getHistoryCount() {
		return mHistoryCount;
	}

	/**
	 * Set the last index for this webview
	 * @param aIndex
	 */
	public void setLastIndex(int aIndex) {
		mLastIndex = aIndex;
	}

	/**
	 * Get the last index
	 * @return Last index
	 */
	public int getLastIndex() {
		return mLastIndex;
	}

	/**
	 * Set the load url
	 * @param aUrl
	 */
	public void setLoadUrl(String aUrl) {
		mLoadUrl = aUrl;
	}

	/**
	 * Get the load url
	 * @return Load url
	 */
	public String getLoadUrl() {
		return mLoadUrl;
	}

	/**
	 * Set the load Mode
	 * @param aLoadMode
	 */
	public void setLoadMode(LoadMode aLoadMode) {
		mLoadMode = aLoadMode;
	}

	/**
	 * Get the load mode
	 * @return Load mode
	 */
	public LoadMode getLoadMode() {
		return mLoadMode;
	}

	/**
	 * Set the page type
	 * @param aPageType
	 *            Page type
	 */
	public void setPageType(PageType aPageType) {
		mPageType = aPageType;
	}

	/**
	 * Get the page type
	 * @return Page type
	 */
	public PageType getPageType() {
		return mPageType;
	}

}
