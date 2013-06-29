package com.baidu.browser.explore;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView.HitTestResult;
import android.widget.Toast;

import com.baidu.browser.core.util.BdLog;
import com.baidu.browser.core.util.BdUtil;
import com.baidu.browser.explore.BPExplorePopView.BdExplorePopViewListener;
import com.baidu.browser.webpool.BPWebPoolView;
import com.baidu.hd.log.Logger;
import com.baidu.hd.R;

/**
 * @ClassName: BPExploreView 
 * @Description: 浏览页
 * @author LEIKANG 
 * @date 2012-12-6 下午2:22:09
 */
public class BPExploreView extends BPWebPoolView implements BdExplorePopViewListener {

	/**
	 * 操作弹出框
	 */
	private BPExplorePopView mPopupWindow;

	/**
	 * 事件监听器
	 */
	private BPExploreViewListener mListener;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            Context
	 */
	public BPExploreView(Context context) {
		super(context);
		init(context);
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            Context
	 * @param attrs
	 *            AttributeSet
	 */
	public BPExploreView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            Context
	 * @param attrs
	 *            AttributeSet
	 * @param defStyle
	 *            DefaultStyle
	 */
	public BPExploreView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	/**
	 * 初始化。
	 * @param context Context
	 */
	private void init(Context context) {

        mPopupWindow = (BPExplorePopView) LayoutInflater.from(getContext()).inflate(
                R.layout.browser_copy_search_view, null);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(mPopupWindow, 1, params);
        mPopupWindow.setVisibility(GONE);
        mPopupWindow.setEventListener(this);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child instanceof BPExplorePopView) {
				BdLog.d(changed + ", " + left + ", " + top + ", " + right + ", " + bottom);
				final int childWidth = child.getMeasuredWidth();
				final int childHeight = child.getMeasuredHeight();
				BdLog.d(childWidth + ", " + childHeight);
				// 计算弹出框位置
				BPExplorePopView popView = (BPExplorePopView) child;
				calcPopWindowPos(popView);
				if (popView != null) {
					left += popView.getPopX();
					top += popView.getPopY();
				}
				right = left + childWidth;
				bottom = top + childHeight;
				child.layout(left, top, right, bottom);
			} else {
				super.onLayout(changed, left, top, right, bottom);
			}
		}
	}

	/**
	 * 计算弹出框位置
	 * 
	 * @param aPopView
	 *            BdExplorePopView
	 */
	private void calcPopWindowPos(BPExplorePopView aPopView) {
		int mPopLeftX = aPopView.getPopLeftX();
		int mPopRightX = aPopView.getPopRightX();
		int mPopTopY = aPopView.getPopTopY();
		int mPopBottomY = aPopView.getPopBottomY();

		if (mPopLeftX > mPopRightX) {
			int temp = mPopRightX;
			mPopRightX = mPopLeftX;
			mPopLeftX = temp;
		}

		if (mPopTopY > mPopBottomY) {
			int temp = mPopBottomY;
			mPopBottomY = mPopTopY;
			mPopTopY = temp;
		}

		BdLog.d(mPopLeftX + ", " + mPopRightX + ", " + mPopTopY + ", " + mPopBottomY);

		int popupWindowWidth = aPopView.getMeasuredWidth();
		int webViewWidth = getWidth();
		int mPopX = (mPopLeftX + mPopRightX - popupWindowWidth) / 2;
		if ((mPopX + popupWindowWidth) > webViewWidth) {
			mPopX = webViewWidth - popupWindowWidth;
		}
		if (mPopX < 0) {
			mPopX = 0;
		}

		int popupWindowHeight = aPopView.getMeasuredHeight();
		int webViewHeight = getHeight();
		popupWindowHeight += BdUtil.dip2pix(getContext(), BPExplorePopView.SELECTION_PADDING);
		int mPopY = mPopTopY - popupWindowHeight;
		if (mPopY < 0) {
			mPopY = mPopBottomY + BdUtil.dip2pix(getContext(), BPExplorePopView.SELECTION_PADDING); // 防止挡住光标
			// 弹出框在选字区域下方
			aPopView.setBackgroundResource(R.drawable.browser_select_menu_up_bg);
		} else {
			// 弹出框在选字区域上方
			aPopView.setBackgroundResource(R.drawable.browser_select_menu_down_bg);
		}

		if ((mPopY + popupWindowHeight) > webViewHeight) {
			mPopY = mPopBottomY - mPopTopY - popupWindowHeight;
		}

		aPopView.setPopX(mPopX);
		aPopView.setPopY(mPopY);
		BdLog.d(mPopX + ", " + mPopY);
	}

	@Override
	public void doSelectionCopy(String aSelection) {
		doSelectionCancel();
		if (aSelection.length() > 0) {
			Toast t = Toast.makeText(getContext(), R.string.text_selection_ok_tip,
					BPExplorePopView.SELECTION_TOP_DUR);
			t.show();
		} else {
			Toast t = Toast.makeText(getContext(), R.string.text_selection_fail_tip,
					BPExplorePopView.SELECTION_TOP_DUR);
			t.show();
		}
	}

	@Override
	public void doSelectionSearch(String aSelection) {
		doSelectionCancel();
		if (aSelection != null && aSelection.length() > 0) {
			BdLog.i("start to search.");
			// 去空，以免encode编码出错
			aSelection = aSelection.trim();
			if (mListener != null) {
				mListener.onSelectionSearch(aSelection);
			}
		} else {
			Toast t = Toast.makeText(getContext(), R.string.text_selection_fail_tip,
					BPExplorePopView.SELECTION_TOP_DUR);
			t.show();
		}
	}

	@Override
	public void doSelectionCancel() {
		if (mPopupWindow != null) {
			mPopupWindow.setVisibility(GONE);
		}
		setExtendSelection(false);
		setTouchSelection(false);
		setShiftIsPressed(false);
		setSelectingText(false);
		boolean extendSelection = getExtendSelection();
		BdLog.d("extendSelection: " + extendSelection);
		boolean touchSelection = getTouchSelection();
		BdLog.d("touchSelection: " + touchSelection);
		boolean shiftIsPressed = getShiftIsPressed();
		BdLog.d("shiftIsPressed: " + shiftIsPressed);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		BdLog.d("action: " + action);
		boolean retValue = mGestureDetector.onTouchEvent(event);
		if (!retValue) {
			// 如果双指操作，则不进行划词搜索
			if (event.getPointerCount() > 1) {
				setExtendSelection(false);
			} else {
				boolean extendSelection = getExtendSelection();
				boolean selectingText = getSelectingText();
				if (action == MotionEvent.ACTION_DOWN) {
					if (extendSelection || selectingText) {
						if (mPopupWindow != null) {
							int touchX = (int) event.getX();
							int touchY = (int) event.getY();
							// 如果第一次划词，记录左上边位置
							String oldSelection = mPopupWindow.getSelection();
							if (oldSelection == null || oldSelection.length() == 0) {
								mPopupWindow.setPopLeftX(touchX);
								mPopupWindow.setPopTopY(touchY);
							}
						}
					}
				}
				if (action == MotionEvent.ACTION_UP) {
					if (extendSelection || selectingText) {
						if (mPopupWindow != null) {
							mPopupWindow.requestLayout();
							int touchX = (int) event.getX();
							int touchY = (int) event.getY();
							// 如果第一次划词，记录右下边位置
							String oldSelection = mPopupWindow.getSelection();
							if (oldSelection == null || oldSelection.length() == 0) {
								mPopupWindow.setPopRightX(touchX);
								mPopupWindow.setPopBottomY(touchY);
							} // SUPPRESS CHECKSTYLE
								// 如果按下的位置，离右边距较近，则滑动的是右光标，否则滑动的是左光标
							else {
								int touchMiddle = (mPopupWindow.getPopLeftX() + mPopupWindow.getPopRightX()) / 2;
								if (touchX > touchMiddle) {
									mPopupWindow.setPopRightX(touchX);
									mPopupWindow.setPopBottomY(touchY);
								} else {
									mPopupWindow.setPopLeftX(touchX);
									mPopupWindow.setPopTopY(touchY);
								}
							}
						}
						// 弹出操作框
						if (Build.VERSION.SDK_INT < 14) { // SUPPRESS CHECKSTYLE
							return onUp();
						}
					}
				}
				if (action == MotionEvent.ACTION_MOVE) {
					// bugfix#SEARHBOX-795
					if (extendSelection || selectingText) {
						mPopupWindow.setVisibility(GONE);
					}
				}
			}
			return superOnTouchEvent(event);
		} else {
			return retValue;
		}

	}

	@Override
	public boolean onLongClick(View v) {
		HitTestResult result = getHitTestResult();
		if (result == null) {
			return false;
		}
		if (mListener != null && !checkPopMenuStatus()) {
			mListener.onLongPress(result);
		}
		int type = result.getType();
		BdLog.d("type: " + type);
		return !(type == HitTestResult.UNKNOWN_TYPE);
	}

	/**
	 * 处理up事件
	 * 
	 * @return true表示消费掉up事件，反之亦然
	 */
	private boolean onUp() {
		boolean extendSelection = getExtendSelection();
		boolean touchSelection = getTouchSelection();
		boolean shiftIsPressed = getShiftIsPressed();
		BdLog.d("extendSelection: " + extendSelection);
		BdLog.d("touchSelection: " + touchSelection);
		BdLog.d("shiftIsPressed: " + shiftIsPressed);
		BdLog.d("popLeftX: " + mPopupWindow.getPopLeftX());
		BdLog.d("popRightX: " + mPopupWindow.getPopRightX());
		BdLog.d("popTopY: " + mPopupWindow.getPopTopY());
		BdLog.d("popBottomY: " + mPopupWindow.getPopBottomY());

		boolean selectingText = getSelectingText();
		boolean drawSelectionPointer = getDrawSelectionPointer();
		if (extendSelection || (selectingText && !drawSelectionPointer)) {
			String selection = getSelection();
			BdLog.i(selection);

			BdLog.d("now show popup window.");
			showPopWindow();
			setDrawSelectionPointer(true);
			mPopupWindow.setSelection(selection);
			return true;
		} else {
			return true;
		}
	}

	/**
	 * 检测是否弹出了PopMenu
	 * 
	 * @return boolean
	 */
	public boolean checkPopMenuStatus() {
		boolean extendSelection = getExtendSelection();
		boolean selectingText = getSelectingText();
		return extendSelection || selectingText;
	}

	/**
	 * 弹出操作框
	 */
	private void showPopWindow() {
		if (mPopupWindow != null && mPopupWindow.getVisibility() != VISIBLE) {
			mPopupWindow.setVisibility(VISIBLE);
		}
	}

	/**
	 * 设置BdExploreView的监听器
	 * 
	 * @param aListener
	 *            BdExploreView的监听器
	 */
	public void setEventListener(BPExploreViewListener aListener) {
		mListener = aListener;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    // <add begin by caohaitao 20120907, 解决视频播放不能全屏问题。
        // 先查看父类能否处理
        boolean result = super.onKeyDown(keyCode, event);
        if (result) {
            return true;
        }
        // add end>

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			// 如果是选词模式，则取消
			if (checkPopMenuStatus()) {
				doSelectionCancel();
				return true;
			}
			//返回
			if (canGoBack()) {
				goBack();
				return true;
			}
		}
		return false;
	}

	/**
	 * 视频要打开，不下载
	 * @param url 视频地址
	 * @param contentDisposition
	 * @param mimetype 视频MimeType
	 * @return 是否打开了视频
	 */
	public boolean openVideoOnDownloadStart(String url, String contentDisposition, String mimetype, String title) {
		// 只打开视频文件，其它的让其下载
		// 默认方式为发送inten
		if (mimetype != null && mimetype.startsWith("video/")) {/*
			if (contentDisposition == null || !contentDisposition.regionMatches(true, 0, "attachment", 0, 10)) { 
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse(url), mimetype);
				Context context = getContext();
				// 查询是否存在有可以打开此类型的应用程序，如果有，让该应用程序打开此数据
				ResolveInfo info = getContext().getPackageManager().resolveActivity(intent,
						PackageManager.MATCH_DEFAULT_ONLY);
				if (info != null) {
					if (context instanceof Activity) {
						Activity act = (Activity) context;
						ComponentName myName = act.getComponentName();
						if (!myName.getPackageName().equals(info.activityInfo.packageName)
								|| !myName.getClassName().equals(info.activityInfo.name)) {
							try {
								act.startActivity(intent);
								return true;
							} catch (ActivityNotFoundException e) {
								BdLog.e("Activity not found." + mimetype);
							}
						}
					}
				}
			}*/
		}
		return false;
	}

	/**
	 * 处理手势操作
	 */
	final GestureDetector mGestureDetector = new GestureDetector(new SimpleOnGestureListener() {

		@Override
		public void onLongPress(MotionEvent e) {
			HitTestResult result = getHitTestResult();
			if (result == null) {
				return;
			}
			int type = result.getType();
			BdLog.d("type: " + type);

			if (mListener != null && !checkPopMenuStatus()) {
				if (type == HitTestResult.UNKNOWN_TYPE) {
					mListener.onLongPress(result);
				}
			}
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			BdLog.i("");
			// 如果是选词模式，则取消
			if (checkPopMenuStatus()) {
				doSelectionCancel();
			}
			return false;
		}
	});

	@Override
	public void onErrorPageGoBack() {
		goBack();
	}

	@Override
	public void onErrorPageRefresh() {
		reload();
	}

}
