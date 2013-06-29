package com.baidu.browser.framework;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.browser.BPBrowser;
import com.baidu.browser.core.ui.BdPopMenuGroup;
import com.baidu.browser.db.HistoryConfig;
import com.baidu.browser.ui.BPAnimationMenuView;
import com.baidu.browser.ui.BPAnimationMultiWindow;
import com.baidu.browser.ui.NativeSearchPanel;
import com.baidu.browser.ui.Tabbar;
import com.baidu.browser.visitesite.VisiteSiteManager;
import com.baidu.hd.BaseActivity;
import com.baidu.hd.BrowserSpecActivity;
import com.baidu.hd.SearchActivity;
import com.baidu.hd.VoiceSearchActivity;
import com.baidu.hd.log.Logger;
import com.baidu.hd.personal.PersonalActivity;
import com.baidu.hd.player.PlayerLauncher;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.settings.SettingsActivity;
import com.baidu.hd.sniffer.SnifferHandler;
import com.baidu.hd.sniffer.SnifferResult;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId.Browser;
import com.baidu.hd.ui.BrowDialogTip;
import com.baidu.hd.ui.FakeProgressBar;
import com.baidu.hd.util.ImageUtil;
import com.baidu.hd.util.SnifferResultUtil;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.Utility;
import com.baidu.hd.util.Const.IntentExtraKey;
import com.baidu.hd.util.SnifferResultUtil.SaveResultArgs;
import com.baidu.hd.R;

/**
 * @ClassName: BPFrameView
 * @Description: 浏览器控制窗口
 * @author LEIKANG
 * @date 2012-12-5 下午3:27:26
 */
public class BPFrameView extends FrameLayout{

    private Logger logger = new Logger("BPFrameView");

    /**
     * Window切换时的动画类型
     */
    public enum WindowStwitchAnimation {
        /** 无动画 */
        NONE,
        /** 关闭窗口动画 */
        CLOSE_WINDOW,
        /** 新开窗口 */
        NEW_WINDOW,
    }

    /** CurrentWindow等待resume。
     * 之所以在真正开始resume之前加上等待和准备这两步操作，是为了确保让CurrentWindow在Activity完全resume后的200毫秒 以后，再执行resume，
     * 从而让App的框架（searchbox、toolbar等）先显示出来，达到快速启动的效果。
     */
    private static final int MSG_CURRENT_WINDOW_RESUME_WAITING = 1;

    /** CurrentWindow准备resume。 */
    private static final int MSG_CURRENT_WINDOW_RESUME_READY = 2;

    /** CurrentWindow真正开始resume。 */
    private static final int MSG_CURRENT_WINDOW_RESUME_GO = 3;

    /**多窗口点击新建窗口 */
    private static final int MSG_CREATE_NEW_WINDOW_FROM_MULTIWINDOW = 4;

    /**多窗口切换 */
    private static final int MSG_SWAP_WINDOW_TO_FOCUS_FROM_MULTI_WINDOW = 5;

    /**刷新嗅探图标 */
    private static final int MSG_UPDATE_ALBUM_NUMBER = 6;

    /**用来保存window总数的key。*/
    private static final String WINDOW_SIZE = "WINDOW_SIZE";

    /**用来保存当前window位置的key。*/
    private static final String CURRENT_WINDOW_POS = "CURRENT_WINDOW_POS";

    /** 是否为从保存的状态中修复 */
    private boolean mRestoredFromState;

    /** 窗口列表 **/
    private List<BPWindow> mWindowList;

    /** 当前窗口 **/
    private BPWindow mCurrentWindow;

    /** 搜索框高度 **/
    private int mFloatPlayerSearchHeight;

    /** 工具条高度 **/
    private int mToolbarHeight;

    /** 工具条阴影区 **/
    private int mToolBarShadowDis;

    /** 多标签栏**/
    private Tabbar mTabbar;

    /** 工具栏 **/
    //private RelativeLayout mToolbar;

    /** toolbar阴影:工具栏上面的阴影 **/
    private ImageView mToolbarShadow;

    /** searchbox阴影:搜索框下面的阴影 **/
    private ImageView mSearchboxShadow;

    /** 搜索框阴影区 **/
    private int mSearchShadowDis;

    /** 进度条高度 **/
    private int mProgressHeight;

    /** 进度条 **/
    private FakeProgressBar mProgressBar;

    /** 搜索框架 **/
    private RelativeLayout mFloatSearch;

    /** 地址栏 */
    private TextView mAdressTextInput;
    
    /** 搜索栏添加书签和清除面板*/
    private LinearLayout mClearAndMarkBtn;
    
    /** 清除按钮*/
    private ImageView mFloatClearcontent;

    /** 搜索栏*/
    //private TextView mSearchTextInput;

    /** 书签图标*/
    private ImageView mBrowTopMark;

    /** 刷新停止按钮*/
    private ImageView mBrowTopRefreshStop;

    /** 窗口容器，用于控制窗口的切换 */
    private BPWindowWrapper mWindowWrapper;

    /** 菜单窗口 */
    private BPAnimationMenuView mMenuPopupWindow;

    /** 多窗口*/
    private BPAnimationMultiWindow mBPAnimationMultiWindow;

    /** Browser 主类 */
    private BPBrowser mBrowser;

    private WebView mBigSnifferWebView = null;

    private Context mContext;

    /** 嗅探控制*/
    private SnifferHandler mSnifferHandler;

    /** 站点历史记录manager*/
    private VisiteSiteManager mVisiteSiteManager;

    private boolean isBookmark = false;

    private String currentUrl = "";

    private BrowDialogTip mBrowDialogTip;

    private static Stat mStat;

    //private HomeView mHomeView;
    
    private NativeSearchPanel mNativeSearchPanel;

    /**
     * @param context
     */
    public BPFrameView(Context context) {
        this(context, null);
        mContext = context;
    }

    /**
     * @param context
     * @param attrs
     */
    public BPFrameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public BPFrameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mContext = context;
        mStat = (Stat)((BaseActivity)mContext).getServiceProvider(Stat.class);
        mWindowList = new ArrayList<BPWindow>();

        mVisiteSiteManager = (VisiteSiteManager)((BaseActivity)context).getServiceProvider(VisiteSiteManager.class);
        mFloatPlayerSearchHeight = context.getResources().getDimensionPixelSize(R.dimen.float_searchbox_height);
        mToolbarHeight = context.getResources().getDimensionPixelSize(R.dimen.bottom_toolbar_height);
        mProgressHeight = context.getResources().getDimensionPixelSize(R.dimen.browser_progress_bar_height);
        mToolBarShadowDis = context.getResources().getDimensionPixelSize(R.dimen.browser_float_toolbar_shadow);
        mSearchShadowDis = context.getResources().getDimensionPixelSize(R.dimen.browser_float_searchbox_shadow);

        mTabbar = new Tabbar(context,this);
        addView(mTabbar, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, mFloatPlayerSearchHeight));

        mFloatSearch = (RelativeLayout) ((Activity) context).getLayoutInflater().inflate(R.layout.float_search_bar, null);

        addView(mFloatSearch, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,mFloatPlayerSearchHeight));

        mWindowWrapper = new BPWindowWrapper(context);
        addView(mWindowWrapper, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));

        //mHomeView = HomeView.getInstance(context);
        //addView(mHomeView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));

        mSearchboxShadow = new ImageView(context);
        mSearchboxShadow.setBackgroundResource(R.drawable.float_shadow);
        addView(mSearchboxShadow, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,mSearchShadowDis));

        mProgressBar = (FakeProgressBar) ((Activity) context).getLayoutInflater().inflate(R.layout.browser_progress_bar, null);
        mProgressBar.setVisibility(View.INVISIBLE);
        addView(mProgressBar, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,mProgressHeight));

        mMenuPopupWindow = new BPAnimationMenuView(context, this);
        addView(mMenuPopupWindow, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));

        mBPAnimationMultiWindow = new BPAnimationMultiWindow(context, this);
        addView(mBPAnimationMultiWindow, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));


        //mToolbar = (RelativeLayout) ((Activity) context).getLayoutInflater().inflate(R.layout.browser_toolbar, null);
        // addView(mToolbar, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, mToolbarHeight));

        mToolbarShadow = new ImageView(context);
        mToolbarShadow.setBackgroundResource(R.drawable.float_toolbar_shadow);
        //addView(mToolbarShadow, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,mToolBarShadowDis));

        mBigSnifferWebView = new WebView(mContext);
        mBigSnifferWebView.setVisibility(View.INVISIBLE);
        addView(mBigSnifferWebView, new FrameLayout.LayoutParams(10, 10));
        
        mNativeSearchPanel = new NativeSearchPanel(context);
        mNativeSearchPanel.setVisibility(View.VISIBLE);
        addView(mNativeSearchPanel, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));
        

        //initToolbar();
        //initFuncListener();
        initFloatSearch();

        //关闭sniffer功能
        //initSniffer();
    }

    public View getWindowWrapper() {
        return mWindowWrapper;
    }

    /**
     * @Title: initSniffer
     * @Description: 初始化嗅探
     * @param     设定文件
     * @return void    返回类型
     * @throws
     */
    private void initSniffer() {
        final ServiceFactory sf = ((BaseActivity)mContext).getPlayerApp().getServiceFactory();
        mSnifferHandler = new SnifferHandler(mContext, sf, new SnifferHandler.CallBack() {
            public void onComplete(SnifferResult result) {

                SaveResultArgs args = SnifferResultUtil.getInstance().saveToDatabase(sf, result, getCurrentWindow().getTitle());

                if (args != null) {

                    if (args.album.asBigServerAlbum() != null || args.album.asSmallServerAlbum() != null) {

                        Message msg = new Message();
                        msg.what = MSG_UPDATE_ALBUM_NUMBER;
                        msg.arg1 = args.number;
                        mHandler.sendMessage(msg);

                        if (args.number>0)
                            showPopDialog(args.number,args.album.getId());
                    }

                    if (args.album !=null) {
                        mCurrentWindow.setSnifferAlbumId(args.album.getId());
                    }

                    if (args.currentVideo != null && !StringUtil.isEmpty(args.currentVideo.getUrl())) {
                        // 插入浏览器首页
                        if (!HistoryConfig.isPrivateMode(mContext) && args.album.asSmallServerAlbum()!=null) {
                            args.album.setHomeShow(true);
                        }

                        logger.d("[PlayerLauncher startup]");

                        PlayerLauncher.startup(mContext, args.album, args.currentVideo);
                    }
                }

            }
            @Override
            public void onCancel(SnifferResult result) {
                logger.d("onCancel");
            }
        }, mBigSnifferWebView, /*mSmallSnifferWebView*/mBigSnifferWebView);
    }

    /**
     * 嗅探气泡
     * @param number
     * @param albumId
     */
    private void showPopDialog(int number, long albumId) {
        SharedPreferences preferences = mContext.getSharedPreferences("application", Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
        boolean bFirstSniffer = preferences.getBoolean("first_sniffer", true);
        if (bFirstSniffer) {
            Editor editor = preferences.edit();
            editor.putBoolean("first_sniffer", false);
            editor.commit();
            mBrowDialogTip = new BrowDialogTip(mContext);
            mBrowDialogTip.createDialog();
            if (mBrowDialogTip.isReady()) {
                mBrowDialogTip.showSnifferDialog(number,albumId);
            }
        }
    }

    /**
     * @Title: updateAlbumNumber
     * @Description: 更新嗅探结果小图标
     * @param  number    设定文件
     * @return void    返回类型
     * @throws
     */
    private void updateAlbumNumber(int number) {
        if (number == 0) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.browser_album_disable);
            ((ImageView)findViewById(R.id.browser_album)).setImageBitmap(bitmap);
            ((ImageView)findViewById(R.id.browser_album)).setEnabled(false);
        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.browser_album_normal);
            ((ImageView)findViewById(R.id.browser_album)).setImageBitmap(ImageUtil.createAlbumIcon(number, bitmap,mContext));
            ((ImageView)findViewById(R.id.browser_album)).setEnabled(true);
        }
        mCurrentWindow.setSnifferNumber(number);

    }

    /**
     * @Title: initToolbar
     * @Description: 初始化Toolbar的状态。
     */
    private void initToolbar() {
//        ImageButton backView = (ImageButton) mToolbar.findViewById(R.id.browser_back);
//        backView.setEnabled(false);
//
//        View forwardView = mToolbar.findViewById(R.id.browser_forward);
//        forwardView.setEnabled(false);
//
//        View album = mToolbar.findViewById(R.id.browser_album);
//        album.setEnabled(false);
//
//        //设置窗口数量
//        ImageView windowsCount = (ImageView) mToolbar.findViewById(R.id.browser_multiwindows);
//        //windowsCount.setEnabled(false);
//        windowsCount.setImageLevel(0);

    }

    /**
     * @Title: initFloatSearch
     * @Description: 初始化搜索框点击事件
     */
    private void initFloatSearch() {
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.SearchTextInput:
                        startSearchActivity(true);
                        break;
                    case R.id.brow_edit_search_first:
                        startSearchActivity(true);
                        break;
                    case R.id.float_clear_content:
                        Intent intent = new Intent(getContext(), SearchActivity.class);
                        intent.putExtra(SearchActivity.TAG_IS_START_FROM_SEARCH, true);
                        getContext().startActivity(intent);
                    	break;
                    case R.id.brow_top_mark:
                        insertOrDelmark();
                        break;
                    case R.id.brow_top_refresh_stop:
                        refreshOrStop();
                        break;
                    case R.id.searchbar_back_btn:
                        goBack();
                        break;
                    case R.id.searchbar_forward_btn:
                        if (!currentUrl.equals(BPBrowser.HOME_PAGE) && mCurrentWindow.getCurrentPageProgerss()!=0) {
                            stopLoading();
                            //goBack();
                        }	else
                            goForward();
                        break;
                    case R.id.searchbar_settings_btn:
						// if (!mMenuPopupWindow.isShow())
						// mMenuPopupWindow.showMenu(currentUrl, isBookmark);
						// else
						// mMenuPopupWindow.hideMenu();
            			Intent intentSettings = new Intent(mContext, SettingsActivity.class);
            			mContext.startActivity(intentSettings);

                        break;
                    case R.id.searchbar_home_btn:
                    	goHome();
                    	break;
                    	
                    case R.id.searchbar_voice_btn:
            			Intent voiceIntent = new Intent(mContext, VoiceSearchActivity.class);
            			voiceIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            			mContext.startActivity(voiceIntent);
                    	break;
                    default:
                        break;
                }
            }
        };

        mAdressTextInput = (TextView)mFloatSearch.findViewById(R.id.SearchTextInput);
        mBrowTopMark =  (ImageView)mFloatSearch.findViewById(R.id.brow_top_mark);
        mBrowTopRefreshStop =  (ImageView)mFloatSearch.findViewById(R.id.brow_top_refresh_stop);
        mAdressTextInput.setOnClickListener(listener);
        mAdressTextInput.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                startSearchActivity(true);
                return false;
            }
        });
        
        //mAdressTextInput.setEnabled(false);
        //mAdressTextInput.requestFocus();
        
        mClearAndMarkBtn = (LinearLayout)mFloatSearch.findViewById(R.id.clear_and_mark_btn);
        
        mFloatClearcontent = (ImageView)mFloatSearch.findViewById(R.id.float_clear_content);
        mFloatClearcontent.setOnClickListener(listener);
 
        mBrowTopMark.setOnClickListener(listener);
        mBrowTopRefreshStop.setOnClickListener(listener);
        
        mFloatSearch.findViewById(R.id.searchbar_back_btn).setOnClickListener(listener);
        mFloatSearch.findViewById(R.id.searchbar_forward_btn).setOnClickListener(listener);
        mFloatSearch.findViewById(R.id.searchbar_home_btn).setOnClickListener(listener);
        mFloatSearch.findViewById(R.id.searchbar_voice_btn).setOnClickListener(listener);
        mFloatSearch.findViewById(R.id.searchbar_settings_btn).setOnClickListener(listener);
    }

    /**
     * @Title: startSearchActivity
     * @Description: 进入搜索页面
     * @param isStartFromSearch 参数表示是否为点击搜索按钮进入
     */
    private void startSearchActivity(boolean isStartFromSearch) {
        Intent intent = new Intent(getContext(), SearchActivity.class);
        intent.putExtra(SearchActivity.TAG_IS_START_FROM_SEARCH, isStartFromSearch);
        intent.putExtra(SearchActivity.TAG_CURRENT_URL, currentUrl);
        getContext().startActivity(intent);
    }

    /**
     * 初始化各个功能按钮的监听。
     * */
    private void initFuncListener() {
        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View v) {

                boolean dissMissMenu = true;
                boolean dissMissMulti = true;
                switch (v.getId()) {
                    case R.id.browser_back:
                        mStat.incEventCount(Browser.Name, Browser.Back);
                        goBack();
                        break;
                    case R.id.browser_forward:
                        mStat.incEventCount(Browser.Name, Browser.Forward);
                        if (!currentUrl.equals(BPBrowser.HOME_PAGE) && mCurrentWindow.getCurrentPageProgerss()!=0) {
                            stopLoading();
                            //goBack();
                        }	else
                            goForward();
                        break;
                    case R.id.browser_bottom_menu:

                        mStat.incEventCount(Browser.Name, Browser.Menu);

                        dissMissMenu = false;
                        if (!mMenuPopupWindow.isShow())
                            mMenuPopupWindow.showMenu(currentUrl, isBookmark);
                        else
                            mMenuPopupWindow.hideMenu();

                        break;
                    case R.id.browser_album:

                        Intent intent = new Intent(mContext, BrowserSpecActivity.class);
                        intent.putExtra(IntentExtraKey.BrowSpecIsFromBdFrameView, true);
                        intent.putExtra(IntentExtraKey.BrowSpecAlbumId, mCurrentWindow.getSnifferAlbumId());
                        intent.putExtra(IntentExtraKey.BrowSpecName, mCurrentWindow.getTitle());

                        mContext.startActivity(intent);

                        break;
                    case R.id.browser_multiwindows:

                        dissMissMulti = false;
                        if (!mBPAnimationMultiWindow.isShow())
                            mBPAnimationMultiWindow.showMenu();
                        else
                            mBPAnimationMultiWindow.hideMenu();

                        break;
                    case R.id.browser_personal:


                        mContext.startActivity(new Intent(mContext, PersonalActivity.class));
                        break;
                    default:
                        break;
                }
                if (dissMissMenu && mMenuPopupWindow.isShow())
                    mMenuPopupWindow.hideMenu(false);
                if (dissMissMulti && mBPAnimationMultiWindow.isShow())
                    mBPAnimationMultiWindow.hideMenu(false);
            }
        };

//        mToolbar.findViewById(R.id.browser_back).setOnClickListener(listener);
//        mToolbar.findViewById(R.id.browser_forward).setOnClickListener(listener);
//        mToolbar.findViewById(R.id.browser_bottom_menu).setOnClickListener(listener);
//        mToolbar.findViewById(R.id.browser_album).setOnClickListener(listener);
//        mToolbar.findViewById(R.id.browser_multiwindows).setOnClickListener(listener);
//        mToolbar.findViewById(R.id.browser_personal).setOnClickListener(listener);

    }

    /**
     * 输入法键盘是否已弹出。使用时需要注意调用时机。
     * 判断方法是通过比较DecorView的底部和Layout的底部，若Layout的底部未达DecorView的底部，则视为输入法键盘弹出了
     * @param height layout高度
     * @return true 输入法键盘弹出
     */
    private boolean isInputMethodShowed(int height) {
        View root = getRootView();
        WindowManager winManager=(WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        final float windowHeight = winManager.getDefaultDisplay().getHeight();
        android.graphics.Rect outRect = new android.graphics.Rect();
        root.getWindowVisibleDisplayFrame(outRect);
        final int buffer = (int) (0.15f * outRect.height()); //增加一些余量，防止类似4.0顶部文字选择工具栏的情况
        return height + buffer < windowHeight - outRect.top;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //调起输入法时不显示工具栏，消失后回复
//        if (isInputMethodShowed(MeasureSpec.getSize(heightMeasureSpec))) {
//            mToolbar.setVisibility(View.GONE);
//            mToolbarShadow.setVisibility(View.GONE);
//        } else {
//            mToolbar.setVisibility(View.VISIBLE);
//            mToolbarShadow.setVisibility(View.VISIBLE);
//        }

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        final int count = getChildCount();
        int measureTop = 0;
        for (int i = 0; i < count; i++) {
            final View childView = getChildAt(i);
            if (childView.getVisibility() != GONE) {
                if (childView.equals(mFloatSearch)) {
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(childView.getLayoutParams().height,
                            MeasureSpec.EXACTLY);
                    measureTop += childView.getLayoutParams().height;
                } else if (childView.equals(mProgressBar)) {
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(childView.getLayoutParams().height,
                            MeasureSpec.EXACTLY);
                } else if (childView instanceof BdPopMenuGroup) {
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                } else if (childView instanceof BPWindow || childView instanceof BPWindowWrapper) {
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height - measureTop - mToolbarHeight,
                            MeasureSpec.EXACTLY);
                }
                else if (childView instanceof BPAnimationMenuView) {
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height - getToolBarHeight(),
                            MeasureSpec.EXACTLY);
                }
                else if (childView instanceof BPAnimationMultiWindow) {
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height - getToolBarHeight(),
                            MeasureSpec.EXACTLY);
                } else if (childView instanceof NativeSearchPanel) {
                	heightMeasureSpec= MeasureSpec.makeMeasureSpec(height,
                            MeasureSpec.EXACTLY);
                }
                else {
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(childView.getLayoutParams().height,
                            MeasureSpec.EXACTLY);
                }
                childView.measure(widthMeasureSpec, heightMeasureSpec);
            }
        }
        setMeasuredDimension(width, height);
    }

    /**
     * @Title: getToolBarHeight
     * @Description:返回工具栏高度
     * @return int
     */
    public int getToolBarHeight() {
//        if (mToolbar.getVisibility() == View.GONE) {
//            return 0;
//        }
//        return mToolbarHeight;
    	return 0;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int layoutTop = 0;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View childView = getChildAt(i);
            if (childView.getVisibility() != GONE) {
                int height = childView.getHeight();
                if (childView.equals(mTabbar)) {
                    childView.layout(0, layoutTop, getWidth(), layoutTop + height);
                    layoutTop += height;
                }else if (childView.equals(mFloatSearch)) {
                    childView.layout(0, layoutTop, getWidth(), layoutTop + height);
                    layoutTop += height;
                } else if (childView.equals(mSearchboxShadow)) {
                    childView.layout(0, layoutTop, getWidth(), layoutTop + height);
                } else if (childView.equals(mToolbarShadow)) {
                    childView.layout(0, getHeight() - mToolbarHeight - mToolBarShadowDis,
                            getWidth(), getHeight() - mToolbarHeight);
                } else if (childView instanceof ImageView) {
                    childView.layout(0, layoutTop, getWidth(), layoutTop + height);
                } else if (childView.equals(mProgressBar)) {
                    childView.layout(0, layoutTop - height / 2, getWidth(), layoutTop - height / 2 + height);
                } else if (childView instanceof BdPopMenuGroup) {
                    childView.layout(0, 0, getWidth(), getHeight());
                } 
//                else if (childView.equals(mToolbar)) {
//                    childView.layout(0, getHeight() - mToolbarHeight, getWidth(), getHeight());
//                }
                else if (childView instanceof BPAnimationMenuView) {
                    childView.layout(0, 0, getWidth(), getHeight() - getToolBarHeight());
                } else if (childView instanceof BPAnimationMultiWindow) {
                    childView.layout(0, 0, getWidth(), getHeight() - getToolBarHeight());
                } else if (childView instanceof NativeSearchPanel) {
                	childView.layout(0, 0, getWidth(), getHeight());
                } else {
                    childView.layout(0, layoutTop, getWidth(), getHeight() - getToolBarHeight());
                } 
            }
        }
    }

    /**
     * 点击返回时隐藏菜单，多窗口
     */
    public boolean hideMenuOrMultiWindow() {
        if (mMenuPopupWindow.isShow()) {
            mMenuPopupWindow.hideMenu();
            return true;
        }
        if (mBPAnimationMultiWindow.isShow()) {
            mBPAnimationMultiWindow.hideMenu();
            return true;
        }

        if (mBrowDialogTip!=null && mBrowDialogTip.isReady()&& mBrowDialogTip.isShow()) {
            mBrowDialogTip.hideDialog();
            return true;
        }

        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:

                if (mCurrentWindow != null
                        && mCurrentWindow.onKeyDown(keyCode, event)) {
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                if (isInputMethodShowed(getMeasuredHeight()))
                    Utility.hideInputMethod(mContext, this);
                else {
                    if (mBrowDialogTip!=null && mBrowDialogTip.isReady()&& mBrowDialogTip.isShow()) {
                        mBrowDialogTip.hideDialog();
                    } else if (mBPAnimationMultiWindow.isShow()) {
                        mBPAnimationMultiWindow.hideMenu(false);
                    }
                    //showSettingsMenu(mToolbar);
                }
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != mCurrentWindow) {
            mCurrentWindow.onActivityResult(requestCode, resultCode, data);
        }
    }



    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (mBrowDialogTip!=null && mBrowDialogTip.isReady()&& mBrowDialogTip.isShow()) {
            mBrowDialogTip.hideDialog();
        }
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 显示设置菜单
     * @param v 设置按钮
     */
    private void showSettingsMenu(View v) {
        if (mMenuPopupWindow.isShow())
            mMenuPopupWindow.hideMenu();
        else
            mMenuPopupWindow.showMenu(currentUrl, isBookmark);
    }

    /**
     * @Title: saveStateToBundle
     * @Description: 保存状态。
     * @param  savedState
     * @return void
     * @throws
     */
    public void saveStateToBundle(Bundle savedState) {
        if (savedState == null) {
            return;
        }

        for (BPWindow window : mWindowList) {
            window.saveStateToBundle(savedState);
        }
        savedState.putInt(WINDOW_SIZE, mWindowList.size());
        int curPos = 0;
        if (mCurrentWindow != null) {
            curPos = mCurrentWindow.getPostition();
        }
        savedState.putInt(CURRENT_WINDOW_POS, curPos);
    }

    /**
     * @Title: restoreFromBundle
     * @Description:浏览器页面回复状态
     * @param  savedInstanceState
     * @return void
     * @throws
     */
    public void restoreFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        mRestoredFromState = true;

        int size = savedInstanceState.getInt(WINDOW_SIZE);
        for (int i = 0; i < size; i++) {
            createWindow(false, savedInstanceState);
        }

        int currentPos = savedInstanceState.getInt(CURRENT_WINDOW_POS);

        if (currentPos >= size)
            currentPos = size -1;
        BPWindow window = mWindowList.get(currentPos);
        swapWindowToFocus(window);
    }

    /**
     * @Title: createWindow
     * @Description: 创建窗口
     * @param focus 是否切换
     * @return BPWindow
     */
    public BPWindow createWindow(boolean focus) {
        return createWindow(focus, null);
    }

    /**
     * @Title: createWindow
     * @Description: 创建窗口
     * @param focus 是否切换到该窗口
     * @param savedState 如果不为null，则从中恢复状态
     * @param
     * @return BdWindow
     * @throws
     */
    public BPWindow createWindow(boolean focus, Bundle savedState) {
        RelativeLayout.LayoutParams exploreLayout = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        exploreLayout.topMargin = mFloatPlayerSearchHeight;
        exploreLayout.bottomMargin = mToolbarHeight;

        BPWindow window = new BPWindow(getContext());
        window.setFrameView(this);
        window.setLayoutParams(exploreLayout);
        window.setPosition(mWindowList.size());
        //关闭嗅探功能
        //window.setBrowCallBack(mBrowserCallBack);
        mTabbar.addTab(window.mTabView);

        mWindowList.add(window);

        if (savedState == null) {
            window.loadInitailHome();
        } else {
            window.restoreFromBundle(savedState);
        }

        if (focus) {
            swapWindowToFocus(window);
        }

        return window;
    }
    
    /**
     * @Title: closeWindow 
     * @Description: 关闭窗口，传入需要关闭的窗口 
     * @param @param window    设定文件 
     * @return void    返回类型 
     * @throws
     */
    public void closeWindow(BPWindow window) {
    	int position  =  mWindowList.indexOf(window);
    	closeWindow(position);
    }

    /**
     * 关闭一个窗口
     * @param windowToClose 需要关闭的窗口
     * @param isBrowsing
     *            是否是在浏览状态下关闭窗窗口。
     *            若为true，且关闭的是当前窗口，则应根据关闭窗口的WindowBackType进行适当的跳转
     *            若为false，则不进行跳转，若为当前窗口，则返回列表中前一个窗口。
     */
    public void closeWindow(int position) {
        int nextPosition = 0;
        final int totalSize = mWindowList.size();
        final int currentPosition = mWindowList.indexOf(mCurrentWindow);
        if (position >= totalSize)
            return;

        BPWindow windowToClose = mWindowList.get(position);

        if (totalSize == 1) {
        	mNativeSearchPanel.setVisibility(View.VISIBLE);
            createWindow(true);
        } else if (currentPosition == position){
            if (position < totalSize -1)
                nextPosition = position + 1;
            else
                nextPosition = position -1;
            BPWindow windowNext = mWindowList.get(nextPosition);
            swapWindowToFocus(windowNext);
        }
 
        mTabbar.removeTab(windowToClose.mTabView);

        mWindowList.remove(position);
        windowToClose.release();

        // 设置窗口数量
        //ImageView windowsCount = (ImageView) findViewById(R.id.browser_multiwindows);
        //windowsCount.setImageLevel(mWindowList.size());
    }

    private BPWindow.BrowserCallBack mBrowserCallBack = new BPWindow.BrowserCallBack() {
        @Override
        public void sniffer(String url) {

            logger.d("[request url]" + url);

            // 【fix bug】重复调起播放器 BEGIN
            mSnifferHandler.cancel();
            initSniffer();
            // 【fix bug】重复调起播放器 END

            Message msg = new Message();
            msg.what = MSG_UPDATE_ALBUM_NUMBER;
            msg.arg1 = 0;
            mHandler.sendMessage(msg);

            if(!TextUtils.equals(url, BPBrowser.HOME_PAGE) && !TextUtils.isEmpty(url)) {
                if (mBigSnifferWebView != null) {
                    removeView(mBigSnifferWebView);
                    mBigSnifferWebView = new WebView(mContext);
                    mBigSnifferWebView.setVisibility(View.INVISIBLE);
                    addView(mBigSnifferWebView, new FrameLayout.LayoutParams(10, 10));
                }
                mSnifferHandler.request(mBigSnifferWebView, url);
            }
        }
    };

    /**
     * @Title: swapWindowToFocus
     * @Description: 直接切换至窗口
     * @param  aWindow
     */
    public void swapWindowToFocus(BPWindow aWindow) {
        swapWindowToFocus(aWindow, WindowStwitchAnimation.NONE, null);
    }

    /**
     * @Title: swapWindowToFocus
     * @Description: 切换窗口 ，切换动作，searchURL 为搜索url
     * @param window
     * @param mWindowStwitchAnimation
     * @param searchUrl
     */
    private void swapWindowToFocus(BPWindow window,WindowStwitchAnimation mWindowStwitchAnimation, String searchUrl) {
    	
        if (window != null && !window.equals(mCurrentWindow)) {
        	//window.mTabView.setEnabled(true);
            if (mCurrentWindow != null && mBrowser != null) {
                //Bundle b = mBrowser.getSearchBoxBundle(true);
                //mCurrentWindow.setSearchboxBundle(b);
                mCurrentWindow.onResume();
                mCurrentWindow.mTabView.setBackgroundResource(R.drawable.image_browser_label_unselected_bg);
            }
            mWindowWrapper.showWindow(window, mWindowStwitchAnimation, searchUrl);
            mCurrentWindow = window;
            //mCurrentWindow.setLastViewedTime(SystemClock.uptimeMillis());
            window.requestFocus();
            if (mCurrentWindow != null && mBrowser != null) {
                //mBrowser.onTabChangeFinished();
                String url = null;
                if (mCurrentWindow.isHomePage()) {
                    url = BPBrowser.HOME_PAGE;
                } else {
                    url = mCurrentWindow.getCurrentUrl();
                }
                switchBetweenHomeAndBrowser(url);
                mCurrentWindow.onResume();
            }
            
            window.mTabView.setBackgroundResource(R.drawable.image_browser_label_selected_bg);
            
            updateState(window);
        } else if (window == mCurrentWindow) {
        	
        	window.mTabView.setBackgroundResource(R.drawable.image_browser_label_selected_bg);
        	
            window.loadUrl(searchUrl);
        }
        
        //多标签跳转到当前标签
        mTabbar.ensureChildVisible(window.mTabView);
    }

    /**
     * @Title: onResume
     * @Description: 之所以在真正开始resume之前加上等待和准备这两步操作，
     * 				 是为了确保让CurrentWindow在Activity完全resume后的200毫秒 以后，再执行resume
     */
    public void onResume() {
        if (!mRestoredFromState) {
            mHandler.sendEmptyMessage(MSG_CURRENT_WINDOW_RESUME_WAITING);
        } else {
            mRestoredFromState = false;
            resumeCurrentWindow();
        }
    }

    /**
     * @Title: onResume
     * @Description: 之所以在真正开始resume之前加上等待和准备这两步操作，是为了确保让CurrentWindow在Activity完全resume后的200毫秒 以后，再执行resume
     */
    private  Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CURRENT_WINDOW_RESUME_WAITING:
                    sendEmptyMessage(MSG_CURRENT_WINDOW_RESUME_READY);
                    break;

                case MSG_CURRENT_WINDOW_RESUME_READY:
                    final long delayTime = 0;
                    sendEmptyMessageDelayed(MSG_CURRENT_WINDOW_RESUME_GO, delayTime);
                    break;

                case MSG_CURRENT_WINDOW_RESUME_GO:
                    if (mCurrentWindow == null) {
                        createWindow(true);
                    }
                    resumeCurrentWindow();
                    break;

                case MSG_CREATE_NEW_WINDOW_FROM_MULTIWINDOW:
                    createWindow(true);
                    break;

                case MSG_SWAP_WINDOW_TO_FOCUS_FROM_MULTI_WINDOW:
                    swapWindowToFocus(mWindowList.get(msg.arg1));
                    break;

                case MSG_UPDATE_ALBUM_NUMBER:
                    int number = msg.arg1;
                    updateAlbumNumber(number);

                    break;

                default:
                    break;
            }
        };
    };

    public void createWindowFromMultiWindow() {
        mHandler.sendEmptyMessage(MSG_CREATE_NEW_WINDOW_FROM_MULTIWINDOW);
    }

    public void swapWindowToFocusFromMultiWindow(int position) {
        Message msg = new Message();
        msg.what = MSG_SWAP_WINDOW_TO_FOCUS_FROM_MULTI_WINDOW;
        msg.arg1 = position;
        mHandler.sendMessage(msg);
    }

    /**
     * @Title: resumeCurrentWindow
     * @Description: 启动当前window
     */
    private void resumeCurrentWindow() {
        if (mCurrentWindow == null) {
            return;
        }
        mCurrentWindow.onResume();
        updateState(mCurrentWindow);

        if (mCurrentWindow.isHomePage()) {
            mCurrentWindow.getHomeView().onResume();
        } else {

        }
    }


    /**
     * @Title: setBrowser
     * @Description: 设置父 BPBrowser
     * @param  bpBrowser
     */
    public void setBrowser(BPBrowser bpBrowser) {
        this.mBrowser = bpBrowser;
    }

    /**
     * @Title: closeSelectedMenu
     * @Description: 关闭划屏菜单
     */
    public void closeSelectedMenu() {
        if (mCurrentWindow != null) {
            //mCurrentWindow.closeSelectedMenu();
        }
    }
    /**
     * @Title: onPause
     * @Description: BPFrameView 暂停
     */
    public void onPause() {
        if (mCurrentWindow != null) {
            mCurrentWindow.getHomeView().onPause();
        }
        for (int i = 0; i < mWindowList.size(); i++) {
            mWindowList.get(i).onPause();
        }
    }

    /**
     * @Title: freeMemory
     * @Description: 释放内存
     */
    public void freeMemory() {
        for (int i = 0; i < mWindowList.size(); i++) {
            mWindowList.get(i).freeMemory();
        }
    }

    /**
     * @Title: release
     * @Description: 释放内存
     */
    public void release() {
        for (BPWindow w:mWindowList) {
            w.release();
        }
        mWindowList.clear();
        if (mCurrentWindow != null) {
            mCurrentWindow.getHomeView().onDestroy();
        }
    }


    /**
     * @Title: goBack
     * @Description: 历史后退
     */
    public void goBack() {
        if (mCurrentWindow == null) {
            return;
        }
        if (mCurrentWindow.canGoBack()) {
            mCurrentWindow.goBack();
        }
        updateState(mCurrentWindow);
    }

    /**
     * @Title: goForward
     * @Description: 历史前进
     */
    public void goForward() {
        if (mCurrentWindow == null) {
            return;
        }
        mCurrentWindow.goForward();
        updateState(mCurrentWindow);
    }
    
    public void goHome() {
        if (mCurrentWindow == null) {
            return;
        }
        
        if (!mCurrentWindow.getUrl().equals(BPBrowser.HOME_PAGE)) {
        	mCurrentWindow.loadInitailHome();
        	updateState(mCurrentWindow);
        }
    }

    /**
     * @Title: canGoForward
     * @Description: 判断是否前进
     * @return boolean
     */
    public boolean canGoForward() {
        if (mCurrentWindow != null) {
            return mCurrentWindow.canGoForward();
        }
        return false;
    }

    /**
     * @Title: canGoBack
     * @Description: 判断是否后退
     * @return boolean
     */
    public boolean canGoBack() {
        if (mCurrentWindow != null) {
            return mCurrentWindow.canGoBack();
        }
        return false;
    }

    /**
     * @Title: loadUrl
     * @Description: 加载URL
     * @param url
     */
    public void loadUrl(String url) {
        if (mCurrentWindow != null) {
            mCurrentWindow.loadUrl(url);
            mCurrentWindow.requestFocus();
        } else {
            createNewWindowOpenUrl(url, null, true, null);
        }
    }

    /**
     * @Title: getCurrentWindow
     * @Description: 获取当前窗口
     * @return  BPWindow
     */
    public BPWindow getCurrentWindow() {
        return mCurrentWindow;
    }

    /**
     * @Title: createNewWindowOpenUrl
     * @Description: 创建窗口
     * @param url 需打开的url
     * @param current 请求新建窗口的窗口
     * @param loadImmediately 是否立即load
     * @param bundle 搜索框状态
     */
    public void createNewWindowOpenUrl(String url, BPWindow current, boolean loadImmediately,
                                       Bundle bundle) {
    	createWindow(true);
    	mCurrentWindow.loadUrl(url);
    	mCurrentWindow.requestFocus();
    	
    }

    /**
     * @Title: reload
     * @Description: 重新载入页面
     */
    public void reload() {
        if (mCurrentWindow == null) {
            return;
        }
        mCurrentWindow.reload();
    }

    /**
     * @Title: stopLoading
     * @Description: 停止加载
     */
    public void stopLoading() {
        if (mCurrentWindow == null) {
            return;
        }
        mCurrentWindow.stopLoading();
        mProgressBar.reset();
        updateState(mCurrentWindow);
    }

    /**
     * @Title: clearHistory
     * @Description: 清除历史
     */
    public void clearHistory() {
        if (mCurrentWindow == null) {
            return;
        }
        mCurrentWindow.clearHistory();
    }

    /**
     * @Title: setUpSelect
     * @Description: 进入选自模式
     */
    public void setUpSelect() {
        if (mCurrentWindow == null) {
            return;
        }
        mCurrentWindow.setUpSelect();
    }

    /**
     * @Title: getBrowser
     * @Description: 返回BPBrowser
     * @return  BPBrowser
     */
    public BPBrowser getBrowser() {
        return mBrowser;
    }

    /**
     * @Title: updateState
     * @Description: 刷新窗口状态 底部工具栏等
     * @param bpWindow
     */
    public void updateState(BPWindow bpWindow) {
        if (bpWindow == null) {
            return;
        }

        if (bpWindow.equals(mCurrentWindow)) {

            /** 工具条相关 */
            boolean canGoBack = canGoBack();
            boolean canGoForword = canGoForward();
            ImageView backView = (ImageView) mFloatSearch.findViewById(R.id.searchbar_back_btn);
            if (backView != null) {
                boolean enabled = false;
                if (canGoBack) {
                    backView.setImageResource(R.drawable.browser_back);
                    enabled = true;
                }
                backView.setFocusable(enabled);
                backView.setEnabled(enabled);
            }

            ImageView forwardView = (ImageView) mFloatSearch.findViewById(R.id.searchbar_forward_btn);
            if (forwardView != null) {
                forwardView.setEnabled(canGoForword);
                forwardView.setFocusable(canGoForword);
            }


            /** 进度条相关 */
            if (mCurrentWindow != null) {
                updateProgressBar();
				// if (mCurrentWindow.isShowLoadingIcon() &&
				// !mCurrentWindow.getUrl().equals(BPBrowser.HOME_PAGE)) {
				// mFloatSearch.showStopLoadingIcon();
				// } else {
				// mFloatSearch.hideStopLoadingIcon();
				// }

                ImageView refreshOrStop = (ImageView)mFloatSearch.findViewById(R.id.brow_top_refresh_stop);
                if (refreshOrStop != null) {
                    if (mCurrentWindow.isHomePage()) {
                        // TODO 更新刷新停止按钮状态和图标
                        refreshOrStop.setImageResource(R.drawable.brow_top_refresh);
                    } else {
                        if (mCurrentWindow.getCurrentPageProgerss() != 0) {
                            refreshOrStop.setImageResource(R.drawable.brow_top_stop_loading);
                        } else {
                            refreshOrStop.setImageResource(R.drawable.brow_top_refresh);
                        }
                    }
                }
            }

            // 设置窗口数量
            //ImageView windowsCount = (ImageView) findViewById(R.id.browser_multiwindows);
            //windowsCount.setImageLevel(mWindowList.size());

            //設置嗅探小圖標
            //updateAlbumNumber(mCurrentWindow.getSnifferNumber());

            // 状态栏相关
            currentUrl = mCurrentWindow.getUrl();
            if (StringUtil.isEmpty(currentUrl))
                currentUrl = mCurrentWindow.getCurrentUrl();

            isBookmark = mVisiteSiteManager.isBookmark(currentUrl);
            mBrowTopMark.setImageResource(isBookmark?R.drawable.brow_adress_search_mark_selected:R.drawable.brow_adress_search_mark_normal);

            if (!currentUrl.equals(BPBrowser.HOME_PAGE)&&Utility.isUrl(currentUrl)) {
                mAdressTextInput.setText(currentUrl);
                
                if(bpWindow.getIcon()==null)
                	mAdressTextInput.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(R.drawable.image_search_input_left_icon_earth), null, null, null);
                else {
                	Bitmap bm = Bitmap.createScaledBitmap(bpWindow.getIcon(), 20, 20, true);
                	
                	BitmapDrawable bd=new BitmapDrawable(bm);
                	mAdressTextInput.setCompoundDrawablesWithIntrinsicBounds(bd, null, null, null);
                }
                
                mClearAndMarkBtn.setVisibility(View.VISIBLE);
                
                mFloatSearch.findViewById(R.id.searchbar_home_btn).setEnabled(true);
                mBrowTopRefreshStop.setEnabled(true);
            }
            else if (currentUrl.equals(BPBrowser.HOME_PAGE)) {
                mAdressTextInput.setText("");
                mAdressTextInput.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(R.drawable.search_sug_keywords_normal), null, null, null);
                mClearAndMarkBtn.setVisibility(View.INVISIBLE);
                
                mFloatSearch.findViewById(R.id.searchbar_home_btn).setEnabled(false);
                mBrowTopRefreshStop.setEnabled(false);
            }
        }
    }

    /**
     * 更新进度条
     */
    private void updateProgressBar() {
        if (mCurrentWindow != null) {
            if (mCurrentWindow.isHomePage()) {
                mProgressBar.reset();
                return;
            }
            int progress = mCurrentWindow.getCurrentPageProgerss();
            FakeProgressBar progressBar = mProgressBar;
            int current = progressBar.getRealProgress();
            if (progress == BPBrowser.PROGRESS_MAX) {
                mProgressBar.hide();
                mCurrentWindow.setCurrentPageProgerss(0);
            } else if (current == 0 && progress != 0 && progress != BPBrowser.PROGRESS_MAX) {
                mProgressBar.start();
            } else if (current > progress && current > 0) {
                //当前进度比进条度设置的进度要小，进度条可能发生了重置。
                //一种常见的情况是进入加载中进入多窗口后返回后，加载一个新页面
                mProgressBar.start();
            }
            progressBar.setProgress(progress);

            int visiblity = progressBar.getVisibility();

            if (progress != 0 && progress != BPBrowser.PROGRESS_MAX && visiblity != VISIBLE ) {
                progressBar.setVisibility(VISIBLE);
            } else if (progress == 0 && visiblity == VISIBLE) {
                progressBar.reset();
            }
        }
    }

    /**
     * @Title: onSelectionSearch
     * @Description: 划词搜索
     * @param aSelection
     */
    public void onSelectionSearch(String aSelection) {
        mBrowser.onSelectionSearch(aSelection);
    }

    /**
     * @Title: openFileChooser
     * @Description: 上传文件 时打开文件夹
     * @param uploadMsg
     * @param acceptType
     */
    public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                String acceptType) {
        mBrowser.openFileChooser(uploadMsg, acceptType);
    }

    /**
     * @Title: switchBetweenHomeAndBrowser
     * @Description: 根据当前窗口状态，在home和SearchBrowser之间切换
     * @param url
     */
    public void switchBetweenHomeAndBrowser(String url) {
        if (mCurrentWindow != null) {
            boolean isHome = TextUtils.equals(url, BPBrowser.HOME_PAGE);
            if (!isHome) {
                mCurrentWindow.hideHomeView();
            } else if (isHome) {
                mCurrentWindow.showHomeView();
            }

            mCurrentWindow.requestFocus();
        }
    }

    /**
     * @Title: onInnerCreateNewWindow
     * @Description: 内部调用新窗口，这里指 ^^
     * @param bpWindow
     * @return
     * BPWindow
     */
    public BPWindow onInnerCreateNewWindow(BPWindow bpWindow) {
        return null;
    }

    /**
     * @Title: getWindowList
     * @Description: 获取窗口列表
     * @return List<BPWindow>
     */
    public List<BPWindow> getWindowList() {
        return mWindowList;
    }

    /**
     * @Title: webviewScrollBy
     * @Description: 控制webview滚动
     * @param x
     * @param y
     */
    public void webviewScrollBy(int x, int y) {
        if (mCurrentWindow == null) {
            return;
        }
        mCurrentWindow.webviewScrollBy(x, y);
    }

    /**
     * @Title: webviewScrollTo
     * @Description: 控制webview滚动
     * @param x
     * @param y
     */
    public void webviewScrollTo(int x, int y) {
        if (mCurrentWindow == null) {
            return;
        }
        mCurrentWindow.webviewScrollTo(x, y);
    }

    /**
     * @Title: addWebViewTitle
     * @Description: 加入webview headView
     * @param aView
     */
    public void addWebViewTitle(View aView) {
        if (mCurrentWindow == null) {
            return;
        }
        mCurrentWindow.setEmbeddedTitleBar(aView);
    }

    public void onDownloadStart(String url, String userAgent,
                                String contentDisposition, String mimetype, long contentLength) {
        // TODO Auto-generated method stub

    }

    /**
     * 插入删除书签
     */
    public void insertOrDelmark() {
        if (!currentUrl.equals(BPBrowser.HOME_PAGE)) {
            isBookmark = mVisiteSiteManager.insertOrDelmark(currentUrl);
            mBrowTopMark.setImageResource(isBookmark?R.drawable.brow_adress_search_mark_selected:R.drawable.brow_adress_search_mark_normal);
            Toast.makeText(mContext, isBookmark?R.string.addBookmark:R.string.deleteBookmark, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 刷新当前网页 或 停止载入
     */
    public void refreshOrStop() {
        if (!currentUrl.equals(BPBrowser.HOME_PAGE)) {
            if (mCurrentWindow.getCurrentPageProgerss() != 0) {
                ((ImageView)mFloatSearch.findViewById(R.id.brow_top_refresh_stop)).setImageResource(R.drawable.brow_top_refresh);
                stopLoading();
                //goBack();
            }
            else
                reload();
        }
    }

	public void handleVoiceSearch(int voiceFrom, String[] sug) {
		mNativeSearchPanel.setVisibility(View.INVISIBLE);
		switch (voiceFrom) {
		 
		case VoiceSearchActivity.EXTRA_START_FROM_NATIVE_PANEL:
			break;
		case VoiceSearchActivity.EXTRA_START_FROM_MAIN_PANEL:
			
			break;
		case VoiceSearchActivity.EXTRA_START_FROM_SEARCH_PANEL:
			
			break;

		default:
			break;
		}
	}

}
