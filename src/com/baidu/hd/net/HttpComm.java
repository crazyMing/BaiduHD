package com.baidu.hd.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.util.Log;

import com.baidu.hd.log.Logger;
import com.baidu.hd.net.HttpResultCallback.HttpDownloaderResult;

/**
 * 应该加一个缓冲区，不能同时开很多的http链接
 */
public class HttpComm {
	
	public static String PLAYERBOX_LIST_URL = "http://php.player.baidu.com/android/home/api.php";
	
	//type=nav 导航
	public static String NAVIGATION_LIST_URL = "http://php.player.baidu.com/android/home/api.php?type=nav";
	//type=rank 搜索排行
	public static String SEARCH_RANKING_LIST_URL = "http://php.player.baidu.com/android/home/api.php?type=rank";
	//type=tag  热词
	public static String HOT_RANKING_LIST_URL = "http://php.player.baidu.com/android/home/api.php?type=tag";
	
	private Logger logger = new Logger(TAG);
	private static final String TAG = "HttpComm";

	private String mUrl = "";
	private boolean mAsync = true;
	private HttpResultCallback mCallback = null;
	
	private boolean isFromHomeSugs = false;
	
	public HttpComm() {
		
	}
	public HttpComm(boolean async) {
		mAsync = async;
	}

	public HttpAsyncTask get(String url, HttpResultCallback callback)
	{
		logger.d("url " + url);
		this.mUrl = url;
		this.mCallback = callback;
		
		// Fix Bug【MEDIA-5343】url中存在非法字符，引起HttpGet崩溃 sunjianshun 2012.12.27
		/**
		 *  抛出异常12-27 15:24:06.289: E/AndroidRuntime(7779):
		 *   java.lang.IllegalArgumentException: Illegal character in query at index 115: 
		 *   http://gate.baidu.com/tc?m=8&video_app=1&ajax=1&src=http://movie.youku.com/search?ccat63560[fe]=1&m63561[cc-ms-q]=a|paid%3A0
		 */
		try {
			
			HttpGet request = new HttpGet(url);
			HttpAsyncTask httpAsyncTask = new HttpAsyncTask();
			if(mAsync) {
				httpAsyncTask.execute(request);
			} else {
				postEvent(work(request));
			}
			return httpAsyncTask;
		}
		catch(IllegalArgumentException e) {
			logger.e(e.toString());
		}
		return null;
	}

	/**
	 * 
	 * @param url
	 * @param messages
	 * @param callback
	 * @param encode 是否转码
	 */
	public void post(String url, Map<String, String> messages, boolean encode, HttpResultCallback callback) {
		
		Log.d(TAG, "url " + url);
		this.mUrl = url;
		this.mCallback = callback;
		
		HttpPost request = new HttpPost(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		if(messages != null) {
			for(Map.Entry<String, String> pair: messages.entrySet()) {
				params.add(new BasicNameValuePair(pair.getKey(), pair.getValue()));
			}
		}
		
		if(encode) {
			try {
				// 转码
				// TODO 考虑使用自己的转码
				HttpEntity charset = new UrlEncodedFormEntity(params, "utf8");
				request.setEntity(charset);
				
			} catch(Exception e) {
				this.postEvent("");
				return;
			}
		}

		if(mAsync) {
			new HttpAsyncTask().execute(request);
		} else {
			postEvent(work(request));
		}
	}
	
	private void postEvent(String result) {
		Log.d(TAG, "result " + result);
		if(this.mCallback != null) {
			this.mCallback.onResponse(result != null ? HttpDownloaderResult.eSuccessful : HttpDownloaderResult.eNone, this.mUrl, result);
		}
	}
	
	private String work(HttpUriRequest request) {

		try
		{
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(request);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) 
			{
				return EntityUtils.toString(response.getEntity(), "UTF-8");
			}
		} 
		catch(ClientProtocolException e) 
		{
			//e.printStackTrace();
		} 
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		} 
		catch(IOException e) 
		{
			//e.printStackTrace();
		} 
		catch(Exception e) 
		{
			//e.printStackTrace();
		}
		return "";
	}

	/**
	 * 执行的异步任务
	 */
	public class HttpAsyncTask extends AsyncTask<HttpUriRequest, Void, String> {

		@Override
		protected String doInBackground(HttpUriRequest... requests) {
			return work(requests[0]);
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onPostExecute(String result) {
			postEvent(result);
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
}
