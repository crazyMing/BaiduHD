package com.baidu.hd.personal;

import com.baidu.hd.log.Logger;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public abstract class BasePage extends RelativeLayout {

	private String TAG = BasePage.class.getSimpleName();
	private Logger logger = new Logger(TAG);
	
	/** ÔËÐÐµÄ×´Ì¬  */
    private enum RunningState {
    	/**¿Õ*/
    	None,
        /**OnStart×´Ì¬¡£*/
        OnStart,
        /**OnResume×´Ì¬¡£*/
        OnResume,
        /**OnPause×´Ì¬¡£*/
        OnPause,
        /**OnStop×´Ì¬¡£*/
        OnStop,
    }
    private RunningState mState = RunningState.None;
	
	public BasePage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public BasePage(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public BasePage(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	protected abstract void onResume();
	protected abstract void onPause();
	protected abstract void onStart();
	protected abstract void onStop();
	
	/** resume */
	public void resume() {
		
		if (mState == RunningState.None || mState == RunningState.OnStop) {
			onStart();
		}
		
		if (mState != RunningState.OnResume) {
			onResume();
		}
		
		mState = RunningState.OnResume;
	}
	
	/** pause */
	public void pause() {
		
		if (mState == RunningState.OnResume) {
			onPause();
		}
		
		mState = RunningState.OnPause;
	}
	
	/** start */
	public void start() {
		
		if (mState == RunningState.None || mState == RunningState.OnStop) {
			onStart();
		}

		mState = RunningState.OnStart;
	}
	
	/** stop */
	public void stop() {
		
		if (mState == RunningState.OnResume) {
			onPause();
			onStop();
		}else if (mState == RunningState.OnPause) {
			onStop();
		}

		mState = RunningState.OnStop;
	}
}
