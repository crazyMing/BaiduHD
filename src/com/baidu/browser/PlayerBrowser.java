package com.baidu.browser;

import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.webkit.ValueCallback;

import com.baidu.browser.BPBrowser.BrowserListener;

/**
 * @ClassName: PlayerBrowser 
 * @Description: 百度HDUI界面，包括HOME 和 Browser 
 * @author LEIKANG 
 * @date 2012-12-10 下午1:04:51
 */
public class PlayerBrowser extends Fragment implements BrowserListener{
	
	
	/**Fragment tag.*/
    public static final String FRAGMENT_TAG = "PlayerBrowser";

	@Override
	public void onGoHome() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAddAsBookmark(String title, String url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSwitchToMultiWinodow() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOpenFromBrowser(String aTitle, String aUrl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBrowserStateChanged(int stateMask, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClickVoiceSearch() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDownloadStart(String url, String userAgent,
			String contentDisposition, String mimetype, long contentLength) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDownloadStartNoStream(String url, String userAgent,
			String contentDisposition, String mimetype, long contentLength) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSelectionSearch(String aSelection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSelectBookmarkPopMenu(String title, String url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProtocolSearch(String aSelection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Bundle getSearchboxBundle(boolean withKeyword) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle onTabChangeFinished(Bundle aBundle) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openFileChooser(ValueCallback<Uri> uploadMsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Message onRequestCopyHref() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDismissPopMenu() {
		// TODO Auto-generated method stub
		
	}
}
