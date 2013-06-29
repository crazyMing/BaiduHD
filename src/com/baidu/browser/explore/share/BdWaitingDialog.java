package com.baidu.browser.explore.share;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.hd.R;

/**
 * 等待提示框
 */
public class BdWaitingDialog extends Dialog {
	/**
	 * 文字大小
	 */
	private static final int TEXT_SIZE = 16;
	/**
	 * 文字PADDING
	 */
	private static final int MESSAGE_PADDING = 12;

	/**
	 * 提示内容
	 */
	private String mMessage;
	/**
	 * Context
	 */
	private Context mContext;

	/**
	 * @param aContext
	 *            Context
	 */
	public BdWaitingDialog(Context aContext) {
		super(aContext, R.style.BdWaitingDialog);

		mContext = aContext;
	}

	/**
	 * @param context
	 *            Context
	 * @param message
	 *            提示内容
	 * @return BdWaitingDialog实例
	 */
	public static BdWaitingDialog show(Context context, CharSequence message) {
		return show(context, message, false);
	}

	/**
	 * @param context
	 *            Context
	 * @param message
	 *            提示内容
	 * @param cancelable 是否可取消
	 * @return BdWaitingDialog实例
	 */
	public static BdWaitingDialog show(Context context, CharSequence message, boolean cancelable) {
		return show(context, message, cancelable, null);
	}

	/**
	 * @param context Context
	 * @param message 提示内容
	 * @param cancelable 是否可取消
	 * @param cancelListener 取消监听器
	 * @return BdWaitingDialog实例
	 */
	public static BdWaitingDialog show(Context context, CharSequence message, boolean cancelable,
			OnCancelListener cancelListener) {
		BdWaitingDialog dialog = new BdWaitingDialog(context);
		dialog.setMessage(message);
		dialog.setCancelable(cancelable);
		dialog.setOnCancelListener(cancelListener);
		dialog.show();
		return dialog;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(new BdWaitingView(mContext));
	}

	/**
	 * @param aMessage 提示内容
	 */
	public void setMessage(String aMessage) {
		mMessage = aMessage;
	}

	/**
	 * @param aMessage 提示内容
	 */
	public void setMessage(CharSequence aMessage) {
		mMessage = aMessage.toString();
	}

	/**
	 * @param aResId 提示内容资源id
	 */
	public void setMessage(int aResId) {
		mMessage = mContext.getResources().getString(aResId);
	}

	/**
	 * 自定义等待提示控件
	 */
	public class BdWaitingView extends LinearLayout {

		/**
		 * 文字大小
		 */
		private int mTextSize;
		/**
		 * 文字PADDING
		 */
		private int mMessagePadding;
		/**
		 * 屏幕宽度
		 */
		private int mScreenWidth;

		/**
		 * Context
		 */
		private Context mContext;

		/**
		 * @param aContext Context
		 */
		public BdWaitingView(Context aContext) {
			super(aContext);

			mContext = aContext;

			loadConfiguration();

			layoutDesign();
		}

		/**
		 * 初始化配置
		 */
		private void loadConfiguration() {
			DisplayMetrics dm = new DisplayMetrics();
			((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
			float density = dm.density;
			mScreenWidth = dm.widthPixels;

			mTextSize = (int) (TEXT_SIZE * Math.sqrt(density));
			mMessagePadding = (int) (MESSAGE_PADDING * density);
		}

		/**
		 * 初始化布局
		 */
		private void layoutDesign() {
			setOrientation(LinearLayout.VERTICAL);
			FrameLayout.LayoutParams lParams0 = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);
			setLayoutParams(lParams0);
			setBackgroundColor(Color.TRANSPARENT);

			ProgressBar progressBar = new ProgressBar(mContext);
			progressBar.setBackgroundColor(Color.TRANSPARENT);
			LinearLayout.LayoutParams lParams1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			lParams1.gravity = Gravity.CENTER;
			progressBar.setLayoutParams(lParams1);
			addView(progressBar);
			TextView textView = new TextView(mContext);
			textView.setText(mMessage);
			LinearLayout.LayoutParams lParams2 = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);
			lParams2.gravity = Gravity.CENTER;
			lParams2.width = mScreenWidth;
			textView.setGravity(Gravity.CENTER);
			textView.setLayoutParams(lParams2);
			textView.setBackgroundColor(Color.TRANSPARENT);
			textView.setPadding(mMessagePadding, mMessagePadding, mMessagePadding, mMessagePadding);
			textView.setTextSize(mTextSize);
			textView.setTextColor(0xffe5e5e5); // SUPPRESS CHECKSTYLE
			addView(textView);
		}
	}

}
