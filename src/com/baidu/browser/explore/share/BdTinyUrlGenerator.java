package com.baidu.browser.explore.share;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONObject;

/**
 * 短链接生成器
 */
public class BdTinyUrlGenerator {

	/**
	 * 短链接服务器地址
	 */
	private static final String SHORTEN_URL = "http://api.t.sina.com.cn/short_url/shorten.json";
	/**
	 * 短链接服务Key
	 */
	private static final String APIKEY = "57425770";

	/**
	 * 长度小于此的不做转换
	 */
	private static final int LENGTH_THRESHOLD = 25;

	/**
	 * 原始的长链接地址
	 */
	private String mLongUrl;

	/**
	 * 短链接事件监听器
	 */
	private BdTinyUrlRecievedListener mListener;

	/**
	 * 生成短链接
	 * 
	 * @param longUrl
	 *            原始的长链接地址
	 */
	public void generate(String longUrl) {
		if (longUrl == null || longUrl.equals("") || longUrl.length() < LENGTH_THRESHOLD) {
			if (mListener != null) {
				mListener.onTinyUrlRecieved(longUrl);
			}
			return;
		}

		mLongUrl = longUrl;

		Runnable runnable = new Runnable() {

			public void run() {
				try {
					String param = "source=" + APIKEY + "&url_long=" + URLEncoder.encode(mLongUrl);
					String url = SHORTEN_URL + "?" + param;
					URL realUrl = new URL(url);

					URLConnection connection = realUrl.openConnection();
					connection.setRequestProperty("accept", "*/*");
					connection.setRequestProperty("connection", "Keep-Alive");
					connection.setRequestProperty("user-agent",
							"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
					connection.connect();

					BufferedReader in = null;
					in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String line;
					String result = "";
					while ((line = in.readLine()) != null) {
						result += line;
					}
					if (result.startsWith("[") && result.endsWith("]")) {
						result = result.substring(1, result.length() - 1);
					}
					JSONObject jsonObject = new JSONObject(result);

					if (mListener != null) {
						mListener.onTinyUrlRecieved(jsonObject.getString("url_short"));
					}

				} catch (Exception e) {
					if (mListener != null) {
						mListener.onTinyUrlRecieved(mLongUrl);
					}
				}
			}

		};
		new Thread(runnable).start();
	}

	/**
	 * @param aListener
	 *            BdTinyUrlRecievedListener
	 */
	public void setEventListener(BdTinyUrlRecievedListener aListener) {
		mListener = aListener;
	}

	/**
	 * 短链接事件监听器
	 */
	public interface BdTinyUrlRecievedListener {

		/**
		 * @param tinyUrl
		 *            获取的短链接
		 */
		void onTinyUrlRecieved(String tinyUrl);

	}
}
