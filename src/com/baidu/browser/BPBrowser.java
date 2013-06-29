package com.baidu.browser;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;

import com.baidu.browser.framework.BPFrameView;
import com.baidu.browser.framework.BPWindow;
import com.baidu.browser.webpool.BPWebPoolView;
import com.baidu.hd.VoiceSearchActivity;
import com.baidu.hd.module.IntentConstants;
import com.baidu.hd.R;

/**
 * @ClassName: BPBrowser 
 * @Description:   
 * @author LEIKANG 
 * @date 2012-12-5 3:10:14
 */
public class BPBrowser extends Fragment{
	
    public static final String FRAGMENT_TAG = "BPBrowser";
    
    public static final String HOME_PAGE = "http://www.baidu.com/";
    
    public static final String HOME_NAT_PAGE = "file:///android_asset/htmls/home.html";
    
	public static final int PROGRESS_MIN = 10;

	public static final int PROGRESS_MAX = 100;
	
	public static final int STATE_PAGE_STARTED = 0x01;

	public static final int STATE_PAGE_FINISHED = 0x02;

	public static final int STATE_PROGRESS_CHANGED = 0x03;
	
	private BPFrameView mFrameView;
	
	private BrowserListener mListener;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initInflate();
		mFrameView.restoreFromBundle(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    mFrameView.saveStateToBundle(outState);
	    super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        
		initInflate();
		if (mFrameView != null) {
			mFrameView.onResume();
		}
	}
	
	@Override
	public void onPause() {
		if (mFrameView != null) {
			mFrameView.closeSelectedMenu();
			mFrameView.onPause();
		}
        super.onPause();
	}
	
	public void onDestroy() {
		freeMemory();
		if (mFrameView != null) {
			mFrameView.release();
			mFrameView = null;
			}
		
		super.onDestroy();
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (mFrameView != null) {
			mFrameView.freeMemory();
		}
	}
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	    View view = getView();
	    if (view == null) {
	        view = getRootView();
	        ViewGroup parent = (ViewGroup) view.getParent();
	        if (parent != null) {
	            parent.removeView(view);
	        }
	    }
	    return view;
	}
	
	
	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mFrameView != null) {
			mFrameView.onActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * @Title: scrollBy 
	 * @Description:
	 * @param y   
	 */
	public void scrollBy(int x, int y) {
		initInflate();
		mFrameView.webviewScrollBy(x, y);
	}
	
	/**
	 * @Title: scrollTo 
	 * @Description:
	 * @param y   
	 */
	public void scrollTo(int x, int y) {
		initInflate();
		mFrameView.webviewScrollTo(x, y);
	}
	
	/**
	 * @Title: addWebViewTitle 
	 * @Description:
	 * @param aView   
	 */
	public void addWebViewTitle(View aView) {
		initInflate();
		mFrameView.addWebViewTitle(aView);
	}
	
	/**
	 * @Title: setmListener 
	 * @Description:
	 */
	public void setmListener(BrowserListener aListener) {
		this.mListener = aListener;
	}
	
	/**
	 * @Title: initInflate
	 */
	public void initInflate() {
		if (mFrameView == null) {
		    Activity activity = getActivity();
		    mFrameView = (BPFrameView) activity.findViewById(R.id.bdframeview_id);
		    if (mFrameView == null) {
		        mFrameView = new BPFrameView(activity);
		        mFrameView.setId(R.id.bdframeview_id);
		        mFrameView.setBrowser(this);
		    }
		}
	}
	
	/**
	 * @Title: freeMemory
	 */
	public void freeMemory() {
		if (mFrameView != null) {
			mFrameView.freeMemory();
		}
	}
	
	/**
	 * 
	 * @Title: onKeyDown
	 * @throws
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mFrameView != null) {
			if (mFrameView.onKeyDown(keyCode, event)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @Title: onKeyUp
	 * @throws
	 */
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (mFrameView != null) {
            if (mFrameView.onKeyUp(keyCode, event)) {
                return true;
            }
        }
	    return false;
	}
 
	/**
	 * @Title: goBack
	 * @throws
	 */
	public void goBack() {
		initInflate();
		mFrameView.goBack();
	}
    
	/**
	 * @Title: goForward
	 * @throws
	 */
	public void goForward() {
		initInflate();
		mFrameView.goForward();
	}
	
	/**
	 * @Title: canGoForward
	 * @throws
	 */
	public boolean canGoForward() {
		initInflate();
		return mFrameView.canGoForward();
	}
	
	/**
	 * @Title: canGoBack
	 * @throws
	 */
	public boolean canGoBack() {
		initInflate();
		return mFrameView.canGoBack();
	}
	
	/**
	 * @Title: loadUrl
	 * @throws
	 */
	public void loadUrl(String url) {
		initInflate();
		mFrameView.loadUrl(url);
	}
	
	/**
	 * @Title: loadUrlFromHome
	 * @throws
	 */
	public void loadUrlFromHome(String url, boolean isOpenBackground) {
		initInflate();
		
		BPWindow current = mFrameView.getCurrentWindow();
        if (!isOpenBackground) {
            if (current != null) {
                current.loadUrl(url);
            }
        } else {
            mFrameView.createNewWindowOpenUrl(url, current, false, null);
        }
	}
	
	/**
	 * @Title: reload
	 * @throws
	 */
	public void reload() {
		initInflate();
		mFrameView.reload();
	}
	
	/**
	 * @Title: stopLoading
	 * @throws
	 */
	public void stopLoading() {
		initInflate();
		mFrameView.stopLoading();
	}
	
	/**
	 * @Title: clearHistory
	 * @throws
	 */
	public void clearHistory() {
		if (mFrameView != null) {
			mFrameView.clearHistory();
		}
	}
	
	/**
	 * @Title: getWindowList
	 */
	public List<BPWindow> getWindowList() {
	    if (mFrameView != null) {
	        return mFrameView.getWindowList();
	    }
	    return null;
	}
	
	/**
	 * @Title: getRootView
	 * @return View
	 */
	public View getRootView() {
		initInflate();
		return mFrameView;
	}
	
	/**
	 * @Title: getCurrentWindow
	 */
    public BPWindow getCurrentWindow() {
        initInflate();
        return mFrameView.getCurrentWindow();
    }
	
	/**
	 * @Title: setUpSelect
	 * @throws
	 */
	public void setUpSelect() {
		mFrameView.setUpSelect();
	}

	public boolean handleUrl(BPWebPoolView view, String url) {
		return false;
	}

	public void pageStateChanged(int statePageStarted, String url) {
		if (mListener != null) {
			mListener.onBrowserStateChanged(statePageStarted, url);
		}		
	}

	public void onGoHome() {
		if (mListener != null) {
			mListener.onGoHome();
		}
	}

    public void onAddAsBookmark(String title, String url) {
        if (mListener != null) {
            mListener.onAddAsBookmark(title, url);
        }
    }

	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
			long contentLength) {
		if (mListener != null) {
			mListener.onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength);
		}
	}

	public void onDownloadStartNoStream(String url, String userAgent, String contentDisposition,
			String mimetype, long contentLength) {
		if (mListener != null) {
			mListener.onDownloadStartNoStream(url, userAgent, contentDisposition, mimetype, contentLength);
		}
	}

	public void onSelectionSearch(String aSelection) {
		if (mListener != null) {
			mListener.onSelectionSearch(aSelection);
		}
	}

	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
		if (mListener != null) {
			mListener.openFileChooser(uploadMsg, acceptType);
		}
	}
	
	public void openFileChooser(ValueCallback<Uri> uploadMsg) { // SUPPRESS CHECKSTYLE
		if (mListener != null) {
			mListener.openFileChooser(uploadMsg);
		}
	}

	public interface BrowserListener {


		void onGoHome();

		void onAddAsBookmark(String title, String url);


		void onSwitchToMultiWinodow();


		void onOpenFromBrowser(String aTitle, String aUrl);


		void onBrowserStateChanged(int stateMask, Object newValue);


		void onClickVoiceSearch();


		void onDownloadStart(String url, String userAgent,
				String contentDisposition, String mimetype, long contentLength);


		void onDownloadStartNoStream(String url, String userAgent,
				String contentDisposition, String mimetype, long contentLength);


		void onSelectionSearch(String aSelection);


		void onSelectBookmarkPopMenu(String title, String url);


		void onProtocolSearch(String aSelection);


		Bundle getSearchboxBundle(boolean withKeyword);


		Bundle onTabChangeFinished(Bundle aBundle);

		// For Android 3.0+
		void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType); // SUPPRESS

		// For Android < 3.0
		void openFileChooser(ValueCallback<Uri> uploadMsg); // SUPPRESS

		Message onRequestCopyHref();  

		void onDismissPopMenu();
	}

	public void initFromIntent(Intent intent) {
		String action = intent.getAction();
		String url = intent.getStringExtra(SearchManager.TAG_KEY_URL);
		if (IntentConstants.ACTION_BROWSER.equals(action)) {
			loadUrl(url);
		}
	}
	
	public void handleVoiceSearch(int voiceFrom, String[] sug) {
	    if (mFrameView != null) {
	        mFrameView.handleVoiceSearch(voiceFrom,sug);
	    }
	}
    
}
