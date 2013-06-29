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
	
	/** ȫѡ״̬ */
	private boolean mIsSelectInverse = false;
	
	/** ��Ӱ�ť */
	private Button mBtnImport = null;
	
	/** ȫѡ��ѡ��ť */
	private Button mSelectInverse = null;
	
	/** �ļ���ListView */
	private ListView mFolderListView = null;
	
	/** ��һ��λ�� */
	private Stack<Integer> mSelectionStack = new Stack<Integer>();
	
	/** ��ǰ·�� */
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
				mIsSelectInverse = false; // û��ȫѡ
			}
			else if (nIsSelete == 2)
			{
				mIsSelectInverse = true; //�Ѿ�ȫѡ
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
				
				// �ļ��������
				if (item.getFolderName() != null) {
					mFolderAdapter.fillList(item);				
					mFolderAdapter.setSelectItemNum(0);
					mCurrentPathTextView.setText(mFolderAdapter.getCurrentPath());
					
					if (item.getFile() != null) { // �����������ļ���
						mSelectionStack.push(Integer.valueOf(paramInt));
						mFolderListView.setSelection(0);					
					}
					else { // ������һ��
						mFolderListView.setSelection(mSelectionStack.pop());
					}
					
					if (mFolderAdapter.getAllItemNum() == 1) {
						ToastUtil.showMessage(FileBrowserActivity.this, getString(R.string.file_browser_activity_no_video), Toast.LENGTH_LONG);
					}
				
				}				
				// �ļ���ѡ�и���Ŀ
				else {
					mFolderAdapter.onItemClick(paramView);
				}
			}
		});

		mFolderAdapter.fillList(null);
	}
	
	/*
	 * ��������ѡ�����������Ƶ�ļ�
	 */
	private void importAllListviewItemIncludeFiles() {
		mFolderAdapter.removeMarkDeleteItem();
	}
	
	private void updateAllListviewItemEditStatue(EditableAdapter<? extends ItemPackage> editableAdapter)
	{
		int selectItemNum = 0;
		for (int i = 0; i < editableAdapter.getItems().size(); ++i)
		{	
			// ����Ƿ�Ϊ��������һ��Ŀ¼�����ж�
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
			// ����fastScroll			
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
