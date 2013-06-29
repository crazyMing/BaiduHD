package com.baidu.hd;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.baidu.hd.ctrl.PopupDialog;
import com.baidu.hd.log.Logger;
import com.baidu.hd.service.ServiceProvider;
import com.baidu.mobstat.StatService;

public class BaseActivity extends FragmentActivity{

	private Logger logger = new Logger("BaseActivity");

	/** 退出标志位 */
	private boolean mBackExitFlag = false;

	/** 当前的弹出对话框 */
	private List<PopupDialog> mPopupDialogs = new ArrayList<PopupDialog>();

    @Override
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	if(!getPlayerApp().getServiceContainer().isCreated()) {
    		getPlayerApp().createService(this);
    	}
    	
    }

	@Override
	protected void onDestroy() {
		for(PopupDialog dialog: mPopupDialogs) {
			dialog.dismiss();
		}
		mPopupDialogs.clear();
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		StatService.onResume(this);
		getPlayerApp().setCurrentActivity(this);
	}

	@Override
	protected void onPause() {
		// 会有返回桌面，但是此时需要弹窗，需要父窗体
		//getPlayerApplication().setCurrentActivity(null);
		StatService.onPause(this);
		super.onPause();
	}

	@Override
	public void onBackPressed() 
	{
		logger.d("onBackPressed");
		if(mBackExitFlag) {

			if(canExit()) {
				getPlayerApp().promptExit();
			} else {
				toHome();
			}
		    return;
		}
		super.onBackPressed();
	}

	/////////////////////////////////////////////////////////////////
	// 
	protected void setBackExitFlag(boolean value) {
		mBackExitFlag = value;
	}
	
	public ServiceProvider getServiceProvider(Class<? extends ServiceProvider> clazz) {
		if (getPlayerApp() != null) {
			return getPlayerApp().getServiceFactory().getServiceProvider(clazz);
		}
		return null;
	}

	public BaiduHD getPlayerApp() {
		return (BaiduHD)getApplication();
	}
	
	public boolean isServiceCreated() {
		return getPlayerApp().getServiceContainer().isCreated();
	}
	
	public void addPopupDialog(PopupDialog value)  {
		mPopupDialogs.add(value);
	}
	
	public void removePopupDialog(PopupDialog value)  {
		mPopupDialogs.remove(value);
	}
	
	/////////////////////////////////////////////////////////////////
	// override
	protected boolean canExit() {
		return false;
	}
	
	/**
	 * 服务初始化完成
	 * 在onCreate中需要服务的操作改在这里完成
	 */
	protected void onServiceCreated() {
	}
	
	protected boolean showProgressDialogWhenCreatingService() {
		return true;
	}
	
	private void toHome() {
//		EventCenter eventCenter = (EventCenter)getServiceProvider(EventCenter.class);
//		MainTabResultArgs tabIndex = new MainTabResultArgs(0);
//		eventCenter.fireEvent(EventId.eMainTabActivity, tabIndex);
	}
}
