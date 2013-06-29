package com.baidu.browser.webpool;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: BPWebPoolBackForwardList 
 * @Description: 前进后退列表 
 * @author LEIKANG 
 * @date 2012-12-6 下午2:24:30
 */
public final class BPWebPoolBackForwardList {

	/** 当前列表位置 */
	private int mCurIndex = -1;
	/** 前进后退列表数组 */
	private List<BPWebPoolBackForwardListItem> mArray;

	/**
	 * Constructor
	 */
	public BPWebPoolBackForwardList() {
		mArray = new ArrayList<BPWebPoolBackForwardListItem>();
	}

	/**
	 * 添加一个元素
	 * 
	 * @param aItem
	 *            待添加的元素
	 */
	public void addItem(BPWebPoolBackForwardListItem aItem) {
		if (aItem != null) {
			mArray.add(aItem);
		}
	}

	/**
	 * 替换一个元素
	 * 
	 * @param aIndex
	 *            待替换的索引
	 * @param aItem
	 *            待替换的元素
	 */
	public void setItem(int aIndex, BPWebPoolBackForwardListItem aItem) {
		if (aItem != null && aIndex >= 0 && aIndex < mArray.size()) {
			mArray.set(aIndex, aItem);
		}
	}

	/**
	 * 删除一个元素
	 * 
	 * @param aIndex
	 *            待删除的元素索引
	 */
	public void removeItem(int aIndex) {
		if (aIndex >= 0 && aIndex < mArray.size()) {
			mArray.remove(aIndex);
		}
	}

	/**
	 * 删除一个元素
	 * 
	 * @param aItem
	 *            待删除的元素
	 */
	public void removeItem(BPWebPoolBackForwardListItem aItem) {
		if (aItem != null) {
			mArray.remove(aItem);
		}
	}

	/**
	 * 获取当前列表位置
	 * 
	 * @return 返回当前列表位置
	 */
	public int getCurIndex() {
		return mCurIndex;
	}

	/**
	 * 获取当前列表元素
	 * 
	 * @return 返回当前列表元素
	 */
	public BPWebPoolBackForwardListItem getCurItem() {
		return getItem(mCurIndex);
	}

	/**
	 * 获取指定列表元素
	 * 
	 * @param aIndex
	 *            指定列表元素索引
	 * @return 返回指定列表元素
	 */
	public BPWebPoolBackForwardListItem getItem(int aIndex) {
		if (aIndex >= 0 && aIndex < mArray.size()) {
			return mArray.get(aIndex);
		} else {
			throw new ArrayIndexOutOfBoundsException(aIndex);
		}
	}

	/**
	 * 获取指定列表元素的索引
	 * 
	 * @param aItem
	 *            指定列表元素
	 * @return 指定列表元素的索引
	 */
	public int getItemIndex(BPWebPoolBackForwardListItem aItem) {
		return mArray.indexOf(aItem);
	}

	/**
	 * 获取列表元素数量
	 * 
	 * @return 列表元素数量
	 */
	public int getSize() {
		return mArray.size();
	}

	/**
	 * 是否能够前进后退指定步数
	 * 
	 * @param aSteps
	 *            The negative or positive number of steps to move the history.
	 * @return 是否能够前进或者后退指定的步数
	 */
	public boolean canGoBackOrForward(int aSteps) {
		int newIndex = mCurIndex + aSteps;
		return newIndex >= 0 && newIndex < mArray.size();
	}

	/**
	 * 是否可以后退
	 * 
	 * @return 能够后退返回true，否则返回false
	 */
	public boolean canGoBack() {
		return canGoBackOrForward(-1);
	}

	/**
	 * 是否能够前进
	 * 
	 * @return 能够前进返回true，否则返回false
	 */
	public boolean canGoForward() {
		return canGoBackOrForward(1);
	}

	/**
	 * 前进后退指定步数
	 * 
	 * @param aSteps
	 *            The negative or positive number of steps to move the history.
	 */
	public void goBackOrForward(int aSteps) {
		if (canGoBackOrForward(aSteps)) {
			mCurIndex += aSteps;
		}
	}

	/**
	 * 后退
	 */
	public void goBack() {
		if (canGoBack()) {
			mCurIndex -= 1;
		}
	}

	/**
	 * 前进
	 */
	public void goForward() {
		if (canGoForward()) {
			mCurIndex += 1;
		}
	}

	/**
	 * 到列表首
	 */
	public void goToFirst() {
		mCurIndex = 0;
	}

	/**
	 * 到列表尾
	 */
	public void goToLast() {
		mCurIndex = mArray.size() - 1;
	}

	/**
	 * 清除列表数据
	 */
	public void clear() {
		mArray.clear();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getSize() + "\n");
		for (BPWebPoolBackForwardListItem item : mArray) {
			if (item != null) {
				sb.append(item.getUrl());
				sb.append(", ");
				sb.append(item.getWebView());
				sb.append(", ");
				sb.append(item.getIndex());
				sb.append(", ");
				sb.append(item.getLoadStatus());
				sb.append("\n");
			}
		}
		return sb.toString();
	}
}
