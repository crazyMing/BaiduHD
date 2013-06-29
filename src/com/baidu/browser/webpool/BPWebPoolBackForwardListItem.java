package com.baidu.browser.webpool;

import android.os.Bundle;
import android.webkit.WebView;

import com.baidu.browser.ui.BaseWebView;

/**
 * @ClassName: BPWebPoolBackForwardListItem 
 * @Description: ǰ�������б�Ԫ�� 
 * @author LEIKANG 
 * @date 2012-12-6 ����2:24:50
 */
public class BPWebPoolBackForwardListItem {

	/** Url��ַ */
	private String mUrl;
	/** ���� */
	private String mTitle;
	/** Ԫ������WebView */
	private BaseWebView mWebView;
	/** Ԫ����ǰ�������б��е�λ�� */
	private int mIndex;
	/** ҳ�����״̬ */
	private LoadStatus mLoadStatus;
	/** �û��������� */
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
	 * @return ���ӵ�ַ
	 */
	public String getUrl() {
		return mUrl;
	}

	/**
	 * @param aUrl
	 *            �������ӵ�ַ
	 */
	public void setUrl(String aUrl) {
		mUrl = aUrl;
	}

	/**
	 * @return ����
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * @param aTitle
	 *            ���ñ���
	 */
	public void setTitle(String aTitle) {
		mTitle = aTitle;
	}

	/**
	 * @return ����WebView
	 */
	public WebView getWebView() {
		return mWebView;
	}

	/**
	 * @param aWebView
	 *            ��������WebView
	 */
	public void setWebView(BaseWebView aWebView) {
		mWebView = aWebView;
	}

	/**
	 * @return ��ȡ��Ԫ�ض�Ӧ��ǰ�������б�λ��
	 */
	public int getIndex() {
		return mIndex;
	}

	/**
	 * @param aIndex
	 *            ���ø�Ԫ�ض�Ӧ��ǰ�������б�λ��
	 */
	public void setIndex(int aIndex) {
		mIndex = aIndex;
	}

	/**
	 * @return ��ȡ�û�����
	 */
	public Bundle getExtras() {
		return mBundle;
	}

	/**
	 * @param aBundle
	 *            �����û�����
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
