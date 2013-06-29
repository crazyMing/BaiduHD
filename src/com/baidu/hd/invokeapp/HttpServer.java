package com.baidu.hd.invokeapp;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import android.content.Context;

import com.baidu.hd.log.Logger;
import com.baidu.hd.util.SystemUtil;
import com.baidu.hd.util.UrlUtil;

/*
 * @ClassName   : HttpServer
 * @Description : http 服务器
 * @Author      : sunyimin
 * @CreateDate  : 2012.11.18
 * @UpdateDate  : 2013.1.5 优化白名单
 */

public class HttpServer extends NanoHTTPD
{
	private Logger logger = new Logger("HttpServer");
	
	private CallBack mCallBack = null;
	private Context ctx = null;
//	private String mWitheListServer = "http://yingyin.olympe.in/WhiteList.xml";
	private String mWhiteListServer = "http://php.player.baidu.com/android/WhiteList.xml";
	
	public interface CallBack
	{
		void loadActivity(String url);
	}

	public HttpServer(CallBack callBack, Context ctx) throws IOException
	{
		super(9081, new File("."));
		this.mCallBack = callBack;
		this.ctx = ctx;
		
		logger.d("start");
	}
	
	public Response serve( String uri, String method, Properties header, Properties parms, Properties files )
	{
		String versionCode = "\"" + SystemUtil.getAppVerison(this.ctx) + "\"";
		String msg = "";
		String url = parms.getProperty("url");
		String use = parms.getProperty("use");
		
		if (url != null)
		{
			String validate = UrlUtil.getHost(url);
			
			logger.d("validate : " +  validate);
			
			XmlDecoder decoder = new XmlDecoder(1L * 12 * 60 * 60 * 1000, mWhiteListServer);
			decoder.checkForUpdate();
			
			if (decoder.isNameValid(validate))
			{
				msg = parms.getProperty("callback") + "({\"code\":\"200\" , \"version\":" + versionCode + "})";
				logger.d("will start activity and load url : " + url);
				this.mCallBack.loadActivity(url);
			}
		}
		
		if (use != null)
		{
			if (use.equals("1"))
			{
				msg = parms.getProperty("callback") + "({\"code\":\"100\", \"version\":" + versionCode + "})";
			}
		}
		logger.d("response : " + msg);
		return new NanoHTTPD.Response( HTTP_OK, MIME_HTML, msg );
	}
	
	public void stop()
	{
		logger.d("stop");
		super.stop();
	}
}
 