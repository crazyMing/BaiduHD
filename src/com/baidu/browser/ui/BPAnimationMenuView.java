package com.baidu.browser.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.browser.BPBrowser;
import com.baidu.browser.framework.BPFrameView;
import com.baidu.hd.BaiduHD;
import com.baidu.hd.BaseActivity;
import com.baidu.hd.CombinedBookmarkHistoryActivity;
import com.baidu.hd.MainActivity;
import com.baidu.hd.settings.MenuClearHistoryDialog;
import com.baidu.hd.settings.SettingsActivity;
import com.baidu.hd.settings.SettingsFeedbackActivity;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId.Browser;
import com.baidu.hd.stat.StatId.Menu;
import com.baidu.hd.R;

/**
 * @ClassName: BPAnimationMenuView 
 * @Description: 浏览器菜单实现，实现上下滑动动画
 * @author LEIKANG 
 * @date 2012-12-20 下午8:03:21
 */
public class BPAnimationMenuView extends LinearLayout implements OnClickListener{

	private View view = null;
	
	private TextView mTextHome = null;
	private TextView mTextMarkHis = null;
	private TextView mTextAddMark = null;
	private TextView mTextDelMark = null;
	private TextView mTextRefresh = null;
	
	private TextView mTextSettings = null;
	private TextView mTextDeleteHis = null;
	private TextView mTextFeedback = null;
	private TextView mTextExit = null;
	
	private View mPopmenuOutside;
	
	private Animation showAction ;
	private Animation hideAction ;
	
	private Context mContext;
	
	private boolean isHiding = false;
	
	private BPFrameView mBPBpFrameView;
	
    private static Stat mStat;
    
	private int windowWidth = 0;
	
	private boolean animationTag = true;
	
	public BPAnimationMenuView(Context context, BPFrameView bPFrameView) {
		super(context);
		mContext = context;
		
		WindowManager winManager=(WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
		
		int width = winManager.getDefaultDisplay().getWidth();
		int height = winManager.getDefaultDisplay().getHeight();

		windowWidth = Math.min(width, height);

		animationTag = (context.getResources().getInteger(R.integer.pop_animation_tag) == 2);
		
    	mStat = (Stat)((BaseActivity)mContext).getServiceProvider(Stat.class);
		
		mBPBpFrameView = bPFrameView;
		
		LayoutInflater inflater = LayoutInflater.from(context);
		view = inflater.inflate(R.layout.browser_menu_popupwindow, null);
		
		showAction = AnimationUtils.loadAnimation(context, R.anim.popshow_anim);
		hideAction = AnimationUtils.loadAnimation(context, R.anim.pophidden_anim);
		
		// 初始化Menu中的各控件
		this.mTextHome = (TextView)view.findViewById(R.id.brow_menu_home);
		this.mTextMarkHis = (TextView)view.findViewById(R.id.brow_menu_markhistory);
		this.mTextAddMark = (TextView)view.findViewById(R.id.brow_menu_add_mark);
		this.mTextDelMark = (TextView)view.findViewById(R.id.brow_menu_del_mark);
		this.mTextRefresh = (TextView)view.findViewById(R.id.brow_menu_refresh);
		
		this.mTextSettings = (TextView)view.findViewById(R.id.brow_menu_settings);
		this.mTextDeleteHis = (TextView)view.findViewById(R.id.brow_menu_deletehistory);
		this.mTextFeedback = (TextView)view.findViewById(R.id.brow_menu_feedback);
		this.mTextExit = (TextView)view.findViewById(R.id.brow_menu_exit);
		
		this.mPopmenuOutside= (View) view.findViewById(R.id.popmenu_outside); 
 
		
		this.mTextHome.setOnClickListener(this);
		this.mTextMarkHis.setOnClickListener(this);
		this.mTextAddMark.setOnClickListener(this);
		this.mTextDelMark.setOnClickListener(this);
		this.mTextRefresh.setOnClickListener(this);
		
		this.mTextSettings.setOnClickListener(this);
		this.mTextDeleteHis.setOnClickListener(this);
		this.mTextFeedback.setOnClickListener(this);
		this.mTextExit.setOnClickListener(this);
		this.mPopmenuOutside.setOnClickListener(this);
		addView(view);
		hideMenu();
	}
	
	public void showMenu(String url, boolean isBookmark){
		updateLayout();
		refreshUI(url, isBookmark);
		this.setVisibility(View.VISIBLE);
		if (animationTag)
			startAnimation(showAction);
		isHiding = false;
	}
	
	public void hideMenu(boolean startAnim) {
		if (startAnim && animationTag)
			startAnimation(hideAction);
		this.setVisibility(View.GONE);
	}
	
	public void hideMenu(){
		if (animationTag)
			startAnimation(hideAction);
		this.setVisibility(View.GONE);
	}
	
	public boolean isShow(){
		return View.VISIBLE == this.getVisibility();
	}

	@Override
	public void onClick(View v) {
		
		
		if (isHiding)
			return;
		isHiding = true;
		
		switch (v.getId()) {
		case R.id.brow_menu_home:
			hideMenu(false);
			if (mBPBpFrameView != null) {
				mBPBpFrameView.getCurrentWindow().loadUrl(BPBrowser.HOME_PAGE);
				
			mStat.incEventCount(Menu.Name, Menu.Go_home);
			}
			break;
			
		case R.id.brow_menu_markhistory:
			hideMenu(false);
			
			mStat.incEventCount(Menu.Name, Menu.Bookmark_history);
			
			mContext.startActivity(new Intent(mContext, CombinedBookmarkHistoryActivity.class));
			break;
					
		case R.id.brow_menu_add_mark:
			hideMenu();
			if (mBPBpFrameView != null) {
				mBPBpFrameView.insertOrDelmark();
				mStat.incEventCount(Menu.Name, Menu.Bookmard_add);	
			}
			
			break;
			
		case R.id.brow_menu_del_mark:
			hideMenu();
			if (mBPBpFrameView != null)
				mBPBpFrameView.insertOrDelmark();
			
			break;
			
		case R.id.brow_menu_refresh:
			hideMenu();
			if (mBPBpFrameView != null) {
				mBPBpFrameView.getCurrentWindow().reload();
				mStat.incEventCount(Menu.Name, Menu.Reload);	
			}
			
			break;
			
		case R.id.brow_menu_settings:
			hideMenu(false);
			mStat.incEventCount(Menu.Name, Menu.Settings);
			mContext.startActivity(new Intent(mContext, SettingsActivity.class));
			break;
			
		case R.id.brow_menu_deletehistory:
			hideMenu();
			mStat.incEventCount(Menu.Name, Menu.Clear);
			mContext.startActivity(new Intent(mContext, MenuClearHistoryDialog.class));
			break;
			
		case R.id.brow_menu_feedback:
			hideMenu(false);
			mStat.incEventCount(Menu.Name, Menu.Feedback);
			Intent intent = new Intent(mContext, SettingsFeedbackActivity.class);
			intent.putExtra("FromHome", false);
			mContext.startActivity(intent);
			break;
			
		case R.id.brow_menu_exit:
			hideMenu(false);
			mStat.incEventCount(Menu.Name, Menu.Exit);
			
			if (MainActivity.mBInvokeApp)
			{
				BaiduHD.cast(mContext).destroyService();
				BaiduHD.cast(mContext).toForceExitApp();
			}
			else 
			{
				BaiduHD.cast(mContext).promptExit();
			}
			
			break;
			
		case R.id.popmenu_outside:
			hideMenu();
			break;
			
		default:
			break;
		}

		
	}
	
	
	
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		updateLayout();
		super.onConfigurationChanged(newConfig);
	}
	
	private void updateLayout() {
		android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
		params.width = windowWidth;
		view.setLayoutParams(params);
		
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setGravity(Gravity.CENTER);
			setPadding(4, 4, 80, 4);
		} else {
			setGravity(Gravity.CENTER);
			setPadding(0, 0, 0, 4);
		}
		
		requestLayout();
	}

	private void refreshUI(String url, boolean isBookmark) {
		
		if (url.equals(BPBrowser.HOME_PAGE)) {
			mTextHome.setEnabled(false);
			mTextAddMark.setEnabled(false);
			mTextDelMark.setEnabled(false);
			mTextRefresh.setEnabled(false);
		} else {
			mTextHome.setEnabled(true);
			mTextAddMark.setEnabled(true);
			mTextDelMark.setEnabled(true);
			mTextRefresh.setEnabled(true);
			
			if (isBookmark) {
				mTextAddMark.setVisibility(View.GONE);
				mTextDelMark.setVisibility(View.VISIBLE);
			}
			else {
				mTextAddMark.setVisibility(View.VISIBLE);
				mTextDelMark.setVisibility(View.GONE);
			}
				
		}
			
		
	}
	
	

}
