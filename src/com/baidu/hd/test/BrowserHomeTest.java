package com.baidu.hd.test;

import android.widget.EditText;
import android.widget.TextView;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.R;

public class BrowserHomeTest extends BaseActivity {
	
	private TextView mBtnEnter = null;
	private EditText mEditTextUrl = null;
	
	private static boolean isOnResumeAnimation = false;
	private static boolean isOnPauseAnimation = false;
	public static void setOnResumeAnimation (boolean isAnimation) {
	    	isOnResumeAnimation = isAnimation;
	}
    public static void setOnPauseAnimation (boolean isAnimation) {
    	isOnPauseAnimation = isAnimation;
    }
    
	@Override
	protected void onResume() {
		if (isOnResumeAnimation) {
			overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
			isOnResumeAnimation = false;
		}
		
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (isOnPauseAnimation) {
			overridePendingTransition(R.anim.in_from_top, R.anim.out_to_bottom);
			isOnPauseAnimation = false;
		}
		
		super.onPause();
	}

}
