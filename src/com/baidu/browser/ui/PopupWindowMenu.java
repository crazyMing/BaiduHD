package com.baidu.browser.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.baidu.browser.framework.BPFrameView;
import com.baidu.hd.BaseActivity;
import com.baidu.hd.R;

/**
 * @ClassName: MenuPopupWindow 
 * @Description: 浏览器窗口
 * @author LEIKANG 
 * @date 2012-12-19 下午8:27:35
 */
public class PopupWindowMenu implements OnClickListener{
	
	private BaseActivity mActivity = null;
	private PopupWindow mWindow = null;
	
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
	
	private BPFrameView mBdFrameView;
	
	private String url;
	
	private View view = null;
	
	public interface windowDismissListener {
		void onDismiss();
	}
	
	TranslateAnimation showAction = new TranslateAnimation(
			Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
			Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
	
	public PopupWindowMenu(BaseActivity activity) {
		this.mActivity = activity;
		LayoutInflater inflater = LayoutInflater.from(mActivity);
		view = inflater.inflate(R.layout.browser_menu_popupwindow, null);
		
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
		
		Display disp = this.mActivity.getWindowManager().getDefaultDisplay();
		this.mWindow = new PopupWindow(view, (int)(disp.getWidth()*29.0/30), (int)(disp.getHeight())-100);
		
		mWindow.setBackgroundDrawable(new ColorDrawable(0));
		mWindow.setOutsideTouchable(true);
		
		mWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
 
		this.mWindow.setTouchInterceptor(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					//mWindow.dismiss();
					return true;
				}
				return false;
			}
		});
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void destroyMenu() {
		if (this.mWindow != null) {
			this.mWindow = null;
		}
	}
	
	public void showMenu(BPFrameView bf, View v) {
		mBdFrameView = bf;
		showMenu(v);
	}
	
	public void showMenu(View v) {
		if (this.mWindow != null) {
			this.mWindow.showAtLocation(mBdFrameView.getWindowWrapper(), Gravity.BOTTOM, 0, v.getHeight());
			this.mWindow.showAsDropDown(mBdFrameView.getWindowWrapper());
				refreshUI();
		}
		
		
	}
	
	public void hideMenu() {
		if (this.mWindow != null) {
			this.mWindow.dismiss();
		}
	}
	
	public boolean isShow() {
		if (this.mWindow != null) {
			return this.mWindow.isShowing();
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		
		if (isShow()) {
			hideMenu();
			if (mBdFrameView != null) {
				//mBdFrameView.mIsMenuShow = false;
			}
		}
		
		switch (v.getId()) {
		case R.id.brow_menu_home:
			if (mBdFrameView != null) {
				//mBdFrameView.getCurrentWindow().loadUrl(Browser.HOME_PAGE);
			}
			break;
			
		case R.id.brow_menu_markhistory:
			//Intent intent = new Intent(mActivity, CombinedBookmarkHistoryActivity.class);
			//MainActivity.setOnPauseAnimation(true);
			//mActivity.startActivity(intent);
			
			break;
					
		case R.id.brow_menu_add_mark:
			//if (mBdFrameView != null)
				//mBdFrameView.insertOrDelmark();
			break;
			
		case R.id.brow_menu_del_mark:
			//if (mBdFrameView != null)
				//mBdFrameView.insertOrDelmark();
			break;
			
		case R.id.brow_menu_refresh:
			//if (mBdFrameView != null)
				//mBdFrameView.getCurrentWindow().reload();
			break;
			
		case R.id.brow_menu_settings:
			//Intent intentSettings = new Intent(mActivity, PlayerSettingsActivity.class);
			//mActivity.startActivity(intentSettings);
			
			break;
			
		case R.id.brow_menu_deletehistory:
			new AlertDialog.Builder(mActivity)
            .setTitle(R.string.title_delete_all_history)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(R.string.delete_history_warning_all)
            .setPositiveButton(R.string.delete_all, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
//                    Bookmarks.clearHistory(mActivity.getContentResolver());
//                    CookieManager.getInstance().removeAllCookie();
//                    ((BaiduPlayer) mActivity.getApplication()).getHistoryControl().clearHistory();
//                    ((DBWriter)((BaseActivity)mActivity).getServiceProvider(DBWriter.class)).deleteBrowSearchHis();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .create().show();
			break;
			
		case R.id.brow_menu_feedback:
//			Intent intentFeedback = new Intent(mActivity, PlayerSettingsFeedbackActivity.class);
//			intentFeedback.putExtra("FromHome", true);
//			MainActivity.setOnPauseAnimation(true);
//			mActivity.startActivity(intentFeedback);
			
			break;
			
		case R.id.brow_menu_exit:
//			((BaiduPlayer)mActivity.getApplication()).onDestroy(true);
//			mActivity.finish();
			break;
			
			
		case R.id.popmenu_outside:
			this.mWindow.dismiss();
			break;
			
		default:
			break;
		}
	}
	
	
	private void refreshUI() {
		if (mBdFrameView != null) {
			mTextAddMark.setEnabled(true);
			mTextDelMark.setEnabled(true);
			mTextRefresh.setEnabled(true);
			mTextHome.setEnabled(true);
			
//			if (mBdFrameView.isMarked()) {
//				mTextDelMark.setVisibility(View.VISIBLE);
//				mTextAddMark.setVisibility(View.GONE);
//			}  else {
//				mTextDelMark.setVisibility(View.GONE);
//				mTextAddMark.setVisibility(View.VISIBLE);
//			}
			mTextAddMark.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.brow_menu_add_mark_selected, 0, 0);
			mTextDelMark.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.brow_menu_del_mark_selected, 0, 0);
			mTextRefresh.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.brow_menu_refresh_selected, 0, 0);
			mTextHome.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.brow_menu_home_selected, 0, 0);
			
		} else {
			mTextAddMark.setEnabled(false);
			mTextDelMark.setEnabled(false);
			mTextRefresh.setEnabled(false);
			mTextHome.setEnabled(false);
			mTextAddMark.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.brow_menu_add_mark_disable, 0, 0);
			mTextDelMark.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.brow_menu_delete_disable, 0, 0);
			mTextRefresh.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.brow_menu_refresh_disable, 0, 0);
			mTextHome.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.brow_menu_home_disable, 0, 0);
		}
		
//		if (Bookmarks.hasHistoried(mActivity.getContentResolver())||CookieManager.getInstance().hasCookies()
//				||((BaiduPlayer) mActivity.getApplication()).getHistoryControl().hasHistory()) {
//			mTextDeleteHis.setEnabled(true);
//			mTextDeleteHis.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.brow_menu_delete_selected, 0, 0);
//		} else {
//			mTextDeleteHis.setEnabled(false);
//			mTextDeleteHis.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.brow_menu_delete_disable, 0, 0);
//		}
			
			
	}
 
}
