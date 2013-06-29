package com.baidu.android.ext.widget;

import android.view.View;

/**
 * 可执行拖动删除条目的View的相关接口。
 */
public interface SwipeCallback {
    
    /**
     * 获取实现该接口的主View。
     * @return content view.
     */
    View getContentView();
    
    /**
     * 设置Adapter。
     * @param adapter SwipeApdater.
     */
    void setAdapter(SwipeAdapter adapter);
    
    /**
     * 获取Adapter。
     * @return SwipeAdapter.
     */
    SwipeAdapter getAdapter();
    
    /**
     * 删除child的回调。
     * @param v child
     */
    void onChildDismissed(View v);
    
    /**
     * 拖动child的回调。
     * @param v child
     */
    void onBeginDrag(View v);
    
    /**
     * 取消拖动的回调。
     * @param v child
     */
    void onDragCancelled(View v);
    
    
    /**
     * 获取child在container中的位置。
     * @param v child
     * @return index.
     */
    int getSwipeChildIndex(View v);
    
    /**
     * 获取指定坐标处的child。
     * @param x X
     * @param y Y
     * @return child.
     */
    View getSwipeChildAtPosition(int x, int y);
    
    /**
     * 获取指定索引处的child。
     * @param index index
     * @return child.
     */
    View getSwipeChildAt(int index);
    
    /**
     * 获取可拖动的child个数。
     * @return count.
     */
    int getSwipeChildCount();
    
    /**
     * 获取index处的child的左坐标。
     * @param index index
     * @return 左坐标。
     */
    int getSwipeChildLeftAt(int index);
    
    /**
     * 获取index处的child的上坐标。
     * @param index index
     * @return 上坐标。
     */
    int getSwipeChildTopAt(int index);
    
    
    /**
     * 获取可见区域中的第一个child的索引。
     * @return child索引。
     */
    int getSwipeFirstVisiblePosition();
    
    /**
     * 获取可见区域中的最后一个child的索引。
     * @return child索引。
     */
    int getSwipeLastVisiblePosition();
    
    
    /**
     * 设置滚动监听。
     * @param listener 监听。
     */
    void setSwipeScrollListener(SwipeScrollListener listener);
    
    /**
     * 滚动监听。
     * @author qumiao
     *
     */
    interface SwipeScrollListener {
        
        /**
         * 计算滚动位置。
         */
        void computeScroll();
        
        /**
         * 滚动位置变化。
         * @param l left
         * @param t top 
         * @param oldl old left
         * @param oldt old top
         */
        void onScrollChanged(int l, int t, int oldl, int oldt);
        
    }
}
