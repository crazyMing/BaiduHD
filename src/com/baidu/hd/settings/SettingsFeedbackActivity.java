package com.baidu.hd.settings;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.detect.Detect;
import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.log.Logger;
import com.baidu.hd.net.Uploader;
import com.baidu.hd.net.UploaderEventArgs;
import com.baidu.hd.R;

public class SettingsFeedbackActivity extends BaseActivity
{
	private Logger logger = new Logger(SettingsFeedbackActivity.class.getSimpleName());
	
	/** 是否从首页跳转 */
	private boolean mIsFromHome = false;
	
	private class UploaderEventListener implements EventListener
	{
		private ProgressDialog progressDialog = null;
		
		public UploaderEventListener(ProgressDialog progressDialog)
		{
			this.progressDialog = progressDialog;
		}
		
		@Override
		public void onEvent(EventId id, EventArgs args)
		{
			UploaderEventArgs uploaderArgs = (UploaderEventArgs) args;
			int textId = 0;
			if (uploaderArgs.isSuccess())
			{
				textId = R.string.settings_other_feedback_successed;
			}
			else
			{
				textId = R.string.settings_other_feedback_net_busy;
			}
			if (textId != 0)
			{
				Toast toast = Toast.makeText(SettingsFeedbackActivity.this, textId, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
			if (this.progressDialog != null)
			{
				progressDialog.cancel();
			}
			onBackPressed();
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		logger.d("onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bdsettings_feedback);
		
		mIsFromHome = getIntent().getBooleanExtra("FromHome", false);
		
		((Button) findViewById(R.id.btn_titlebar_back)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		Button submitButton = (Button) findViewById(R.id.submit);
		submitButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				EditText contentEditText = (EditText)findViewById(R.id.feedback_info_content);
				if("".equals(contentEditText.getText().toString().trim())) {
					Toast.makeText(SettingsFeedbackActivity.this, R.string.settings_other_feedback_input_content, Toast.LENGTH_SHORT).show();
					return;
				}
				
				// TODO
				Detect detect = (Detect) SettingsFeedbackActivity.this.getServiceProvider(Detect.class);
				if (detect.isNetAvailabe())
				{
					EditText contactEditText = (EditText)findViewById(R.id.feedback_info_contact);
					final EventCenter eventCenter = (EventCenter) getServiceProvider(EventCenter.class);
					ProgressDialog dialog = new ProgressDialog(SettingsFeedbackActivity.this);
					final EventListener listener = new UploaderEventListener(dialog);

					dialog.setMessage(SettingsFeedbackActivity.this.getText(R.string.settings_other_feedback_title));
					dialog.setIndeterminate(true);
					dialog.setCanceledOnTouchOutside(false);
					dialog.setCancelable(true);
					dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
					{
						@Override
						public void onCancel(DialogInterface dialog)
						{
							eventCenter.removeListener(listener);
							onBackPressed();
						}
					});
					eventCenter.addListener(EventId.eFeedBackComplete, listener);
					dialog.show();
					dialog.getWindow().setGravity(Gravity.CENTER);
					((Uploader)getServiceProvider(Uploader.class)).feedBack(contentEditText.getText().toString(), contactEditText.getText().toString());
				}
				else 
				{
					Toast.makeText(SettingsFeedbackActivity.this, R.string.settings_other_feedback_net_error, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (mIsFromHome) {
//			MainActivity.setOnResumeAnimation(true);
		}
		else {
		}
		super.onBackPressed();
	}
}
