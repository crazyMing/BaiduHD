package com.baidu.hd.invokeapp;


import java.io.IOException;

import com.baidu.hd.log.Logger;
import com.baidu.hd.module.IntentConstants;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/*
 * @ClassName   : HttpService
 * @Description : http service 前端调起app的http服务器
 * @Author      : sunyimin
 * @CreateDate  : 2012.12.24
 */

public class HttpService extends Service
{
	
	private Logger logger = new Logger("HttpService");
	
	private HttpServer mHttpServer = null;
	private HttpServerSmall mHttpServerSmall = null;
	
	// wise httpserver callback
	private HttpServer.CallBack mCallBack = new HttpServer.CallBack()
	{
		
		@Override
		public void loadActivity(String url)
		{
			Intent intent = new Intent();
			intent.setAction(IntentConstants.ACTION_HOME);
			intent.putExtra("url", url);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	};
	
	// other small site httpserver callback
	private HttpServerSmall.CallBack mSmallCallBack = new HttpServerSmall.CallBack()
	{
		
		@Override
		public void loadActivity(String url)
		{
			Intent intent = new Intent();
			intent.setAction(IntentConstants.ACTION_HOME);
			intent.putExtra("url", url);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	};
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		logger.d("onStartCommand");
		
		Thread httpServerThread = new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				try
				{
					mHttpServer = new HttpServer(mCallBack, getApplicationContext());
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});
		
		Thread smallHttpServerThread = new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				try
				{
					mHttpServerSmall = new HttpServerSmall(mSmallCallBack, getApplicationContext());
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});
		
		httpServerThread.start();
		smallHttpServerThread.start();
		
		
		flags = Service.START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy()
	{
		logger.d("onDestroy");
		mHttpServer.stop();
		mHttpServerSmall.stop();
		super.onDestroy();
	}
}
