package com.baidu.hd.ui;

import android.app.Activity;
import android.view.View.OnClickListener;

import com.baidu.hd.ui.OnViewChangedListener;
import com.baidu.hd.R;
/**
 * ����ҳ�����ڰٶ�App������ƣ�����һ��Activity���󣬽�������
 */
public class IntroductionPage {
	
	private Activity mActivity = null;
	private IntroductionViewDragableSpace mScrollLayout = null;
	
	/** �ر�����ҳ���� */
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
