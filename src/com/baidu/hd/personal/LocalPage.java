package com.baidu.hd.personal;

import java.util.ArrayList;
import java.util.List;

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

import com.baidu.hd.adapter.EditableAdapter;
import com.baidu.hd.adapter.LocalAdapter;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.ItemPackage;
import com.baidu.hd.module.LocalItemPackage;
import com.baidu.hd.module.album.LocalVideo;
import com.baidu.hd.player.PlayerLauncher;
import com.baidu.hd.stat.StatId.Personal;
import com.baidu.hd.util.ToastUtil;
import com.baidu.hd.R;

public class LocalPage extends PersonalBasePage implements OnClickListener {

	private String TAG = LocalPage.class.getSimpleName();
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

	private VideoInfoGetAsyncTask mVideoInfoGetTask = null;
	
	/** 长按项索引 */
	private int mLongPressedIndex = 0;
	
	/** 保持现有状态 */
	private boolean mDoNotRefresh = false;
	
	/** 刷新文件异步任务 */
	private ScanAsyncTask mScanTask = null;
	
	/** 单击监听 */
	private OnItemClickListener mItemClickListenter = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parentView, View itemView, int position, long id) {
			if (!mIsEditting) {
				LocalVideo video= mLocalAdapter.getItems().get(position).getVideo();
				PlayerLauncher.startup(getContext(), video);
				getStat().incEventCount(Personal.Name, Personal.LocalPlay);
			}
			else {
				LocalItemPackage item = mLocalAdapter.getItems().get(position);
				ImageView commonTaskDelete = (ImageView) itemView.findViewById(R.id.common_task_delete);
				item.setSelectedDel(!item.isSelectedDel());
				commonTaskDelete.setImageResource(item.isSelectedDel() ? R.drawable.ic_list_check_on : R.drawable.ic_list_check_off);
				mLocalAdapter.setSelectItemNum(mLocalAdapter.getSelectItemNum() + (item.isSelectedDel() ? 1 : -1));
			}
		}
	};
	
	/** 长按监听 */
	private OnItemLongClickListener mItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parentView, View itemView, int position, long id) {
			mLongPressedIndex = position;
			showLocalPageMenu(itemView);
			return true;
		}
	};

	/** adapter */
	private LocalAdapter mLocalAdapter = null;
	private EditableAdapter.Callback mEditableAdapterCallback = new EditableAdapter.Callback() {
		@Override
		public void onEditButton(Integer nIsEditEnable) {
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
		}
	};

	public LocalPage(Context context) {
		super(context);
	}

	public LocalPage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LocalPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void initWidgets() {
		super.initWidgets();

		mListView = (ListView) mActivity.findViewById(R.id.per_local_listview);
		mEditBar = mActivity.findViewById(R.id.per_local_select_delete_bar);
		mSDCardDetectorBar = mActivity.findViewById(R.id.per_local_sdcard_detector_bar);
		mAllSelectedBtn = (Button) mEditBar.findViewById(R.id.per_footer_select_all);
		mDeleteBtn = (Button) mEditBar.findViewById(R.id.per_footer_delete);
		mNoneBackground = mActivity.findViewById(R.id.per_local_none);

		/** 设置事件监听 */
		mAllSelectedBtn.setOnClickListener(this);
		mDeleteBtn.setOnClickListener(this);

		mLocalAdapter = new LocalAdapter(getContext(), mEditableAdapterCallback);
		mListView.setAdapter(mLocalAdapter);

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
			updateList();
			getVideoInfo();
			updateSDCardInfo();
			
			// 【fix bug】【 MEDIA-4353】 BEGIN
			if (mLocalAdapter.getAllItemNum() == 0) {
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
		
		// 【fix bug】【MEDIA-5296】
		if (mScanTask != null) {
			mScanTask.quit(true);
			mScanTask = null;
		}
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
		logger.d("enterEditState");
		mSDCardDetectorBar.setVisibility(View.GONE);
		mEditBar.setVisibility(View.VISIBLE);
		mLocalAdapter.setIsEditCancel(mIsEditting);
		mIsSelectedAll = false;
		updateList();
	}

	@Override
	protected void exitEditState() {
		mSDCardDetectorBar.setVisibility(View.VISIBLE);
		mEditBar.setVisibility(View.GONE);
		mLocalAdapter.setIsEditCancel(mIsEditting);
		mIsSelectedAll = false;
		updateAllListviewItemEditStatue(mLocalAdapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.per_footer_select_all:
			mIsSelectedAll = !mIsSelectedAll;
			updateAllListviewItemEditStatue(mLocalAdapter);
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
			for (LocalItemPackage item : mLocalAdapter.getItems()) {
				item.setSelectedDel(false);
			}
			mLocalAdapter.getItems().get(index).setSelectedDel(true);
		}
		
		mLocalAdapter.removeMarkDeleteItem();
		updateList();
		updateSDCardInfo();
	}
	
	@Override
	public void cancel() {
		mDoNotRefresh = true;
	}

	@Override
	public void menuToDeleteLongPressedItem() {
		Intent intent = new Intent(mActivity, PersonalDeleteDialog.class);
		intent.putExtra("LongPressed", mLongPressedIndex);
		mActivity.startActivityForResult(intent, PersonalConst.MESSAGE_DELETE_DIALOG);
	}

	@Override
	public void menuToPlay() {
		LocalItemPackage item = mLocalAdapter.getItems().get(mLongPressedIndex);
		if (item != null) {
			LocalVideo video= item.getVideo();
			PlayerLauncher.startup(getContext(), video);
			getStat().incEventCount(Personal.Name, Personal.LocalPlay);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			logger.d("portrait");
			findViewById(R.id.per_local_none_video_image).setVisibility(View.VISIBLE);
		}
		else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			logger.d("landscape");
			findViewById(R.id.per_local_none_video_image).setVisibility(View.GONE);
		}
	}

	/**
	 * 刷新文件
	 */
	public void refresh() {
		List<String> pathList = new ArrayList<String>();
		if (SDCardUtil.getInstance().isMediaMounted()) {
			
			// 初始化扫描任务，并执行扫描BEGIN
			if (mScanTask == null) {
				mScanTask = new ScanAsyncTask(getContext(), mActivity,
						new ScanAsyncTask.Callback() {

					@Override
					public void onComplete(int nResult) {
						// Toast 先放到ScanAsyncTask中，若以后有变动，可以在这里修改
						updateList();
						getVideoInfo();
						logger.d("OnComplete file number: "
								+ ((Integer) mLocalAdapter.getAllItemNum())
										.toString());
						
						mScanTask = null;
					}

					@Override
					public boolean isNeedRefresh() {
						return true;
					}
				});
			}
			mScanTask.execute(pathList);
			// END
			
		} else {
			String message = mActivity
					.getString(R.string.dialog_sdcard_message);
			ToastUtil.showMessage(getContext(),
					message, Toast.LENGTH_LONG);
		}
	}

	private void updateList() {
		this.mLocalAdapter.fillList();
		updateAllListviewItemEditStatue(mLocalAdapter);

		this.mListView.setVisibility(this.mLocalAdapter.getAllItemNum() == 0 ? View.GONE : View.VISIBLE);
		this.mNoneBackground.setVisibility(this.mLocalAdapter.getAllItemNum() > 0 ? View.GONE : View.VISIBLE);
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

	private void getVideoInfo() {
		if (this.mVideoInfoGetTask != null) {
			this.mVideoInfoGetTask.cancel(false);
		}
		this.mVideoInfoGetTask = new VideoInfoGetAsyncTask(getContext(),
				new VideoInfoGetAsyncTask.Callback() {

					@Override
					public void onUpdate() {
						mLocalAdapter.notifyDataSetChanged();
					}
				});
		this.mVideoInfoGetTask.execute();
	}

	/**
	 * 显示菜单
	 */
	private void showLocalPageMenu(View v) {
		Intent intent = new Intent(getContext(), PersonalMenuDialog.class);
		intent.putExtra("PageType", PersonalConst.PERSONAL_PAGE_LOCAL);
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
}
