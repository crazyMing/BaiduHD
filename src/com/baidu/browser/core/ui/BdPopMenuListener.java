package com.baidu.browser.core.ui;
/**
 * @ClassName: BdPopMenuListener 
 * @Description: 弹出菜单监听类
 * @author LEIKANG 
 * @date 2012-12-11 下午5:19:28
 */
public interface BdPopMenuListener {

	/**
	 * 当菜单点击
	 * 
	 * @param mid
	 *            menuid
	 * @param index
	 *            位置
	 * @param name
	 *            名字
	 */
	void onPopMenuClick(int mid, int index, String name);

	/**
	 * 当菜单显示
	 * 
	 * @param mid
	 *            menuid
	 */
	void onPopMenuShow(int mid);

	/**
	 * 当菜单隐藏
	 * 
	 * @param mid
	 *            menuid
	 */
	void onPopMenuHide(int mid);
}
