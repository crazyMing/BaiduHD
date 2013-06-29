package com.baidu.hd.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

import com.baidu.hd.log.Logger;
import com.baidu.hd.net.HttpResultCallback.HttpDownloaderResult;

public class NewHttpComm
{
	private Logger logger = new Logger("NewHttpComm");
	
	private String mUrl = "";
	private boolean mAsync = true;
	private HttpResultCallback mCallback = null;
	
	public NewHttpComm()
	{}
	
	public NewHttpComm(boolean async)
	{
		this.mAsync = async;
	}
	
	public HttpAsyncTask get(String url, HttpResultCallback callback)
	{
		logger.d("url " + url);
		this.mUrl = url;
		this.mCallback = callback;
		
		HttpGet request = null;
		HttpAsyncTask httpAsyncTask = null;
		
		try
		{
			request = new HttpGet(url);
			httpAsyncTask = new HttpAsyncTask();
			if(mAsync) {
				httpAsyncTask.execute(request);
			} else {
				postEvent(work(request));
			}
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return httpAsyncTask;
		
	}
	
	private String work(HttpUriRequest request)
	{
		try
		{
			HttpURLConnection urlConnection = (HttpURLConnection) new URL(request.getURI().toString()).openConnection();
			if (urlConnection.getContentType().toString().startsWith("text"))
			{
				HttpClient client = new DefaultHttpClient();
				HttpResponse response = client.execute(request);
				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					return EntityUtils.toString(response.getEntity(), "UTF-8");
				}
			}
		} 
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		} 
		catch (ClassCastException e)
		{
			e.printStackTrace();
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return "";
	}
	
	private void postEvent(String result) {
		logger.d("result " + result);
		if(this.mCallback != null) {
			this.mCallback.onResponse(result != null ? HttpDownloaderResult.eSuccessful : HttpDownloaderResult.eNone, this.mUrl, result);
		}
	}
	
	public class HttpAsyncTask extends AsyncTask<HttpUriRequest, Void, String>
	{

		@Override
		protected String doInBackground(HttpUriRequest... params)
		{
			return work(params[0]);
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
