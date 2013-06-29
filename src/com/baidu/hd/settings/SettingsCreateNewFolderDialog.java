package com.baidu.hd.settings;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.log.Logger;
import com.baidu.hd.personal.SDCardUtil;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.ToastUtil;
import com.baidu.hd.R;

public class SettingsCreateNewFolderDialog extends BaseActivity implements OnClickListener, TextWatcher {

	private Logger logger = new Logger(this.getClass().getSimpleName());
	
	/** 当前路径 */
	private String mBasePath = null;
	
	/** 当前文件夹名称 */
	private String mFoldeName = "new folder";
	
	private Button mButtonOk = null;
	private Button mButtonCancel = null;
	private EditText mEditText = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_create_new_folder);
		
		mButtonOk = (Button)findViewById(R.id.per_del_dlg_positiveButton);
		mButtonCancel = (Button)findViewById(R.id.per_del_dlg_negativeButton);
		mEditText = (EditText)findViewById(R.id.fileName);
		
		mButtonOk.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);
		mEditText.addTextChangedListener(this);
		mEditText.setSelection(mEditText.getText().toString().length());
		mEditText.selectAll();
		
		mBasePath = getIntent().getExtras().getString("path");
		mFoldeName = getResources().getText(R.string.settings_new_folder).toString();
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, SettingsBufferPathActivity.class);
		intent.putExtra("path", mFoldeName);
		setResult(SettingsBufferPathActivity.NEW_FOLDER_DIALOG, intent);
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.per_del_dlg_positiveButton:
//			mFoldeName = mEditText.getText().toString();
//			if (!StringUtil.isEmpty(mFoldeName)) {
//				mFoldeName = mFoldeName.trim();
//			}
			
			createFolder();
			onBackPressed();
			break;
			
		case R.id.per_del_dlg_negativeButton:
			onBackPressed();
			break;
		}
	}
	
	/**
	 * 创建文件夹
	 */
	private void createFolder() {
		if (StringUtil.isEmpty(mBasePath)) {
			return;
		}
		
		String path = mBasePath + mFoldeName;
		logger.d("create folder = " + path);
		
		File file = new File(path);
		if (file.exists()) {
			Intent intent = new Intent(this, SettingsFolderExistedTipDialog.class);
			startActivity(intent);
		}
		else {
			String message = getText(R.string.settings_new_folder_succeed).toString();
			if (!SDCardUtil.getInstance().isMediaMounted()) {
				message = getText(R.string.dialog_sdcard_message).toString();
			}
			else if (!file.mkdir()) {
				message = getText(R.string.settings_new_folder_failed).toString();
			}
			
			ToastUtil.showMessage(this, message, Toast.LENGTH_LONG);
			onBackPressed();
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		int length = 0;
		
		String text = s.toString();
		if (!StringUtil.isEmpty(text)) {
			text = text.trim();
			if (!StringUtil.isEmpty(text)) {
				length = text.length();
			}
		}
		
//		logger.d("afterTextChanged count=" + length);

		mFoldeName = text;
		
		if (length == 0) {
			mButtonOk.setEnabled(false);
		}
		else {
			mButtonOk.setEnabled(true);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, 	int after) {
//		logger.d("beforeTextChanged count=" + count);
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
//		logger.d("onTextChanged count=" + count);
	}
}
