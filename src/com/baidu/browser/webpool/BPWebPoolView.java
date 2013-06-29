package com.baidu.browser.webpool;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebHistoryItem;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.baidu.browser.BPBrowser;
import com.baidu.browser.core.util.BdLog;
import com.baidu.browser.ui.BaseWebView;
import com.baidu.browser.ui.BaseWebView.ActivityNotStartedException;
import com.baidu.browser.ui.BaseWebView.BaseWebViewClient;
import com.baidu.browser.visitesite.VisiteSite;
import com.baidu.browser.visitesite.VisiteSiteManager;
import com.baidu.browser.webpool.BPErrorView.BPErrorViewListener;
import com.baidu.browser.webpool.BPWebPoolCustomView.LoadMode;
import com.baidu.hd.BaseActivity;
import com.baidu.hd.util.Utility;
import com.baidu.hd.R;
/**
 * @ClassName: BPWebPoolView 
 * @Description: BdWebPoolView 
 * @author LEIKANG 
 * @date 2012-12-6 下午2:26:09
 */
public class BPWebPoolView extends FrameLayout implements BPErrorViewListener, OnLongClickListener {

	/** DEBUG mode */
	public static final boolean DEBUG = false;

	/** 默认池的最大size */
	public static final int DEFAULT_MAX_POOL_SIZE = 4;

	/** 标识WebView正在点击的链接 */
	public static final String BUNDLE_CLINK_MODE = "CLINK_MODE";

	/** 表示从页面内点击加载 */
	private static final byte CLICK_FROM_PAGE = 1;

	/** 标识从页面外点击加载 */
	private static final byte CLICK_FROM_OUTSIDE_PAGE = 2;

	/** 非wap1.0页面类型 */
	public static final byte PAGE_TYPE_NORMAL = 1;

	/** wap1.0页面类型 */
	public static final byte PAGE_TYPE_WAP10 = 2;

	/** 页面类型，目前包括wap1.0和其它类型 */
	public static final String BUNDLE_PAGE_TYPE = "PAGE_TYPE";

	/** 准备加载浏览页。 */
	public static final int STATE_PAGE_STARTED = 0x01;
	/** 加载浏览页的进度。 */
	public static final int STATE_PROGRESS_CHANGED = 0x02;
	/** 浏览页加载完毕。 */
	public static final int STATE_PAGE_FINISHED = 0x04;
	/** 浏览页开始接收。 */
	public static final int STATE_PAGE_RECEIVED = 0x08;
	/** 页面开始显示。 */
	public static final int STATE_START_SHOW = 0x10;
	/** 收到错误。 */
	public static final int STATE_RECEIVE_ERROR = 0x20;

	/** 尚未显示。 */
	public static final int NOT_SHOW = 0;
	/** 开始显示。 */
	public static final int START_SHOW = 1;
	/** 完成显示。 */
	public static final int HAS_SHOWN = 2;

	/** 进度条最小值。 */
	public static final int PROGRESS_MIN = 10;
	/** 进度条最大值。 */
	public static final int PROGRESS_MAX = 100;
	/** 延时。 */
	public static final int DELAYED_TIME = 200;

	/** 用来标识页面的显示状态。 */
	private int mShowState;

	/** BdWebPoolCustomView List */
	private List<BPWebPoolCustomView> mWebViewList;
	/** BdWebPoolViewClient */
	private BPWebPoolViewClient mWebPoolViewClient;
	/** BdWebPoolChromeClient */
	private BPWebPoolChromeClient mWebPoolChromeClient;

	/** 当前的BdWebPoolCustomView */
	private BPWebPoolCustomView mCurWebView;

	/** 池的最大size */
	private int mMaxPoolSize = DEFAULT_MAX_POOL_SIZE;
	
	/** 多View的遮罩View */
	private BPWebPoolMaskView mMaskView;

	/** WebView设置 */
	private WebSettings mSettings;

	/** Attached Javascript interfaces */
	private Map<String, Object> mJsItems;

	/** 回调通知上层的底层BdWebPoolCustomView是否是当前的BdWebPoolCustomView */
	private boolean mCurWebViewNotify;

	/** 错误页对照表。 */
	private HashMap<Integer, Integer> mErrorCodeList = new HashMap<Integer, Integer>();

	/** 显示的错误页。 */
	private BPErrorView mErrorView;

	/** 下载监听器 */
	private DownloadListener mDownloadListener;

	/**访问的站点。只用一个实例，以方便只对部分内容（比如title）更新。*/
    private VisiteSite mVisitedSite = new VisiteSite();
    
    /** webview custom view.  */
    private View mCustomView;
    
    /** custom view 对应的全屏 container. */
    protected FrameLayout mFullscreenContainer;
    
    /** 全屏之前activity的 orientation. */
    private int mOriginalOrientation;
    
    /** customview callback. */
    private CustomViewCallback mCustomViewCallback;
    
    private VisiteSiteManager mVisiteSiteManager;
    
    /** custom view layout param. */
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
        new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);

    
	/**
	 * Constructor
	 * 
	 * @param aContext
	 *            Context
	 */
	public BPWebPoolView(Context aContext) {
		this(aContext, null);
	}

	/**
	 * Constructor
	 * 
	 * @param aContext
	 *            Context
	 * @param aAttrs
	 *            AttributeSet
	 */
	public BPWebPoolView(Context aContext, AttributeSet aAttrs) {
		this(aContext, aAttrs, 0);
	}

	/**
	 * Constructor
	 * 
	 * @param aContext
	 *            Context
	 * @param aAttrs
	 *            AttributeSet
	 * @param aDefStyle
	 *            DefaultStyle
	 */
	public BPWebPoolView(Context aContext, AttributeSet aAttrs, int aDefStyle) {
		this(aContext, null, 0, DEFAULT_MAX_POOL_SIZE);
	}

	/**
	 * @param aMaxPoolSize
	 *            池的最大size
	 * @param aContext
	 *            Context
	 * @param aAttrs
	 *            AttributeSet
	 * @param aDefStyle
	 *            DefaultStyle
	 */
	public BPWebPoolView(Context aContext, AttributeSet aAttrs, int aDefStyle, int aMaxPoolSize) {
		super(aContext, aAttrs, aDefStyle);
		WebIconDatabase.getInstance().open(aContext.getDir("icons", Context.MODE_PRIVATE).getPath());
		mVisiteSiteManager = (VisiteSiteManager)((BaseActivity)aContext).getServiceProvider(VisiteSiteManager.class);
		setBackgroundColor(Color.WHITE);

		mMaxPoolSize = aMaxPoolSize;
		mWebViewList = new ArrayList<BPWebPoolCustomView>(DEFAULT_MAX_POOL_SIZE);
		
		// 初始化BdWebPoolView的BdWebSettings
		BPWebPoolCustomView initWebView = getFreeWebView();
		WebSettings settings = initWebView.getSettings();
		
		mSettings = settings;

		// 添加前进后退的MaskView
		mMaskView = new BPWebPoolMaskView(aContext);
		mMaskView.setBackgroundColor(Color.WHITE);
		mMaskView.setVisibility(GONE);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		addView(mMaskView, params);
	}

	/**
	 * Set the WebViewClient that will receive various notifications and
	 * requests. This will replace the current handler.
	 * 
	 * @param aClient
	 *            An implementation of BdWebPoolViewClient.
	 */
	public void setWebViewClient(BPWebPoolViewClient aClient) {
		mWebPoolViewClient = aClient;
	}

	/**
	 * Set the chrome handler. This is an implementation of WebChromeClient for
	 * use in handling Javascript dialogs, favicons, titles, and the progress.
	 * This will replace the current handler.
	 * 
	 * @param aClient
	 *            An implementation of BdWebPoolChromeClient.
	 */
	public void setWebChromeClient(BPWebPoolChromeClient aClient) {
		mWebPoolChromeClient = aClient;
	}

	/**
	 * Use this function to bind an object to Javascript so that the methods can
	 * be accessed from Javascript.
	 * <p>
	 * <strong>IMPORTANT:</strong>
	 * <ul>
	 * <li>Using addJavascriptInterface() allows JavaScript to control your
	 * application. This can be a very useful feature or a dangerous security
	 * issue. When the HTML in the WebView is untrustworthy (for example, part
	 * or all of the HTML is provided by some person or process), then an
	 * attacker could inject HTML that will execute your code and possibly any
	 * code of the attacker's choosing.<br>
	 * Do not use addJavascriptInterface() unless all of the HTML in this
	 * WebView was written by you.</li>
	 * <li>The Java object that is bound runs in another thread and not in the
	 * thread that it was constructed in.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param obj
	 *            The class instance to bind to Javascript
	 * @param interfaceName
	 *            The name to used to expose the class in Javascript
	 */
	public void addJavascriptInterface(Object obj, String interfaceName) {
		if (mJsItems == null) {
			mJsItems = new HashMap<String, Object>();
		}
		if (mJsItems.containsKey(interfaceName)) {
			mJsItems.remove(interfaceName);
		}
		mJsItems.put(interfaceName, obj);

		for (BPWebPoolCustomView webView : mWebViewList) {
			webView.addJavascriptInterface(obj, interfaceName);
		}
	}

	/**
	 * Return the WebSettings object used to control the settings for this
	 * WebView.
	 * 
	 * @return A WebSettings object that can be used to control this WebView's
	 *         settings.
	 */
	public WebSettings getSettings() {
		return mSettings;
	}

	/**
	 * Load the given url.
	 * 
	 * @param aUrl
	 *            The url of the resource to load.
	 */
	public void loadUrl(String aUrl) {
		Bundle extra = new Bundle();
		extra.putByte(BUNDLE_CLINK_MODE, CLICK_FROM_OUTSIDE_PAGE);
		extra.putByte(BPWebPoolView.BUNDLE_PAGE_TYPE, BPWebPoolView.PAGE_TYPE_NORMAL);
		loadUrl(aUrl, extra);
	}

	/**
	 * 获取当前WebView的Url
	 * 
	 * @return 获取当前页面的Url
	 */
	public String getUrl() {
		if (mCurWebView != null) {
			return mCurWebView.getUrl();
		} else {
			return "";
		}
	}
	
	/**
     * Gets a new picture that captures the current display of this WebView.
     * This is a copy of the display, and will be unaffected if this WebView
     * later loads a different URL.
     *
     * @return a picture containing the current contents of this WebView. Note
     *         this picture is of the entire document, and is not restricted to
     *         the bounds of the view.
     */
    public android.graphics.Picture capturePicture() {
        if (mCurWebView != null) {
            return mCurWebView.capturePicture();
        } else {
            return null;
        }
    }
    
    /**
     * Return the scrolled left position of this view. This is the left edge of
     * the displayed part of your view. You do not need to draw any pixels
     * farther left, since those are outside of the frame of your view on
     * screen.
     *
     * @return The left edge of the displayed part of your view, in pixels.
     */
    public int getWebViewScrollX() {
        if (mCurWebView != null) {
            return mCurWebView.getScrollX();
        } else {
            return 0;
        }
    }
    
    /**
     * 得到实际使用的WebView
     * @return webview
     */
    public View getWebView() {
        if (mCurWebView != null) {
            return mCurWebView;
        } else {
            return null;
        }
    }
    
    /**
     * Return the scrolled top position of this view. This is the top edge of
     * the displayed part of your view. You do not need to draw any pixels above
     * it, since those are outside of the frame of your view on screen.
     *
     * @return The top edge of the displayed part of your view, in pixels.
     */
    public int getWebViewScrollY() {
        if (mCurWebView != null) {
            return mCurWebView.getScrollY();
        } else {
            return 0;
        }
    }
	/**
	 * Reload the current url.
	 */
	public void reload() {
		if (mCurWebView != null) {
			mCurWebView.setLoadMode(LoadMode.LOAD_RELOAD);
			mCurWebView.reload();
		}
		if (mWebPoolViewClient != null) {
			mWebPoolViewClient.onReload(this);
		}
	}

	/**
	 * Stop the current load.
	 */
	public void stopLoading() {
		if (mCurWebView != null) {
			mCurWebView.stopLoading();
		}
	}

	/**
	 * Tell the WebView to clear its internal back/forward list.
	 */
	public void clearHistory() {
		for (BPWebPoolCustomView webview : mWebViewList) {
			webview.setHistoryCount(0);
			webview.clearHistory();
		}
 
	}

	/**
	 * Call this to inform the view that memory is low so that it can free any
	 * available memory.
	 */
	public void freeMemory() {
		BdLog.w("");
		for (BPWebPoolCustomView webview : mWebViewList) {
			webview.freeMemory();
		}
	}

	/**
	 * Use this method to put the WebView into text selection mode. Do not rely
	 * on this functionality; it will be deprecated in the future.
	 */
	public void emulateShiftHeld() {
		if (mCurWebView != null) {
			//mCurWebView.emulateShiftHeld();
		}
	}

	/**
	 * 清除WebPoolView内容
	 */
	public void clear() {
		while (mWebViewList.size() > 0) {
			BPWebPoolCustomView webview = mWebViewList.get(0);
			webview.stopLoading();
			webview.clearFocus();
			webview.clearView();
			webview.clearHistory();
			webview.destroy();
			mWebViewList.remove(0);
		}
	}

	/**
	 * clearView
	 */
	public void clearView() {
		if (mCurWebView != null) {
			mCurWebView.clearView();
		}
	}

	/**
	 * 是否能够前进后退指定步数
	 * 
	 * @param aSteps
	 *            The negative or positive number of steps to move the history.
	 * @return 是否能够前进或者后退指定的步数
	 */
	public boolean canGoBackOrForward(int aSteps) {
        //使用单WebView，让其自己控制前进后退历史列表
	    if (mCurWebView != null) {
	        return mCurWebView.canGoBackOrForward(aSteps);
	    }
	    return false;
	}

	/**
	 * 是否可以后退
	 * 
	 * @return 能够后退返回true，否则返回false
	 */
	public boolean canGoBack() {
		return canGoBackOrForward(-1);
	}

	/**
	 * 是否能够前进
	 * 
	 * @return 能够前进返回true，否则返回false
	 */
	public boolean canGoForward() {
		return canGoBackOrForward(1);
	}

	/**
	 * 后退
	 */
	public void goBack() {
        //使用单WebView，让其自己控制前进后退历史列表
        if (mCurWebView != null) {
            mCurWebView.goBack();
            // 4.0以上版本前进后退强制翻页，解决白屏问题 bugfix#SEARHBOX-859
            //if (Build.VERSION.SDK_INT >= 14) {
                //if (mCurWebView.scrollUp()) {
                    //mCurWebView.scrollDown();
                //}
            //}
        }
	}

	/**
	 * 前进
	 */
	public void goForward() {
        //使用单WebView，让其自己控制前进后退历史列表
	    if (mCurWebView != null) {
	        mCurWebView.goForward();
            // 4.0以上版本前进后退强制翻页，解决白屏问题 bugfix#SEARHBOX-859
	        if (Build.VERSION.SDK_INT >= 14) {
                //if (mCurWebView.scrollUp()) {
                //    mCurWebView.scrollDown();
                //}
            }
	    }
	}

	/**
	 * 获取当前列表位置
	 * 
	 * @return 返回当前列表位置
	 */
	public int getCurIndex() {
        //使用单WebView，让其自己控制前进后退历史列表
	    if (mCurWebView != null) {
	        mCurWebView.copyBackForwardList().getCurrentIndex();
	    }
	    return -1;
	}

	/**
	 * 获取当前列表Url
	 * 
	 * @return 返回当前列表Url
	 */
	public String getCurUrl() {
		String curUrl = null;
        //使用单WebView，让其自己控制前进后退历史列表
        WebHistoryItem curItem = null;
        try {
            curItem = mCurWebView.copyBackForwardList().getCurrentItem();
        } catch (Exception e) {
            BdLog.w(e.getMessage());
        }
        
		if (curItem != null) {
			curUrl = curItem.getUrl();
		}
		return curUrl;
	}

	/**
	 * Add or remove a title bar to be embedded into the WebView, and scroll
	 * along with it vertically, while remaining in view horizontally. Pass null
	 * to remove the title bar from the WebView, and return to drawing the
	 * WebView normally without translating to account for the title bar.
	 * 
	 * @param aView
	 *            标题栏View
	 */
	public void setEmbeddedTitleBar(View aView) {
		if (mCurWebView != null) {
		    //mCurWebView.setEmbeddedTitleBar(aView);
		    
		}
	}
	
	/**
	 * 如果是网址刚打开该网页，否则打开百度搜索页面
	 * 
	 * @param aUrl
	 *            要访问的url或要搜索的关键词
	 * @return 新的url
	 */
	public String composeUrl(String aUrl) {
		String searchUrl = "http://www.baidu.com/ssid=0/from=0/bd_page_type=1/uid=wiaui_1298960413_1175/s?tn=iphone&st=11104i&tj=i_sbtn0&pu=sz%401320_480&word=";// SUPPRESS
																																								// CHECKSTYLE
		String newUrl = aUrl;
		if (aUrl != null && aUrl.length() > 0) {
			if ((!aUrl.startsWith("http://")) && (!aUrl.startsWith("https://"))
					&& (!aUrl.startsWith("ftp://")) && (!aUrl.startsWith("rtsp://"))
					&& (!aUrl.startsWith("mms://"))) {
				newUrl = "http://" + aUrl;
			}
			// 不是url，则搜索
			if (!checkStrIsUrl(newUrl)) {
				try {
					newUrl = searchUrl + URLEncoder.encode(aUrl, "gbk");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return newUrl;
	}

	/**
	 * 复制接口
	 * 
	 * @return true表示复制成功
	 */
	public boolean commitCopy() {
		if (mCurWebView != null) {
			//TODO
			return true;//mCurWebView.commitCopy();
		} else {
			return false;
		}
	}

	/**
	 * nativeGetSelection方法
	 * 
	 * @return 选中的字符串
	 */
	public String getSelection() {
		if (mCurWebView != null) {
			return "";//mCurWebView.getSelection();
		} else {
			return "";
		}
	}

	/**
	 * 获取WebView的mExtendSelection值
	 * 
	 * @return mExtendSelection值
	 */
	public boolean getExtendSelection() {
		if (mCurWebView != null) {
			return false ;//mCurWebView.getExtendSelection();
		} else {
			return false;
		}
	}

	/**
	 * 设置WebView的mExtendSelection值
	 * 
	 * @param aExtendSelection
	 *            新的mExtendSelection属性值
	 * @return true表示设置成功，反之亦然
	 */
	public boolean setExtendSelection(boolean aExtendSelection) {
		if (mCurWebView != null) {
			return false;//mCurWebView.setExtendSelection(aExtendSelection);
		} else {
			return false;
		}
	}

	/**
	 * 获取WebView的mSelectingText值
	 * 
	 * @return mSelectingText值
	 */
	public boolean getSelectingText() {
		if (mCurWebView != null) {
			return false;//mCurWebView.getSelectingText();
		} else {
			return false;
		}
	}

	/**
	 * 设置WebView的mSelectingText值
	 * 
	 * @param aSelectingText
	 *            新的mSelectingText属性值
	 * @return true表示设置成功，反之亦然
	 */
	public boolean setSelectingText(boolean aSelectingText) {
		if (mCurWebView != null) {
			return false;//mCurWebView.setSelectingText(aSelectingText);
		} else {
			return false;
		}
	}

	/**
	 * 获取WebView的mDrawSelectionPointer值
	 * 
	 * @return mDrawSelectionPointer值
	 */
	public boolean getDrawSelectionPointer() {
		if (mCurWebView != null) {
			return false;//mCurWebView.getDrawSelectionPointer();
		} else {
			return false;
		}
	}

	/**
	 * 设置WebView的aDrawSelectionPointer值
	 * 
	 * @param aDrawSelectionPointer
	 *            新的aDrawSelectionPointer属性值
	 * @return true表示设置成功，反之亦然
	 */
	public boolean setDrawSelectionPointer(boolean aDrawSelectionPointer) {
		if (mCurWebView != null) {
			return false;//mCurWebView.setDrawSelectionPointer(aDrawSelectionPointer);
		} else {
			return false;
		}
	}

	/**
	 * 获取WebView的mShiftIsPressed值
	 * 
	 * @return mExtendSelection值
	 */
	public boolean getShiftIsPressed() {
		if (mCurWebView != null) {
			return false;//mCurWebView.getShiftIsPressed();
		} else {
			return false;
		}
	}

	/**
	 * 设置WebView的mShiftIsPressed值
	 * 
	 * @param aShiftIsPressed
	 *            新的mShiftIsPressed属性值
	 * @return true表示设置成功，反之亦然
	 */
	public boolean setShiftIsPressed(boolean aShiftIsPressed) {
		if (mCurWebView != null) {
			return false;//mCurWebView.setShiftIsPressed(aShiftIsPressed);
		} else {
			return false;
		}
	}

	/**
	 * 获取WebView的mTouchSelection值
	 * 
	 * @return mTouchSelection值
	 */
	public boolean getTouchSelection() {
		if (mCurWebView != null) {
			return false;//mCurWebView.getTouchSelection();
		} else {
			return false;
		}
	}

	/**
	 * @return the mCurWebViewNotify
	 */
	public boolean isCurWebViewNotify() {
		return mCurWebViewNotify;
	}

	/**
	 * 设置WebView的mTouchSelection值
	 * 
	 * @param aTouchSelection
	 *            新的mTouchSelection属性值
	 * @return true表示设置成功，反之亦然
	 */
	public boolean setTouchSelection(boolean aTouchSelection) {
		if (mCurWebView != null) {
			return false;//mCurWebView.setTouchSelection(aTouchSelection);
		} else {
			return false;
		}
	}

	/**
	 * 获取BdHitTestResult
	 * 
	 * @return BdHitTestResult实例
	 */
	public HitTestResult getHitTestResult() {
		if (mCurWebView != null) {
			//return null;//mCurWebView.getHitTestResult();
			return mCurWebView.getHitTestResult();
		} else {
			return null;
		}
	}

	/**
	 * Call this view's OnLongClickListener, if it is defined. Invokes the
	 * context menu if the OnLongClickListener did not consume the event.
	 * 
	 * @return True if one of the above receivers consumed the event, false
	 *         otherwise.
	 */
	public boolean performLongClick() {
		return superPerformLongClick();
	}

	/**
	 * Super performLongClick
	 * 
	 * @return True if one of the above receivers consumed the event, false
	 *         otherwise.
	 */
	public boolean superPerformLongClick() {
		if (mCurWebView != null) {
			return mCurWebView.performLongClick();
		} else {
			return false;
		}
	}

	/**
	 * Draw one child of this View Group. This method is responsible for getting
	 * the canvas in the right state. This includes clipping, translating so
	 * that the child's scrolled origin is at 0, 0, and applying any animation
	 * transformations.
	 * 
	 * @param canvas
	 *            The canvas on which to draw the child
	 * @param child
	 *            Who to draw
	 * @param drawingTime
	 *            The time at which draw is occuring
	 * @return True if an invalidate() was issued
	 */
	public boolean drawChild(Canvas canvas, View child, long drawingTime) {
		if (child.getClass().getName().equals("com.adobe.flashplayer.FlashPaintSurface")) {
			SurfaceView flashSurface = (SurfaceView) child;
			flashSurface.setZOrderOnTop(false);
		}
		return superDrawChild(canvas, child, drawingTime);
	}

	/**
	 * Super drawChild
	 * 
	 * @param canvas
	 *            The canvas on which to draw the child
	 * @param child
	 *            Who to draw
	 * @param drawingTime
	 *            The time at which draw is occuring
	 * @return True if an invalidate() was issued
	 */
	public boolean superDrawChild(Canvas canvas, View child, long drawingTime) {
		if (mCurWebView != null) {
			return mCurWebView.drawChild(canvas, child, drawingTime);
		} else {
			return false;
		}
	}

	/**
	 * Implement this method to handle touch screen motion events.
	 * 
	 * @param event
	 *            The motion event.
	 * @return True if the event was handled, false otherwise.
	 */
	public boolean onTouchEvent(MotionEvent event) {
		return superOnTouchEvent(event);
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mCustomView != null) {
                hideCustomView();
                return true;
            }
        }
        
        return super.onKeyDown(keyCode, event);
    }



	/**
	 * Super onTouchEvent
	 * 
	 * @param event
	 *            The motion event.
	 * @return True if the event was handled, false otherwise.
	 */
	public boolean superOnTouchEvent(MotionEvent event) {
		if (mCurWebView != null) {
			return mCurWebView.onTouchEvent(event);
		} else {
			return false;
		}
	}

	/**
	 * Return the scrolled left position of this view. This is the left edge of
	 * the displayed part of your view. You do not need to draw any pixels
	 * farther left, since those are outside of the frame of your view on
	 * screen.
	 * 
	 * @return The left edge of the displayed part of your view, in pixels.
	 */
	public int getCurScrollX() {
		if (mCurWebView != null) {
			View view = mCurWebView;
			return view.getScrollX();
		} else {
			return -1;
		}
	}

	/**
	 * Return the scrolled top position of this view. This is the top edge of
	 * the displayed part of your view. You do not need to draw any pixels above
	 * it, since those are outside of the frame of your view on screen.
	 * 
	 * @return The top edge of the displayed part of your view, in pixels.
	 */
	public final int getCurScrollY() {
		if (mCurWebView != null) {
			View view = mCurWebView;
			return view.getScrollY();
		} else {
			return -1;
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (BPWebPoolCustomView webView : mWebViewList) {
			sb.append(getWebViewDebugInfo(webView));
		}
		return sb.toString();
	}

	/**
	 * 获取错误码
	 * 
	 * @return 错误码
	 */
	public int getErrorCode() {
		Integer errorCode = null;
		if (mCurWebView != null) {
			//BWebBackForwardList list = mCurWebView.copyBackForwardList();
			errorCode = mErrorCodeList.get(0);
		}
		return errorCode == null ? 0 : errorCode;
	}

	/**
	 * 设置错误码
	 * 
	 * @param errorCode
	 *            错误码
	 */
	public void setErrorCode(int errorCode) {
		if (mCurWebView != null) {
			//BWebBackForwardList list = mCurWebView.copyBackForwardList();
			mErrorCodeList.put(0, errorCode);
		}
	}

	/**
	 * 隐藏WebContent的错误页。
	 */
	public void hideErrorPage() {
		View errorView = mErrorView;
		if (errorView != null) {
			ViewGroup parent = (ViewGroup) errorView.getParent();
			if (parent != null) {
				parent.removeView(errorView);
			}
		}
	}

	/**
	 * 显示WebContent的错误页。
	 */
	public void showErrorPage() {
		int errorCode = getErrorCode();
		if (errorCode == 0) {
			hideErrorPage();
			return;
		}

		BPErrorView errorView = mErrorView;

		if (errorView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			errorView = (BPErrorView) inflater.inflate(R.layout.browser_webview_error, null);
			mErrorView = errorView;
			errorView.setErrorCode(errorCode);
			errorView.setAttachedFixedWebView(this);
			errorView.setEventListener(this);
		}

		ViewGroup oldParent = (ViewGroup) errorView.getParent();
		if (oldParent != null) {
			oldParent.removeView(errorView);
		}
		//ViewGroup newParent = (ViewGroup) getParent();
		ViewGroup.LayoutParams params = getLayoutParams();
		//newParent.addView(errorView, params);
		addView(errorView, params);
	}

	/**
	 * 获取页面显示状态。有NOT_SHOW, START_SHOW和HAS_SHOWN三种状态。
	 * 
	 * @return 显示状态。
	 */
	public int getShowState() {
		return mShowState;
	}

	/**
	 * 设置页面显示状态。有NOT_SHOW, START_SHOW和HAS_SHOWN三种状态。
	 * 
	 * @param showState
	 *            显示状态。
	 */
	public void setShowState(int showState) {
		mShowState = showState;
	}

	/**
	 * Set the scrolled position of your view. This will cause a call to
	 * {@link #onScrollChanged(int, int, int, int)} and the view will be
	 * invalidated.
	 * 
	 * @param x
	 *            the x position to scroll to
	 * @param y
	 *            the y position to scroll to
	 */
	public void scrollTo(int x, int y) {
		if (mCurWebView != null) {
			mCurWebView.scrollTo(x, y);
		}
	}

	/**
	 * Move the scrolled position of your view. This will cause a call to
	 * {@link #onScrollChanged(int, int, int, int)} and the view will be
	 * invalidated.
	 * 
	 * @param x
	 *            the amount of pixels to scroll by horizontally
	 * @param y
	 *            the amount of pixels to scroll by vertically
	 */
	public void scrollBy(int x, int y) {
		if (mCurWebView != null) {
			mCurWebView.scrollBy(x, y);
		}
	}

	/**
	 * Register the interface to be used when content can not be handled by the
	 * rendering engine, and should be downloaded instead. This will replace the
	 * current handler.
	 * 
	 * @param listener
	 *            An implementation of DownloadListener.
	 */
	public void setDownloadListener(DownloadListener listener) {
		for (BPWebPoolCustomView webview : mWebViewList) {
			webview.setDownloadListener(listener);
		}
		mDownloadListener = listener;
	}

	/**
	 * Get the title for the current page. This is the title of the current page
	 * until WebViewClient.onReceivedTitle is called.
	 * 
	 * @return The title for the current page.
	 */
	public String getTitle() {
		if (mCurWebView != null) {
			return mCurWebView.getTitle();
		} else {
			return "";
		}
	}
	
	public String getWebViewTitle(BPWebPoolCustomView view) {
		if (view != null) {
			return view.getTitle();
		} else {
			return "";
		}
	}
	
	public String getWebViewUrl(BPWebPoolCustomView view) {
		if (view != null) {
			return view.getUrl();
		} else {
			return "";
		}
	}

	@Override
	public void onErrorPageGoBack() {
		goBack();
	}

	@Override
	public void onErrorPageRefresh() {
		reload();
	}

	/**
     * 保存状态。
     * @param savedState Bundle.
     */
    public void saveStateToBundle(Bundle savedState) {
        if (savedState == null || mCurWebView == null) {
            return;
        }
        
        mCurWebView.saveState(savedState);
    }
    
    /**
     * 恢复状态。
     * @param savedState Bundle.
     */
    public void restoreFromBundle(Bundle savedState) {
        if (savedState == null || mCurWebView == null) {
            return;
        }
        
        mCurWebView.restoreState(savedState);
    }
    
	/**
	 * 获取一个可用的BPWebview
	 * 
	 * @param aClickMode
	 *            点击模式
	 * @return BdWebview实例
	 */
	private BPWebPoolCustomView getAvailableWebView(byte aClickMode) {
		// 1. 第一次加载，使用初始化的WebView
		if (mCurWebView == null) {
			return getWebViewByPos(0);
		} else {
			// 2.1. 页面内加载
			// 判断当前webview的历史记录是否有增长，如果有，则使用下一个webview加载，否则说明是重定向过来的，使用当前webview加载
			if (aClickMode == CLICK_FROM_PAGE) {
				// 2.1.1 使用当前webview
				if (isRedirectLoad()) {
					BdLog.v("the request is redirect, use current webview to load it.");
					return mCurWebView;
				} else {
					// 2.1.2 新开webview
					return getFreeWebView();
				}
			} else {
				// 2.2. 页面外加载，新开webview
				return getFreeWebView();
			}
		}
	}

	/**
	 * 获取一个新的BPWebview
	 * 
	 * @return BPWebview实例
	 */
	private BPWebPoolCustomView getFreeWebView() {
 
	    BPWebPoolCustomView webview = mCurWebView;
	    if (webview == null) {
			Context context = getContext();
			if (!(context instanceof Activity)) {
				BdLog.w("context is not activity, can not create webview.");
				return webview;
			}
			Activity act = (Activity) context;
			webview = new BPWebPoolCustomView(this, act.getParent() == null ? act : act.getParent());
			webview.setWebViewClient(new BPWebPoolCustomViewClient());
			webview.setWebChromeClient(new BPWebPoolCustomChromeClient());
			webview.setOnLongClickListener(this);
			
			BaseWebView.setSupportHtml5(context, webview);
			BaseWebView.removeInitialFocus(context, webview);

			// 初始化JavascriptInterface
			initJavascriptClients(webview);
			// 设置下载监听器
			if (mDownloadListener != null) {
				webview.setDownloadListener(mDownloadListener);
			}
			
			webview.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
			
			mWebViewList.add(webview);
			//将原来switchWebView中的逻辑移到此处，保证单WebView正常使用
			addWebView(webview);
			webview.onResume();
			
            webview.requestFocus();
            mCurWebView = webview;
		}
		return webview;
	}

	/**
	 * 根据索引获取指定的BdWebView
	 * 
	 * @param aPos
	 *            BdWebView的索引
	 * @return BdWebView实例
	 */
	private BPWebPoolCustomView getWebViewByPos(int aPos) {
		if (aPos < 0 || aPos >= mWebViewList.size()) {
			return null;
		}
		return mWebViewList.get(aPos);
	}
	
	/**
	 * 获取可用的BdWebview前，判断当前BdWebView是否在加载重定向Url
	 * 
	 * @return true表示是重定向，false表示不是重定向
	 */
	private boolean isRedirectLoad() {
		return isRedirectLoad(mCurWebView);
	}

	/**
	 * 获取可用的BdWebview前，判断指定BdWebView是否在加载重定向Url
	 * 
	 * @param aWebView
	 *            BdWebView实例
	 * @return true表示是重定向，false表示不是重定向
	 */
	private boolean isRedirectLoad(BPWebPoolCustomView aWebView) {
		boolean isRedirectLoad = false;
		int index = getWebViewIndex(aWebView);
		BPWebPoolCustomView webview = getWebViewByPos(index);
		if (webview != null) {
			int count_1 = aWebView.copyBackForwardList().getSize();
			int count_2 = webview.getHistoryCount();
			if (count_1 <= count_2) {
				isRedirectLoad = true;
			}
		}
		// 有可能误判为重定向，如果获取到用户点击的链接，则认为不是重定向
		if (isRedirectLoad) {
			String clinkLink = getClickLink(aWebView);
			BdLog.d("clinkLink: " + clinkLink);
			if (clinkLink != null && clinkLink != "undefined" && !clinkLink.startsWith("#")
					&& !clinkLink.startsWith("javascript")) {
				isRedirectLoad = false;
				BdLog.i("because get clink link, we think this load is not redirect.");
			}
		}
		return isRedirectLoad;
	}

	/**
	 * 获取指定BdWebView的索引
	 * 
	 * @param aWebView
	 *            BdWebView实例
	 * @return 指定BdWebView的索引
	 */
	private int getWebViewIndex(BPWebPoolCustomView aWebView) {
		for (int i = 0; i < mWebViewList.size(); i++) {
			BPWebPoolCustomView webView = mWebViewList.get(i);
			if (webView.equals(aWebView)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * 添加BdWebView实例到UI上
	 * 
	 * @param aWebView
	 *            BdWebView实例
	 */
	private void addWebView(WebView aWebView) {
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		if (aWebView != null) {
			View aView = aWebView;
			if (aView != null && aView.getParent() == null) {
				addView(aView, 0, params);
			}
		}
	}

	/**
	 * 显示遮罩层
	 * 
	 */
	protected void showMaskView() {
		BdLog.d("");
		mMaskView.setVisibility(VISIBLE);
	}

	/**
	 * 隐藏遮罩层
	 */
	protected void hideMaskView() {
		BdLog.d("");
		if (mMaskView.getVisibility() == VISIBLE) {
			mMaskView.setVisibility(GONE);
		}
	}
	
	/**
	 * 设置WebView上一次的当前位置
	 * 
	 * @param aWebView
	 *            BdWebView实例
	 * @param aCurrentIndex
	 *            WebView上一次的当前位置
	 */
	private void setLastCurrentIndex(BPWebPoolCustomView aWebView, int aCurrentIndex) {
		int currentIndex = aWebView.copyBackForwardList().getCurrentIndex();
		int webview_index = getWebViewIndex(aWebView);
		BPWebPoolCustomView webview = getWebViewByPos(webview_index);
		if (webview != null) {
			webview.setLastIndex(currentIndex);
		}
	}

	/**
	 * 获取WebView上一次的当前位置
	 * 
	 * @param aWebView
	 *            BdWebView实例
	 * @return WebView上一次的当前位置
	 */
	private int getLastCurrentIndex(BPWebPoolCustomView aWebView) {
		int webview_index = getWebViewIndex(aWebView);
		BPWebPoolCustomView webview = getWebViewByPos(webview_index);
		if (webview != null) {
			return webview.getLastIndex();
		} else {
			return -1;
		}
	}

	/**
	 * 获取BdWebView点击的链接
	 * 
	 * @param aWebView
	 *            BdWebView实例
	 * @return 点击的链接
	 */
	private String getClickLink(BPWebPoolCustomView aWebView) {
		int webview_index = getWebViewIndex(aWebView);
		BPWebPoolCustomView webview = getWebViewByPos(webview_index);
		if (webview != null) {
			return webview.getLoadUrl();
		} else {
			return "";
		}
	}

	/**
	 * 设置BdWebView点击的链接
	 * 
	 * @param aWebView
	 *            BdWebView实例
	 * @param aClickLink
	 *            点击的链接
	 */
	private void setClickLink(BPWebPoolCustomView aWebView, String aClickLink) {
		int webview_index = getWebViewIndex(aWebView);
		BPWebPoolCustomView webview = getWebViewByPos(webview_index);
		if (webview != null) {
			webview.setLoadUrl(aClickLink);
		}
	}

	/**
	 * 如果链接地址以#号开头或者javascript开头，则不是有效的新链接，用单View打开，否则用多View打开
	 * 
	 * @param aClickLink
	 *            获取到的点击链接
	 * @return true表示有效的点击链接，用多View打开，反之亦然
	 */
	private boolean isValidClickLink(String aClickLink) {
	    return false;
	}

	/**
	 * 加载指定的url
	 * 
	 * @param aUrl
	 *            要加载的url
	 * @param aExtra
	 *            Extra数据
	 */
	protected void loadUrl(String aUrl, Bundle aExtra) {
		byte clickMode = aExtra.getByte(BUNDLE_CLINK_MODE, CLICK_FROM_OUTSIDE_PAGE);
		byte pageType = aExtra.getByte(BUNDLE_PAGE_TYPE, PAGE_TYPE_NORMAL);
		BdLog.i(clickMode + ", " + pageType);
		// 1. 如果当前不是正在加载wap1.0页面，则检查是否是wap1.0页面
		if (pageType != PAGE_TYPE_WAP10) {
 
		} else {
			// 加载wap1.0页面，点击模式设为页面内加载
			// aExtra.putByte(BUNDLE_CLINK_MODE, CLICK_FROM_PAGE);
			clickMode = CLICK_FROM_PAGE;
		}
		if (mCurWebView != null) {
			mCurWebView.setLoadMode(LoadMode.LOAD_NORMAL);
			mCurWebView.loadUrl(aUrl);
		}

		hideErrorPage();
	}
	
	/**
     * 加载js代码
     * 
     * @param js js code
     */
	public void loadJavaScript(String js) {
	    if (mCurWebView != null) {
            mCurWebView.setLoadMode(LoadMode.LOAD_NORMAL);
            initJavascriptClients(mCurWebView);
            mCurWebView.loadUrl(js);
        }
	}

	/**
	 * 多View机制，由于初始化时只创建一个BdWebView，后面创建BdWebView，JavascriptClient要做一次初始化设置
	 * 
	 * @param aWebView
	 *            BdWebView实例
	 */
	private void initJavascriptClients(WebView aWebView) {
		if (mJsItems != null) {
			Iterator<String> iter = mJsItems.keySet().iterator();
			while (iter.hasNext()) {
				String interfaceName = iter.next();
				Object obj = mJsItems.get(interfaceName);
				aWebView.addJavascriptInterface(obj, interfaceName);
			}
		}
	}

	/**
	 * 打印出BdWebView信息，调试用
	 * 
	 * @param aWebView
	 *            BdWebView实例
	 * @return BdWebView调试信息
	 */
	private String getWebViewDebugInfo(WebView aWebView) {
		StringBuffer sb = new StringBuffer();
		int size = aWebView.copyBackForwardList().getSize();
		sb.append(aWebView + ", " + size);
		if (aWebView instanceof BPWebPoolCustomView) {
			BPWebPoolCustomView customView = (BPWebPoolCustomView) aWebView;
			sb.append(", " + customView.getLoadMode());
		}
		sb.append("\n");
		for (int i = 0; i < size; i++) {
			sb.append(i + ": ");
			WebHistoryItem item = aWebView.copyBackForwardList().getItemAtIndex(i);
			if (item != null) {
				sb.append(item.getUrl());
				sb.append("\n");
			}
		}
		sb.append("***************************");
		sb.append("\n");
		return sb.toString();
	}

	/**
	 * 判断字符串是否为网址
	 * 
	 * @param input
	 *            输入的字符串
	 * @return 如果为网站则返回trur，反之亦然
	 */
	private boolean checkStrIsUrl(String input) {
		Pattern pattern = Pattern.compile("^((https|http|ftp|rtsp|mms)?://)"
				+ "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?"
				+ "(([0-9]{1,3}\\.){3}[0-9]{1,3}" + "|" + "([0-9a-z_!~*'()-]+\\.)*"
				+ "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." + "[a-z]{2,6})" + "(:[0-9]{1,4})?" + "((/?)|"
				+ "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(input);
		return matcher.find();
	}

	/**
	 * 由于多View切换，可能会导致当前WebView被切到后台了，但还在加载
	 * 
	 * @param aCallBackWebView
	 *            BdWebView实例
	 */
	private void setCurWebViewNotify(WebView aCallBackWebView) {
		if (aCallBackWebView == null) {
			mCurWebViewNotify = false;
		} else {
			mCurWebViewNotify = aCallBackWebView.equals(mCurWebView);
		}
	}
	
	/**
	 * 根据错误码改变状态
	 * 
	 * @param view
	 *            webview
	 * @param changeStateMask
	 *            旧的状态
	 * @return 新状态
	 */
	private int changeStateMaskByErrorCode(View view, int changeStateMask) {
		Object errorTag = view.getTag(R.id.webcontent_error_code);
		int errorTagCode = errorTag == null ? 0 : (Integer) errorTag;
		if (errorTagCode != 0) {
			changeStateMask |= STATE_RECEIVE_ERROR;
		}
		return changeStateMask;
	}

	/**
	 * 当浏览器状态发生改变时，触发此方法。
	 * 
	 * @param sview
	 *            发生改变的主View
	 * @param changeStateMask
	 *            状态改变标志位
	 * @param newValue
	 *            新值
	 * */
	public void onStateChanged(int changeStateMask, Object newValue) {
		// 页面开始显示(推断出来的，已经显示出来)
		BdLog.d(changeStateMask + ", " + getShowState());
		if ((changeStateMask & BPWebPoolView.STATE_START_SHOW) != 0) {
			// 页面开始显示时先隐藏错误页
			if (getShowState() > BPWebPoolView.NOT_SHOW) {
				hideErrorPage();
			}

			if ((changeStateMask & BPWebPoolView.STATE_RECEIVE_ERROR) != 0) {
				Object errorTag = getTag(R.id.webcontent_error_code);
				int errorTagCode = errorTag == null ? 0 : (Integer) errorTag;
				setErrorCode(errorTagCode);
				showErrorPage();
			} else if (getErrorCode() != 0) {
				showErrorPage();
			} else {
				hideErrorPage();
			}
		}

		// 页面加载进度改变。
		if ((changeStateMask & BPWebPoolView.STATE_PROGRESS_CHANGED) != 0) {
			int progress = (Integer) newValue;
			if (progress < BPWebPoolView.PROGRESS_MIN) {
				progress = BPWebPoolView.PROGRESS_MIN;
			}
		}
	}

	@Override
	public boolean onLongClick(View v) {
		
		return false;
	}

	/**
	 * Call this to balanace a previous call to onPause()
	 */
	public void onResume() {
		if (mCurWebView != null) {
			mCurWebView.onResume();
			// 4.0以上版本前进后退强制翻页，解决白屏问题  
            if (Build.VERSION.SDK_INT >= 14) {
                //if (mCurWebView.scrollUp()) {
                //    mCurWebView.scrollDown();
                //}
            }
		}
	}

	/**
	 * Call this to pause any extra processing associated with this view and its
	 * associated DOM/plugins/javascript/etc. For example, if the view is taken
	 * offscreen, this could be called to reduce unnecessary CPU and/or network
	 * traffic. When the view is again "active", call onResume().
	 * 
	 * Note that this differs from pauseTimers(), which affects all views/DOMs
	 */
	public void onPause() {
		if (mCurWebView != null) {
			mCurWebView.onPause();
		}
	}

	/**
	 * @param resultMsg
	 *            Message
	 * @param bdTransport
	 *            BdWebViewTransport
	 * @return 是否执行成功
	 */
	public boolean setWebViewToTarget(Message resultMsg, WebView.WebViewTransport bdTransport) {
		BPWebPoolCustomView webPoolCustomView = getAvailableWebView(CLICK_FROM_PAGE);
		if (webPoolCustomView != null) {
			bdTransport.setWebView(webPoolCustomView);
			resultMsg.sendToTarget();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Request the href of an anchor element due to getFocusNodePath returning
	 * "href." If hrefMsg is null, this method returns immediately and does not
	 * dispatch hrefMsg to its target.
	 * 
	 * @param hrefMsg
	 *            This message will be dispatched with the result of the request
	 *            as the data member with "url" as key. The result can be null.
	 */
	public void requestFocusNodeHref(Message hrefMsg) {
		if (mCurWebView != null) {
			mCurWebView.requestFocusNodeHref(hrefMsg);
		}
	}
    
    /**
     * 设置 activity是否全屏.
     * @param activity Activity
     * @param enabled 是否全屏。
     */
    public void setFullscreen(Activity activity, boolean enabled) {
        Window win = activity.getWindow();
        
        int flag = !enabled ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
        win.setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    
    /**
     * 隐藏 custom view。
     */
    private void hideCustomView() {
        if (mCustomView == null) {
            return;
        }
        
        Context context = getContext();
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        }
        
        if (activity != null) {
            setFullscreen(activity, false);
            FrameLayout decor = (FrameLayout) activity.getWindow().getDecorView();
            decor.removeView(mFullscreenContainer);
            mFullscreenContainer = null;
            mCustomView = null;
            mCustomViewCallback.onCustomViewHidden();
            // Show the content view.
            activity.setRequestedOrientation(mOriginalOrientation);
        }
    }
    
    /**
     * 显示 customview.
     * 把回调的 custom view 添加到 layout中。显示出来。
     * 
     * @param view 要显示的 customview。
     * @param requestedOrientation  customview 申请的 orientation 
     * @param callback callback.
     */
    private void showCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        Context context = getContext();
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        }
        
        if (activity != null) {
            // if a view already exists then immediately terminate the new one
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }

            mOriginalOrientation = activity.getRequestedOrientation();
            FrameLayout decor = (FrameLayout) activity.getWindow().getDecorView();
            mFullscreenContainer = new FullscreenHolder(activity);
            mFullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
            decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS);
            mCustomView = view;
            setFullscreen(activity, true);
            mCustomViewCallback = callback;
            activity.setRequestedOrientation(requestedOrientation);
        }

    }
    
    /**
     * webview custom view full screen holder. as a container.
     */
    static class FullscreenHolder extends FrameLayout {

        /**
         * constructor.
         * @param ctx Context
         */
        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }

    }
    

	/**
	 * BdWebPoolCustomViewClient
	 */
	class BPWebPoolCustomViewClient extends BaseWebViewClient{

		/** BdWebView Wrapper */
		private SoftReference<WebView> mWebViewWrapper;

		/**
		 * obtain WebView Wrapper
		 * 
		 * @param aWebView
		 *            WebView
		 * @return WebView Wrapper
		 */
		public WebView obtainWebViewWrapper(WebView aWebView) {
			WebView result = null;
			if (mWebViewWrapper != null) {
				result = mWebViewWrapper.get();
			}

			if (result == null || !result.equals(aWebView)) {
				result = new BPWebPoolCustomView(BPWebPoolView.this, aWebView.getContext());
				mWebViewWrapper = new SoftReference<WebView>(result);
			}
			return result;
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView aView, String aUrl) {
			BdLog.d(aUrl);
			
			if (aUrl.startsWith("about:")) {
			    return false;
			}

			// 抽离特殊协议处理，放到BaseWebViewBaseWebView.handleSpecialScheme()
			try {
			    if (BaseWebView.handleSpecialScheme(getContext(), aUrl)) {
			        return true;
			    }
			    
			} catch (ActivityNotStartedException e) {
			    Toast.makeText(getContext(), R.string.activity_not_found, Toast.LENGTH_SHORT).show();
			    return true;
			}
			
			if (mWebPoolViewClient != null) {
				if (mWebPoolViewClient.shouldOverrideUrlLoading(BPWebPoolView.this, aUrl)) {
					return true;
				}
			}


			// 分析点击的链接
			String clinkLink = getClickLink((BPWebPoolCustomView)aView);
			BdLog.d("clinkLink: " + clinkLink);
			boolean result;
			if (isValidClickLink(clinkLink)) {
				// 新链接，用多View打开
				Bundle extra = new Bundle();
				extra.putByte(BUNDLE_CLINK_MODE, CLICK_FROM_PAGE);
				extra.putByte(BPWebPoolView.BUNDLE_PAGE_TYPE, BPWebPoolView.PAGE_TYPE_NORMAL);
				loadUrl(aUrl, extra);
				result = true;
			} else {
				// 非新链接，用单View打开
				result = false;
			}
			setClickLink((BPWebPoolCustomView)aView, null);

			return result;
		}

		boolean isSniffer = false;
		
		@Override
		public void onPageStarted(WebView aView, String aUrl, Bitmap aFavicon) {
			BdLog.d(aUrl);
			
			// 在onPageStarted时，表示加载一个新页面，所以需要将其置为NOT_SHOW。
			setShowState(NOT_SHOW);

			if (mWebPoolViewClient != null) {
				mWebPoolViewClient.onPageStarted(BPWebPoolView.this, aUrl, aFavicon);
				isSniffer = true;
				int changeStateMask = changeStateMaskByErrorCode(BPWebPoolView.this, STATE_PAGE_STARTED);
				onStateChanged(changeStateMask, null);
			}
		}

		@Override
		public void onPageFinished(WebView aView, String aUrl) {
			BdLog.d(aUrl);
			// 3. 隐藏前进后退的遮罩图
			hideMaskView();

			setCurWebViewNotify(aView);
			if (!isSniffer) {
				if (mWebPoolViewClient != null) {
					mWebPoolViewClient.onPageFinished(BPWebPoolView.this, aUrl);
			} else  
				isSniffer = false;
				
				// 如果执行的操作是页面的前进或后退，并且拥有页面缓存，会直接执行onPageStarted和onPageFinished，
				// 而不经过doUpdateVisitedHistory和onProgressChanged。
				// 所以在此处将STATE_START_SHOW补上，表示页面已经开始显示。
				int changeStateMask = STATE_PAGE_FINISHED;
				if (mShowState == NOT_SHOW) {
					changeStateMask |= STATE_START_SHOW;
				}
				setShowState(HAS_SHOWN);
				changeStateMask = changeStateMaskByErrorCode(BPWebPoolView.this, changeStateMask);

				onStateChanged(changeStateMask, null);

				// webcontent_error_code的tag用来标记该页面是否是一个错误页。
				// 此tag应该是在onPageFinished里清掉，而不是在onPageStarted里剔除，
				// 因为这个tag是在上一个页面的onReceivedError发起的，之后就会经过onPageStarted加载默认错误页。
				setTag(R.id.webcontent_error_code, 0);
			}

		}

		@Override
		public void onLoadResource(WebView aView, String aUrl) {
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			BdLog.d(errorCode + ", " + description);
			if (mWebPoolViewClient != null) {
				mWebPoolViewClient.onReceivedError(BPWebPoolView.this, errorCode, description, failingUrl);
			}

			// 设上webcontent_error_code的tag，表示下一次加载的页面将会是错误页。
			setTag(R.id.webcontent_error_code, errorCode);
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			BdLog.d("");
			handler.proceed();
			hideErrorPage();
		}

		@Override
		public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
			if (url != null) { 
				// url ==null表示是从onReceivedError过来的，不应该置START_SHOW。
				// 在doUpdateVisitedHistory触发后的下一次onProgressChanged时开始显示页面。
				// 所以此处将START_SHOW置上，以便在onProgressChanged时通知页面已开始显示。
				setShowState(START_SHOW);
				
				if (!TextUtils.equals(BPBrowser.HOME_PAGE, url)) {
				    mVisitedSite.setSiteUrl(url);
				    mVisitedSite.setSiteTitle(getWebViewTitle((BPWebPoolCustomView)view));
				    mVisitedSite.setIcon(null);
				    mVisitedSite.setCreateTime(System.currentTimeMillis());
				    mVisitedSite.setVisitedTime(System.currentTimeMillis());
				    mVisiteSiteManager.insertVisitedSite(mVisitedSite);
				}
			}
			
			
			
			super.doUpdateVisitedHistory(view, url, isReload);
		}
	}

	/**
	 * BdWebPoolCustomChromeClient
	 */
	class BPWebPoolCustomChromeClient extends WebChromeClient {

		/** BdWebView Wrapper */
		private SoftReference<WebView> mWebViewWrapper;
		
		/**
		 * obtain WebView Wrapper
		 * 
		 * @param aWebView
		 *            WebView
		 * @return WebView Wrapper
		 */
		public WebView obtainWebViewWrapper(WebView aWebView) {
			WebView result = null;
			if (mWebViewWrapper != null) {
				result = mWebViewWrapper.get();
			}
			if (result == null) {
				result = new BPWebPoolCustomView(BPWebPoolView.this, aWebView.getContext());
				mWebViewWrapper = new SoftReference<WebView>(result);
			}
			return result;
		}

		@Override
		public void onProgressChanged(WebView aView, int aNewProgress) {
			setCurWebViewNotify(aView);
			if (mWebPoolChromeClient != null) {
				mWebPoolChromeClient.onProgressChanged(BPWebPoolView.this, aNewProgress);
				// 如果显示状态为START_SHOW，表示doUpdateVisitedHistory已触发，
				// 本次的onProgressChanged之后就会开始显示页面。
				int changeStateMask = STATE_PROGRESS_CHANGED;
				if (mShowState == START_SHOW) {
					changeStateMask |= STATE_START_SHOW;
					setShowState(HAS_SHOWN);
					// 正在联网时，不知结果是否显示错误页，所以先隐藏，再后面需要时再显示出来
					hideErrorPage();
				}
				// 如果联网了就清除错误码
				if (aNewProgress <= PROGRESS_MIN) {
					setErrorCode(0);
				}

				if (mWebPoolViewClient != null) {
					onStateChanged(changeStateMask, aNewProgress);
				}

				if (aNewProgress == 100) {
					hideMaskView();
				}
			}
		}

		@Override
		public void onReceivedTitle(WebView aView, String aTitle) {
			setCurWebViewNotify(aView);
			if (mWebPoolChromeClient != null) {
				mWebPoolChromeClient.onReceivedTitle(BPWebPoolView.this, aTitle);
				int changeStateMask = changeStateMaskByErrorCode(BPWebPoolView.this, STATE_PAGE_RECEIVED);
				if (mWebPoolViewClient != null) {
					onStateChanged(changeStateMask, null);
				}
			}
			//if (!HistoryConfig.isPrivateMode(getContext())) {
				mVisitedSite.setSiteUrl(getWebViewUrl((BPWebPoolCustomView)aView));
				mVisitedSite.setSiteTitle(aTitle);
				mVisiteSiteManager.updateVisitedTitle(mVisitedSite);
			//}
		}

		@Override
		public void onReceivedIcon(WebView view, Bitmap icon) {
		    super.onReceivedIcon(view, icon);
		    
			if (mWebPoolChromeClient != null) {
				mWebPoolChromeClient.onReceivedIcon(BPWebPoolView.this, icon);
			}
		    
		    //if (!HistoryConfig.isPrivateMode(getContext())) {
		    	mVisitedSite.setSiteUrl(getWebViewUrl((BPWebPoolCustomView)view));
            	mVisitedSite.setIcon(Utility.getIconStream(icon));
            	mVisiteSiteManager.updateVisitedIcon(mVisitedSite);
		    //}
		}
		
		private String defaultVideoUrl = "";
		
		@Override
		public Bitmap getDefaultVideoPoster() {
			final String url = getUrl();
			if (!url.equals(defaultVideoUrl)) {
				defaultVideoUrl = url;
				if (mWebPoolChromeClient != null) {
					mWebPoolChromeClient.getDefaultVideoPoster();
				}
				stopLoading();
			} else
				defaultVideoUrl = "";
			return null;
		}

		@Override
		public View getVideoLoadingProgressView() {
			return null;
		}

		@Override
		public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
			if (mWebPoolChromeClient != null) {
				//BWebView.BWebViewTransport transport = (BWebView.BWebViewTransport) resultMsg.obj;
				WebView.WebViewTransport bdTransport = view.new WebViewTransport();
				return mWebPoolChromeClient.onCreateWindow(BPWebPoolView.this, dialog, userGesture,
						resultMsg, bdTransport);
			} else {
				return false;
			}
		}

		@Override
		public void onCloseWindow(WebView window) {
		}

        @Override
        public void onHideCustomView() {
            hideCustomView();
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
 
        }
        
        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        	
        }

		// For Android 3.0+
		//@Override
		public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) { // SUPPRESS CHECKSTYLE
			if (mWebPoolChromeClient != null) {
				//mWebPoolChromeClient.openFileChooser(uploadMsg, acceptType);
			}
		}

		// For Android < 3.0
		//@Override
		public void openFileChooser(ValueCallback<Uri> uploadMsg) { // SUPPRESS CHECKSTYLE
			if (mWebPoolChromeClient != null) {
				//mWebPoolChromeClient.openFileChooser(uploadMsg);
			}
		}

		/**
		 * Instructs the client to show a prompt to ask the user to set the
		 * Geolocation permission state for the specified origin.
		 */
		public void onGeolocationPermissionsShowPrompt(String origin,
				GeolocationPermissions.Callback callback) {
			if (mWebPoolChromeClient != null) {
				mWebPoolChromeClient.onGeolocationPermissionsShowPrompt(origin, callback);
			}
		}

		/**
		 * Instructs the client to hide the Geolocation permissions prompt.
		 */
		public void onGeolocationPermissionsHidePrompt() {
			if (mWebPoolChromeClient != null) {
				mWebPoolChromeClient.onGeolocationPermissionsHidePrompt();
			}
		}
	}

}