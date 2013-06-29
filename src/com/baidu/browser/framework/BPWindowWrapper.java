package com.baidu.browser.framework;

import java.util.ArrayList;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.baidu.browser.framework.BPFrameView.WindowStwitchAnimation;

/**
 * @ClassName: BdWindowWrapper 
 * @Description: 用于装载BdWindow的ViewGroup，同时支持动画显示切换效果。
 * @author LEIKANG 
 * @date 2012-12-7 下午3:33:36
 */
public class BPWindowWrapper extends FrameLayout {
    /** 当前窗口 */
    private BPWindow mCurrentWindow;
    /**
     *  需要关闭的窗口，在resetWrapper()中进行关闭 
     *  只在关闭窗口并且返回首页时会使用。
     * */
    private BPWindow mWindowToClose;

    /**
     * @param context
     *            context
     * @param attrs
     *            attrs
     * @param defStyle
     *            defStyle
     */
    public BPWindowWrapper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 
     * @param context
     *            context
     * @param attrs
     *            attrs
     */
    public BPWindowWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param context
     *            context
     */
    public BPWindowWrapper(Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
    }

    /**
     * 显示一个BdWindow，同时移除当前Window。
     * 
     * @param window
     *            将要显示的window.
     * 
     * @param animation
     *            切换动画效果，若为NONE则表示没有动画
     * 
     * @param url
     *            动画完成后需要加载的url
     */
    public void showWindow(BPWindow window, WindowStwitchAnimation animation,
            String url) {
        if (window == null || window == mCurrentWindow) {
            return;
        }
        
        resetWrapper();
        removeCallbacks(mResetWrapperAction);
        
        if (animation == WindowStwitchAnimation.CLOSE_WINDOW) {
            
            if (mCurrentWindow != null) {
                mCurrentWindow.setVisibility(View.INVISIBLE);
                removeView(mCurrentWindow);
            }
            
            mCurrentWindow = window;
            window.setVisibility(View.VISIBLE);
            addViewSafely(window, 0);
            window.requestFocus();
            
        } else if (animation == WindowStwitchAnimation.NEW_WINDOW) {
            mCurrentWindow = window;
            window.setVisibility(View.INVISIBLE);
            addViewSafely(window);
            window.requestFocus();
 
        } else {
            addViewSafely(window);
            if (mCurrentWindow != null) {
                removeView(mCurrentWindow);
            }
            
            mCurrentWindow = window;
            
            if (!TextUtils.isEmpty(url)) {
                mCurrentWindow.loadUrl(url);
            }
            
            if (mCurrentWindow != null) {
            	
                mCurrentWindow.isFocusable();
                
                mCurrentWindow.isFocusableInTouchMode();
                
                mCurrentWindow.setFocusable(true);
                mCurrentWindow.setFocusableInTouchMode(true);
            	
            	
            	
                mCurrentWindow.requestFocus();
            }
            post(mResetWrapperAction);
        }
    }
    
    /**
     * add view safely
     * @param child child
     */
    private void addViewSafely(View child) {
        ViewParent parent = child.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(child);
        }

        addView(child);
    }
    
    /**
     * add view safely
     * @param child child
     * @param index index
     */
    private void addViewSafely(View child, int index) {
        ViewParent parent = child.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(child);
        }

        addView(child, index);
    }
    
    /**
     * 
     * 在动画开始前，将BdWindowWrapper重置到一般状态，即只保留当前窗口。
     */
    private void resetWrapper() {
        mViewsToRemove.clear();
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child == null) {
                continue;
            }
            
            if (child != null && child == mCurrentWindow) {
                if (child.getVisibility() != View.VISIBLE) {
                    child.setVisibility(View.VISIBLE);
                }
                continue;
            }
            mViewsToRemove.add(child);
        }
        
        for (View child : mViewsToRemove) {
            removeView(child);
            if (child != null && child == mWindowToClose) {
                mWindowToClose.release();
                mWindowToClose = null;
            }
        }
        
        mViewsToRemove.clear();
    }
    
    /**
     * 保存需要删除的View的列表。
     */
    private ArrayList<View> mViewsToRemove = new ArrayList<View>();
    
    /**
     * 在Animation结束时，post此Action,重置wrapper中的view
     */
    private Runnable mResetWrapperAction = new Runnable() {
        
        @Override
        public void run() {
            resetWrapper();
        }
    };
    

    /**
     * 保证wrapper中窗口状态正常 ，主要是解决动画过程中切换的情况。
     */
    public void ensureWindow() {
        resetWrapper();
        removeCallbacks(mResetWrapperAction);
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ensureWindow();
    }
}
