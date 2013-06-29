package com.baidu.browser.core.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * @ClassName: BdPopMenuGroup 
 * @Description: 公用弹出菜单控件
 * @author LEIKANG 
 * @date 2012-12-11 下午5:18:53
 */
public class BdPopMenuGroup extends FrameLayout {

	/**BdPopMenuGroup**/
	private static BdPopMenuGroup instance;

	/**当前层级所有菜单**/ 
	private List<BdPopMenu> menus;

	/**上下文**/
	private Context context;

	/**显示模式**/
	private boolean menuShowMode;

	/**布局**/
	private FrameLayout.LayoutParams absParames = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, 
			FrameLayout.LayoutParams.WRAP_CONTENT);
	
	/** 当前显示的菜单在菜单list中的index  */
	private int mShowMenuId = -1;

	/**
	 * 构造方法
	 * @param aContext Context
	 */
	public BdPopMenuGroup(Context aContext) {
		this(aContext, null);
	}

	/**
	 * 构造方法
	 * @param aContext Context
	 * @param attrs AttributeSet
	 */
	public BdPopMenuGroup(Context aContext, AttributeSet attrs) {
		super(aContext, attrs);
		this.context = aContext;
		instance = this;
		absParames.gravity = Gravity.CENTER;
		menus = new ArrayList<BdPopMenu>();
		setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 
				FrameLayout.LayoutParams.MATCH_PARENT));
	}

	/**
	 * 单例
	 * @return BdPopMenuGroup
	 */
	public static BdPopMenuGroup getInstance() {
		return instance;
	}

	/**
	 * 对指定id菜单加入项
	 * @param mid menuid
	 * @param key 文本
	 * @param colorResId colorResId
	 * @param iconResId iconResId
	 */
	public void addItem(int mid, String key, int colorResId, int iconResId) {
		if (mid < 0 || mid >= menus.size()) {
			return;
		}
		BdPopMenu cmenu = menus.get(mid);
		cmenu.addItem(key, colorResId, iconResId);
	}

	/**
	 * 添加方法
	 * @param mid menuid
	 * @param keys 文本列表
	 * @param colorResId colorResId
	 * @param iconResIds iconResIds
	 */
	public void addItem(int mid, String[] keys, int colorResId, int[] iconResIds) {
		if (mid < 0 || mid >= menus.size()) {
			return;
		}
		BdPopMenu cmenu = menus.get(mid);
		cmenu.addItem(keys, colorResId, iconResIds);
	}

	/**
	 * 添加方法
	 * @param mid menuid
	 * @param keys 文本列表
	 * @param colorResId colorResId
	 * @param iconResIds iconResIds
	 * @param bgResIds 背景
	 */
	public void addItem(int mid, String[] keys, int colorResId, int[] iconResIds, int bgResIds) {
		if (mid < 0 || mid >= menus.size()) {
			return;
		}
		BdPopMenu cmenu = menus.get(mid);
		cmenu.addItem(keys, colorResId, iconResIds, bgResIds);
	}

	/**
	 * 布局
	 * @param mid menuid
	 */
	public void layoutMenu(int mid) {
		if (mid < 0 || mid >= menus.size()) {
			return;
		}
		BdPopMenu cmenu = menus.get(mid);
		cmenu.layoutMenu();
	}

	/**
	 *  默认居中显示
	 * @param mid menuid
	 */
	public void show(int mid) {
		if (mid < 0 || mid >= menus.size()) {
			return;
		}
		mShowMenuId = mid;
		menuShowMode = true;
		BdPopMenu cmenu = menus.get(mid);
		if (cmenu.getListener() != null) {
			cmenu.getListener().onPopMenuShow(mid);
		}
		cmenu.setDrawingCacheEnabled(true);
		cmenu.setVisibility(VISIBLE);
	}

	/**
	 * 显示指定菜单的指定id项
	 * @param mid menuid
	 * @param index 位置
	 */
	public void showItem(int mid, int index) {
		if (mid < 0 || mid >= menus.size()) {
			return;
		}
		BdPopMenu cmenu = menus.get(mid);
		cmenu.showItem(index);
	}

	/**
	 * 隐藏指定菜单的指定id项
	 * @param mid menuid
	 * @param index 位置
	 */
	public void hideItem(int mid, int index) {
		if (mid < 0 || mid >= menus.size()) {
			return;
		}
		BdPopMenu cmenu = menus.get(mid);
		cmenu.hideItem(index);
	}

	/**
	 * 隐藏指定菜单全部项
	 * @param mid menuid
	 */
	public void hideAllItem(int mid) {
		if (mid < 0 || mid >= menus.size()) {
			return;
		}
		BdPopMenu cmenu = menus.get(mid);
		cmenu.hideAllItem();
	}

	/**
	 * 显示指定菜单全部项
	 * @param mid menuid
	 */
	public void showAllItem(int mid) {
		if (mid < 0 || mid >= menus.size()) {
			return;
		}
		BdPopMenu cmenu = menus.get(mid);
		cmenu.showAllItem();
	}

	/**
	 * 隐藏
	 * @param mid menuid
	 */
	public void hide(int mid) {
		if (mid < 0 || mid >= menus.size()) {
			return;
		}
		mShowMenuId = -1;
		menuShowMode = false;
		BdPopMenu cmenu = menus.get(mid);
		if (cmenu.getListener() != null) {
			cmenu.getListener().onPopMenuHide(mid);
		}
		cmenu.setVisibility(INVISIBLE);
		cmenu.destroyDrawingCache();
	}

	/**
	 * 隐藏全部显示菜单
	 */
	public void hideAll() {
		for (BdPopMenu m : menus) {
			if (m.getVisibility() == VISIBLE) {
				if (m.getListener() != null) {
					m.getListener().onPopMenuHide(m.getId());
				}
				m.setVisibility(INVISIBLE);
			}
		}
		mShowMenuId = -1;
		menuShowMode = false;
	}

	/**
	 * 创建单个菜单 返回当前菜单id
	 * @param listener BdPopMenuListener
	 * @return 菜单id
	 */
	public int createMenu(BdPopMenuListener listener) {
		BdPopMenu menu = new BdPopMenu(context);
		menu.setListener(listener);
		menu.setVisibility(INVISIBLE);
		menus.add(menu);
		this.addView(menu, absParames);
		int index = menus.indexOf(menu);
		menu.setId(index);
		return index;
	}

	/**
	 * 设置item皮肤
	 * @param mid menuid
	 * @param res 资源
	 */
	public void setItemIcon(int mid, int res) {
		if (mid < 0 || mid >= menus.size()) {
			return;
		}
		BdPopMenu cmenu = menus.get(mid);
		cmenu.setSkin(res);
	}

	/**
	 * 调置menu皮肤
	 * @param mid menuid
	 * @param res 资源
	 */
	public void setMenuSkin(int mid, int res) {
		if (mid < 0 || mid >= menus.size()) {
			return;
		}
		BdPopMenu cmenu = menus.get(mid);
		cmenu.setBackgroundResource(res);
	}

	/**
	 * 设置字体大小
	 * 
	 * @param mid menuid
	 * @param textSize 文本大小
	 */
	public void setMenuTextSize(int mid, float textSize) {
		if (mid < 0 || mid >= menus.size()) {
			return;
		}
		BdPopMenu cmenu = menus.get(mid);
		cmenu.setTextSize(textSize);
	}
	
	/**
	 * 更新菜单文本
	 * @param mid menuid
	 * @param index 位置
	 * @param text 文本
	 */
	public void updateMenuItemText(int mid, int index, String text) {
		if (index < 0 || index > menus.size()) {
			return;
		}
		BdPopMenu cmenu = menus.get(mid);
		cmenu.updateItemText(index, text);
		postInvalidate();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (menuShowMode) {
					hideAll();
					return true;
				}
				break;
			case MotionEvent.ACTION_UP:

				break;
			case MotionEvent.ACTION_MOVE:

				break;
			default:
				break;
		}

		return super.onTouchEvent(event);
	}

	/**
	 * 菜单显示
	 * @return boolean
	 */
	public boolean isMenuShow() {
		return menuShowMode;
	}

	/**
	 * 获取菜单
	 * @return BdPopMenu
	 */
	public BdPopMenu getShowMenu() {
		if (mShowMenuId >= 0 && mShowMenuId < menus.size()) {
			return menus.get(mShowMenuId);
		} else {
			return null;
		}
	}

}
