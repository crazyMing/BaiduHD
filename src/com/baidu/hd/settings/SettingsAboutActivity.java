package com.baidu.hd.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.log.Logger;
import com.baidu.hd.util.SystemUtil;
import com.baidu.hd.R;

public class SettingsAboutActivity extends BaseActivity
{
	private Logger logger = new Logger("SettingAboutActivity");
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		logger.d("onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_settings_about);
		
		TextView versionInfo = (TextView)findViewById(R.id.version_info);
		versionInfo.setText(SystemUtil.getAppVerison(this));
		
		((Button)findViewById(R.id.btn_about_titlebar_back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		TextView txt=(TextView)findViewById(R.id.txt);
		String scrollMessage=txt.getText().toString();
		SpannableString msp = new SpannableString(scrollMessage);
		final String xkxy=getResources().getString(R.string.xkxx);
		final String yssm=getResources().getString(R.string.yssm);
		int start1 = scrollMessage.indexOf("<<"+xkxy+">>");
		int end1 = ("<<"+xkxy+">>").length() + start1;
		int start2 = scrollMessage.indexOf("<<"+yssm+">>");
		int end2 = ("<<"+xkxy+">>").length() + start2;
		msp.setSpan(new URLSpan(""){
			@Override
			public void onClick(View widget) {
				Intent it=new Intent();
				it.setClass(SettingsAboutActivity.this, DocShowActivity.class);
				it.putExtra("str", SettingsAboutActivity.this.getResources().getString(R.string.dialog_disclaimer_xksyxx));
				it.putExtra("title", xkxy);
				((Activity)SettingsAboutActivity.this).startActivity(it);
			}
		}, start1, end1,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		msp.setSpan(new URLSpan(""){
			@Override
			public void onClick(View widget) {
				Intent it=new Intent();
				it.setClass(SettingsAboutActivity.this, DocShowActivity.class);
				it.putExtra("str", SettingsAboutActivity.this.getResources().getString(R.string.dialog_disclaimer_ysbhsm));
				it.putExtra("title", yssm);
				((Activity)SettingsAboutActivity.this).startActivity(it);
			}
		}, start2, end2,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		txt.setText(msp);						
		txt.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onDestroy()
	{
		logger.d("onDestroy");
		super.onDestroy();
	}
}
