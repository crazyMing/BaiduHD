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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.hd.adapter.BufferAdapter;
import com.baidu.hd.adapter.EditableAdapter;
import com.baidu.hd.adapter.BufferAdapter.SDCardCallback;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.BufferItemPackage;
import com.baidu.hd.module.ItemPackage;
import com.baidu.hd.util.ToastUtil;
import com.baidu.hd.R;

public class BufferPage extends PersonalBasePage implements OnClickListener {
	
	private String TAG = BufferPage.class.getSimpleName();
	private Logger logger = new Logger(TAG);
	
	/** 文件列表 */
	private ListView mListView = null;
	 
	/** 全选/删除bar */
	private View mEditBar = null;
	
	/** 磁盘检测bar */
	private View mSDCardDetectorBar = null;
	
	/** 全选按钮 */
	private Button mAllSelectedBtn = null;

	/** 删除按钮 */
	private Button mDeleteBtn = null;

	/** 无记录背景 */
	private View mNoneBackground = null;

	/** 全选状态 */
	private boolean mIsSelectedAll = false;
	
	/** 全部开始 */
	private boolean mIsAllStopped = false;
	
	/** 长按项索引 */
	private int mLongPressedIndex = 0;
	
	/** 保持现有状态 */
	private boolean mDoNotRefresh = false;
	
	/** 单击监听 */
	private OnItemClickListener mItemClickListenter = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parentView, View itemView, int position, long id) {
			if (!mIsEditting) {
				mBufferAdapter.launcherTask(position);
			}
			else {
				BufferItemPackage item = mBufferAdapter.getItems().get(position);
				ImageView commonTaskDelete = (ImageView) itemView.findViewById(R.id.common_task_delete);
				item.setSelectedDel(!item.isSelectedDel());
				commonTaskDelete.setImageResource(item.isSelectedDel() ? R.drawable.ic_list_check_on : R.drawable.ic_list_check_off);
				mBufferAdapter.setSelectItemNum(mBufferAdapter.getSelectItemNum() + (item.isSelectedDel() ? 1 : -1));
			}
		}
	};
	
	/** 长按监听 */
	private OnItemLongClickListener mItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parentView, View itemView, int position, long id) {
			mLongPressedIndex = position;
			showBufferPageMenu(itemView);
			return true;
		}
	};
	
	/** adapter */
	private BufferAdapter mBufferAdapter = null;
	private EditableAdapter.Callback mEditableAdapterCallback = new EditableAdapter.Callback() {
		@Override
		public void onEditButton(Integer nIsEditEnable) {
			// 不可编辑
			if (nIsEditEnable == 0) {
				mIsEditting = false;
				updateEditButtonState(-1);
				updateAllStartPauseButtonState(-1);
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
			} else if (nIsSelete == 2) {
				mIsSelectedAll = true;
			}
			mAllSelectedBtn.setText(!mIsSelectedAll ? R.string.select_all : R.string.select_reverse);
		}

		@Override
		public void onDeleteButton(Integer nIsDeleteEnable) {
			// 0 表示disabled；1表示enabled
			mDeleteBtn.setEnabled(nIsDeleteEnable != 0 || !mIsEditting ? true : false);
			ColorStateList csl1 = getResources().getColorStateList(
					R.drawable.button_delete_white_text);
			ColorStateList csl2 = getResources().getColorStateList(
					R.drawable.button_normal_white_text);
			mDeleteBtn.setTextColor(nIsDeleteEnable == 0 ? csl2 : csl1);
		}

		@Override
		public void onStartStopAllButton(Integer nIsStartStopEnable) {
			// 0 表示不可用			
			// 1 表示全部都暂停		
			// 2 表示不是全部停止
			
			// 只要有一个在下载就显示“全部暂停”，全部暂停时显示“全部开始”
			// 禁用时显示“全部开始”
			if (nIsStartStopEnable == 0) {
				mIsAllStopped = false;
				updateAllStartPauseButtonState(-1);
			}
			else if (nIsStartStopEnable == 1) {
				mIsAllStopped = true;
				updateAllStartPauseButtonState(0);
			}
			else if (nIsStartStopEnable == 2) {
				mIsAllStopped = false;
				updateAllStartPauseButtonState(1);
			}
		}
	};
	
	private SDCardCallback mSDCardCallback = new BufferAdapter.SDCardCallback() {
		
		@Override
		public void notifySDCardInfo() {
			updateSDCardInfo();
		}
	};

	public BufferPage(Context context) {
		super(context);
	}

	public BufferPage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public BufferPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	public void initWidgets() {
		super.initWidgets();
		
		mListView = (ListView)mActivity.findViewById(R.id.per_buffer_listview);
		mEditBar = mActivity.findViewById(R.id.per_buffer_select_delete_bar);
		mSDCardDetectorBar = mActivity.findViewById(R.id.per_buffer_sdcard_detector_bar);
		mAllSelectedBtn = (Button) mEditBar.findViewById(R.id.per_footer_select_all);
		mDeleteBtn = (Button) mEditBar.findViewById(R.id.per_footer_delete);
		mNoneBackground = mActivity.findViewById(R.id.per_buffer_none);

		/** 设置事件监听 */
		mAllSelectedBtn.setOnClickListener(this);
		mDeleteBtn.setOnClickListener(this);

		mBufferAdapter = new BufferAdapter(getContext(), mEditableAdapterCallback);
		mBufferAdapter.setSDCardCallback(mSDCardCallback);
		mListView.setAdapter(mBufferAdapter);

		/** 事件监听 */
		mListView.setOnItemClickListener(mItemClickListenter);
		mListView.setOnItemLongClickListener(mItemLongClickListener);
	}
	
	@Override
	protected void onResume() {
		logger.d("onResume");
		super.onResume();
		if (!mDoNotRefresh) {
			mIsSelectedAll = false;
			mBufferAdapter.onResume();
			updateList();
			updateSDCardInfo();
			
			// 【fix bug】【 MEDIA-4353】 BEGIN
			if (mBufferAdapter.getAllItemNum() == 0) {
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
		mBufferAdapter.onPause();
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
	protected void enterEditState() {
		mSDCardDetectorBar.setVisibility(View.GONE);
		mEditBar.setVisibility(View.VISIBLE);
		mBufferAdapter.setIsEditCancel(mIsEditting);
		mIsSelectedAll = false;
		updateList();
	}

	@Override
	protected void exitEditState() {
		mSDCardDetectorBar.setVisibility(View.VISIBLE);
		mEditBar.setVisibility(View.GONE);
		mBufferAdapter.setIsEditCancel(mIsEditting);
		mIsSelectedAll = false;
		updateAllListviewItemEditStatue(mBufferAdapter);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.per_footer_select_all:
			mIsSelectedAll = !mIsSelectedAll;
			updateAllListviewItemEditStatue(mBufferAdapter);
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
	public void delete(int index) {
		if (index != -1) {
			for (BufferItemPackage item : mBufferAdapter.getItems()) {
				item.setSelectedDel(false);
			}
			mBufferAdapter.getItems().get(index).setSelectedDel(true);
		}
		
		mBufferAdapter.removeMarkDeleteItem();
		updateList();
		updateSDCardInfo();
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
		BufferItemPackage item = mBufferAdapter.getItems().get(mLongPressedIndex);
		if (item != null) {
			mBufferAdapter.launcherTask(mLongPressedIndex);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			logger.d("portrait");
			findViewById(R.id.per_buffer_none_video_image).setVisibility(View.VISIBLE);
		}
		else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			logger.d("landscape");
			findViewById(R.id.per_buffer_none_video_image).setVisibility(View.GONE);
		}
	}
	
	public void menuToChangeStateLongPressedItem() {
		BufferItemPackage item = mBufferAdapter.getItems().get(mLongPressedIndex);
		if (item != null) {
			mBufferAdapter.changeTaskState(mLongPressedIndex);
		}
	}
	
	public void menuToAllPauseStart() {
		changeAllStartPauseState();
	}
	
	private void updateList() {
		this.mBufferAdapter.fillList();
		updateAllListviewItemEditStatue(mBufferAdapter);

		this.mListView.setVisibility(this.mBufferAdapter.getAllItemNum() == 0 ? View.GONE : View.VISIBLE);
		this.mNoneBackground.setVisibility(this.mBufferAdapter.getAllItemNum() > 0 ? View.GONE : View.VISIBLE);
	}

	private void updateAllListviewItemEditStatue(EditableAdapter<? extends ItemPackage> editableAdapter) {
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
	private void showBufferPageMenu(View v) {
		Intent intent = new Intent(getContext(), PersonalMenuDialog.class);
		intent.putExtra("PageType", PersonalConst.PERSONAL_PAGE_BUFFER);
		intent.putExtra("TaskState", mBufferAdapter.getTaskState(mLongPressedIndex));
		intent.putExtra("AllStopped", mIsAllStopped);
		mActivity.startActivityForResult(intent, PersonalConst.MESSAGE_MENU_DIALOG);
	}
	
	/** 检测SD卡信息 */
	private void updateSDCardInfo() {
		
		double availableSize = SDCardUtil.getInstance().getAvailableSize();
		double totoalSize = SDCardUtil.getInstance().getAllSize();
		ProgressBar progressBar = (ProgressBar)mSDCardDetectorBar.findViewById(R.id.per_select_sdcardpb);
		TextView left = (TextView)mSDCardDetectorBar.findViewById(R.id.per_select_sdcard_left);
		TextView use = (TextView)mSDCardDetectorBar.findViewById(R.id.per_select_sdcard_use);
		
		if (availableSize != -1 && totoalSize != -1) {
			progressBar.setProgress((int)((totoalSize-availableSize)/totoalSize*100));
//			progressBar.setProgress((int)((availableSize / totoalSize) * 100));
			String text1 = String.format(getResources().getString(R.string.personal_sdcard_use), totoalSize - availableSize);
			String text2 = String.format(getResources().getString(R.string.personal_sdcard_left), availableSize);
			use.setText(text1);
			left.setText(text2);
		}
		else {
			ToastUtil.showMessage(getContext(), getResources().getString(R.string.personal_sdcard_no_tip), Toast.LENGTH_SHORT);
			progressBar.setProgress(0);
			left.setText(R.string.personal_sdcard_no);
			use.setText(R.string.personal_sdcard_no);
		}
	}
	
	public void changeAllStartPauseState() {
		if (mIsAllStopped) {
			mBufferAdapter.toStartTask(null);
		}
		else {
			mBufferAdapter.stopAll();
		}
	}
}