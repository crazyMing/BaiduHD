package com.baidu.hd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.baidu.hd.log.Logger;
import com.baidu.hd.ui.IntroductionViewDragableSpace;
import com.baidu.hd.ui.OnViewChangedListener;
import com.baidu.hd.R;

public class StartHelpActivity extends BaseActivity{

	private Logger logger = new Logger("StartHelpActivity");
	private IntroductionViewDragableSpace mScrollLayout = null;
	
	/** ¹Ø±ÕÒýµ¼Ò³¼àÌý */
	private OnClickListener mCloseListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            startActivity(new Intent(StartHelpActivity.this, MainActivity.class));
            finish();
        }
    };
	
	private OnViewChangedListener mOnViewChangedListener = new OnViewChangedListener() {
		@Override
		public void onViewChanged(int viewIndex) {
			if (viewIndex < 0 || viewIndex >= mScrollLayout.getChildCount()) {
				mCloseListener.onClick(null);
				finish();
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		logger.d("onCreate");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.introduction);
		
		mScrollLayout = (IntroductionViewDragableSpace)findViewById(R.id.personal_scroll_layout);
		mScrollLayout.SetOnViewChangedListener(mOnViewChangedListener);
		findViewById(R.id.introduction_close_cirle_01).setOnClickListener(mCloseListener);
		
		this.setBackExitFlag(true);
	}

	@Override
	protected boolean canExit() {
		return true;
	}
}
