package com.baidu.hd.util;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

/*
 * @ClassName   : JudgeCompentAlive
 * @Description : 判断app,service是否在运行中
 * @Author      : sunyimin
 * @CreateDate  : 2012.11.18
 */

public class JudgeCompentAlive
{
	private final int SERVICE_NUM = 30;
	private Context ctx = null;
	
	private static JudgeCompentAlive mJudgeCompentAlive = null;
	
	public static JudgeCompentAlive getInstance(Context ctx) 
	{
		if (mJudgeCompentAlive == null)
		{
			mJudgeCompentAlive = new JudgeCompentAlive(ctx);
		}	
		return mJudgeCompentAlive;
	}
	
	private JudgeCompentAlive(Context ctx)
	{
		this.ctx = ctx;
	}

	/* 判断service是否在运行中
	 * @param serviceClassName ：要判断的service类名全称（包含包名）
	 * 
	 * @return                 : true 运行；false 没运行
	 */
	public boolean JudgeServiceAlive(String serviceClassName)
	{
		ActivityManager am = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> infos = am.getRunningServices(SERVICE_NUM);
		
		for (RunningServiceInfo info : infos)
		{
			if (info.service.getClassName().equals(serviceClassName))
			{
				return true;
			}
		}
		return false;
	}
}
