package com.baidu.hd.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.baidu.hd.BrowserSpecActivity;
import com.baidu.hd.log.Logger;
import com.baidu.hd.util.Const.IntentExtraKey;
import com.baidu.hd.R;
public class BrowDialogTip {
	
	private Logger logger = new Logger("BrowDialogTip");
	
	private Context mActivity = null;
	private PopupWindow mDialogTip = null;
	private View mViewDialogTip = null;
	private TextView mTextDialogTip = null;
	private LinearLayout mLinearLayout = null;
	private ImageView mImageClose = null;
	
	private long mAlbumId = 0;
	
	public BrowDialogTip(Context activity) {
		logger.d("constructor");
		this.mActivity = activity;
		
		// 初始化SnifferDialog布局及控件
		LayoutInflater inflater = LayoutInflater.from(mActivity);
		mViewDialogTip = inflater.inflate(R.layout.browser_dialog_tip_layout, null);
		mTextDialogTip = (TextView)mViewDialogTip.findViewById(R.id.brow_dialog_tip_text);
		mLinearLayout = (LinearLayout)mViewDialogTip.findViewById(R.id.brow_dialog_tip_parent);
		mImageClose = (ImageView)mViewDialogTip.findViewById(R.id.brow_dialog_tip_close);
		mImageClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDialogTip.isShowing()) {
					mDialogTip.dismiss();
				}
			}
		});
		
		mViewDialogTip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialogTip.dismiss();
				Intent intent = new Intent(mActivity, BrowserSpecActivity.class);
				intent.putExtra(IntentExtraKey.BrowSpecIsFromBdFrameView, true);
				intent.putExtra(IntentExtraKey.BrowSpecAlbumId, mAlbumId);
				mActivity.startActivity(intent);
				
			}
		});
	}
	
	public void createDialog() {
		logger.d("create sniffer dialog");
		
		int height = mActivity.getResources().getDimensionPixelSize(R.dimen.float_searchbox_height);
		
		Display disp = ((Activity)mActivity).getWindowManager().getDefaultDisplay();
		this.mDialogTip = new PopupWindow(mViewDialogTip, (int)(disp.getWidth()*29.0/30), height);
		mDialogTip.setBackgroundDrawable(new ColorDrawable(0));
		mDialogTip.setOutsideTouchable(true);
		this.mDialogTip.setTouchInterceptor(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					mDialogTip.dismiss();
					return true;
				}
				return false;
			}
		});
		
	}
	
	public void destroyDialog() {
		logger.d("destroy sniffer dialog");
		
		if (this.mDialogTip != null) {
			this.mDialogTip = null;
		}
	}
	
	public void showSnifferDialog(int count, long albumId) {
		logger.d("show sniffer dialog");
		mAlbumId = albumId;
		String format = mActivity.getResources().getString(R.string.brow_sniffer_tip_text);
		String tip = String.format(format, count);
		mTextDialogTip.setText(tip);
		mLinearLayout.setBackgroundResource(R.drawable.browser_sniffer_tip);
		
		this.mDialogTip.showAtLocation((View)((Activity) mActivity).findViewById(R.id.MainRoot), Gravity.BOTTOM, 0,mActivity.getResources().getDimensionPixelSize(R.dimen.bottom_toolbar_height));
	}
	
	public void hideDialog() {
		logger.d("hide sniffer or first dialog");
		this.mDialogTip.dismiss();
	}
	
	public boolean isReady() {
		return (mDialogTip != null ? true : false);
	}
	
	public boolean isShow() {
		return mDialogTip.isShowing();
	}
	
	

}
