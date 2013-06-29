package com.baidu.hd.sniffer;

import android.webkit.WebView;

import com.baidu.hd.service.ServiceProvider;
import com.baidu.hd.sniffer.BigSiteSnifferResult.BigSiteAlbumResult;

/**
 * 嗅探大小站视视频链接的工厂
 */
public interface Sniffer extends ServiceProvider {

	public interface BigSiteCallback {
		/** url 视频链接 */
		void onComplete(String refer, String url, BigSiteAlbumResult result);
		void onCancel(String refer);
	}
	
	public interface SmallSiteCallback {
		void onComplete(String refer, SmallSiteUrl url);
		void onCancel(String refer);
	}
	
	/** 创建大站嗅探实体 */
	SnifferEntity createBig(BigSiteCallback callback, WebView webView);
	
	/** 创建M3U8嗅探实体 */
	SnifferEntity createM3U8Entity(BigSiteCallback callback, WebView webView);
	
	/** 创建小站嗅探实体 */
	SnifferEntity createSmall(SmallSiteCallback callback, WebView webView);
}
