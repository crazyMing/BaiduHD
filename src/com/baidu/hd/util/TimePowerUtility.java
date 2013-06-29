package com.baidu.hd.util;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.hd.log.Logger;
import com.baidu.hd.R;

public class TimePowerUtility
{
	private Logger logger = new Logger("TimePowerUtility");
	
	private static final int BATTERY_BACKGROUND_TOTAL_LEGNTH = 29;
	private static final int TIME_UPDATE_INTERVAL = 60000;
	
	private BatteryBroadcastReceiver mBatteryChangeReceiver = new BatteryBroadcastReceiver();
	private ImageView mBatteryImageView;
	private RelativeLayout mBatteryLayout;
	private Context mContext;
	private TextView mCurTimeTextView;
	private Handler mTimerUpdateHandler = new Handler();
	private Runnable mTimerUpdateRunnable = new Runnable()
	{
		public void run()
		{
			TimePowerUtility.this.mCurTimeTextView.setText(TimePowerUtility.this.getCurTime());
			TimePowerUtility.this.mTimerUpdateHandler.postDelayed(this, TIME_UPDATE_INTERVAL);
		}
	};

	public TimePowerUtility(TextView paramTextView, RelativeLayout paramRelativeLayout, Context paramContext)
	{
		this.mCurTimeTextView = paramTextView;
		this.mBatteryLayout = paramRelativeLayout;
		this.mContext = paramContext;
		fillView();
	}

	private void fillView()
	{
		if (this.mBatteryLayout != null)
		{
			this.mBatteryImageView = ((ImageView) this.mBatteryLayout.findViewById(R.id.play_iv_battery));
		}
	}

	private String getCurTime()
	{
		Calendar localCalendar = Calendar.getInstance();
		int i = localCalendar.get(11);
		int j = localCalendar.get(12);
		String str2 = Integer.toString(i);
		String str1 = Integer.toString(j);
		if (i < 10)
			str2 = "0" + str2;
		if (j < 10)
			str1 = "0" + str1;
		return str2 + ":" + str1;
	}

	private void registerBatteryReceiver()
	{
		this.mContext.registerReceiver(this.mBatteryChangeReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
	}

	private void startTimer()
	{
		this.mTimerUpdateHandler.post(this.mTimerUpdateRunnable);
	}

	private void stopTimer()
	{
		this.mTimerUpdateHandler.removeCallbacks(this.mTimerUpdateRunnable);
	}

	private void unregisterBatteryReceiver()
	{
		logger.d("unregisterBatteryReceiver...............");
		this.mContext.unregisterReceiver(this.mBatteryChangeReceiver);
	}

	public void startWork()
	{
		startTimer();
		registerBatteryReceiver();
	}

	public void stopWork()
	{
		stopTimer();
		unregisterBatteryReceiver();
	}

	class BatteryBroadcastReceiver extends BroadcastReceiver
	{
		BatteryBroadcastReceiver()
		{
		}

		public void onReceive(Context paramContext, Intent paramIntent)
		{
			if (paramIntent.getAction().equals("android.intent.action.BATTERY_CHANGED"))
			{
				int level = paramIntent.getIntExtra("level", 0);
				int scale = paramIntent.getIntExtra("scale", 100);
				level = level * 100 / scale;
				
				if (level < 10) {
					TimePowerUtility.this.mBatteryImageView.setImageResource(R.drawable.battery_0);
				}
				else if (level >= 10 && level < 30) {
					TimePowerUtility.this.mBatteryImageView.setImageResource(R.drawable.battery_1);
				}
				else if (level >= 30 && level < 70) {
					TimePowerUtility.this.mBatteryImageView.setImageResource(R.drawable.battery_2);
				}
				else {
					TimePowerUtility.this.mBatteryImageView.setImageResource(R.drawable.battery_3);
				}
			}
		}
	}
}