package com.baidu.browser.core.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @ClassName: BdPopMenuItem 
 * @Description: 菜单项 
 * @author LEIKANG 
 * @date 2012-12-11 下午5:19:12
 */
public class BdPopMenuItem extends LinearLayout {
	
	/**icon**/
	private ImageView mIcon;
	
	/**文本**/
	private TextView mText;

	/**
	 * 构造方法
	 * @param context Context
	 */
	public BdPopMenuItem(Context context) {
		this(context, null);
	}

	/**
	 * 构造方法
	 * @param context Context
	 * @param attrs AttributeSet
	 */
	public BdPopMenuItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT
				, LinearLayout.LayoutParams.WRAP_CONTENT);
		iconParams.gravity = Gravity.CENTER;
		mIcon = new ImageView(context);
		
		LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		textParams.gravity = Gravity.CENTER;
		mText = new TextView(context);
		mText.setGravity(Gravity.CENTER);
		mText.setEllipsize(TextUtils.TruncateAt.END);
		mText.setSingleLine();
		addView(mIcon, iconParams);
		addView(mText, textParams);
		setOrientation(VERTICAL);
		//setBackgroundResource(R.drawable.browser_select_btn_bg);
	}
	
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (mIcon != null) {
					mIcon.setPressed(true);
				}
				break;
			case MotionEvent.ACTION_UP:
				if (mIcon != null) {
					mIcon.setPressed(false);
				}
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_CANCEL:
				if (mIcon != null) {
					mIcon.setPressed(false);
				}
				break;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 设置文本
	 * @param text String
	 */
	public void setText(String text) {
		if (mText != null) {
			mText.setText(text);
		}
	}
	
	public String getText() {
		if (mText != null) {
			return mText.getText().toString();
		}
		return "";
	}
	
	/**
	 * setTextColor
	 * @param resid ids
	 */
	public void setTextColor(ColorStateList resid) {
		if (mText != null) {
			mText.setTextColor(resid);
		}
	}
	
	public void setTextSize(float aTextSize) {
		if (mText != null) {
			mText.setTextSize(aTextSize);
		}
	}
	
	/**
	 * 设置icon
	 * @param res id
	 */
	public void setIcon(int res) {
		if (mIcon != null) {
			mIcon.setImageResource(res);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
}
