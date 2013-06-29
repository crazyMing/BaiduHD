package com.baidu.android.ext.widget;

import android.widget.BaseAdapter;

/**
 * 用来管理item拖动、删除与插入等逻辑的adapter。继承自BaseAdapter。
 */
public abstract class SwipeAdapter extends BaseAdapter {
    
    /**只在指定位置执行删除。*/
    public static final int ONLY_REMOVE = -2;
    
    /**
     * 删除与插入。
     * @param removePos 在该位置处执行删除。
     * @return 如果不为{@link #ONLY_REMOVE}，则在该位置处执行插入。
     */
    protected abstract int removeAndInsert(int removePos);
    
    /**
     * 判断指定位置处能否执行拖动和删除。
     * @param position 位置。
     * @return 64bit(int). {@link #SWIPE_DRAG}标志位为1，表示可以拖动；{@link #SWIPE_REMOVE}标志位为1，表示可以删除。
     */
    public abstract int getSwipeAction(int position);
    
    /**不可拖动，不可删除*/
    public static final int SWIPE_FIXED = 0;
    
    /**可以拖动。*/
    public static final int SWIPE_DRAG = 0x01;
    
    /**可以删除。*/
    public static final int SWIPE_REMOVE = 0x02;
}
