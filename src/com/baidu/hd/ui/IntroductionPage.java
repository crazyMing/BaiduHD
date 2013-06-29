package com.baidu.hd.ui;

import android.app.Activity;
import android.view.View.OnClickListener;

import com.baidu.hd.ui.OnViewChangedListener;
import com.baidu.hd.R;
/**
 * 引导页，基于百度App进行设计，传入一个Activity对象，进行设置
 */
public class IntroductionPage {
	
	private Activity mActivity = null;
	private IntroductionViewDragableSpace mScrollLayout = null;
	
	/** 关闭引导页监听 */
	private OnClickListener mCloseListener = null;
	
	private OnViewChangedListener mOnViewChangedListener = new OnViewChangedListener() {
		@Override
		public void onViewChanged(int viewIndex) {
			  if (viewIndex < 0 || viewIndex >= mScrollLayout.getChildCount()) {
                  mCloseListener.onClick(null);
              }
		}
	};
	
	public IntroductionPage(Activity actvity, OnClickListener closeListener) {
		mActivity = actvity;
		mCloseListener = closeListener;
		mActivity.setContentView(R.layout.introduction);
		mScrollLayout = (IntroductionViewDragableSpace)mActivity.findViewById(R.id.personal_scroll_layout);
		mScrollLayout.SetOnViewChangedListener(mOnViewChangedListener);
		mActivity.findViewById(R.id.introduction_close_cirle_01).setOnClickListener(mCloseListener);
	}
}
