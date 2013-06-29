package com.baidu.hd.personal;

import java.util.Stack;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.adapter.EditableAdapter;
import com.baidu.hd.adapter.FolderAdapter;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.FolderItemPackage;
import com.baidu.hd.module.ItemPackage;
import com.baidu.hd.util.ToastUtil;
import com.baidu.hd.R;

public class FileBrowserActivity extends BaseActivity {
	
	private Logger logger = new Logger("FileBrowserActivity");
	
	/** 全选状态 */
	private boolean mIsSelectInverse = false;
	
	/** 添加按钮 */
	private Button mBtnImport = null;
	
	/** 全选反选按钮 */
	private Button mSelectInverse = null;
	
	/** 文件夹ListView */
	private ListView mFolderListView = null;
	
	/** 上一屏位置 */
	private Stack<Integer> mSelectionStack = new Stack<Integer>();
	
	/** 当前路径 */
	private TextView mCurrentPathTextView = null;
	
	/** Adapter */
	private FolderAdapter mFolderAdapter = null;
	
	private EditableAdapter.Callback mEditableAdapterCallback = new EditableAdapter.Callback() {

		@Override
		public void onEditButton(Integer nIsEditEnable) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSelectInverseButton(Integer nIsSelete) {
			// TODO Auto-generated method stub
			if (nIsSelete == 1)
			{
				mIsSelectInverse = false; // 没有全选
			}
			else if (nIsSelete == 2)
			{
				mIsSelectInverse = true; //已经全选
			}
			mSelectInverse.setText(!mIsSelectInverse ? R.string.select_all : R.string.select_reverse);
		}

		@Override
		public void onDeleteButton(Integer nIsDeleteEnable) {
			// TODO Auto-generated method stub
			if (nIsDeleteEnable == 0) {
				mBtnImport.setEnabled(false);
			}
			else if (nIsDeleteEnable == 1) {
				mBtnImport.setEnabled(true);
			}
		}

		@Override
		public void onStartStopAllButton(Integer nIsStartStopEnable) {
			// TODO Auto-generated method stub
			
		}
	};
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.logger.d("onCreate");
		this.setContentView(R.layout.file_browser);
		
		super.onCreate(savedInstanceState);

		((Button)findViewById(R.id.btn_titlebar_back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		mSelectionStack.push(0);
		this.mCurrentPathTextView = (TextView)findViewById(R.id.folder_titlebar_dir_text);
		this.mCurrentPathTextView.setText(SDCardUtil.getInstance().getSDCardRootDir());
		
	    
		mBtnImport = (Button)findViewById(R.id.folder_bottombar_import_button);
		mBtnImport.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				importAllListviewItemIncludeFiles();
			}
		});
				
		mSelectInverse = (Button)findViewById(R.id.folder_bottombar_select_button);
		mSelectInverse.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mIsSelectInverse = !mIsSelectInverse;
				updateAllListviewItemEditStatue(mFolderAdapter);
			}
		});
		
		
		mFolderListView = (ListView)findViewById(R.id.folder_listview);
		mFolderAdapter = new FolderAdapter(this, mEditableAdapterCallback);
		mFolderListView.setAdapter(mFolderAdapter);
		
		mFolderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt,
					long paramLong) {
				// TODO Auto-generated method stub
				FolderItemPackage item = ((FolderItemPackage)mFolderAdapter.getItem(paramInt));
				
				// 文件夹则进入
				if (item.getFolderName() != null) {
					mFolderAdapter.fillList(item);				
					mFolderAdapter.setSelectItemNum(0);
					mCurrentPathTextView.setText(mFolderAdapter.getCurrentPath());
					
					if (item.getFile() != null) { // 进入真正的文件夹
						mSelectionStack.push(Integer.valueOf(paramInt));
						mFolderListView.setSelection(0);					
					}
					else { // 返回上一层
						mFolderListView.setSelection(mSelectionStack.pop());
					}
					
					if (mFolderAdapter.getAllItemNum() == 1) {
						ToastUtil.showMessage(FileBrowserActivity.this, getString(R.string.file_browser_activity_no_video), Toast.LENGTH_LONG);
					}
				
				}				
				// 文件则选中该条目
				else {
					mFolderAdapter.onItemClick(paramView);
				}
			}
		});

		mFolderAdapter.fillList(null);
	}
	
	/*
	 * 导入所有选中项包含的视频文件
	 */
	private void importAllListviewItemIncludeFiles() {
		mFolderAdapter.removeMarkDeleteItem();
	}
	
	private void updateAllListviewItemEditStatue(EditableAdapter<? extends ItemPackage> editableAdapter)
	{
		int selectItemNum = 0;
		for (int i = 0; i < editableAdapter.getItems().size(); ++i)
		{	
			// 添加是否为“返回上一级目录”的判断
			FolderItemPackage item = (FolderItemPackage)editableAdapter.getItems().get(i);
			if (item.isToParent()) {
				continue;
			}
			
			editableAdapter.getItems().get(i).setSelectedDel(mIsSelectInverse);
			if (mIsSelectInverse)
			{
				selectItemNum++;
			}
		}
		editableAdapter.setSelectItemNum(selectItemNum);
		editableAdapter.notifyDataSetChanged();

		if (selectItemNum > 0) {
			((FolderAdapter)editableAdapter).notifyDeleteButton(1);
		}
		else {
			((FolderAdapter)editableAdapter).notifyDeleteButton(0);
		}
		
		mSelectInverse.setText(!mIsSelectInverse ? R.string.select_all : R.string.select_reverse);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && 
			event.getAction()  == KeyEvent.ACTION_UP    &&
			mFolderAdapter.toParent())
		{	
			mCurrentPathTextView.setText(mFolderAdapter.getCurrentPath());
			// 设置fastScroll			
			mFolderListView.setSelection(mSelectionStack.pop());
			return true;
		}
		
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public void onBackPressed() 
	{
		logger.d("onBackPressed");
	//	MainTabActivity.setOnResumeAnimation(true);
		finish();
		super.onBackPressed();
	}
}
