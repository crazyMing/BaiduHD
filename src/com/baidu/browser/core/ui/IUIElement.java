package com.baidu.browser.core.ui;

import com.baidu.browser.core.IElement;
import com.baidu.browser.core.IEventSource;
/**
 * @ClassName: IUIElement 
 * @Description: UI元素接口
 * @author LEIKANG 
 * @date 2012-12-11 下午5:20:01
 */
public interface IUIElement extends IElement, IEventSource {

	/** Enable State */
	int STATE_ENABLED = 0;
	/** Disabled State */
	int STATE_DISABLED = 1;
	/** Focused State */
	int STATE_FOCUSED = 2;
	/** Selected State */
	int STATE_SELECTED = 3;
	/** Checked State */
	int STATE_CHECKED = 4;
	/** Activated State */
	int STATE_ACTIVATED = 5;
	/** Max State Count */
	int STATE_MAX_COUNT = 6;

	/**
	 * 设置当前状态
	 * 
	 * @param aState
	 *            待设置的状态
	 */
	void setState(int aState);

	/**
	 * 获取当前状态
	 * 
	 * @return 当前状态
	 */
	int getState();

	/**
	 * 当前状态改变通知
	 * 
	 * @param aState
	 *            改变后的状态
	 */
	void onStateChanged(int aState);

	/**
	 * 设置按下状态
	 * 
	 * @param aPressed
	 *            如果按下则为true，否则为false
	 */
	void setPressed(boolean aPressed);

	/**
	 * 是否按下
	 * 
	 * @return 如果按下则返回true，否则返回false
	 */
	boolean isPressed();

	/**
	 * 动作改变后回调，目前仅有按下状态通知
	 */
	void onActionChanged();

	/**
	 * 输入框丢失焦点时回调
	 * @return 如果是ture则拦截，否则返回false
	 */
	boolean onCaptureLoseFocus();

}
