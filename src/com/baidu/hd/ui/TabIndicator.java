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
    /** ѡ�е�ͼ�� */
    private Drawable mSelected;
    /** δѡ�е�ͼ�� */
    private Drawable mUnselected;
    /** ͼ������ */
    private int mCount;
    /** ��ǰѡ�е�ͼ�� */
    private int mCurrentIndex = -1;
    /** ͸���� */
    private int mAlpha = 255; //SUPPRESS CHECKSTYLE 255��ʾ��͸����
    
    /**
     * constructor
     * 
     * @param res res
     * @param selectedIcon selectedIcon
     * @param unselectedIcon unselectedIcon
     * @param max ������ʾ�� indicator����
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
     * һ��������indicator��ռ�г���
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
     * �����ܴ�С
     * @param cap �ܹ�����ʾ��������
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
     * ���õ�ǰѡ�е�ͼ�ꡣ
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
            
            if (mAlpha != 255) { //SUPPRESS CHECKSTYLE 255��ʾ��͸����
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

