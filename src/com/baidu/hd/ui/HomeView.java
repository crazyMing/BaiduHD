package com.baidu.hd.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.browser.SearchManager;
import com.baidu.browser.db.HistoryConfig;
import com.baidu.browser.framework.BPFrameView;
import com.baidu.browser.visitesite.SearchKeyword;
import com.baidu.browser.visitesite.SearchKeywordManager;
import com.baidu.hd.BaseActivity;
import com.baidu.hd.PlayerhomeFloatDialog;
import com.baidu.hd.adapter.HomeRecentAdapter;
import com.baidu.hd.ctrl.PopupDialog;
import com.baidu.hd.ctrl.PopupDialog.ReturnType;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.personal.PersonalConst;
import com.baidu.hd.player.PlayerLauncher;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.util.Utility;
import com.baidu.hd.R;

/**
 * @ClassName: HomeView 
 * @Description:
 *  Home页以前是一个Fragment（Home类），经整理后融进浏览框架后，成为BPWindow的一个子孩子（HomeRoot->HomeView）
 *  HomeView在本应用中应该是一个单实例的View，请各位RD不要使用构造方法或者xml生成HomeView实例，应该使用getInstance()获得。
 *  HomeView将展示主页部分的UI内容：
 *  不再包括：搜索框、工具栏
 * @author LEIKANG 
 * @date 2012-12-5 下午3:36:24
 */
public class HomeView extends RelativeLayout implements OnClickListener, OnItemClickListener, OnItemLongClickListener{
    
    /** log tag. */
    private static final String TAG = "HomeView";
    /** debug 开关. */
    private static final boolean DEBUG = false;
 
    /** 父BdFrameView **/
    private BPFrameView mFrameView;
    
    /** 静态单实例引用 */
    private static HomeView sInstance;
    
    private Context mContext;
    
    /** 导航栏 滚动区域 */
    private HomeScrollView mHomeScrollView;
    
    private ImageView mScreenIndicator;
    
    /** 导航栏下方布局*/
    private RelativeLayout playerboxsRecommended = null;
    
    private LinearLayout mSearchRanking;
    private LinearLayout mHotRanking;
    private LinearLayout mWebsiteHistory;
    
    private Button mSearchRankingButton;
    private Button mHotRankingButton;
    private Button mWebsiteHistoryButton;
    
    private LinearLayout mLastPlayHistoryListWithClear;
    private Button mLastPlayHistoryClear;
    private LinearLayout mWebsiteHistoryListWithClear;
    private Button mPlayerhomeDeleteAllsite_btn;
    
    private ListView mSearchRankingList;
    private GridView mLastPlayHistoryList;
    private GridView mHotRankingList;
    private ListView mWebsiteHistoryList;
    
	private String mWebsiteLongClickUrl;
    
    
   	/* 最近播放为空时，显示text */
   	private TextView mHomeRecentEmpty;
   	
    
	private HomeRecentAdapter mHomeLastPlayRecentAdapter;
    
	
	
	private int mClickOrLongclickPosition = 0;
    
    /** 
     * 获得单实例的引用
     * @param context Context 
     * @return HomeView
     */
    public static HomeView getInstance(Context context) {
        if (null == sInstance) {
            sInstance = (HomeView) ((Activity) context).findViewById(R.id.homeview_id);
            if (sInstance == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                sInstance = (HomeView) inflater.inflate(R.layout.homeview, null);
                sInstance.setId(R.id.homeview_id);
            }
        }
        return sInstance;
    }
    
    /**
     * Constructor
     * @param context context
     */
    public HomeView(Context context) {
        super(context);
        checkInstance(context);
    }
    
    /**
     * Constructor
     * @param context context
     * @param attrs attrs
     */
    public HomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        checkInstance(context);
    }
 
    
    /**
     * Constructor
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public HomeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        checkInstance(context);
    }
    
    /**
     * 确保HomeView是单实例
     * 
     * @param context Context
     */
    private void checkInstance(Context context) {
        if (null == sInstance) {
        	mContext = context;
        } else {
            throw new RuntimeException("HomeView should be Single Instance.");
        }
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initUI();
        initNetContent();
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
 
    }
    
    /**
     * 设置BdFrameView
     * 
     * @param aFrameView
     *            BdFrameView
     */
    public void setFrameView(BPFrameView aFrameView) {
        mFrameView = aFrameView;
    }
    
    /**
     * 获得BdFrameView
     * @return mFrameView
     */
    public BPFrameView getFrameView() {
        return mFrameView;
    }
    
    /**
     * 初始化UI上的元素
     */
    private void initUI() {
 
    }
 
    
    /**
     * 更新快速导航指示器
     * @param total 总屏数
     * @param current 当前屏
     */
    public void updateScreenIndex(int total, int current) {
    	
        if (total < 2) {
            mScreenIndicator.setVisibility(View.GONE);
            return;
        }
        mScreenIndicator.setVisibility(View.VISIBLE);
        
        TabIndicator tabIndicator = (TabIndicator) mScreenIndicator.getDrawable();
        tabIndicator.setIndicatorCount(total);
        tabIndicator.setSelectedIndex(current);
    }
    
    
    private void setPlayerBoxRecommended() {
    	((BaseActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebsiteViewHolder holder = new WebsiteViewHolder();
                holder.state = 0;
				mSearchRankingButton.setTag(holder);
				mSearchRankingButton.setVisibility(View.VISIBLE);
				holder = new WebsiteViewHolder();
                holder.state = 0;
				holder = new WebsiteViewHolder();
                holder.state = 0;
				mHotRankingButton.setTag(holder);
				mHotRankingButton.setVisibility(View.VISIBLE);
				holder = new WebsiteViewHolder();
                holder.state = 0;
				mWebsiteHistoryButton.setTag(holder);
				mWebsiteHistoryButton.setVisibility(View.VISIBLE);
				
				playerboxsRecommended.setVisibility(View.VISIBLE);
            }
        });
    }
    
    class WebsiteViewHolder {
        /** 名称*/
        String name;
        /** url*/
        String url;
        /** 状态. 正常|按下*/
        int state;
    }

	public void onPause() {
	}

	public void onResume() {
		updateRecentGrid();
		updateWebsiteList();
	}

	public void onDestroy() {
        sInstance = null;
	}

	@Override
	public void onClick(View v) {
		WebsiteViewHolder info = (WebsiteViewHolder) v.getTag();
		
		if (v == mSearchRankingButton) {
			updatePlayerBoxListView(mSearchRanking, info, mSearchRankingList);
			updatePlayerBoxClickStausUI(info.state, mSearchRankingButton);
		}  else if (v == mHotRankingButton) {
			updatePlayerBoxListView(mHotRanking, info, mHotRankingList);
			updatePlayerBoxClickStausUI(info.state, mHotRankingButton);
		} else if (v == mWebsiteHistoryButton) {
			updatePlayerBoxListView(mWebsiteHistory, info, mWebsiteHistoryListWithClear);
			updatePlayerBoxClickStausUI(info.state, mWebsiteHistoryButton);
		} else if (v == mLastPlayHistoryClear) {
			PopupDialog dialog = new PopupDialog((BaseActivity)mContext,new PopupDialog.Callback() {
						public void onReturn(ReturnType type, boolean checked) {
							if (type == PopupDialog.ReturnType.OK)
								deleteAllPlayHistory();
						}
					});
			dialog.setTitle(dialog.createText(R.string.title_delete_all_history))
				  .setMessage(dialog.createText(R.string.confirm_clean_play_history))
				  .setPositiveButton(dialog.createText(R.string.ok))
				  .setNegativeButton(dialog.createText(R.string.cancel)).show();
		} else if (v == mPlayerhomeDeleteAllsite_btn) {
			deleteAllUrlWebsites();
		}

	}
	
	/** 删除所有播放站点 */
	public void deleteAllUrlWebsites() {
		PopupDialog dialog = new PopupDialog((BaseActivity)mContext,new PopupDialog.Callback() {
			public void onReturn(ReturnType type, boolean checked) {
				if (type == PopupDialog.ReturnType.OK) {
				}
			}
		});
	  dialog.setTitle(dialog.createText(R.string.title_delete_all_history))
	  .setMessage(dialog.createText(R.string.delete_history_warning_all))
	  .setPositiveButton(dialog.createText(R.string.delete_all))
	  .setNegativeButton(dialog.createText(R.string.cancel)).show();
	}
	
    private void updatePlayerBoxListView(View v, WebsiteViewHolder info, View listview) {
        if (listview == null || listview.getParent() == null) {
            return;
        }
        ViewGroup parent =  (ViewGroup) (listview.getParent());
        if (info.state == 0) {
        		parent.setVisibility(View.VISIBLE);
                parent.setMinimumHeight(listview.getHeight());
                mHomeScrollView.spanView(parent, v);
                listview.requestFocus();
            info.state = 1;
        } else {
            parent.setVisibility(View.GONE);
            info.state = 0;
        }
 
    }
    
    /**
     * @Title: updatePlayerBoxClickStausUI 
     * @Description: 更新点击button背景 
     * @param @param info
     * @param @param button    设定文件 
     * @return void    返回类型 
     * @throws
     */
    private void updatePlayerBoxClickStausUI (int  state , Button button) {
        Resources resources = mContext.getResources();
        if (state == 0) {
            //显示正常背景
        	button.setBackgroundResource(R.drawable.website_bottom_right_selector);
        	button.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.list_title), null, resources.getDrawable(R.drawable.into_icon), null);
        	button.setPadding(resources.getDimensionPixelSize(R.dimen.website_padding_left),0, 
                    resources.getDimensionPixelSize(R.dimen.website_padding_right),0);
        } else {
            //显示被按下背景
        	button.setBackgroundResource(R.drawable.website_bottom_press);
        	
        	button.setPadding(resources.getDimensionPixelSize(R.dimen.website_padding_left),0, 
                    resources.getDimensionPixelSize(R.dimen.website_padding_right),0);
        }
    }
    
    /**
     * @Title: initNetContent 
     * @Description: 网络拉取数据
     * @param     设定文件 
     * @return void    返回类型 
     * @throws
     */
    private void initNetContent() {
    }
    
	/**
	 * 更新最近播放列表
	 */
	public void updateRecentGrid() {
 
	}
	
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		updateRecentGrid();
	}

	/**
	 * 更新播放站点记录
	 */
	public void updateWebsiteList() {
	}
	
	/** 删除播放历史 */
	public void deletePlayHistoryNetVideo() {
		PlayListManager playListManager = (PlayListManager) ((BaseActivity)mContext).getServiceProvider(PlayListManager.class);
		playListManager.removeHomeShowAlbum(((Album) mHomeLastPlayRecentAdapter.getItem(mClickOrLongclickPosition)));
		updateRecentGrid();
	}
	
	public void deleteAllPlayHistory() {
		PlayListManager playListManager = (PlayListManager) ((BaseActivity)mContext).getServiceProvider(PlayListManager.class);
		playListManager.removeAllHomeShowAlbum();
		updateRecentGrid();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		mClickOrLongclickPosition = position;
	}
	
	
	/** 播放历史 */
	public void startPlayHistoryNetVideo() {
		Album album = (Album) mHomeLastPlayRecentAdapter.getItem(mClickOrLongclickPosition);
		NetVideo video = album.getCurrent();
		PlayerLauncher.startup(mContext,((Album) mHomeLastPlayRecentAdapter.getItem(mClickOrLongclickPosition)), video);
	}
	
	/** 加载URL*/
	public void loadUrlOfWebsite() {
		mWebsiteLongClickUrl = Utility.addSchemeIfNeed(mWebsiteLongClickUrl);
		SearchManager.launchURL(mContext, Utility.fixUrl(mWebsiteLongClickUrl));
	}
	
	
	/** 删除站点*/
	public void deleteUrlWebsite() {
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View v, int position,
			long arg3) {
		mClickOrLongclickPosition = position;
		Intent intent = new Intent(mContext, PlayerhomeFloatDialog.class);
		/** 长按播放记录item */
		if (v.getId() == R.id.brow_home_list_item) {
			TextView tv = (TextView) v.findViewById(R.id.brow_home_list_item_address);
			mWebsiteLongClickUrl = tv.getText().toString();
			intent.putExtra(PlayerhomeFloatDialog.PLAYER_HOME_DIALOG_TYPE,PlayerhomeFloatDialog.WEBSITE);
		} else if (v.getId() == R.id.homerecent_grid_item){ // 长按播放站点item
			intent.putExtra(PlayerhomeFloatDialog.PLAYER_HOME_DIALOG_TYPE,PlayerhomeFloatDialog.HISTORY);
		}
		((BaseActivity)mContext).startActivityForResult(intent, PersonalConst.MESSAGE_MENU_DIALOG);
		return true;
	}
	
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (resultCode) {
		case PlayerhomeFloatDialog.PLAYERHOME_FLOAT_PLAYNOW:
			startPlayHistoryNetVideo();
			break;
		case PlayerhomeFloatDialog.PLAYERHOME_FLOAT_DELETEFILE:
			deletePlayHistoryNetVideo();
			break;
		case PlayerhomeFloatDialog.PLAYERHOME_FLOAT_OPENSITE:
			loadUrlOfWebsite();
			break;
		case PlayerhomeFloatDialog.PLAYERHOME_FLOAT_DELETESITE:
			deleteUrlWebsite();
			break;
		default:
			break;
		}
	}
	
}
