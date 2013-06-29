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
	
	/** ���е�״̬  */
    private enum RunningState {
    	/**��*/
    	None,
        /**OnStart״̬��*/
        OnStart,
        /**OnResume״̬��*/
        OnResume,
        /**OnPause״̬��*/
        OnPause,
        /**OnStop״̬��*/
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
// �����ǽ��������Ϣ
	protected boolean mIsEditting = false;
	/** �����༭״̬ */
	protected boolean mIsNeedHoldEditingState = false;
	
	/** ��ʼ���������̬����ʱ��onFinishInflate��findViewByIdʧ�ܣ���˲�����ʽ��ʼ��
	 * *���߿����޸�Ϊ��onAttachedToWindow�г�ʼ��Ҳ�ǿ��е�
	 * *���������ⲿ��ʾ��ʼ�����д������Ż� */
	// TODO
	public void initWidgets(){}	
	
	public boolean isEditting() {
		return mIsEditting;
	}

	public void setEditting(boolean isEditting) {
		mIsEditting = isEditting;
	}

	/** �л��༭״̬ */
	public void changeEditState() {
		mIsEditting = !mIsEditting;
		if ( mIsEditting ) {
			enterEditState();
		}
		else {
			exitEditState();
		}
	}
	/** ����༭״̬  */
	protected void enterEditState() {
		if (!mIsEditting) {
			mIsEditting = true;
		}
	}
	
	/** �˳��༭״̬  */
	protected void exitEditState() {
		if (mIsEditting) mIsEditting = false;
	}
	
	/** ɾ������ */
	public abstract void delete(int index);
	/** ɾ��ȡ�� */
	public abstract void cancel();
	/** ɾ�������� */
	public abstract void menuToDeleteLongPressedItem();
	/** ���Ų��� */
	public abstract void menuToPlay();
	/** �������л� */
	public abstract void onConfigurationChanged(Configuration newConfig);
	
	
	/** ���±༭��ť
	 * -1�������ã�0�����ڱ༭���״̬��1���������ڱ༭״̬ */
	protected void updateEditButtonState(final int state) {
		logger.d("updateEditButtonState update = " + state );
		
		if (mActivity instanceof PersonalActivity) {
			PersonalActivity act = (PersonalActivity)mActivity;
			act.updateEditButtonState(state);
		}
	}
	
	/** ���±༭��ť״̬�����ⲿ����
	 *-1�������ã�0�����ڡ�ȫ����ͣ����1�����ڡ�ĳЩ�����أ�����ȫ�������ء�*/
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
