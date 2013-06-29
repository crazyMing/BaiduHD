package com.baidu.hd.image;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

import com.baidu.hd.conf.Configuration;
import com.baidu.hd.db.DBReader;
import com.baidu.hd.db.DBWriter;
import com.baidu.hd.detect.Detect;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.Image;
import com.baidu.hd.net.HttpDownloader;
import com.baidu.hd.net.HttpResultCallback;
import com.baidu.hd.service.ServiceConsumer;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;

public class ImageManagerImp implements ImageManager, ServiceConsumer
{
	public static final int Unknown = 0;
	public static final int DownLoadError = 1;
	public static final int DownLoading = 2;
	public static final int DownLoaded = 3;
	
	private Logger logger = new Logger("ImageManager");

	private Context mContext = null;
	private ServiceFactory mServiceFactory = null;

	private String mPath = "";
	private List<Image> mImages = new ArrayList<Image>();
	private Map<Integer, Integer> mDefault = new HashMap<Integer, Integer>();
	private Map<String, Integer> mImageStatus = new HashMap<String, Integer>();
	private int mRequestNum = 0;
	private int mRequestResponseNum = 0;

	private class Package
	{
		String url = "";
		String fileName = "";

		Package(String url, String fileName)
		{
			this.url = url;
			this.fileName = fileName;
		}
	}

	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			Package pack = (Package) msg.obj;
			onResponse(msg.what == 1 ? true : false, pack.url, pack.fileName);
		}
	};

	private HttpResultCallback mCallback = new HttpResultCallback()
	{
		@Override
		public void onResponse(HttpDownloaderResult result, String url, String message)
		{
			boolean success = (HttpDownloaderResult.eSuccessful == result);
			if (mHandler != null)
			{
				mHandler.sendMessageDelayed(mHandler.obtainMessage(success ? 1 : 0, new Package(url, message)), 0);
				mImageStatus.put(url, success ? DownLoaded : DownLoadError);
				if (!success || !Image.isImage(message))
				{
					mImageStatus.remove(url);
					new File(message).delete();
				}
			}
		}
		
		@Override
		public void onProgress(String url, float rate)
		{
			
		}
	};

	@Override
	public void setContext(Context ctx)
	{
		this.mContext = ctx;
	}

	@Override
	public void onCreate()
	{
		Configuration conf = (Configuration) this.mServiceFactory.getServiceProvider(Configuration.class);
		this.mPath = conf.getImagePath();
		File dir = new File(this.mPath);
		if (!dir.exists())
		{
			if (!dir.mkdirs())
			{
				this.logger.e("create dir fail " + this.mPath);
			}
		}

		DBReader reader = (DBReader) this.mServiceFactory.getServiceProvider(DBReader.class);
		this.mImages = reader.getAllImage();

		this.mDefault.put(Image.Type.Album, R.drawable.album);
		this.mDefault.put(Image.Type.SiteIcon, R.drawable.ic_default_brow_home_list);
		this.mDefault.put(Image.Type.HomeNavigation, R.drawable.album);
	}

	@Override
	public void onDestory()
	{
		this.mHandler = null;
	}

	@Override
	public void onSave() {
	}

	@Override
	public void setServiceFactory(ServiceFactory factory)
	{
		this.mServiceFactory = factory;
	}

	@Override
	public Drawable request(String url, int type)
	{
		if (StringUtil.isEmpty(url)) {
			for (Map.Entry<Integer, Integer> entry : this.mDefault.entrySet()) {
				if (entry.getKey() == type) 	{
					return this.mContext.getResources().getDrawable(entry.getValue());
				}
			}
		}
		
		Image image = this.find(url);
		if (image == null)
		{
			image = new Image();
			image.setUrl(url);
			image.setType(type);
			this.getDBWriter().modifyImage(image, DBWriter.Action.Add);
			this.mImages.add(image);
		}
		else
		{
			if (!"".equals(image.getPath()) && new File(image.getPath()).exists())
			{
				if (Image.isImage(image.getPath()))
				{
					return Drawable.createFromPath(image.getPath());
				}
				else 
				{
					new File(image.getPath()).delete();
				}
			}
		}

		Detect detect = (Detect) this.mServiceFactory.getServiceProvider(Detect.class);
		if ((null == mImageStatus.get(url) || DownLoadError == mImageStatus.get(url)) && detect.isNetAvailabe() && detect.isSDCardAvailable())
		{
			mImageStatus.put(url, DownLoading);
			String fileName = this.mPath + StringUtil.createUUID() + ".png";
			new HttpDownloader().download(url, fileName, this.mCallback);
			mRequestNum++;
			logger.d("request num = " + mRequestNum);
		}
		
		for (Map.Entry<Integer, Integer> entry : this.mDefault.entrySet())
		{
			if (entry.getKey() == type)
			{
				return this.mContext.getResources().getDrawable(entry.getValue());
			}
		}
		return null;
	}

	private void onResponse(boolean success, String url, String fileName)
	{
		Image image = this.find(url);
		if (image != null)
		{
			image.setPath(fileName);
			this.getDBWriter().modifyImage(image, DBWriter.Action.Update);
		}
		
		if (!success)
		{
			logger.d("onResponse update Error : " + url);
		}
		
		mRequestResponseNum++;
		logger.d("onResponse num = " + mRequestResponseNum);
		if (mRequestResponseNum == mRequestNum)
		{
			logger.d("onResponse update once");
			EventCenter eventCenter = (EventCenter) this.mServiceFactory.getServiceProvider(EventCenter.class);
			eventCenter.fireEvent(EventId.eImageNeedReload, null);
		}
	}

	private Image find(String url)
	{
		for (Image image : this.mImages)
		{
			if (image.getUrl().equals(url))
			{
				return image;
			}
		}
		return null;
	}

	private DBWriter getDBWriter()
	{
		return (DBWriter) this.mServiceFactory.getServiceProvider(DBWriter.class);
	}
}
