package com.baidu.browser.explore;

import android.graphics.Bitmap;

import com.baidu.browser.webpool.BPWebPoolView;
import com.baidu.browser.webpool.BPWebPoolViewClient;

/**
 * @ClassName: BPExploreViewClient 
 * @Description: 
 * @author LEIKANG 
 * @date 2012-12-6 ÏÂÎç2:23:02
 */
public class BPExploreViewClient extends BPWebPoolViewClient {

	@Override
	public boolean shouldOverrideUrlLoading(BPWebPoolView view, String url) {
		return false;
	}

	@Override
	public void onPageStarted(BPWebPoolView view, String url, Bitmap favicon) {
	}

	@Override
	public void onPageFinished(BPWebPoolView view, String url) {
	}

	@Override
	public void onLoadResource(BPWebPoolView view, String url) {
	}

	@Override
	public void onReceivedError(BPWebPoolView view, int errorCode, String description, String failingUrl) {
	}

}
