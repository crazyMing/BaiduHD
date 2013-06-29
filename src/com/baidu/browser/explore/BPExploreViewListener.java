package com.baidu.browser.explore;

import android.webkit.WebView.HitTestResult;

/**
 * @ClassName: BPExploreViewListener 
 * @Description:  
 * @author LEIKANG 
 * @date 2012-12-6 ����2:24:03
 */
public interface BPExploreViewListener {

	/**
	 * ����ҳ��ӿ�
	 * 
	 * @param result
	 *            BdHitTestResult
	 */
	public void onLongPress(HitTestResult result);

	/**
	 * ѡ������
	 * 
	 * @param aSelection ѡ����ַ���
	 */
	public void onSelectionSearch(String aSelection);

}
