package com.baidu.hd.event;

public enum EventId {
	
	eNone,
	eAll,
	
	eFatal,	/** ������ֲ��ɻָ����󣬱���ֹͣ */
	
	eAdressleisteSuggestList,/** ��ַ��suggest�б� */
	eSearchKeyWordBySearchSite,/** ���������Ĺؼ��� */
	eSearchKeyWordByHomeSite,/** ���������Ĺؼ��� */
	eStartSearchSiteActivity,/** ���������ҳ */
	
	eMainTabActivity,/** �л�MainTab�����activity */
	eLocalTabActivity,/** �л�LocalTab�����activity */
	
	/** ���ط������� */
	eDownloadServiceDie,
	
	/** ��������ǩ�л� */
	ePersonalSwitchTab,
	
	/**
	 * �����б����
	 * �����ߣ�������ʷ���棬���ؽ���
	 */
	ePlayListUpdate,
	
	/**
	 * ��ҳ�����������а��ȴ� ����֪ͨ
	 */
	eHomeHotRankingDetailUpdate,
	eHomeSearchRankingDetailUpdate,
	eHomeNavigationDetailUpdate,
	
	eNetStateChanged,
	eSDCardStateChanged,
	
	/** ������� */
	eFeedBackComplete,

	/** ͼƬ��Ҫ���¼��� */
	eImageNeedReload,
	
	/** app ���¸��� */
	eFriendAppNeedUpdate,
	eMoreAppNeedUpdate,
	
	/** ���������� */
	eUpgradeCheckCancel,
	eUpgradeCheckComplete,
	ePlayCoreCheckComplete,

	/** �����¼� */
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
	 * ������Բ���
	 * �����ߣ�PlayerActivity����������
	 */
	eTaskPlay,
	
	/** 
	 * �缯�б��ȡ���  
	 * ���ڵ�Album��ȡ��ɺ�֪ͨ
	 * �����ߣ�PlayerActivity�Ĳ����б�ռ�
	 **/
	eAlbumComplete,

	/**
	 * �¼����ѻ�ȡ���
	 * ����������ȡ�¼����Ѻ�֪ͨ
	 * �����ߣ��ղؽ���
	 */
	eAlbumNewest,
	
	/**
	 * �����������ʾTab
	 * �����ߣ�MainTabActivity
	 */
	eMainTabsHideOrShow,
	
	/**
	 * ��ת�����ҳ��
	 * �����ߣ�BrowserHomeActivity
	 */
	eBrowserHomeActivity,
	
	/**
	 * ���¾Ź�����⣬ͼƬ
	 * �����ߣ�BrowHomeListLayout
	 */
	eBrowHomeListLayout,
	
	eBrowserAdressSearchSugList;
}
