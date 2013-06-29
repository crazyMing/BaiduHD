package com.baidu.hd.ui;

/**
 * 滑动动画，当页面切换的接口.
 * @author daichanglin
 * 
 */
public interface OnViewChangedListener {
    /**
     * When view changed.
     * @param viewIndex index.
     */
	void onViewChanged(int viewIndex);
}