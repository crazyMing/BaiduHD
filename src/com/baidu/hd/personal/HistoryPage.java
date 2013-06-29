package com.baidu.hd.personal;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.baidu.hd.adapter.EditableAdapter;
import com.baidu.hd.adapter.HistoryAdapter;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.HistoryItemPackage;
import com.baidu.hd.module.ItemPackage;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.player.PlayerLauncher;
import com.baidu.hd.stat.StatId.Personal;
import com.baidu.hd.R;

public class HistoryPage extends PersonalBasePage implements OnClickListener {

	private String TAG = HistoryPage.class.getSimpleName();
	private Logger logger = new Logger(TAG);
	
	/** 文件列表 */
	private ListView mListView = null;
	 
	/** 全选/删除bar */
	private View mEditBar = null;
	
	/** 全选按钮*/
	private Button mAllSelectedBtn = null;
	
	/** 删除按钮 */
	private Button mDeleteBtn = null;
	
	/** 无记录背景 */
	private View mNoneBackground = null;
	
	/** 全选状态 */
	private boolean mIsSelectedAll = false;
	
	/** 长按项索引 */
	private int mLongPressedIndex = 0;
	
	/** 保持现有状态 */
	private boolean mDoNotRefresh = false;
	
	/** 单击监听 */
	private OnItemClickListener mItemClickListenter = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parentView, View itemView, int position, long id) {
			if (!mIsEditting) {
				NetVideo video= mHistoryAdapter.getItems().get(position).getAlbum().getCurrent();
				PlayerLauncher.startup(getContext(), mHistoryAdapter.getItems().get(position).getAlbum(), video);
				getStat().incEventCount(Personal.Name, Personal.HistoryPlay);
			}
			else {
				HistoryItemPackage item = mHistoryAdapter.getItems().get(position);
				ImageView commonTaskDelete = (ImageView) itemView.findViewById(R.id.history_item_delete);
				item.setSelectedDel(!item.isSelectedDel());
				commonTaskDelete.setImageResource(item.isSelectedDel() ? R.drawable.ic_list_check_on : R.drawable.ic_list_check_off);
				mHistoryAdapter.setSelectItemNum(mHistoryAdapter.getSelectItemNum() + (item.isSelectedDel() ? 1 : -1));
			}
		}
	};
	
	/** 长按监听 */
	private OnItemLongClickListener mItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parentView, View itemView, int position, long id) {
			mLongPressedIndex = position;
			showLocalHistoryMenu(itemView);
			return true;
		}
	};
	
	/** adapter */
	private HistoryAdapter mHistoryAdapter = null;
	
	private EditableAdapter.Callback mEditableAdapterCallback = new EditableAdapter.Callback() 
	{
		@Override
		public void onEditButton(Integer nIsEditEnable)
		{
			// 不可编辑
			if (nIsEditEnable == 0) {
				mIsEditting = false;
				updateEditButtonState(-1);
				exitEditState();
			}
			// 可以编辑时，根据编辑状态设置图标
			else {
				updateEditButtonState(mIsEditting ? 1 : 0);
			}
		}
		
		@Override
		public void onSelectInverseButton(Integer nIsSelete) {
			// 1 表示部分选择；2表示全部选择
			if (nIsSelete == 1) {
				mIsSelectedAll = false;
			} else if(nIsSelete == 2) {
				mIsSelectedAll = true;
			}
			mAllSelectedBtn.setText(!mIsSelectedAll ? R.string.select_all : R.string.select_reverse);
		}
		
		@Override
		public void onDeleteButton(Integer nIsDeleteEnable) {
			// 0 表示disabled；1表示enabled
			mDeleteBtn.setEnabled(nIsDeleteEnable != 0 || !mIsEditting ? true : false);
			ColorStateList csl1 = getResources().getColorStateList(R.drawable.button_delete_white_text);
			ColorStateList csl2 = getResources().getColorStateList(R.drawable.button_normal_white_text);
			mDeleteBtn.setTextColor(nIsDeleteEnable == 0 ? csl2 : csl1);
		}
		
		@Override
		public void onStartStopAllButton(Integer nIsStartStopEnable) {
			
		}
	};
	
	public HistoryPage(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public HistoryPage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public HistoryPage(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void initWidgets() {
		super.initWidgets();
		
		mListView = (ListView) mActivity.findViewById(R.id.per_history_listview);
		mEditBar = mActivity.findViewById(R.id.per_history_select_delete_bar);
		mAllSelectedBtn = (Button) mEditBar.findViewById(R.id.per_footer_select_all);
		mDeleteBtn = (Button) mEditBar.findViewById(R.id.per_footer_delete);
		mNoneBackground = mActivity.findViewById(R.id.per_history_none);
		
		this.mHistoryAdapter = new HistoryAdapter(getContext(), mEditableAdapterCallback);
		mListView.setAdapter(mHistoryAdapter);
		mListView.setOnItemClickListener(mItemClickListenter);
		mListView.setOnItemLongClickListener(mItemLongClickListener);

		/** 设置时间监听 */
		mAllSelectedBtn.setOnClickListener(this);
		mDeleteBtn.setOnClickListener(this);
		
	}

	@Override
	protected void onResume() {
		logger.d("onResume");
		super.onResume();
		if (!mDoNotRefresh) {
			mHistoryAdapter.onResume();
			mIsSelectedAll = false;
			updateList();
			
			// 【fix bug】【 MEDIA-4353】 BEGIN
			if (mHistoryAdapter.getAllItemNum() == 0) {
				updateEditButtonState(-1);
			}
			else {
				updateEditButtonState(0);
			}
			// 【fix bug】【 MEDIA-4353】 END 
		}
		
		mDoNotRefresh = false;
	}

	@Override
	protected void onPause() {
		logger.d("onPause");
		super.onPause();
		mHistoryAdapter.onPause();
	}

	@Override
	protected void onStart() {
		logger.d("onStart");
		super.onStart();
	}

	@Override
	protected void onStop() {
		logger.d("onStop");
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.per_footer_select_all:
			mIsSelectedAll = !mIsSelectedAll;
			updateAllListviewItemEditStatue(mHistoryAdapter);
			break;
		case R.id.per_footer_delete:
			// 保留编辑状态
			mIsNeedHoldEditingState = true;
			Intent intent = new Intent(mActivity, PersonalDeleteDialog.class);
			mActivity.startActivityForResult(intent, PersonalConst.MESSAGE_DELETE_DIALOG);
			break;

		default:
			break;
		}
	}

	@Override
	protected void enterEditState() {
		super.enterEditState();
		
		mEditBar.setVisibility(View.VISIBLE);
		mHistoryAdapter.setIsEditCancel(mIsEditting);
		mIsSelectedAll = false;
		updateList();
	}

	@Override
	protected void exitEditState() {
		super.exitEditState();
		
		mEditBar.setVisibility(View.GONE);
		mHistoryAdapter.setIsEditCancel(mIsEditting);
		mIsSelectedAll = false;
		updateAllListviewItemEditStatue(mHistoryAdapter);
	}

	@Override
	public void delete(int index) {
		if (index != -1) {
			for (HistoryItemPackage item : mHistoryAdapter.getItems()) {
				item.setSelectedDel(false);
			}
			mHistoryAdapter.getItems().get(index).setSelectedDel(true);
		}
		
		mHistoryAdapter.removeMarkDeleteItem();
		updateList();
	}
	
	@Override
	public void cancel() {
		mDoNotRefresh = true;
	}	

	// TODO 在Adapter类中增加接口，直接删除
	@Override
	public void menuToDeleteLongPressedItem() {
		Intent intent = new Intent(mActivity, PersonalDeleteDialog.class);
		intent.putExtra("LongPressed", mLongPressedIndex);
		mActivity.startActivityForResult(intent, PersonalConst.MESSAGE_DELETE_DIALOG);
	}
	
	@Override
	public void menuToPlay() {
		NetVideo video= mHistoryAdapter.getItems().get(mLongPressedIndex).getAlbum().getCurrent();
		PlayerLauncher.startup(getContext(), mHistoryAdapter.getItems().get(mLongPressedIndex).getAlbum(), video);
		getStat().incEventCount(Personal.Name, Personal.HistoryPlay);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			logger.d("portrait");
			findViewById(R.id.per_history_none_video_image).setVisibility(View.VISIBLE);
		}
		else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			logger.d("landscape");
			findViewById(R.id.per_history_none_video_image).setVisibility(View.GONE);
		}
	}
	
	/** 进入详情页 */
	public void menuDetail() {
		mHistoryAdapter.onClickLayoutRight(mLongPressedIndex);
	}
	
	private void updateList() {
		// 1.填充数据
		this.mHistoryAdapter.fillList();
		// 2.更新删除标记
		updateAllListviewItemEditStatue(mHistoryAdapter);
		
		this.mListView.setVisibility(this.mHistoryAdapter.getAllItemNum() == 0 ? View.GONE : View.VISIBLE);
		this.mNoneBackground.setVisibility(this.mHistoryAdapter.getAllItemNum() > 0 ? View.GONE : View.VISIBLE);
	}
	
	private void updateAllListviewItemEditStatue(EditableAdapter< ? extends ItemPackage> editableAdapter) {
		int selectItemNum = 0;
		for (int i = 0; i < editableAdapter.getItems().size(); ++i) {
			if (!mIsEditting) {		
				editableAdapter.getItems().get(i).setSelectedDel(false);
			} else {
				editableAdapter.getItems().get(i).setSelectedDel(mIsSelectedAll);
				if (mIsSelectedAll) {
					selectItemNum++;
				}
			}
		}
		editableAdapter.setSelectItemNum(selectItemNum); 
		editableAdapter.notifyDataSetChanged();
		
		mAllSelectedBtn.setText(!mIsSelectedAll ? R.string.select_all : R.string.select_reverse);
	}
	
	/**
	 * 显示菜单
	 */
	private void showLocalHistoryMenu(View v) {
		Intent intent = new Intent(getContext(), PersonalMenuDialog.class);
		intent.putExtra("PageType", PersonalConst.PERSONAL_PAGE_HISTORY);
		mActivity.startActivityForResult(intent, PersonalConst.MESSAGE_MENU_DIALOG);
	}
}
