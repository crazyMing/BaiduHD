package com.baidu.hd.settings;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.log.Logger;
import com.baidu.hd.R;

public class DocShowActivity extends BaseActivity
{
	private Logger logger = new Logger("PlayerDocShowActivity");
	private String strToShow="";
	private String title="";
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		logger.d("onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showdoc);
		strToShow=getIntent().getStringExtra("str");
		title=getIntent().getStringExtra("title");
		((TextView)findViewById(R.id.txt)).setText(strToShow);
		((TextView)findViewById(R.id.title)).setText(title);
		
		((Button) findViewById(R.id.btn_titlebar_back)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
