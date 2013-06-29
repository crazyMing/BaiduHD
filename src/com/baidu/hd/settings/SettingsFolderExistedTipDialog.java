package com.baidu.hd.settings;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.R;

public class SettingsFolderExistedTipDialog extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_folder_exited_tip);
		
		((Button)findViewById(R.id.button_back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

}
