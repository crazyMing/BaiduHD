package com.baidu.browser.explore;

import android.webkit.WebView.HitTestResult;

/**
 * @ClassName: BPExploreViewListener 
 * @Description:  
 * @author LEIKANG 
 * @date 2012-12-6 下午2:24:03
 */
public interface BPExploreViewListener {

	/**
	 * 长按页面接口
	 * 
	 * @param result
	 *            BdHitTestResult
	 */
	public void onLongPress(HitTestResult result);

	/**
	 * 选词搜索
	 * 
	 * @param aSelection 选择的字符串
	 */
	public void onSelectionSearch(String aSelection);

}
