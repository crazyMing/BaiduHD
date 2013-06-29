package com.baidu.browser.explore;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.baidu.hd.R;

/**
 * @ClassName: BPExplorePopView 
 * @Description: WebView弹出层 该类负责处理浏览器长按选词 复制/搜索等功能 
 * @author LEIKANG 
 * @date 2012-12-6 下午2:20:26
 */
public class BPExplorePopView extends LinearLayout implements OnClickListener {

	/**
	 * 划词搜索提示时长
	 */
	public static final int SELECTION_TOP_DUR = 3000;

	/**
	 * 划词搜索弹出框padding
	 */
	public static final int SELECTION_PADDING = 15;

	/**
	 * 复制按钮
	 */
	private LinearLayout mCopyView;

	/**
	 * 搜索按钮
	 */
	private LinearLayout mSearchView;

	/**
	 * 弹出框x坐标
	 */
	private int mPopX;

	/**
	 * 弹出框y坐标
	 */
	private int mPopY;

	/**
	 * 选字区域左边界x坐标
	 */
	private int mPopLeftX;

	/**
	 * 选字区域右边界x坐标
	 */
	private int mPopRightX;

	/**
	 * 选字区域上边界y坐标
	 */
	private int mPopTopY;

	/**
	 * 选字区域下边界y坐标
	 */
	private int mPopBottomY;

	/** 选择的文本 */
	private String mSelection;

	/** BdExplorePopView监听器 */
	private BdExplorePopViewListener mListener;

	/**
	 * @param aContext
	 *            Context
	 */
	public BPExplorePopView(Context aContext) {
		this(aContext, null);
	}

	/**
	 * @param aContext
	 *            Context
	 * @param aAttrs
	 *            AttributeSet
	 */
	public BPExplorePopView(Context aContext, AttributeSet aAttrs) {
		super(aContext, aAttrs);
	}

	@Override
	protected void onFinishInflate() {
		mCopyView = (LinearLayout) findViewById(R.id.btn_wv_copy);
		mCopyView.setOnClickListener(this);
		mSearchView = (LinearLayout) findViewById(R.id.btn_wv_search);
		mSearchView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.equals(mCopyView)) { // 复制
			if (mListener != null) {
				mListener.doSelectionCopy(mSelection);
			}
		} else if (v.equals(mSearchView)) { // 搜索
			if (mListener != null) {
				mListener.doSelectionSearch(mSelection);
			}
		}
	}

	/**
	 * @return the mPopX
	 */
	public int getPopX() {
		return mPopX;
	}

	/**
	 * @param aPopX
	 *            the mPopX to set
	 */
	public void setPopX(int aPopX) {
		mPopX = aPopX;
	}

	/**
	 * @return the mPopY
	 */
	public int getPopY() {
		return mPopY;
	}

	/**
	 * @param aPopY
	 *            the mPopY to set
	 */
	public void setPopY(int aPopY) {
		mPopY = aPopY;
	}

	/**
	 * @return the mPopLeftX
	 */
	public int getPopLeftX() {
		return mPopLeftX;
	}

	/**
	 * @param aPopLeftX
	 *            the mPopLeftX to set
	 */
	public void setPopLeftX(int aPopLeftX) {
		mPopLeftX = aPopLeftX;
	}

	/**
	 * @return the mPopRightX
	 */
	public int getPopRightX() {
		return mPopRightX;
	}

	/**
	 * @param aPopRightX
	 *            the mPopRightX to set
	 */
	public void setPopRightX(int aPopRightX) {
		mPopRightX = aPopRightX;
	}

	/**
	 * @return the mPopTopY
	 */
	public int getPopTopY() {
		return mPopTopY;
	}

	/**
	 * @param aPopTopY
	 *            the mPopTopY to set
	 */
	public void setPopTopY(int aPopTopY) {
		mPopTopY = aPopTopY;
	}

	/**
	 * @return the mPopBottomY
	 */
	public int getPopBottomY() {
		return mPopBottomY;
	}

	/**
	 * @param aPopBottomY
	 *            the mPopBottomY to set
	 */
	public void setPopBottomY(int aPopBottomY) {
		mPopBottomY = aPopBottomY;
	}

	/**
	 * @param aSelection
	 *            选择的文本
	 */
	protected void setSelection(String aSelection) {
		mSelection = aSelection;
	}

	/**
	 * 获取选择的文本
	 * 
	 * @return 选择的文本
	 */
	protected String getSelection() {
		return mSelection;
	}

	/**
	 * 设置BdWebPoolView的监听器
	 * 
	 * @param aListener
	 *            BdWebPoolView的监听器
	 */
	public void setEventListener(BdExplorePopViewListener aListener) {
		mListener = aListener;
	}

	/**
	 * BdExplorePopView监听类
	 */
	public interface BdExplorePopViewListener {

		/**
		 * 复制接口
		 * 
		 * @param aSelection
		 *            选择的文本
		 */
		void doSelectionCopy(String aSelection);

		/**
		 * 搜索接口
		 * 
		 * @param aSelection
		 *            选择的文本
		 */
		void doSelectionSearch(String aSelection);

		/**
		 * 取消接口
		 */
		void doSelectionCancel();
	}

}
