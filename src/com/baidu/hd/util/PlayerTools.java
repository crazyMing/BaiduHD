package com.baidu.hd.util;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.widget.Toast;



public class PlayerTools
{
	public static String[] EXTERNAL_PATH = null;
	public static final String TAG = "PlayerTools";
	public static final int TYPE_MOBILE = 0;
	public static final int TYPE_NO_NETWORK = 64;
	public static final int TYPE_WIFI = 1;
	public static boolean isShowing;
	public static boolean showningErrorDialog;
	public static boolean createSDCardReturn;

	static
	{
		String[] arrayOfString = new String[2];
		arrayOfString[0] = "3gp";
		arrayOfString[1] = "mp4";
		isShowing = false;
		showningErrorDialog = false;
		createSDCardReturn = false;
	}
	
	public enum NetworkType
	{
		NETWORK_TYPE_WIFI ,
		NETWORK_TYPE_4G,
		NETWORK_TYPE_3G,
		NETWORK_TYPE_GPRS
	}


	public static void deleteFile(File paramFile)
	{
		if (paramFile.exists())
		{
			if (paramFile.isFile())
			{
				paramFile.delete();
			}
			else if (paramFile.isDirectory())
			{
				File[] arrayOfFile = paramFile.listFiles();
				for (int i = 0; i < arrayOfFile.length; i++)
				{
					deleteFile(arrayOfFile[i]);
				}
			}
		}
	}



	
	public static String getStoreInPath()
	{
		String sdPath = "";
		if (checkSDPath())
		{
			sdPath = Environment.getDataDirectory().toString();
		}
		Log.d("PlayerTools", "DataDirectory = " + sdPath);
		return sdPath;
	}

	public static boolean checkSDPath()
	{
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	public static String getSDPath()
	{
		String sdPath = "";
		if (checkSDPath())
		{
			sdPath = Environment.getExternalStorageDirectory().toString();
		}
		Log.d("PlayerTools", "SDPath = " + sdPath);
		return sdPath;
	}


	public static String getDownLoadPath()
	{
		String strDownLoadPath = null;
		if (getSDPath().length() != 0)
		{
			strDownLoadPath = getSDPath() + "/baidu player";
		}
		Log.d("PlayerTools", "DownLoadPath = " + strDownLoadPath);
		return strDownLoadPath;
	}

	public static long getAvailaleSize(String paramString)
	{
		StatFs localStatFs = new StatFs(new File(paramString).getPath());
		long blockSize = localStatFs.getAvailableBlocks();
        long totalBlocks = localStatFs.getBlockCount();
		long sdSize = blockSize * totalBlocks;
		Log.d("PlayerTools", "SDSize = " + sdSize);
		return sdSize;
	}




	
	


	public static void downloadWith3G(Context paramContext, boolean paramBoolean)
	{
		Log.i("PlayerTools", "downloadWith3G");
		if (paramContext == null)
		{
			Log.d("PlayerTools", "the function downloadWith3G called with parameter context = null!");
		}
		else
		{
			Log.d("PlayerTools", "downloadWith3G sendBroadcast Intent");
			Intent localIntent = new Intent("PLAYER.DOWNLOAD_WITH_3G_ACTION");
			localIntent.putExtra("use_3G", paramBoolean);
			paramContext.sendBroadcast(localIntent);
		}
	}



	public static boolean checkNetwork(Context paramContext)
	{
		boolean bReturn = false;
		ConnectivityManager localConnectivityManager = (ConnectivityManager)paramContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (localConnectivityManager != null)
		{
			NetworkInfo localNetworkInfo = localConnectivityManager.getActiveNetworkInfo();
			if (localNetworkInfo != null)
			{
				bReturn = localNetworkInfo.isAvailable();
			}
		}
		Log.d("PlayerTools", "checkNetwork = " + bReturn);
		return bReturn;
	}




	public static int getNetWorkConnected(Context paramContext)
	{
		ConnectivityManager localConnectivityManager = (ConnectivityManager)paramContext.getApplicationContext().getSystemService("connectivity");
		NetworkInfo[] arrayOfNetworkInfo;
		int networkType = -1;
		if (localConnectivityManager != null)
		{
			arrayOfNetworkInfo = localConnectivityManager.getAllNetworkInfo();
			if (arrayOfNetworkInfo != null)
			{
				for (int j = 0; j < arrayOfNetworkInfo.length; j++)
				{
					if (arrayOfNetworkInfo[j].isConnected())
					{
						networkType = arrayOfNetworkInfo[j].getType();
						break;
					}
				}
			}
		}
		Log.d("PlayerTools", "getNetWorkConnected = " + networkType);
		return networkType;
	}
	
	
	public static boolean isNetworkConnected(Context paramContext, NetworkType networkType)
	{
		boolean bReturn = false;
		ConnectivityManager localConnectivityManager = (ConnectivityManager)paramContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (localConnectivityManager != null)
		{
			NetworkInfo[] arrayOfNetworkInfo = localConnectivityManager.getAllNetworkInfo();
			if ((arrayOfNetworkInfo != null ) && 
				isNetworkType(paramContext, networkType))
			{
				int connectType = 0;
				switch (networkType)
				{
					case NETWORK_TYPE_WIFI :
						connectType = ConnectivityManager.TYPE_WIFI;
						break;
						
					case NETWORK_TYPE_3G :
					case NETWORK_TYPE_4G :
					case NETWORK_TYPE_GPRS :
						connectType = ConnectivityManager.TYPE_MOBILE;
						break;
						
					default:
						break;
				}
				
				for (int j = 0; j < arrayOfNetworkInfo.length; j++)
				{
					if (arrayOfNetworkInfo[j].isConnected())
					{
						Log.d("PlayerTools", "NetworkConnectType = " + arrayOfNetworkInfo[j].getType());
						if (arrayOfNetworkInfo[j].getType() == connectType)
						{
							bReturn = true;
							break;
						}
					}
				}
			}
		}
		Log.d("PlayerTools", "isNetworkConnected = " + networkType + " bReturn = " + bReturn);
		return bReturn;
	}
	
	public static boolean isNetworkType(Context paramContext, NetworkType networkType)
	{
		boolean bReturn = false;
		TelephonyManager localTelephonyManager = null;
		switch (networkType)
		{
			case NETWORK_TYPE_WIFI :
				if ((WifiManager)paramContext.getSystemService(Context.WIFI_SERVICE) != null)
				{
					bReturn = true;
				}
				break;
				
			case NETWORK_TYPE_4G :
				
				break;
				
			case NETWORK_TYPE_3G :
				localTelephonyManager = (TelephonyManager)paramContext.getSystemService(Context.TELEPHONY_SERVICE);
				if (localTelephonyManager != null)
				{
					int phoneNetworkType = localTelephonyManager.getPhoneType();
					if ((phoneNetworkType != TelephonyManager.NETWORK_TYPE_UNKNOWN ) && 
						(phoneNetworkType != TelephonyManager.NETWORK_TYPE_GPRS) &&
						(phoneNetworkType != TelephonyManager.NETWORK_TYPE_EDGE ))
					{
						bReturn = true;
					}
				}
				break;
				
			case NETWORK_TYPE_GPRS :
				localTelephonyManager = (TelephonyManager)paramContext.getSystemService("phone");
				if ((localTelephonyManager != null) &&
					(localTelephonyManager.getPhoneType() == TelephonyManager.NETWORK_TYPE_GPRS))
				{
					bReturn = true;
				}
				break;
	
			default:
				break;
		}
		
		Log.d("PlayerTools", "isNetworkType = " + networkType + " bReturn = " + bReturn);
		return bReturn;
	}
	
	private static SimpleDateFormat dateTimeFormat;
	private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	static
	{
		dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public static void alert(Context paramContext, String paramString)
	{
		Toast.makeText(paramContext, paramString, 1);
	}

	public static Bitmap drawableToBitmap(Drawable paramDrawable)
	{
		Bitmap localBitmap = null;
		if (paramDrawable != null)
		{
			
			int i = paramDrawable.getIntrinsicWidth();
			int j = paramDrawable.getIntrinsicHeight();
			Bitmap.Config bc;
			if (paramDrawable.getOpacity() == -1)
				bc = Bitmap.Config.RGB_565;
			else
				bc = Bitmap.Config.ARGB_8888;
			localBitmap = Bitmap.createBitmap(i, j, (Bitmap.Config) bc);
			Object localObject = new Canvas(localBitmap);
			paramDrawable.setBounds(0, 0, i, j);
			paramDrawable.draw((Canvas) localObject);
		}
		return (Bitmap) localBitmap;
	}

	public static int formatDipToPx(Context paramContext, float paramInt)
	{
		DisplayMetrics localDisplayMetrics = new DisplayMetrics();
		((Activity) paramContext).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
		return (int) FloatMath.ceil(paramInt * localDisplayMetrics.density);
	}
	
	public static float formatPxToDip(Context paramContext, int paramInt)
	{
		DisplayMetrics localDisplayMetrics = new DisplayMetrics();
		((Activity) paramContext).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
		if (localDisplayMetrics.density > 0)
		{
			return paramInt/localDisplayMetrics.density;
		}
		else return paramInt;
	}

	public static String getFileAttrStrings(File paramFile)
	{
		Runtime localRuntime = Runtime.getRuntime();
		String str = "ls -l \"" + paramFile.getAbsolutePath() + "\"";
		try
		{
			return  new Scanner(localRuntime.exec(str).getInputStream()).nextLine();
		}
		catch (IOException localIOException)
		{
			while (true)
				localIOException.printStackTrace();
		}
	}

	public static String toDateTimeString(Date paramDate)
	{
		String str;
		if (paramDate == null)
			str = "";
		else
			str = dateTimeFormat.format(paramDate);
		return str;
	}

	public static String toTimeString(Date paramDate)
	{
		String str;
		if (paramDate == null)
			str = "";
		else
			str = timeFormat.format(paramDate);
		return str;
	}
}