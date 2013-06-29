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

	/** �ļ��б� */
	private ListView mListView = null;

	/** ȫѡ/ɾ��bar */
	private View mEditBar = null;

	/** ���̼��bar */
	private View mSDCardDetectorBar = null;

	/** ȫѡ��ť */
	private Button mAllSelectedBtn = null;

	/** ɾ����ť */
	private Button mDeleteBtn = null;

	/** �޼�¼���� */
	private View mNoneBackground = null;

	/** ȫѡ״̬ */
	private boolean mIsSelectedAll = false;

	private VideoInfoGetAsyncTask mVideoInfoGetTask = null;
	
	/** ���������� */
	private int mLongPressedIndex = 0;
	
	/** ��������״̬ */
	private boolean mDoNotRefresh = false;
	
	/** ˢ���ļ��첽���� */
	private ScanAsyncTask mScanTask = null;
	
	/** �������� */
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
	
	/** �������� */
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
			// ���ɱ༭
			if (nIsEditEnable == 0) {
				mIsEditting = false;
				updateEditButtonState(-1);
				exitEditState();
			}
			// ���Ա༭ʱ�����ݱ༭״̬����ͼ��
			else {
				updateEditButtonState(mIsEditting ? 1 : 0);
			}
		}

		@Override
		public void onSelectInverseButton(Integer nIsSelete) {
			// 1 ��ʾ����ѡ��2��ʾȫ��ѡ��
			if (nIsSelete == 1) {
				mIsSelectedAll = false;
			} else if (nIsSelete == 2) {
				mIsSelectedAll = true;
			}
			mAllSelectedBtn.setText(!mIsSelectedAll ? R.string.select_all : R.string.select_reverse);
		}

		@Override
		public void onDeleteButton(Integer nIsDeleteEnable) {
			// 0 ��ʾdisabled��1��ʾenabled
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

		/** �����¼����� */
		mAllSelectedBtn.setOnClickListener(this);
		mDeleteBtn.setOnClickListener(this);

		mLocalAdapter = new LocalAdapter(getContext(), mEditableAdapterCallback);
		mListView.setAdapter(mLocalAdapter);

		/** �¼����� */
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
			
			// ��fix bug���� MEDIA-4353�� BEGIN
			if (mLocalAdapter.getAllItemNum() == 0) {
				updateEditButtonState(-1);
			}
			else {
				updateEditButtonState(0);
			}
			// ��fix bug���� MEDIA-4353�� END 
		}
		mDoNotRefresh = false;
	}

	@Override
	protected void onPause() {
		logger.d("onPause");
		super.onPause();
		
		// ��fix bug����MEDIA-5296��
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
			// �����༭״̬
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
	 * ˢ���ļ�
	 */
	public void refresh() {
		List<String> pathList = new ArrayList<String>();
		if (SDCardUtil.getInstance().isMediaMounted()) {
			
			// ��ʼ��ɨ�����񣬲�ִ��ɨ��BEGIN
			if (mScanTask == null) {
				mScanTask = new ScanAsyncTask(getContext(), mActivity,
						new ScanAsyncTask.Callback() {

					@Override
					public void onComplete(int nResult) {
						// Toast �ȷŵ�ScanAsyncTask�У����Ժ��б䶯�������������޸�
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
	 * ��ʾ�˵�
	 */
	private void showLocalPageMenu(View v) {
		Intent intent = new Intent(getContext(), PersonalMenuDialog.class);
		intent.putExtra("PageType", PersonalConst.PERSONAL_PAGE_LOCAL);
		mActivity.startActivityForResult(intent, PersonalConst.MESSAGE_MENU_DIALOG);
	}
	
	/** ���SD����Ϣ */
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
