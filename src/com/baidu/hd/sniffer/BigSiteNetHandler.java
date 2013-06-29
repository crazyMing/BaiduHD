package com.baidu.hd.sniffer;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.conf.Configuration;
import com.baidu.hd.net.HttpComm;
import com.baidu.hd.net.HttpResultCallback;
import com.baidu.hd.net.HttpResultCallback.HttpDownloaderResult;
import com.baidu.hd.service.ServiceFactory;

/**
 * 大站网络支持处理器
 */
class BigSiteNetHandler extends BaseHandler {

	private String mUrlFormat = "";
	
	public void onCreate(OnCompleteListener listener, ServiceFactory serviceFactory, Context context) {
		super.onCreate(listener, context);
		
		Configuration conf = (Configuration)serviceFactory.getServiceProvider(Configuration.class);
		mUrlFormat = conf.getBigSiteSnifferUrl();
	}

	@Override
	protected void start() {

		if("".equals(mRefer)) {
			snifferComplete();
			return;
		}
		final String refer = mRefer;
		String server = String.format(mUrlFormat, mRefer);
		new HttpComm().get(server, new HttpResultCallback() {
			
			@Override
			public void onResponse(HttpDownloaderResult result, String url, String message) {
				if(mContext == null || !BaiduHD.cast(mContext).getServiceContainer().isCreated()) {
					return;
				}
				// 取消或超时后返回
				if(!refer.equals(mRefer)) {
					return;
				}
				try {
					JSONObject o = new JSONObject(message);
					String type = o.optString("video_source_type");
					if(type.equalsIgnoreCase("iphone")) {
						mUrl = o.optString("video_source_url");
					}
				} catch(JSONException e) {
					e.printStackTrace();
				}
				snifferComplete();
			}
			
			@Override
			public void onProgress(String url, float rate) {
			}
		});
	}

	@Override
	protected void stop() {
	}

	@Override
	String getType() {
		return "BigSite";
	}
}
