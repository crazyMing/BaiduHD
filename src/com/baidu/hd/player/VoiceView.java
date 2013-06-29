package com.baidu.hd.player;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import com.baidu.hd.ctrl.VerticalSeekBar;
import com.baidu.hd.log.Logger;
import com.baidu.hd.util.Const;
import com.baidu.hd.R;

class VoiceView
{
	private Logger logger = new Logger("VoiceChanger");
	
	private PlayerAccessor mAccessor = null;
	
	/**
	 * 弹出窗口
	 */
	private PopupWindow mWindow = null;

	/**
	 * 声音设备
	 */
	private AudioManager mAudioManager = null;
	
	private View mVoiceView = null;
	
	private VerticalSeekBar mVerticalSeekBar = null;
	
	private ImageButton mVoiceButton = null;
	
	private int mCurrentVolume = 0;
	
	/*
	 * 手势相关
	 */
	private int mGestureVol = 0;

	VoiceView(PlayerAccessor accessor)
	{
		this.mAccessor = accessor;

		this.mAudioManager = (AudioManager) this.mAccessor.getHost().getSystemService(Context.AUDIO_SERVICE);
		this.mVoiceButton =  (ImageButton) this.mAccessor.getHost().findViewById(R.id.btn_voice);
		this.mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		
		LayoutInflater mInflater;
		mInflater = LayoutInflater.from(this.mAccessor.getHost());
		this.mVoiceView = mInflater.inflate(R.layout.player_voice, null);
		this.mVerticalSeekBar = (VerticalSeekBar) this.mVoiceView.findViewById(R.id.seekbar_voice);
		this.mVerticalSeekBar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		if (null != mVoiceButton && (this.mCurrentVolume <= 0))
		{
			mVoiceButton.setImageResource(R.drawable.ic_mute_btn_videoplayer);
		}
		
		this.mVerticalSeekBar.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(VerticalSeekBar VerticalSeekBar, int progress, boolean fromUser)
			{
				logger.d("mVerticalSeekBar onProgressChanged progress : " + progress);
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
				mCurrentVolume = progress;
				if (null != mVoiceButton && (progress <= 0))
				{
					mVoiceButton.setImageResource(R.drawable.ic_mute_btn_videoplayer);
				}
				else if (null != mVoiceButton && (progress > 0))
				{
					mVoiceButton.setImageResource(R.drawable.ic_volume_btn_videoplayer);
				}
			}

			@Override
			public void onStartTrackingTouch(VerticalSeekBar VerticalSeekBar)
			{
				mAccessor.stopHideControl();
			}

			@Override
			public void onStopTrackingTouch(VerticalSeekBar VerticalSeekBar)
			{
				mAccessor.startHideControl();
			}
		});
		
		this.mVerticalSeekBar.setProgress(this.mCurrentVolume);
		mVerticalSeekBar.init();
	}

	void destroy()
	{
		// TODO
		this.hide();
	}

	boolean isShow()
	{
		return this.mWindow != null;
	}

	void show()
	{
		if (!isShow())
		{
			this.mWindow = new PopupWindow(this.mVoiceView, 80, mAccessor.getScreenSize().getY() / 2);
//			this.mVerticalSeekBar.setProgress(mCurrentVolume);
			this.mWindow.showAtLocation(this.mAccessor.getControlHolder(), Gravity.CENTER | Gravity.RIGHT, 10, -20);
			//add by juqiang to adapt sonyPhone
			mVerticalSeekBar.setVisibility(View.GONE);
			mVerticalSeekBar.setVisibility(View.VISIBLE);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mVerticalSeekBar.setProgress(mVerticalSeekBar.getProgress()+1);
					mVerticalSeekBar.setProgress(mVerticalSeekBar.getProgress()-1);
				}
			}, 50);
			mAccessor.getHost().findViewById(R.id.rightbar)
			.setVisibility(View.GONE);
		}
	}
	
	public void updateCurrentVolume()
	{
		if (null != this.mAudioManager)
		{
			this.mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			this.mVerticalSeekBar.setProgress(this.mCurrentVolume);
		}
	}

	public void onVoiceDown()
	{
		int volume = this.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		logger.d("onVoiceDown mAudioManager.getStreamVolume voice : " + volume);
		this.mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, --volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		logger.d("mVerticalSeekBar.setProgress : " + volume);
		this.mVerticalSeekBar.setProgress(volume);
	}

	public void onVoiceUp()
	{
		int volume = this.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		logger.d("onVoiceUp mAudioManager.getStreamVolume voice : " + volume);
		this.mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, ++volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		logger.d("mVerticalSeekBar.setProgress : " + volume);
		this.mVerticalSeekBar.setProgress(volume);
	}
	
	void hide()
	{
		if (this.mWindow != null)
		{
			this.mWindow.dismiss();
			this.mWindow = null;
		}
		mAccessor.getHost().findViewById(R.id.rightbar)
		.setVisibility(View.VISIBLE);
		mAccessor.getHost().findViewById(R.id.rightbar).setVisibility(
				mAccessor.getHost().findViewById(R.id.btn_episode).getVisibility());
	}
	
	public void calGestureVol() {
		if (this.mGestureVol == 0) {
			this.mGestureVol = Const.BrightVolume.GestureVoiceRatio * this.mCurrentVolume;
		}
	}
	
	public int getGestureVol() {
		calGestureVol();
		return this.mGestureVol;
	}
	
	public void setIncreaseVol(int increaseValue) {
		
		int gestureVol = increaseValue + this.getGestureVol();
		if (gestureVol > Const.BrightVolume.GestureVoiceMax) {
			gestureVol = Const.BrightVolume.GestureVoiceMax;
		}
		else if (gestureVol < 0) {
			gestureVol = 0;
		}
		this.mGestureVol = gestureVol;
		int volume = this.mGestureVol / Const.BrightVolume.GestureVoiceRatio;
		if (this.mAudioManager != null) {
		this.mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		}
		this.mVerticalSeekBar.setProgress(volume);
	}
}
