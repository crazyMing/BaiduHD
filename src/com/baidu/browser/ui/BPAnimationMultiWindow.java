package com.baidu.browser.ui;

import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.browser.framework.BPFrameView;
import com.baidu.browser.framework.BPWindow;
import com.baidu.hd.R;

/**
 * @ClassName: BPAnimationMultiWindow 
 * @Description: 浏览器多窗口实现
 * @author LEIKANG 
 * @date 2012-12-20 下午9:52:43
 */
public class BPAnimationMultiWindow extends LinearLayout implements OnClickListener, OnItemClickListener, MultiWindowItemCloseListener{

	private LayoutInflater inflater;
	private BPFrameView mBPFrameView;
	
	private View view = null;
	
	private MultiWindowListAdapter adapter;
	private ListView mMultiWindowList;
	private TextView mMultiWindowNew;
	private LinearLayout mMultiWindowNewPanel;
	
	private View mPopmenuOutside;
	
	private LinearLayout mMultiWindowPanel;
	
	private Animation showAction ;
	private Animation hideAction ;
	
	private boolean isHiding = false;;
	
	private int multiWindowItemHeight = 0;
	
	private Context mContext;
	
	private int windowWidth = 0;
	private int windowHeight = 0;
	
	private boolean animationTag = true;
	
	public BPAnimationMultiWindow(Context context, BPFrameView bfv) {
		super(context);
		mContext = context;
		inflater = LayoutInflater.from(context);
		view = inflater.inflate(R.layout.multiwindow_root, null);
		
		WindowManager winManager=(WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
		
		int width = winManager.getDefaultDisplay().getWidth();
		int height = winManager.getDefaultDisplay().getHeight();

		windowWidth = Math.min(width, height);
		windowHeight = Math.max(width, height);
		
		multiWindowItemHeight = context.getResources().getDimensionPixelSize(R.dimen.multi_window_item_height);
		
		animationTag = (context.getResources().getInteger(R.integer.pop_animation_tag) == 2);
		
		mBPFrameView = bfv;
		showAction = AnimationUtils.loadAnimation(context, R.anim.popshow_anim);
		hideAction = AnimationUtils.loadAnimation(context, R.anim.pophidden_anim);
		
		
		adapter = new MultiWindowListAdapter(context, inflater);
		adapter.setMultiWindowItemCloseListener(this);
		
		mMultiWindowList = (ListView)view.findViewById(R.id.multi_window_list);
		mMultiWindowList.setAdapter(adapter);
		mMultiWindowList.setOnItemClickListener(this);
		
		mMultiWindowNew = (TextView) view.findViewById(R.id.multi_window_new);
		mPopmenuOutside= (View) view.findViewById(R.id.popmenu_outside); 
		mMultiWindowNewPanel = (LinearLayout)view.findViewById(R.id.multi_window_new_panel);
		
		
		mMultiWindowPanel = (LinearLayout) view.findViewById(R.id.multi_window_panel);
		
		mPopmenuOutside.setOnClickListener(this);
		mMultiWindowNew.setOnClickListener(this);
		
		addView(view);
		hideMenu();
		
	}

	@Override
	public void onClick(View v) {
		if (isHiding)
			return;
		isHiding = true;
		switch (v.getId()) {
		case R.id.popmenu_outside:
			hideMenu();
			break;
		case R.id.multi_window_new:
			hideMenu(false);
			if (mBPFrameView.getWindowList().size() == 8)
				Toast.makeText(mContext, R.string.multi_window_max_toast_string, Toast.LENGTH_SHORT).show();
			else
				mBPFrameView.createWindowFromMultiWindow();
			break;
		}
		
		
	}
	
	public void showMenu(){
		updateUI(true);
		isHiding = false;
	}
 
	
	public void hideMenu(boolean startAnim) {
		if (startAnim) {
			setHideAction();
		}
		this.setVisibility(View.GONE);
	}
	
	public void hideMenu(){
		if (animationTag)
			setHideAction();
		this.setVisibility(View.GONE);
	}
	
	private void setHideAction() {
		hideAction = new TranslateAnimation(0, 0, 0, mMultiWindowPanel.getHeight());
		hideAction.setInterpolator(new AccelerateDecelerateInterpolator());
		hideAction.setDuration(150);
		startAnimation(hideAction);
	}
	
	public boolean isShow(){
		return View.VISIBLE == this.getVisibility();
	}
	
	/**
	 * @Title: updateUI 
	 * @Description:更新窗口列表
	 * @param     设定文件 
	 * @return void    返回类型 
	 * @throws
	 */
	private void updateUI(boolean isHide) {
		
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setGravity(Gravity.RIGHT);
			setPadding(0, 0, windowHeight/8, 0);
		} else {
			setGravity(Gravity.CENTER);
			setPadding(0, 0, 0, 0);
		}
		
		int maxnumber = mContext.getResources().getInteger(R.integer.animation_multi_window_max);
		
		int maxheight = maxnumber * multiWindowItemHeight + mMultiWindowNewPanel.getHeight();
		
		final List<BPWindow> windowList = mBPFrameView.getWindowList();
		
		final int currentPosition = windowList.indexOf(mBPFrameView.getCurrentWindow());
		
		adapter.setMultiWindows(windowList, currentPosition);
		
		int listCount = windowList.size();
		
		if (listCount > maxnumber) {
			int top = 0;
			final View view  = mMultiWindowList.getChildAt(listCount - maxnumber);
			if (view != null)
				top = view.getTop();
			mMultiWindowList.setSelectionFromTop(currentPosition,top);
			listCount = maxnumber;
		}
		
		android.view.ViewGroup.LayoutParams layoutParams = mMultiWindowList.getLayoutParams();
		layoutParams.height = multiWindowItemHeight * listCount;
		layoutParams.width = windowWidth;
		
		mMultiWindowList.setLayoutParams(layoutParams);
		
		android.view.ViewGroup.LayoutParams panelParams =  mMultiWindowPanel.getLayoutParams();
		panelParams.width = windowWidth;
		
		mMultiWindowPanel.setLayoutParams(panelParams);
		
		if (isHide && animationTag) {
			showAction = new TranslateAnimation(0, 0, mMultiWindowPanel.getHeight()==0?maxheight:mMultiWindowPanel.getHeight(), 0);
			showAction.setInterpolator(new AccelerateDecelerateInterpolator());
			showAction.setDuration(150);
			startAnimation(showAction);
		}
		
		this.setVisibility(View.VISIBLE);
		
	}
	
	

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		if (isShow())
			updateUI(true);
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
		hideMenu(false);
		mBPFrameView.swapWindowToFocusFromMultiWindow(position);
	}

	@Override
	public void onMultiWindowItemCloseClicked(int position) {
		
		mBPFrameView.closeWindow(position);
		updateUI(false);
		
	}
 
}
