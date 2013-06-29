package com.baidu.hd.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;

import com.baidu.hd.log.Logger;
import com.baidu.hd.net.HttpResultCallback.HttpDownloaderResult;

public class HttpDownloader
{
	private final String TAG = getClass().getSimpleName();
	private Logger logger = new Logger(TAG);
	
	private String mUrl = "";
	private String mFileName = "";
	private boolean mAsync = true;
	private HttpResultCallback mCallback = null;

	public HttpDownloader()
	{
	}

	public HttpDownloader(boolean async)
	{
		mAsync = async;
	}

	public void download(String url, String fileName, HttpResultCallback callback)
	{
		this.mUrl = url;
		this.mFileName = fileName;
		this.mCallback = callback;

		if (mAsync)
		{
			new DownloadAsyncTask().execute();
		}
		else
		{
			postEvent(work());
		}
	}

	private void postEvent(HttpDownloaderResult success)
	{
		if (mCallback != null)
		{
			mCallback.onResponse(success, this.mUrl, this.mFileName);
		}
	}

	private HttpDownloaderResult work()
	{
		HttpDownloaderResult result = HttpDownloaderResult.eNone;
		OutputStream output = null;
		try
		{
			/*
			 * ͨ��URLȡ��HttpURLConnection Ҫ�������ӳɹ�������AndroidMainfest.xml�н���Ȩ������
			 * <uses-permission android:name="android.permission.INTERNET"
			 * />
			 */
			HttpURLConnection conn = null;
			try
			{
				URL url = null;
				try
				{
					url = new URL(mUrl);
				}
				catch (MalformedURLException   e)
				{
					e.printStackTrace();
					return HttpDownloaderResult.eUrlIllegal;
				}
				conn = (HttpURLConnection) url.openConnection();
			}
			catch (IOException  e)
			{
				e.printStackTrace();
				return HttpDownloaderResult.eOpenUrlError;
			}
			
//			conn.setRequestMethod("GET");
			conn.setRequestProperty("Referer", mUrl);
//			conn.setDoOutput(true); // ע�͸��У��޸�ĳЩ�ֻ��޷����ص�Bug��
			conn.setRequestProperty("Accept-Encoding", "identity"); // ��Ӹ��У��޸�getContentLength() = -1������
			conn.setDoInput(true);
			conn.setDefaultUseCaches(false);
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			int totalsize = conn.getContentLength();
			
//			int responseCode = conn.getResponseCode();
//			if (responseCode == 404)
//			{
//				return false;
//			}
			
			
			// ȡ��inputStream���������е���Ϣд��SDCard

			/*
			 * дǰ׼�� 1.��AndroidMainfest.xml�н���Ȩ������ <uses-permission
			 * android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
			 * ȡ��д��SDCard��Ȩ�� 2.ȡ��SDCard��·����
			 * Environment.getExternalStorageDirectory() 3.���Ҫ������ļ����Ƿ��Ѿ�����
			 * 4.�����ڣ��½��ļ��У��½��ļ� 5.��input���е���Ϣд��SDCard 6.�ر���
			 */
			File file = new File(mFileName);
			InputStream input = null;
			try
			{
				input = conn.getInputStream();
			}
			catch (IOException e)
			{
				logger.e("conn.getInputStream :" + e.toString());
				return HttpDownloaderResult.eReadError;
			}

			file.createNewFile();// �½��ļ�
			output = new FileOutputStream(file);

			// ��ȡ���ļ�
			int readed = 0;
			int downloadsize = 0;
			byte[] buffer = new byte[100 * 1024];
			while (readed != -1)
			{
				if (readed > 0)
				{
					try
					{
						output.write(buffer, 0, readed);
					}
					catch (IOException e)
					{
						result = HttpDownloaderResult.eWriteError;
						break;
					}
					
					downloadsize += readed;
					if (totalsize > 0)
					{
						mCallback.onProgress(mUrl, downloadsize / (float)totalsize);
					}
				}
				
				try
				{
					readed = input.read(buffer);
				}
				catch (IOException e)
				{
					result = HttpDownloaderResult.eReadError;
					break;
				}
			}
			output.flush();
			if (result == HttpDownloaderResult.eNone)
			{
				result = HttpDownloaderResult.eSuccessful;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		finally
		{
			try
			{
				if (output != null)
				{
					output.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * ִ�е��첽����
	 */
	private class DownloadAsyncTask extends AsyncTask<Void, Void, HttpDownloaderResult>
	{

		@Override
		protected HttpDownloaderResult doInBackground(Void... params)
		{
			return work();
		}

		@Override
		protected void onPostExecute(HttpDownloaderResult result)
		{
			postEvent(result);
		}
	}
}
