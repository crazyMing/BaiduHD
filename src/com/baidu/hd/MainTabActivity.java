package com.baidu.hd;

import java.util.ArrayList;
import java.util.List;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TextView;

import com.baidu.hd.detect.Detect;
import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.log.Logger;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId;
import com.baidu.hd.util.Const;
import com.baidu.hd.R;

public class MainTabActivity extends TabActivity implements OnCheckedChangeListener
{
	private final static String TAB_HOT = "hot";
	private final static String TAB_RANK = "rank";
	private final static String TAB_PERSONAL = "personal";
	private final static String TAB_MORE = "more";
	private final static String TAB_BROWSER = "browser";	
	private class TabData {
		
		private int id = 0;
		private int index = 0;
		private String name = "";
		private Class target = null;
		
		public TabData(int id, int index, String name, Class target) {
			this.id = id;
			this.index = index;
			this.name = name;
			this.target = target;
		}
		public int getId() {
			return id;
		}
		public int getIndex() {
			return index;
		}
		public String getName() {
			return name;
		}
		public Class getTarget() {
			return target;
		}
	}
	
	private Logger logger = new Logger("MainTabActivity");
	private RadioGroup mainTab = null;

	private List<TabData> mTabs = new ArrayList<TabData>();
	
	private static boolean isOnResumeAnimation = false;
	private static boolean isOnPauseAnimation = false;
	
	/** 是否为用户手动返回到首页的 */
	private boolean isUserBack = false;
	
	/** 百度HD按钮 */
	private TextView mBtnBrowser = null;
	
	/** 记录进入浏览器模式前的页面索引 */
	private int mLastTabIndex = 0;
	
	/** 是否通过事件跳转到浏览器模式首页 */
	private boolean mIsEventToBrowserHome = false;
	
	private ServiceContainer.Callback mServiceContainerCallback = new ServiceContainer.Callback()
	{
		@Override
		public void onServiceCreated()
		{
			// TODO Auto-generated method stub
			EventCenter eventCenter = (EventCenter)getServiceFactory().getServiceProvider(EventCenter.class);
			eventCenter.addListener(mEventListener);
		}
	};
	
	/*
	 * 标签
	 */
	public static class MainTabResultArgs extends EventArgs 
	{
		public MainTabResultArgs(int tab) 
		{
			this.tabIndex = tab;
		}

		private int tabIndex = -1;
		
		public int getTable() 
		{
			return this.tabIndex;
		}
	};
	
	/** 显示或者隐藏Tab */
	public static class MainTabHideOrShow extends EventArgs {
		private boolean shouldHide = false;
		public MainTabHideOrShow(boolean shouldHide) {
			this.shouldHide = shouldHide;
		}
		public boolean shouldHide() {
			return shouldHide;
		}
	}
	
	private EventListener mEventListener = new EventListener() 
    {
		@Override
		public void onEvent(EventId id, EventArgs args) 
		{
			switch (id) 
			{
				case eMainTabActivity:
					logger.d("onEvent eMainTabActivity");
					isUserBack = true;
					MainTabResultArgs suggestArgs = (MainTabResultArgs) args;
					if (suggestArgs.getTable() == -1) {
						getTabHost().getCurrentView().startAnimation(AnimationUtils.loadAnimation(MainTabActivity.this, R.anim.out_to_bottom));
						setTable(mLastTabIndex);
						getTabHost().getCurrentView().startAnimation(AnimationUtils.loadAnimation(MainTabActivity.this, R.anim.in_from_top));
					}
					else {
						setTable(suggestArgs.getTable());
					}
					
					showTabs(false);
					isUserBack = false;
					break;
				
				case eMainTabsHideOrShow:
					logger.d("onEvent eMainTabsHideOrShow");
					MainTabHideOrShow shouldHide = (MainTabHideOrShow)args;
					if (getTabHost().getCurrentTabTag().equals(TAB_BROWSER)) {
						showTabs(true);
					}
					else {
						showTabs(shouldHide.shouldHide());
					}
					break;
					
				case eBrowserHomeActivity:
					logger.d("onEvent eBrowserHomeActivity");
//					if (args instanceof BrowserHomeActivityResultArgs) {
//						BrowserHomeActivityResultArgs bharArgs = (BrowserHomeActivityResultArgs)args;
//						if (bharArgs.getMode() == Mode.UNKNOWN) {
//							if (getStat() != null) {
//								getStat().incEventCount(StatId.TabClick.Name, StatId.TabClick.InputUrl);
//							}
//							return;
//						}
//						else if (bharArgs.getMode() == Mode.MAIN_TO_MAIN) {
//							toBrowserHome();
//							if (bharArgs.IsContine()) {
//								EventCenter eventCenter = (EventCenter)getServiceFactory().getServiceProvider(EventCenter.class);
//								eventCenter.fireEvent(EventId.eBrowserHomeActivity, new BrowserHomeActivityResultArgs(Mode.UNKNOWN, bharArgs.getUrl(), true));
//							}
//							bharArgs.setContine(false);
//						}
//						else if (bharArgs.getMode() == Mode.OTHER_TO_MAIN) {
//							mIsEventToBrowserHome = true;
//						}
//					}
			}
		}
	};	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	logger.d("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
//        setContentView(R.layout.main_tab);
//        this.mainTab = (RadioGroup)findViewById(R.id.main_tab);
        this.mainTab.setOnCheckedChangeListener(this);
        setupTabs();
        
        int tabIndex = 0;
		if (((BaiduHD)getApplication()).getServiceContainer().isCreated())
		{
	        EventCenter eventCenter = (EventCenter)getServiceFactory().getServiceProvider(EventCenter.class);
			eventCenter.addListener(mEventListener);
			if (!((Detect)getServiceFactory().getServiceProvider(Detect.class)).isNetAvailabe())
			{
				//切换到本地标签的本地视频
//				tabIndex = 2;
//				SharedPreferences preferences = getSharedPreferences("application", Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
//				String tabTag = preferences.getString("personal_tab_switch", PersonalTabActivity.TAB_LOCAL);
//				if (!tabTag.equals(PersonalTabActivity.TAB_BUFFER)) {
//					preferences.edit().putString("personal_tab_switch" , PersonalTabActivity.TAB_LOCAL).commit();
//				}
			}
		}
		else
		{
			((BaiduHD)getApplication()).setCreateServiceCallback(mServiceContainerCallback);
		}
		setTable(tabIndex);
		
		// 百度HD按钮
//		mBtnBrowser = (TextView)findViewById(R.id.main_browser_btn);
		mBtnBrowser.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toBrowserHome();
				if (getStat() != null) {
					getStat().incEventCount(StatId.TabClick.Name, StatId.TabClick.Browser);
				}
			}
		});
    }
	
	@Override
	protected void onNewIntent(Intent intent)
	{
		logger.d("onNewIntent()");
		super.onNewIntent(intent);
		setIntent(intent);
		if (null != intent && ((BaiduHD)getApplication()).getServiceContainer().isCreated())
		{
//			int isToBuffer = intent.getIntExtra(Const.IntentExtraKey.LocalActivity, -1);
//			if (isToBuffer != -1)
//			{
//				setTable(2);
//				SharedPreferences preferences = getSharedPreferences("application", Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
////				String tabTag = preferences.getString("personal_tab_switch", PersonalTabActivity.TAB_BUFFER);
////				preferences.edit().putString("personal_tab_switch" , PersonalTabActivity.TAB_BUFFER).commit();
//			}
		}
	}
    
    public static void setOnResumeAnimation (boolean isAnimation)
    {
    	MainTabActivity.isOnResumeAnimation = isAnimation;
    }
    
    public static void setOnPauseAnimation (boolean isAnimation)
    {
    	MainTabActivity.isOnPauseAnimation = isAnimation;
    }

	@Override
	protected void onResume() 
	{
		logger.d("onResume");
		if (MainTabActivity.isOnResumeAnimation)
		{
			//overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			MainTabActivity.isOnResumeAnimation = false;
		}
		super.onResume();
		
		if (mIsEventToBrowserHome) {
			toBrowserHome();
			mIsEventToBrowserHome = false;
		}
	}
	
	@Override
	protected void onPause() 
	{
		logger.d("onPause");
		if (MainTabActivity.isOnPauseAnimation)
		{
			//overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			MainTabActivity.isOnPauseAnimation = false;
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		logger.d("onDestroy");
		super.onDestroy();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) 
	{
		logger.d("onCheckedChanged");
		for(TabData td: this.mTabs) {
			if(checkedId == td.getId()) {
				if(getTabHost().getCurrentTab() != td.index) {					
					getTabHost().setCurrentTabByTag(td.getName());
					stat();
				}
				break;
			}
		}
	}
	
	private ServiceFactory getServiceFactory() {
		return (ServiceFactory)((BaiduHD)getApplication()).getServiceFactory();
	}
    
	public void setTable(int tab) 
	{
		for(TabData td: this.mTabs) {
			if(tab == td.getIndex()) {
				this.mainTab.check(td.getId());
				break;
			}
		}
	}
	
	/** 隐藏显示Tab */
	public void showTabs(boolean shouldHide) {
		
//		View view = findViewById(R.id.main_tab_layout);
//		if(view == null) return;
//		view.setVisibility(shouldHide ? View.GONE : View.VISIBLE);
	}
	
	private void setupTabs()
	{
//		this.mTabs.add(new TabData(R.id.radio_button0, 0, TAB_HOT, HomeActivity.class));
//		this.mTabs.add(new TabData(R.id.radio_button1, 1, TAB_RANK, SearchActivity.class));
//		this.mTabs.add(new TabData(R.id.radio_button2, 2, TAB_PERSONAL, PersonalTabActivity.class));
//		this.mTabs.add(new TabData(R.id.radio_button3, 3, TAB_MORE, SettingsActivity.class));
		
		for (TabData td : this.mTabs)
		{
			TabHost.TabSpec spec = getTabHost().newTabSpec(td.getName()).setIndicator(td.getName()).setContent(new Intent(this, td.getTarget()));
			getTabHost().addTab(spec);
		}
		// 补丁：浏览器模式主页
//		TabHost.TabSpec spec = getTabHost().newTabSpec(TAB_BROWSER).setIndicator(TAB_BROWSER).setContent(new Intent(this, BrowserHomeActivity.class));
//		getTabHost().addTab(spec);
	}
	
	private void toBrowserHome() {
		
		// 1.radio button 全部置灰
		this.mainTab.clearCheck();
		showTabs(true);
		
		// 2.跳转
		// 原来就在历史模式首页标签页上，则不进行记录
		if (!getTabHost().getCurrentTabTag().equals(TAB_BROWSER)) {
			mLastTabIndex = getTabHost().getCurrentTab();
		}
		
		// 通过事件跳转，无动画
		if (mIsEventToBrowserHome) {
			getTabHost().setCurrentTabByTag(TAB_BROWSER);
		}
		else {
			getTabHost().getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.out_to_top));
			getTabHost().setCurrentTabByTag(TAB_BROWSER);
			getTabHost().getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.in_from_bottom));
		}
	}
	
	private void stat() {
		if(!((BaiduHD)getApplication()).getServiceContainer().isCreated()) {
			return;
		}
		ServiceFactory serviceFactory = ((BaiduHD)getApplication()).getServiceFactory();
		if(serviceFactory == null) {
			return;
		}
		// 用户手动返回回来的网页不统计
		if(isUserBack) {
			return;
		}
		Stat stat = (Stat)serviceFactory.getServiceProvider(Stat.class);
		if(stat == null) {
			return;
		}
		switch(getTabHost().getCurrentTab()) {
			case 0:
				stat.incLogCount(StatId.HomeTab);
				stat.incEventCount(StatId.TabClick.Name, StatId.TabClick.Home);
				break;
			case 1:
				stat.incLogCount(StatId.RankTab);
				stat.incEventCount(StatId.TabClick.Name, StatId.TabClick.Rank);
				break;
			case 2:
			//	stat.incLogCount(StatId.LocalTab);
				stat.incEventCount(StatId.TabClick.Name, StatId.TabClick.Personal);
				break;
			case 3:
				stat.incLogCount(StatId.MoreTab);
				stat.incEventCount(StatId.TabClick.Name, StatId.TabClick.More);
				break;
		}
	}
	
	private Stat getStat() {
		if(!((BaiduHD)getApplication()).getServiceContainer().isCreated()) {
			return null;
		}
		ServiceFactory serviceFactory = ((BaiduHD)getApplication()).getServiceFactory();
		if(serviceFactory == null) {
			return null;
		}
		
		return (Stat)serviceFactory.getServiceProvider(Stat.class);
	}
}