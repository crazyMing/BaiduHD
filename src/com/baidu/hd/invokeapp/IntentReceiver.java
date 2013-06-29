package com.baidu.hd.invokeapp;

import com.baidu.hd.log.Logger;
import com.baidu.hd.util.JudgeCompentAlive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
 * @ClassName   : IntentReceiver
 * @Description : 接收系统广播，用于启动HttpService
 * @Author      : sunyimin
 * @CreateDate  : 2012.11.18
 */

public class IntentReceiver extends BroadcastReceiver
{
	private Logger logger = new Logger("IntentReceiver");
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		
		// 开机启动service
		if (action.equals("android.intent.action.BOOT_BOOT_COMPLETED")) 
		{
			logger.d("start http service by boot");
			startService(context);
		}
		
		if (action.equals(Intent.ACTION_PACKAGE_ADDED))
		{
			startService(context);
		}
		
		if (action.equals(Intent.ACTION_PACKAGE_REMOVED))
		{
			logger.d("start http service by uninstall");
			startService(context);
		}
		
		if (action.equals("android.intent.action.SIG_STR"))
		{
			logger.d("start http service by sig_str");
			startService(context);
		}
	}
	
	private void startService(Context ctx)
	{
		String serviceClassName = "com.baidu.hd.invokeapp.HttpService";
		if (!JudgeCompentAlive.getInstance(ctx).JudgeServiceAlive(serviceClassName))
		{
			Intent service = new Intent(ctx, HttpService.class);
			ctx.startService(service);
		}
	}
}
