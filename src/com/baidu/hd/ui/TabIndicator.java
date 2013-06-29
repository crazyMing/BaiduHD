package com.baidu.hd.ui;


import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * TabIndicator
 */
public class TabIndicator extends Drawable {
    /** 选中的图标 */
    private Drawable mSelected;
    /** 未选中的图标 */
    private Drawable mUnselected;
    /** 图标数量 */
    private int mCount;
    /** 当前选中的图标 */
    private int mCurrentIndex = -1;
    /** 透明度 */
    private int mAlpha = 255; //SUPPRESS CHECKSTYLE 255表示无透明度
    
    /**
     * constructor
     * 
     * @param res res
     * @param selectedIcon selectedIcon
     * @param unselectedIcon unselectedIcon
     * @param max 最多可显示的 indicator数量
     */
    public TabIndicator(Resources res, int selectedIcon, int unselectedIcon, int max) {
        mSelected = res.getDrawable(selectedIcon);
        mUnselected = res.getDrawable(unselectedIcon);
        mSelected.setBounds(0, 0, mSelected.getIntrinsicWidth(),
                mSelected.getIntrinsicHeight());
        mUnselected.setBounds(0, 0, mUnselected.getIntrinsicWidth(),
                mUnselected.getIntrinsicHeight());
        
        
        int height = Math.max(mUnselected.getIntrinsicHeight(),
                mSelected.getIntrinsicHeight());
        setBounds(0, 0, getWidthForIndicators(max), height);

    }
    
    /**
     * 一定数量的indicator所占有长度
     * @param count count
     * @return width
     */
    private int getWidthForIndicators(int count) {
        int unselectedWidth = mUnselected.getIntrinsicWidth();
        int selectedWidth = mSelected.getIntrinsicWidth();
        int totalWidth = (count - 1) * unselectedWidth + selectedWidth;
        return totalWidth;
    }
    
    /**
     * 设置总大小
     * @param cap 总共可显示的数量。
     */
    public void setIndicatorCount(int cap) {
        mCount = cap;
    }
    
    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
    }
    @Override
    public int getIntrinsicWidth() {
        return getBounds().width();
    }
    @Override
    public int getIntrinsicHeight() {
        return getBounds().height();
    }
    
    
    /**
     * 设置当前选中的图标。
     * @param index index
     */
    public void setSelectedIndex(int index) {
        mCurrentIndex = index;
        setIndicatorCount(mCount);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        int saveCount = canvas.save();
        int count = mCount;
        int width = getWidthForIndicators(count);
        int offset = (getBounds().width() - width) / 2;
        canvas.translate(offset, 0);
        for (int i = 0; i < count; i++) {
            Drawable drawable = null;
            if (i == mCurrentIndex) {
                drawable = mSelected;
            } else {
                drawable = mUnselected;
            }
            
            if (mAlpha != 255) { //SUPPRESS CHECKSTYLE 255表示无透明度
                drawable.setAlpha(mAlpha);
            }
            drawable.draw(canvas);
            canvas.translate(drawable.getIntrinsicWidth(), 0);
        }
        canvas.restoreToCount(saveCount);
    }
    
    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        //do nothing
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}

