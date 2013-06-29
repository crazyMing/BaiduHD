package com.baidu.hd.sniffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.conf.Configuration;
import com.baidu.hd.log.Logger;
import com.baidu.hd.net.HttpResultCallback;
import com.baidu.hd.net.NewHttpComm;
import com.baidu.hd.util.SystemUtil;
import com.baidu.hd.util.Turple;
import com.baidu.hd.util.UrlUtil;

/**
 * 小站网络支持处理器
 */
class SmallSiteHandler extends SmallBaseHandler{
	
	private Logger logger = new Logger("Sniffer");
	
	public void onCreate(OnSmallCompleteListener listener, Context context)
	{
		super.onCreate(listener, context);
	}

	@Override
	protected void start()
	{
		final String fRefer = mRefer;
		String srcRefer = mRefer;
		
		// TODO
		if(fRefer.startsWith("http://m.baidu.com")) {
			Map<String, String> query = UrlUtil.getQuery(fRefer);
			if(query.containsKey("src")) {
				srcRefer = UrlUtil.decode(query.get("src"));
			} else {
				mSmallSiteUrl = new SmallSiteUrl();
				snifferComplete();
				return;
			}
		}
		
		logger.d("will sniffer " + srcRefer);

		Configuration conf = (Configuration) BaiduHD.cast(mContext)
				.getServiceFactory().getServiceProvider(Configuration.class);
		String format = conf.getSmallSiteSnifferUrl();
		String server = String.format(format,
				SystemUtil.getAppVerison(mContext), SystemUtil.getEmid(mContext),
				srcRefer);
		
		mHttpAsyncTask = new NewHttpComm(true).get(server, new HttpResultCallback() {

			@Override
			public void onResponse(HttpDownloaderResult success, String url,
					String message) {
				
//				if(mCancel) {
//					fCallback.onCancel(fRefer);
//					return;
//				}	
				
				if (success != HttpDownloaderResult.eSuccessful) {
					mSmallSiteUrl = new SmallSiteUrl();
					mSmallSiteUrl.setSnifferType(false);
					snifferComplete();
					return;
				}
				try {
					SmallSiteUrl urls = new SmallSiteUrl();

					JSONObject obj = new JSONObject(message);
					urls.setRefer(obj.optString("ref_url"));
					urls.setLink(obj.optString("bdhd"));

					List<Turple<String, String>> playUrls = new ArrayList<Turple<String, String>>();
					JSONArray array = obj.optJSONArray("play_urls");
					for (int i = 0; i < array.length(); ++i) {
						JSONObject entry = array.getJSONObject(i);
						playUrls.add(new Turple<String, String>(entry
								.optString("url"), entry.optString("bdhd")));
					}
					urls.setPlayUrls(playUrls);
					mSmallSiteUrl = urls;
					mSmallSiteUrl.setSnifferType(false);
					snifferComplete();

				} catch (JSONException e) {
					e.printStackTrace();
					mSmallSiteUrl = new SmallSiteUrl();
					mSmallSiteUrl.setSnifferType(false);
					snifferComplete();
				}
			}

			@Override
			public void onProgress(String url, float rate) {
			}
		});
	}

	@Override
	protected void stop()
	{
		
	}

	@Override
	protected String getType()
	{
		return "SmallServerSniffer";
	}
}
