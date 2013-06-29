package com.baidu.hd.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Scroller;

/**
 * @ClassName: HomeScrollView 
 * @Description: 首页可滚动部分，不包括工具栏和搜索栏
 * @author LEIKANG 
 * @date 2012-12-7 下午2:42:19
 */
public class HomeScrollView extends ScrollView {
    /** 自行滚动时的时间 */
    private static final int SCROLL_TIME = 300;
    
    /** 需要滚动到的View */
    private View mViewToScrollTo;
    /** rect */
    private Rect mRect = new Rect();
    /**
     * 首页搜索框阴影
     */
    private ImageView mShadow;
    
    /**
     * 自主滚动的scroller.
     */
    private Scroller mScroller = new Scroller(getContext(),
            new android.view.animation.AccelerateDecelerateInterpolator());

    /**
     * @param context
     *            context
     */
    public HomeScrollView(Context context) {
        super(context);
    }

    /**
     * @param context
     *            context
     * @param attrs
     *            attrs
     */
    public HomeScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    /**
     * @param context
     *            context
     * @param attrs
     *            attrs
     * @param defStyle
     *            defStyle
     */
    public HomeScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (mViewToScrollTo != null
                && mViewToScrollTo.getVisibility() == View.VISIBLE) {
            mRect.left = 0;
            mRect.right = mViewToScrollTo.getRight();
            mRect.bottom = mViewToScrollTo.getHeight();
            mRect.top = 0;
            offsetDescendantRectToMyCoords(mViewToScrollTo, mRect);
            int toScroll = mRect.top - getScrollY();
            mScroller.abortAnimation();
            mScroller.startScroll(0, getScrollY(), 0, toScroll, SCROLL_TIME);
            mViewToScrollTo = null;
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int scrollY = mScroller.getCurrY();
            scrollTo(0, scrollY);
            postInvalidate();
        } else {
            super.computeScroll();
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
        }
        return super.onTouchEvent(ev);
    }
    /**
     * 将一个Visibilty为GONE的View显示出来，并滚动到一个指定子View的顶部。
     * 
     * @param toSpan
     *            需要展开的View，此时的状态必须是GONE。
     * @param toScrollTo
     *            需要滚动到的View，必须是HomeScrollView的子View
     */
    public void spanView(View toSpan, View toScrollTo) {
        toSpan.setVisibility(View.VISIBLE);
        mViewToScrollTo = toScrollTo;
    }
    
    /**
     * 使用自定义的scroller滚动到指定位置
     * @param scrollToY 需要滚动到的位置
     */
    public void customScrollTo(int scrollToY) {
        int toScroll = scrollToY - getScrollY();
        mScroller.abortAnimation();
        mScroller.startScroll(0, getScrollY(), 0, toScroll, SCROLL_TIME);
        postInvalidate();
    }
    
    /**
     * 设置搜索框
     * @param shadow searchbox阴影
     */
    public void setSearchBoxShadow(ImageView shadow) {
        mShadow = shadow;
    }
    
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mShadow != null) {
            if (oldt == 0 && t != 0) {
                mShadow.setVisibility(View.VISIBLE);
            }
            if (oldt != 0 && t == 0) {
                mShadow.setVisibility(View.INVISIBLE);
            }
        }
 
    }
    
    /**
     * 截图。
     */
    private Bitmap mSnapshot;
    
    /**
     * 窗口截图
     * @param width 截图宽度
     * @param height 截图高度
     * @param useHomeCache 是否在截图时缓存，仅在多窗口时为true
     * @return 窗口截图
     */
    public Bitmap captureSnapshot(int width, int height, boolean useHomeCache) {
        if (useHomeCache && mSnapshot != null && !mSnapshot.isRecycled()) {
            return mSnapshot;
        }
        if (getWidth() > 0 && getHeight() > 0) {
            try {
 
                Bitmap capture = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                capture.eraseColor(Color.WHITE);
                Canvas c = new Canvas(capture);
                final int left = getScrollX();
                final int top = getScrollY();
                int state = c.save();
                float scale = capture.getWidth() / (float) getWidth();
                c.scale(scale, scale);
                c.clipRect(0, 0, getWidth(), (int) (height / scale)); //弹出输入法键盘时，高度会变小，故使用宽度反推
                c.translate(-left, -top);
                draw(c);
                c.restoreToCount(state);
                
                if (useHomeCache) {
                    mSnapshot = capture;
                }
                return capture;
            } catch (Exception e) { 
                //因为在绘图过程中，可能产生空指针，也可能出现内存不足，故在此统一处理。
                return null;
            }
        } else {
            return null;
        }

    }
    /**
     * 清除截图
     */
    public void clearSnapshot() {
        if (mSnapshot != null) {
            mSnapshot = null;
        }
    }
}
