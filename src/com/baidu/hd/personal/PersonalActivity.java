package com.baidu.hd.personal;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.MainActivity;
import com.baidu.hd.detect.Detect;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.player.PlayerLauncher;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId;
import com.baidu.hd.stat.StatId.BrowPlay;
import com.baidu.hd.test.RegionActivity;
import com.baidu.hd.test.SiteActivity;
import com.baidu.hd.ui.ViewDragableSpace;
import com.baidu.hd.util.Const;
import com.baidu.mobstat.StatService;
import com.baidu.hd.R;

public class PersonalActivity extends BaseActivity implements OnClickListener {
	
	private final String TAG = PersonalActivity.class.getSimpleName();
	private Logger logger = new Logger(TAG);
	
	public static int LAST_PAGE = -1;
	
	/** ������ */
	private final class TitleBar {
		public View bufferIcon = null;
		public View localIcon = null;
		public View favoriteIcon = null;
		public View historyIcon = null;
		public TitleBar(Activity act) {
			if (act != null) {
				bufferIcon =  act.findViewById(R.id.personal_buffer_icon);
				localIcon = act.findViewById(R.id.personal_local_icon);
				favoriteIcon = act.findViewById(R.id.personal_favorite_icon);
				historyIcon = act.findViewById(R.id.personal_history_icon);
			}
		}
	}
	private TitleBar mTitleBar = null;
	
	/** �ײ������� */
	private final class BottomBar {
		public ImageButton backButton = null;
		public ImageButton editButton = null;
		public ImageButton allStartButton = null;
		public ImageButton importButton = null;
		public ImageButton refreshButton = null;
		public BottomBar(Activity act) {
			backButton = (ImageButton)act.findViewById(R.id.personal_back);
			editButton = (ImageButton)act.findViewById(R.id.personal_edit_complete);
			allStartButton = (ImageButton)act.findViewById(R.id.personal_all_start_pause);
			importButton = (ImageButton)act.findViewById(R.id.personal_import);
			refreshButton = (ImageButton)act.findViewById(R.id.personal_refresh);
		}
	}
	private BottomBar mBottomBar = null;
	
	/** ҳ�滬������ */
	private ViewDragableSpace mPageContainer = null;
	private List<PersonalBasePage> mPageList = new ArrayList<PersonalBasePage>();
	
//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
//״̬��Ϣ��stopʱ��Ҫ�������е�״̬���ģ�startʱ��Ҫ�ָ����е�״̬��Ϣ
	/** ��ʼ��ʾPage */
	private int mDefaultPage = PersonalConst.PERSONAL_PAGE_BUFFER;
	/** ��ǰPage */
	private int mCurrentPage = mDefaultPage;
	/** ����ѭ���л� */
	private boolean mIsLoop = false;
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		logger.d("onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personal);
		
		
		Detect detect = (Detect) getServiceProvider(Detect.class);
		// ����
		if (!detect.isNetAvailabe()) {
			if (LAST_PAGE == -1) {
				LAST_PAGE = PersonalConst.PERSONAL_PAGE_LOCAL;
			}
		}
		// ����
		else {
			if (getIntent().getExtras() != null) {
				LAST_PAGE = getIntent().getExtras().getInt(Const.IntentExtraKey.PersonalActivity);
			}
		}
				 
		init();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		logger.d("onNewIntent");
		super.onNewIntent(intent);
		
		if (intent.getExtras() != null) {
			LAST_PAGE = intent.getExtras().getInt(Const.IntentExtraKey.PersonalActivity);
		}
	}

	@Override
	public void onBackPressed() {
		if (mPageList.get(mCurrentPage).mIsEditting){
			mPageList.get(mCurrentPage).changeEditState();
			updateBottomBarIcon();
		}
		else {
			goBack();
		}
	}
	
	private void goBack() {
		Intent intent = new Intent(this, MainActivity.class);
//		MainActivity.setAnimBottomTop(true);
//		MainActivity.setOnResumeAnimation(true);
		setResult(PersonalConst.PERSONAL_BACK, intent);
		super.onBackPressed();
	}
	

	@Override
	protected void onResume() {
		logger.d("onResume");
		
		int lastPage = LAST_PAGE;
		super.onResume();
		
		if (lastPage == -1) {
			mCurrentPage = mDefaultPage;
		}
		else  {
			mCurrentPage = lastPage;
		}
		
		mPageList.get(mCurrentPage).resume();
		mPageContainer.setScreen(mCurrentPage);
		updateTitlebarState();
		updateBottomBarVisibility();
	}

	@Override
	protected void onPause() {
		logger.d("onPause");
		
		super.onPause();
		
		LAST_PAGE = mCurrentPage;
		mPageList.get(mCurrentPage).pause();
	}

	@Override
	protected void onStop() {
		logger.d("onStop");
		
		for (PersonalBasePage page : mPageList) {
			page.stop();
		}

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		logger.d("onDestroy");
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		logger.d("onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
		mPageList.get(mCurrentPage).onConfigurationChanged(newConfig);
		
		// ת��ʱֱ��ָ����Ŀ����
		forceToCurrentPage();
	}

	private void init() {
		initWidgets();
		
		mPageContainer.setOnViewChangedListener(new ViewDragableSpace.OnViewChangedListener() {
			@Override
			public void onViewChanged(int viewIndex) {
				switchPageTo(viewIndex);
			}
		});
		
		/** ������ */
		findViewById(R.id.personal_buffer_title).setOnClickListener(this);
		findViewById(R.id.personal_local_title).setOnClickListener(this);
		findViewById(R.id.personal_favorite_title).setOnClickListener(this);
		findViewById(R.id.personal_history_title).setOnClickListener(this);
		
		/** �ײ������� */
		mBottomBar.backButton.setOnClickListener(this);
		mBottomBar.allStartButton.setOnClickListener(this);
		mBottomBar.importButton.setOnClickListener(this);
		mBottomBar.refreshButton.setOnClickListener(this);
		mBottomBar.editButton.setOnClickListener(this);
	}
	
	/**
	 * ��ʼ�����
	 */
	private void initWidgets() {
		mTitleBar = new TitleBar(this);
		mBottomBar = new BottomBar(this);
		
		mPageContainer = (ViewDragableSpace)findViewById(R.id.personal_scroll_layout);
		
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mPageList.add((PersonalBasePage) inflater.inflate(R.layout.per_buf_page, null));
		mPageList.add((PersonalBasePage) inflater.inflate(R.layout.per_loc_page, null));
		mPageList.add((PersonalBasePage) inflater.inflate(R.layout.per_fav_page, null));
		mPageList.add((PersonalBasePage) inflater.inflate(R.layout.per_his_page, null));
	
		for (int i = 0; i < mPageList.size(); i++) {
			mPageContainer.addView(mPageList.get(i));
			mPageList.get(i).initWidgets();
		}
	}
	
	/**
	 * ���ó�ʼҳ
	 */
	public void setDefaultPage(int type) {
		mDefaultPage = type;
		mCurrentPage = type;
	}
	
	/**
	 * ���±�����״̬
	 */
	private void updateTitlebarState() {
		switch (mCurrentPage) {
		case PersonalConst.PERSONAL_PAGE_BUFFER:
			mTitleBar.bufferIcon.setSelected(true);
			mTitleBar.localIcon.setSelected(false);
			mTitleBar.favoriteIcon.setSelected(false);
			mTitleBar.historyIcon.setSelected(false);
			break;
		case PersonalConst.PERSONAL_PAGE_LOCAL:
			mTitleBar.bufferIcon.setSelected(false);
			mTitleBar.localIcon.setSelected(true);
			mTitleBar.favoriteIcon.setSelected(false);
			mTitleBar.historyIcon.setSelected(false);
			break;
		case PersonalConst.PERSONAL_PAGE_FAVORITE:
			mTitleBar.bufferIcon.setSelected(false);
			mTitleBar.localIcon.setSelected(false);
			mTitleBar.favoriteIcon.setSelected(true);
			mTitleBar.historyIcon.setSelected(false);
			break;
		case PersonalConst.PERSONAL_PAGE_HISTORY:
			mTitleBar.bufferIcon.setSelected(false);
			mTitleBar.localIcon.setSelected(false);
			mTitleBar.favoriteIcon.setSelected(false);
			mTitleBar.historyIcon.setSelected(true);
			break;

		default:
			break;
		}
	}
	
	/**
	 * ���µײ���������ʾ������
	 */
	private void updateBottomBarVisibility() {
		switch (mCurrentPage) {
		case PersonalConst.PERSONAL_PAGE_BUFFER:
			mBottomBar.allStartButton.setVisibility(View.VISIBLE);
			mBottomBar.importButton.setVisibility(View.GONE);
			mBottomBar.refreshButton.setVisibility(View.GONE);
			break;
		case PersonalConst.PERSONAL_PAGE_LOCAL:
			mBottomBar.allStartButton.setVisibility(View.GONE);
			mBottomBar.importButton.setVisibility(View.VISIBLE);
			mBottomBar.refreshButton.setVisibility(View.VISIBLE);
			break;
//		case PageType.FAVORITE:
//			break;
//		case PageType.HISTORY:
//			break;
		default:
			mBottomBar.allStartButton.setVisibility(View.GONE);
			mBottomBar.importButton.setVisibility(View.GONE);
			mBottomBar.refreshButton.setVisibility(View.GONE);
			break;
		}
	}
	
	/**
	 * ���µײ��������İ�ťͼ��
	 */
	private void updateBottomBarIcon() {
		mBottomBar.editButton.setSelected(mPageList.get(mCurrentPage).isEditting());

		switch (mCurrentPage) {
		case PersonalConst.PERSONAL_PAGE_BUFFER:
			break;
		case PersonalConst.PERSONAL_PAGE_LOCAL:
			break;
		case PersonalConst.PERSONAL_PAGE_FAVORITE:
			break;
//		case PageType.HISTORY:
//			break;
		default:
			break;
		}
	}
	
	/**
	 * ѭ��Page
	 */
	// TODO ��ʱδʵ��
	private void loopPages() {
		
	}
	
	/**
	 * �л�Page
	 */
	private void switchPageTo(int index) {
		if (index == mCurrentPage){
			return;
		}

		// 1.pause�л�ǰ��ҳ
		mPageList.get(mCurrentPage).pause();
		// 2.resume��Ҫ�л�����ҳ
		mPageList.get(index).resume();
		// 3.���õ�ǰҳ������
		mCurrentPage = index;
		// 4.�л�ҳ��
		mPageContainer.setScreen(mCurrentPage, Const.PersonalConst.SilderDuration);
		// 5.���±�����
		updateTitlebarState();
		// 6.���µײ�����������ʾ������
		updateBottomBarVisibility();
		// 7.���µײ���������ͼ����״
		updateBottomBarIcon();
		
		// ͳ��
		switch (mCurrentPage) {
		case 0:
			getStat().incEventCount(StatId.Personal.Name, StatId.Personal.BufferPage);
			break;
		case 1:
			getStat().incEventCount(StatId.Personal.Name, StatId.Personal.LocalPage);
			break;
		case 2:
			getStat().incEventCount(StatId.Personal.Name, StatId.Personal.FavoritePage);
			break;
		case 3:
			getStat().incEventCount(StatId.Personal.Name, StatId.Personal.HistoryPage);
			break;
		}
	}
	
	/**
	 * ֱ��ָ������ǰҳ
	 */
	private void forceToCurrentPage() {
		logger.d("forceToCurrentPage");
		
		if (mPageContainer.isScrollerFinished()){
			return;
		}
		
		logger.d("mCurrentPage = " + mCurrentPage);
		// 1.resume��Ҫ�л�����ҳ
		mPageList.get(mCurrentPage).resume();
		// 2.�л�ҳ��
		mPageContainer.setScreen(mCurrentPage, 0);
		// 3.���±�����
		updateTitlebarState();
		// 4.���µײ�����������ʾ������
		updateBottomBarVisibility();
		// 5.���µײ���������ͼ����״
		updateBottomBarIcon();
	}

	@Override
	public void onClick(View v) {
		
		/** ������ */
		int clickedPage = mCurrentPage;
		switch (v.getId()) {
		case R.id.personal_buffer_title: clickedPage = 0; break;
		case R.id.personal_local_title: clickedPage = 1; break;
		case R.id.personal_favorite_title: clickedPage = 2; break;
		case R.id.personal_history_title: clickedPage = 3; break;
		}
		if (clickedPage != mCurrentPage) {
			switchPageTo(clickedPage);
		}
		
		/** �ײ������� */
		PersonalBasePage page = mPageList.get(mCurrentPage);
		switch (v.getId()) {
		case R.id.personal_back:
			goBack();
			break;
		case R.id.personal_edit_complete:
			mPageList.get(mCurrentPage).changeEditState();
			updateBottomBarIcon();
			break;
		case R.id.personal_all_start_pause:
			if (page instanceof BufferPage) {
				BufferPage bufferPage = (BufferPage)page;
				bufferPage.changeAllStartPauseState();
			}
		case R.id.personal_refresh:
			if (page instanceof LocalPage) {
				LocalPage localPage = (LocalPage)page;
				localPage.refresh();
			}
			break;
		case R.id.personal_import:
			Intent intent = new Intent(this, FileBrowserActivity.class);
			startActivity(intent);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		int index = -1;
		if (data != null) {
			index = data.getIntExtra("LongPressed", -1);
		}
		
		switch (requestCode) {
		// ɾ���Ի���
		case PersonalConst.MESSAGE_DELETE_DIALOG:
			
			switch (resultCode) {
			case RESULT_OK:
				mPageList.get(mCurrentPage).delete(index);
				break;
			case RESULT_CANCELED:
				mPageList.get(mCurrentPage).cancel();
				break;
			}
			
			break;
			
		// �˵��Ի���
		case PersonalConst.MESSAGE_MENU_DIALOG:
			switch (resultCode) {
			// buffer
			case PersonalConst.PERSONAL_BUFFER_PLAY:
				mPageList.get(mCurrentPage).menuToPlay();
				break;
			case PersonalConst.PERSONAL_BUFFER_PASUE:
				if (mPageList.get(mCurrentPage) instanceof BufferPage) {
					((BufferPage)mPageList.get(mCurrentPage)).menuToChangeStateLongPressedItem();
				}
				break;
			case PersonalConst.PERSONAL_BUFFER_ALL_PAUSE:
				if (mPageList.get(mCurrentPage) instanceof BufferPage) {
					((BufferPage)mPageList.get(mCurrentPage)).menuToAllPauseStart();
				}
				break;
			case PersonalConst.PERSONAL_BUFFER_DELETE:
				mPageList.get(mCurrentPage).menuToDeleteLongPressedItem();
				break;
				
			// local
			case PersonalConst.PERSONAL_LOCAL_PLAY:
				mPageList.get(mCurrentPage).menuToPlay();
				break;
			case PersonalConst.PERSONAL_LOCAL_DELETE:
				mPageList.get(mCurrentPage).menuToDeleteLongPressedItem();
				break;
			
			// favorite
			case PersonalConst.PERSONAL_FAVORITE_PLAY:
				mPageList.get(mCurrentPage).menuToPlay();
				break;
			case PersonalConst.PERSONAL_FAVORITE_DETAIL:
				if (mPageList.get(mCurrentPage) instanceof FavoritePage) {
					((FavoritePage)mPageList.get(mCurrentPage)).menuDetail();
				}
				break;
			case PersonalConst.PERSONAL_FAVORITE_DELETE:
				mPageList.get(mCurrentPage).menuToDeleteLongPressedItem();
				break;
				
			// history
			case PersonalConst.PERSONAL_HISTORY_PLAY:
				mPageList.get(mCurrentPage).menuToPlay();
				break;
			case PersonalConst.PERSONAL_HISTORY_DETAIL:
				if (mPageList.get(mCurrentPage) instanceof HistoryPage) {
					((HistoryPage)mPageList.get(mCurrentPage)).menuDetail();
				}
				break;
			case PersonalConst.PERSONAL_HISTORY_DELETE:
				mPageList.get(mCurrentPage).menuToDeleteLongPressedItem();
				break;
			}
			break;

		default:
			break;
		}
	}
	
	/** ���±༭��ť״̬�����ⲿ����
	 *-1�������ã�0�����ڱ༭���״̬��1���������ڱ༭״̬*/
	public void updateEditButtonState(final int state) {
		if (state == -1) {
			// ͼ��Ϊ���༭���״̬���ҽ���
			mBottomBar.editButton.setEnabled(false);
			mBottomBar.editButton.setSelected(false);
		} else if (state == 0) {
			// ͼ��Ϊ���༭���״̬���ҿ���ʹ��
			mBottomBar.editButton.setEnabled(true);
			mBottomBar.editButton.setSelected(false);
		} else if (state == 1) {
			// ͼ��Ϊ�����ڱ༭״̬���ҿ���ʹ��
			mBottomBar.editButton.setEnabled(true);
			mBottomBar.editButton.setSelected(true);
		}
	}
	
	/** ���±༭��ť״̬�����ⲿ����
	 *-1�������ã�0�����ڡ�ȫ����ͣ����1�����ڡ�ĳЩ�����أ�����ȫ�������ء�*/
	public void updateAllStartPauseButtonState(final int state) {
		if (state == -1) {
			// ͼ��Ϊ����ȫ����ʼ���Ҳ�����
			mBottomBar.allStartButton.setEnabled(false);
			mBottomBar.allStartButton.setSelected(false);
		} else if (state == 0) {
			// ͼ��Ϊ�����ã���ȫ����ʼ��
			mBottomBar.allStartButton.setEnabled(true);
			mBottomBar.allStartButton.setSelected(false);
		} else if (state == 1) {
			// ͼ��Ϊ�����ã���ȫ����ͣ��
			mBottomBar.allStartButton.setEnabled(true);
			mBottomBar.allStartButton.setSelected(true);
		}
	}
	
	private Stat getStat() {
		return (Stat) getServiceProvider(Stat.class);
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		
//		super.onCreateOptionsMenu(menu);
//		
//		if (Const.isDebug)
//		{
//			int base = Menu.FIRST;
//			menu.add(base, base, base, "Home");
//			menu.add(base, base + 1, base + 1, "Site");
//			menu.add(base, base + 2, base + 2, "Test");
//		}
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		
//		if (Const.isDebug)
//		{
//			switch(item.getItemId()) {
//			case 1:
//				PlayerLauncher.startup(this, "ed2k://|file|%E6%B3%95%E7%BD%91%E7%8B%99%E5%87%BB(%E5%9B%BD%E8%AF%AD)%E7%AC%AC9%E9%9B%86[www.qire123.com].rmvb|200110621|3E2E0DA4A3B78A810D3D5F19B564422F|h=AS5F4KA6SJBZYUTPQLVOWBKF24CROUHK|/");
//				break;
//			case 2:
//				startActivity(new Intent(this, SiteActivity.class));
//				break;
//			case 3:
//				startActivity(new Intent(this, RegionActivity.class));
//				break;
//			
//			default:
//				break;
//			}
//		}
//		return super.onOptionsItemSelected(item);
//	}
//	
	
}
