package com.baidu.hd;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.baidu.hd.log.Logger;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.R;

public class WelcomeActivity extends BaseActivity {

	private Logger logger = new Logger("WelcomeActivity");
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		logger.d("onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(isServiceCreated()) {
			new Handler() {

				@Override
				public void handleMessage(Message msg) {
					finish();
				}
				
			}.sendEmptyMessage(0);
		}
	}

	@Override
	protected void onDestroy() {
		logger.d("onDestroy");
		super.onDestroy();
		getPlayerApp().toForceExitApp();
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		logger.d("onNewIntent");
		//getPlayerApp().onDestroy();
		finish();
	}

	@Override
	protected void onServiceCreated() {
		super.onServiceCreated();
		logger.d("onServiceCreated");
		
		SharedPreferences preferences = getSharedPreferences("application", Context.MODE_WORLD_READABLE);
		boolean bFirstLanuch = preferences.getBoolean("first_launch", true);
		if (bFirstLanuch) {
			Intent intent = new Intent(this, StartHelpActivity.class);
			startActivity(intent);
		}
		else  {
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
		}
		
		// ×·¾ç
		PlayListManager playlistManager = (PlayListManager)getServiceProvider(PlayListManager.class);
		playlistManager.fetchNewestAlbum();
	}

	@Override
	protected boolean showProgressDialogWhenCreatingService() {
		return false;
	}
	
	@Override
	protected void onStop() {
		logger.d("onStop");
		super.onStop();
	}

	@Override
	public void finish() {
		logger.d("finish");
		super.finish();
	}
}