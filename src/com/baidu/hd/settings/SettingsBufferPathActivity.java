package com.baidu.hd.settings;

import java.util.List;
import java.util.Stack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.baidu.hd.adapter.SettingsBufferPathAdapter;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.FolderItemPackage;
import com.baidu.hd.module.ItemPackage;
import com.baidu.hd.personal.SDCardUtil;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.ToastUtil;
import com.baidu.hd.R;

public class SettingsBufferPathActivity extends BaseActivity implements OnClickListener {
	
	private Logger logger = new Logger("SettingsBufferPathActivity");
	
	public static final int NEW_FOLDER_DIALOG = 1;
	
	/** 返回按钮 */
	private Button mButtonBack = null;
	/** 确定按钮 */
	private Button mButtonSave = null;
	/** 新建文件夹 */
	private Button mButtonNew = null;
	/** 文件夹ListView */
	private ListView mFolderListView = null;
	/** 上一屏位置 */
	private Stack<Integer> mSelectionStack = new Stack<Integer>();
	/** 当前路径 */
	private TextView mCurrentPathTextView = null;
	private String mCurrentPath = "";
	/** Adapter */
	private SettingsBufferPathAdapter mAdapter = null;
	/** 当前选中项 */
	private FolderItemPackage mCurrentItemPackage = null;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.logger.d("onCreate");
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.file_browser);
		
		// 设定View
		mButtonBack = ((Button)findViewById(R.id.btn_titlebar_back));
		mButtonSave = (Button)findViewById(R.id.folder_bottombar_select_button);
		mButtonNew = (Button)findViewById(R.id.folder_bottombar_import_button);
		mCurrentPathTextView = (TextView)findViewById(R.id.folder_titlebar_dir_text);
		mFolderListView = (ListView)findViewById(R.id.folder_listview);
		
		// 设置初始显示内容
		mButtonSave.setText(getResources().getText(R.string.settings_save));
		mButtonNew.setText(getResources().getText(R.string.settings_new_folder));
		mCurrentPathTextView.setText(Const.Path.NetVideoBufferFilePath);
		mButtonNew.setEnabled(true);
		
		// 设置监听
		mButtonBack.setOnClickListener(this);
		mButtonSave.setOnClickListener(this);
		mButtonNew.setOnClickListener(this);
		
		// 其他初始化内容
		SharedPreferences sharedPreferences = 	getSharedPreferences(Const.SharedPreferences.NAME_NetVideoBufferPath, Context.MODE_PRIVATE);
		mCurrentPath = sharedPreferences.getString(Const.SharedPreferences.KEY_NetVideoBufferPath, Const.Path.NetVideoBufferFilePath);
		mCurrentPathTextView.setText(mCurrentPath);
		
		refreshListView();
	}
	
	@Override
	protected void onResume() {
		logger.d("onResume");
		super.onResume();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && 
			event.getAction()  == KeyEvent.ACTION_UP    &&
			mAdapter.toParent()) {
			mCurrentPath = mAdapter.getCurrentPath();
			mCurrentPathTextView.setText(mCurrentPath);
			// 设置fastScroll
			mFolderListView.setSelection(mSelectionStack.pop());
			return true;
		}
		
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public void onBackPressed() {
		logger.d("onBackPressed");
		finish();
		super.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == NEW_FOLDER_DIALOG) {
			String path = data.getExtras().getString("path");
			logger.d("onActivityResult path = " + path);
			
			if (!StringUtil.isEmpty(path)) {
				// 刷新listiew，指定到新建的文件夹上
				refreshListView();
				// 指定到当前文件夹
				int index = mAdapter.getIndex(path);
				mFolderListView.setSelection(index);
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btn_titlebar_back: // 返回
			onBackPressed();
			break;
			
		case R.id.folder_bottombar_select_button: // 确定
			SharedPreferences sharedPreferences = 	getSharedPreferences(Const.SharedPreferences.NAME_NetVideoBufferPath, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
	        editor.putString(Const.SharedPreferences.KEY_NetVideoBufferPath, mCurrentPath);
	        editor.commit();
	        
	        // Toast提示
	        String message = getString(R.string.settings_save_tips);
	        message += mCurrentPath;
	        ToastUtil.showMessage(this, message, Toast.LENGTH_LONG);
	        
	        // 退出
	        onBackPressed();
			break;
			
		case R.id.folder_bottombar_import_button: // 新建文件夹
			Intent intent = new Intent(this, SettingsCreateNewFolderDialog.class);
			intent.putExtra("path", mCurrentPath);
			startActivityForResult(intent,  SettingsBufferPathActivity.NEW_FOLDER_DIALOG);
			break;
		}
	}
	
	/**
	 * 展开当前项
	 */
	private void refreshListView() {
		List<String> pathList = SettingsBufferPathAdapter.getPathListToSDCard(mCurrentPath);
		for (int i = 0; i < pathList.size(); i++) {
			mSelectionStack.push(0);
		}
		
		mAdapter = new SettingsBufferPathAdapter(this, mCurrentPath);
		mFolderListView.setAdapter(mAdapter);
		
		mFolderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, 	long paramLong) {
				
				mCurrentItemPackage = ((FolderItemPackage)mAdapter.getItem(paramInt));
				
				// 文件夹则进入
				if (mCurrentItemPackage.getFolderName() != null) {
					mAdapter.fillList(mCurrentItemPackage);		
					mCurrentPath = mAdapter.getCurrentPath();
					mCurrentPathTextView.setText(mCurrentPath);
					
					if (mCurrentItemPackage.getFile() != null) { // 进入真正的文件夹
						mSelectionStack.push(Integer.valueOf(paramInt));
						mFolderListView.setSelection(0);					
					}
					else { // 返回上一层
						mFolderListView.setSelection(mSelectionStack.pop());
					}
				}				
			}
		});
		
		mAdapter.fillList(null);
	}
}
