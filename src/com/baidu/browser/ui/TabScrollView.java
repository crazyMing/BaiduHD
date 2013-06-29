package com.baidu.browser.ui;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.baidu.hd.R;

/**
 * @ClassName: TabScrollView 
 * @Description: 多标签滚动view
 * @author LEIKANG 
 * @date 2013-6-19 下午6:38:57
 */
public class TabScrollView extends HorizontalScrollView {

    private LinearLayout mContentView;
    private int mSelected;
    private int mAnimationDuration;
    private int mTabOverlap;

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public TabScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public TabScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * @param context
     */
    public TabScrollView(Context context) {
        super(context);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void init(Context ctx) {
        mAnimationDuration = ctx.getResources().getInteger(
                R.integer.tab_animation_duration);
        mTabOverlap = (int) ctx.getResources().getDimension(R.dimen.tab_overlap);
        setHorizontalScrollBarEnabled(false);
        setOverScrollMode(OVER_SCROLL_NEVER);
        mContentView = new TabLayout(ctx);
        mContentView.setOrientation(LinearLayout.HORIZONTAL);
        mContentView.setLayoutParams(
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        mContentView.setPadding(
                (int) ctx.getResources().getDimension(R.dimen.tab_first_padding_left),
                0, 0, 0);
        addView(mContentView);
        mSelected = -1;
        setScroll(getScroll());
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        ensureChildVisible(getSelectedTab());
    }

    public void updateLayout() {
        final int count = mContentView.getChildCount();
        for (int i = 0; i < count; i++) {
            //final Button tv = (Button) mContentView.getChildAt(i);
            //tv.updateLayoutParams();
        }
        ensureChildVisible(getSelectedTab());
    }

    public void setSelectedTab(int position) {
        View v = getSelectedTab();
        if (v != null) {
            v.setActivated(false);
        }
        mSelected = position;
        v = getSelectedTab();
        if (v != null) {
            v.setActivated(true);
        }
        requestLayout();
    }

    public int getChildIndex(View v) {
        return mContentView.indexOfChild(v);
    }

    public View getSelectedTab() {
        if ((mSelected >= 0) && (mSelected < mContentView.getChildCount())) {
            return mContentView.getChildAt(mSelected);
        } else {
            return null;
        }
    }

    public void clearTabs() {
        mContentView.removeAllViews();
    }

    public void addTab(View tab) {
        mContentView.addView(tab);
        tab.setActivated(false);
    }

    public void removeTab(View tab) {
        int ix = mContentView.indexOfChild(tab);
        if (ix == mSelected) {
            mSelected = -1;
        } else if (ix < mSelected) {
            mSelected--;
        }
        mContentView.removeView(tab);
    }

    //TODO 优化滚动
    public void ensureChildVisible(View child) {
        if (child != null) {
            int childl = child.getLeft();
            int childr = childl + child.getWidth();
            int viewl = getScrollX();
            int viewr = viewl + getWidth();
            if (childl < viewl) {
                // need scrolling to left
                animateScroll(childl);
            } else if (childr > viewr) {
                // need scrolling to right
                animateScroll(childr - viewr + viewl);
            }
        }
    }

    private void animateScroll(int newscroll) {
        ObjectAnimator animator = ObjectAnimator.ofInt(this, "scroll", getScrollX(), newscroll);
        animator.setDuration(mAnimationDuration);
        animator.start();
    }

    /**
     * required for animation
     */
    public void setScroll(int newscroll) {
        scrollTo(newscroll, getScrollY());
    }

    /**
     * required for animation
     */
    public int getScroll() {
        return getScrollX();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
 
        if (isHardwareAccelerated()) {
            int count = mContentView.getChildCount();
            for (int i = 0; i < count; i++) {
                mContentView.getChildAt(i).invalidate();
            }
        }
    }

    class TabLayout extends LinearLayout {

        public TabLayout(Context context) {
            super(context);
            setChildrenDrawingOrderEnabled(true);
        }

        @Override
        protected void onMeasure(int hspec, int vspec) {
            super.onMeasure(hspec, vspec);
            int w = getMeasuredWidth();
            w -= Math.max(0, mContentView.getChildCount() - 1) * mTabOverlap;
            setMeasuredDimension(w, getMeasuredHeight());
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            if (getChildCount() > 1) {
                int nextLeft = getChildAt(0).getRight() - mTabOverlap;
                for (int i = 1; i < getChildCount(); i++) {
                    View tab = getChildAt(i);
                    int w = tab.getRight() - tab.getLeft();
                    tab.layout(nextLeft, tab.getTop(), nextLeft + w, tab.getBottom());
                    nextLeft += w - mTabOverlap;
                }
            }
        }

        @Override
        protected int getChildDrawingOrder(int count, int i) {
            int next = -1;
            if ((i == (count - 1)) && (mSelected >= 0) && (mSelected < count)) {
                next = mSelected;
            } else {
                next = count - i - 1;
                if (next <= mSelected && next > 0) {
                    next--;
                }
            }
            return next;
        }

    }

}
