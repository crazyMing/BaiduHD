package com.baidu.hd.sniffer;

import android.webkit.WebView;

import com.baidu.hd.service.ServiceProvider;
import com.baidu.hd.sniffer.BigSiteSnifferResult.BigSiteAlbumResult;

/**
 * ��̽��Сվ����Ƶ���ӵĹ���
 */
public interface Sniffer extends ServiceProvider {

	public interface BigSiteCallback {
		/** url ��Ƶ���� */
		void onComplete(String refer, String url, BigSiteAlbumResult result);
		void onCancel(String refer);
	}
	
	public interface SmallSiteCallback {
		void onComplete(String refer, SmallSiteUrl url);
		void onCancel(String refer);
	}
	
	/** ������վ��̽ʵ�� */
	SnifferEntity createBig(BigSiteCallback callback, WebView webView);
	
	/** ����M3U8��̽ʵ�� */
	SnifferEntity createM3U8Entity(BigSiteCallback callback, WebView webView);
	
	/** ����Сվ��̽ʵ�� */
	SnifferEntity createSmall(SmallSiteCallback callback, WebView webView);
}
