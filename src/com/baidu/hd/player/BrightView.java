package com.baidu.hd.player;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.SeekBar;

import com.baidu.hd.util.Const;
import com.baidu.hd.util.PlayerTools;
import com.baidu.hd.R;

class BrightView
{
	private static final int MinValue = 30;
	
	private PlayerAccessor mAccessor = null;
	private int mBrightChanger = 0;
	private int mOldBrightChanger = 0;
	private SeekBar mSeekBar = null;

	/**
	 * 弹出窗口
	 */
	private PopupWindow mWindow = null;

	BrightView(PlayerAccessor accessor)
	{	
		this.mAccessor = accessor;
		
		SharedPreferences preferences = this.mAccessor.getHost().getSharedPreferences("application", Context.MODE_WORLD_READABLE);
		this.mBrightChanger = preferences.getInt("bright_value", 0);
		
		if (this.mBrightChanger == 0) {
			this.mBrightChanger = this.getScreenBrightness();
			this.mOldBrightChanger = this.mBrightChanger;
		}
		
		if(mBrightChanger >= MinValue) {
			setBrightness(mBrightChanger);
		}
		
	}

	void destroy()
	{
		if (this.mWindow != null)
		{
			this.mWindow.dismiss();
			this.mWindow = null;
		}
	}
	
	void storeBrightValue() {
		
		if (this.mBrightChanger != this.mOldBrightChanger) {
			SharedPreferences preferences = this.mAccessor.getHost().getSharedPreferences("application", Context.MODE_WORLD_WRITEABLE);
			Editor editor = preferences.edit();
			editor.putInt("bright_value", this.mBrightChanger);
			editor.commit();
		}
	}

	boolean isShow()
	{
		return this.mWindow != null;
	}

	void show()
	{
		LayoutInflater mInflater = LayoutInflater.from(this.mAccessor.getHost());
		View view = mInflater.inflate(R.layout.player_bright, null);
		
		this.mWindow = new PopupWindow(view, 
				PlayerTools.formatDipToPx(this.mAccessor.getHost(), 300), 
				PlayerTools.formatDipToPx(this.mAccessor.getHost(), 80));
//		this.mWindow.setTouchable(true);
		this.mWindow.setFocusable(false);
//		this.mWindow.setOutsideTouchable(true);

		mSeekBar = (SeekBar) view.findViewById(R.id.seekbar_bright);
		// 进度条的的范围为:0--Const.BrightVolume.BrightMax - MinValue
		mSeekBar.setMax(Const.BrightVolume.BrightMax - MinValue);
		
		mSeekBar.setProgress(mBrightChanger-MinValue);
		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser)
			{
				if (fromUser)
				{
					setBrightness(progress + MinValue);
					mBrightChanger = progress + MinValue;
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekbar)
			{
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekbar)
			{
			}
		});
		

		this.mWindow.showAtLocation(this.mAccessor.getControlHolder(), Gravity.CENTER, 0, 0);
	}

	/**
	 * 获取屏幕的亮度
	 * 
	 * @param activity
	 * @return
	 */
	public int getScreenBrightness()
	{
		int nowBrightnessValue = 0;
		try
		{
			nowBrightnessValue = android.provider.Settings.System.getInt(this.mAccessor.getHost().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return nowBrightnessValue;
	}

	/**
	 * 设置亮度
	 * 
	 * @param activity
	 * @param brightness
	 */
	public void setBrightness(int brightness)
	{
		WindowManager.LayoutParams lp = this.mAccessor.getHost().getWindow().getAttributes();
		lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
		this.mAccessor.getHost().getWindow().setAttributes(lp);
	}
	
	public void setIncreaseBright(int brightness) {
		
		int brightValue = brightness + this.mBrightChanger;
		if (brightValue > Const.BrightVolume.BrightMax) {
			brightValue = Const.BrightVolume.BrightMax;
		}
		else if (brightValue < MinValue) {
			brightValue = MinValue;
		}
		this.mBrightChanger = brightValue;
		WindowManager.LayoutParams lp = this.mAccessor.getHost().getWindow().getAttributes();
		lp.screenBrightness = Float.valueOf(brightValue) * (1f / 255f);
		this.mAccessor.getHost().getWindow().setAttributes(lp);
		
		if (mSeekBar != null) {
			mSeekBar.setProgress(this.mBrightChanger - MinValue);
		}
		
	}
	
	public int getBrightValue() {
		return this.mBrightChanger;
	}
}
