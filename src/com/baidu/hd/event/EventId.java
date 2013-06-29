package com.baidu.hd.event;

public enum EventId {
	
	eNone,
	eAll,
	
	eFatal,	/** 程序出现不可恢复错误，必须停止 */
	
	eAdressleisteSuggestList,/** 地址栏suggest列表 */
	eSearchKeyWordBySearchSite,/** 即将搜索的关键词 */
	eSearchKeyWordByHomeSite,/** 即将搜索的关键词 */
	eStartSearchSiteActivity,/** 打开搜索结果页 */
	
	eMainTabActivity,/** 切换MainTab里面的activity */
	eLocalTabActivity,/** 切换LocalTab里面的activity */
	
	/** 下载服务死了 */
	eDownloadServiceDie,
	
	/** 个人面板标签切换 */
	ePersonalSwitchTab,
	
	/**
	 * 播放列表更新
	 * 接收者：播放历史界面，本地界面
	 */
	ePlayListUpdate,
	
	/**
	 * 首页导航栏，排行榜，热词 更新通知
	 */
	eHomeHotRankingDetailUpdate,
	eHomeSearchRankingDetailUpdate,
	eHomeNavigationDetailUpdate,
	
	eNetStateChanged,
	eSDCardStateChanged,
	
	/** 反馈完成 */
	eFeedBackComplete,

	/** 图片需要重新加载 */
	eImageNeedReload,
	
	/** app 重新更新 */
	eFriendAppNeedUpdate,
	eMoreAppNeedUpdate,
	
	/** 升级监测完成 */
	eUpgradeCheckCancel,
	eUpgradeCheckComplete,
	ePlayCoreCheckComplete,

	/** 播放事件 */
	eStartPlay,
	eStopPlay,
	ePlayFail,

	eTaskCreate,
	eTaskStart,
	eTaskQueue,
	eTaskStop,
	eTaskRemove,
	eTaskComplete,
	eTaskError,
	
	/** 
	 * 任务可以播放
	 * 接收者：PlayerActivity的任务处理器
	 */
	eTaskPlay,
	
	/** 
	 * 剧集列表获取完成  
	 * 用在当Album获取完成后通知
	 * 接收者：PlayerActivity的播放列表空间
	 **/
	eAlbumComplete,

	/**
	 * 新集提醒获取完成
	 * 用在启动获取新集提醒后通知
	 * 接收者：收藏界面
	 */
	eAlbumNewest,
	
	/**
	 * 主面板隐藏显示Tab
	 * 接收者：MainTabActivity
	 */
	eMainTabsHideOrShow,
	
	/**
	 * 跳转浏览器页面
	 * 接收者：BrowserHomeActivity
	 */
	eBrowserHomeActivity,
	
	/**
	 * 更新九宫格标题，图片
	 * 接收者：BrowHomeListLayout
	 */
	eBrowHomeListLayout,
	
	eBrowserAdressSearchSugList;
}
