package com.baidu.hd.net;

import java.util.Hashtable;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.conf.Configuration;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.service.ServiceConsumer;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.util.SystemUtil;

public class UploaderImp implements Uploader, ServiceConsumer {
	
	private Context mContext = null;
	private ServiceFactory mServiceFactory = null;

	@Override
	public void setContext(Context ctx) {
		mContext = ctx;
	}

	@Override
	public void onCreate() {
	}

	@Override
	public void onDestory() {
	}

	@Override
	public void onSave() {
	}

	@Override
	public void setServiceFactory(ServiceFactory factory) {
		this.mServiceFactory = factory;
	}

	@Override
	public void feedBack(String msg, String contact) {
		
		JSONObject v = new JSONObject();
		try {
			v.put("msg", msg);
			v.put("contact", contact);
			v.put("deviceid", SystemUtil.getEmid(mContext));
			v.put("version", SystemUtil.getAppVerison(mContext));
			v.put("devices", "android");
		}catch(JSONException e) {
			e.printStackTrace();
			return;
		}

		String server = this.getConf().getFeedbackUrl();
		Map<String, String> param = new Hashtable<String, String>();
		param.put("postdata", v.toString());
		new HttpComm().post(server, param, true, new HttpResultCallback()
		{
			@Override
			public void onResponse(HttpDownloaderResult result, String url, String message)
			{
				if(mContext == null || !BaiduHD.cast(mContext).getServiceContainer().isCreated()) {
					return;
				}
				UploaderEventArgs args = new UploaderEventArgs(HttpDownloaderResult.eSuccessful == result, message);
				EventCenter center = (EventCenter)mServiceFactory.getServiceProvider(EventCenter.class);
				center.fireEvent(EventId.eFeedBackComplete, args);
			}
			
			@Override
			public void onProgress(String url, float rate)
			{
				
			}
		});
	}

	private Configuration getConf() {
		return (Configuration)this.mServiceFactory.getServiceProvider(Configuration.class);
	}
}
