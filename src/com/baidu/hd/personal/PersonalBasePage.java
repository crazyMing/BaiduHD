package com.baidu.hd.personal;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.BaseActivity;
import com.baidu.hd.log.Logger;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.service.ServiceProvider;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public abstract class PersonalBasePage extends RelativeLayout {

	private String TAG = PersonalBasePage.class.getSimpleName();
	private Logger logger = new Logger(TAG);
	
	/** 运行的状态  */
    private enum RunningState {
    	/**空*/
    	None,
        /**OnStart状态。*/
        OnStart,
        /**OnResume状态。*/
        OnResume,
        /**OnPause状态。*/
        OnPause,
        /**OnStop状态。*/
        OnStop,
    }
    private RunningState mState = RunningState.None;
    
    /** activity */
    protected Activity mActivity = null;
    
	public PersonalBasePage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		if (context instanceof Activity) {
			mActivity = (Activity) context;
		}
	}

	public PersonalBasePage(Context context, AttributeSet attrs) {
		super(context, attrs);

		if (context instanceof Activity) {
			mActivity = (Activity) context;
		}
	}

	public PersonalBasePage(Context context) {
		super(context);

		if (context instanceof Activity) {
			mActivity = (Activity) context;
		}
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}
	
	@Override
	protected void onAttachedToWindow() {
		logger.d("onAttachedToWindow");
		super.onAttachedToWindow();
	}

	protected void onResume() {
		mIsNeedHoldEditingState = false;
		onConfigurationChanged(getResources().getConfiguration());
	}
	
	protected void onPause() {
		if (!mIsNeedHoldEditingState) {
			mIsEditting = false;
			exitEditState();
		}
	}
	
	protected void onStart() {
		
	}
	
	protected void onStop() {
		
	}
	
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

/////////////////////////////////////////////////////////////////////////////////////////////
// 以下是界面相关信息
	protected boolean mIsEditting = false;
	/** 保留编辑状态 */
	protected boolean mIsNeedHoldEditingState = false;
	
	/** 初始化组件，动态创建时在onFinishInflate中findViewById失败，因此采用显式初始化
	 * *或者可以修改为在onAttachedToWindow中初始化也是可行的
	 * *暂且先用外部显示初始化，有待后续优化 */
	// TODO
	public void initWidgets(){}	
	
	public boolean isEditting() {
		return mIsEditting;
	}

	public void setEditting(boolean isEditting) {
		mIsEditting = isEditting;
	}

	/** 切换编辑状态 */
	public void changeEditState() {
		mIsEditting = !mIsEditting;
		if ( mIsEditting ) {
			enterEditState();
		}
		else {
			exitEditState();
		}
	}
	/** 进入编辑状态  */
	protected void enterEditState() {
		if (!mIsEditting) {
			mIsEditting = true;
		}
	}
	
	/** 退出编辑状态  */
	protected void exitEditState() {
		if (mIsEditting) mIsEditting = false;
	}
	
	/** 删除操作 */
	public abstract void delete(int index);
	/** 删除取消 */
	public abstract void cancel();
	/** 删除长按项 */
	public abstract void menuToDeleteLongPressedItem();
	/** 播放操作 */
	public abstract void menuToPlay();
	/** 横竖屏切换 */
	public abstract void onConfigurationChanged(Configuration newConfig);
	
	
	/** 更新编辑按钮
	 * -1：不可用；0：处于编辑完成状态；1：处于正在编辑状态 */
	protected void updateEditButtonState(final int state) {
		logger.d("updateEditButtonState update = " + state );
		
		if (mActivity instanceof PersonalActivity) {
			PersonalActivity act = (PersonalActivity)mActivity;
			act.updateEditButtonState(state);
		}
	}
	
	/** 更新编辑按钮状态，供外部调用
	 *-1：不可用；0：处于“全部暂停”；1：处于“某些在下载，或者全部在下载”*/
	protected void updateAllStartPauseButtonState(final int state) {
		if (mActivity instanceof PersonalActivity) {
			PersonalActivity act = (PersonalActivity)mActivity;
			act.updateAllStartPauseButtonState(state);
		}
	}

	protected Stat getStat() {
		return (Stat) ((BaseActivity)mActivity).getServiceProvider(Stat.class);
	}
}
