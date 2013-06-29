package com.baidu.hd;

import com.baidu.hd.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

public class PlayerhomeFloatDialog extends BaseActivity implements OnClickListener {
	
	public static final String PLAYER_HOME_DIALOG_TYPE = "DialogType";
	
	public static final int HISTORY = 0;
	public static final int WEBSITE = 1;
	public static final int PLAYERHOME_FLOAT_PLAYNOW = 1;
	public static final int PLAYERHOME_FLOAT_DELETEFILE = 2;
	public static final int PLAYERHOME_FLOAT_OPENSITE = 3;
	public static final int PLAYERHOME_FLOAT_DELETESITE = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initWidgets(getIntent().getExtras());
	}
	
	private void initWidgets(Bundle b) {
		int pageType = b.getInt(PLAYER_HOME_DIALOG_TYPE);
		
		setContentView(R.layout.playerhome_float_dialog);
		
		switch (pageType) {
		case HISTORY:
			findViewById(R.id.playerhome_float_history).setVisibility(View.VISIBLE);
			findViewById(R.id.playerhome_float_playnow).setOnClickListener(this);
			findViewById(R.id.playerhome_float_deletefile).setOnClickListener(this);
			break;
		case WEBSITE:
			findViewById(R.id.playerhome_float_site).setVisibility(View.VISIBLE);
			findViewById(R.id.playerhome_float_opensite).setOnClickListener(this);
			findViewById(R.id.playerhome_float_deletesite).setOnClickListener(this);
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.playerhome_float_playnow:
			setResult(PLAYERHOME_FLOAT_PLAYNOW, new Intent(this, MainActivity.class));
			finish();
			break;
		case R.id.playerhome_float_deletefile:
			setResult(PLAYERHOME_FLOAT_DELETEFILE, new Intent(this, MainActivity.class));
			break;
		case R.id.playerhome_float_opensite:
			setResult(PLAYERHOME_FLOAT_OPENSITE, new Intent(this, MainActivity.class));
			break;
		case R.id.playerhome_float_deletesite:
			setResult(PLAYERHOME_FLOAT_DELETESITE, new Intent(this, MainActivity.class));
			break;
		default:
			break;
		}
		finish();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return super.onTouchEvent(event);
	}
}
