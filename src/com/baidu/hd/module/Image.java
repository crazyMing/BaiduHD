package com.baidu.hd.module;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class Image {
	
	public class Type {
		public static final int Unknown = 0;
		public static final int Album = 1;
		public static final int FriendApp = 2;
		public static final int MoreApp = 3;
		public static final int SiteIcon = 4;
		public static final int HomeNavigation = 5;
	}

	private long id = -1;
	private String url = "";
	private String path = "";
	private int type = 0;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	public static boolean isImage(String path)
	{
		try
		{
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0)
			{
				if (bitmap != null)
				{
					bitmap.recycle();
				}
				return false;
			}
			else
			{
				bitmap.recycle();
				return true;
			}
		}
		catch (Exception err)
		{
			err.printStackTrace();
			return false;
		}
	}
}
