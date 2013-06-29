package com.baidu.browser.webpool;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @ClassName: BPWebPoolMaskView 
 * @Description: WebpPoolView遮罩层，拦截TouchEvent，防止多View遮罩的时候，还能点击以前页面的链接 
 * @author LEIKANG 
 * @date 2012-12-6 下午2:25:49
 */
public class BPWebPoolMaskView extends View {

	/**
	 * @param aContext
	 *            Context
	 */
	public BPWebPoolMaskView(Context aContext) {
		this(aContext, null);
	}

	/**
	 * @param aContext
	 *            Context
	 * @param aAttrs
	 *            AttributeSet
	 */
	public BPWebPoolMaskView(Context aContext, AttributeSet aAttrs) {
		super(aContext, aAttrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

}
