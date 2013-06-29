package com.baidu.hd.invokeapp;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;

import com.baidu.hd.log.Logger;
import com.baidu.hd.util.SystemUtil;
import com.baidu.hd.util.UrlUtil;

/*
 * @ClassName   : HttpServerSmall
 * @Description : http 小站 服务器
 * @Author      : sunyimin
 * @CreateDate  : 2012.12.4
 * @UpdateDate  : 2013.1.5 晚 优化白名单
 */


public class HttpServerSmall extends NanoHTTPD
{
	private Logger logger = new Logger("HttpServerSmall");
	private CallBack mCallBack = null;
	private Context ctx = null;
	
//	private String mWihteListServer = "http://php.player.baidu.com/android/WhiteList.php?callback=%s";
//	private String mWitheListServer = "http://yingyin.olympe.in/WhiteList.xml";
	private String mWhiteListServer = "http://php.player.baidu.com/android/WhiteList.xml";
	private String msg = "";
	
	public interface CallBack
	{
		void loadActivity(String url);
	}
	
	public HttpServerSmall(CallBack callBack, Context ctx) throws IOException
	{
		super(9082, new File("."));
		this.mCallBack = callBack;
		this.ctx = ctx;
	
		logger.d("start");
	}

	public Response serve( String uri, String method, Properties header, Properties parms, Properties files )
	{
		msg = "";
		final String version = "\"" + SystemUtil.getAppVerison(this.ctx) + "\"";
		final String loadUrl = parms.getProperty("url");
		final String use = parms.getProperty("use");
		msg = parms.getProperty("callback");
		if (loadUrl != null && !loadUrl.equals(""))
		{
			String validate = UrlUtil.getHost(loadUrl);
			
			XmlDecoder decoder = new XmlDecoder(1L * 12 * 60 * 60 * 1000, mWhiteListServer);
			decoder.checkForUpdate();
			if (decoder.isNameValid(validate))
			{
				if (use != null)
				{
					if (use.equals("1")) // 查询是否在app内
					{
						if (isInApp())
						{
							msg = msg + "({\"return\" : \"1\" , \"version\" : " + version + "})";
						}
						else 
						{
							msg = msg + "({\"return\" : \"0\" , \"version\" : " + version + "})";
						}
					}
					else if (use.equals("2")) // 调起app
					{
						msg = msg + "({\"return\" : \"2\" , \"version\" : " + version + "})";
						logger.d("will start activity and load url : " + loadUrl);
						mCallBack.loadActivity(loadUrl);
					}
				}
			}
		}
		
		logger.d("response : " + msg);
		
		return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, msg);
	}
	
	public void stop()
	{
		logger.d("stop");
		super.stop();
	}
	
	private boolean isInApp()
	{
		String packageName = "com.baidu.hd";
		
		ActivityManager am = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> taskInfos = am.getRunningTasks(1);
		if (taskInfos.size() > 0)
		{
			if (packageName.equals(taskInfos.get(0).topActivity.getPackageName()))
			{
				return true;
			}
			else 
			{
				return false;
			}
		}
		else 
			return false;
	}
}
