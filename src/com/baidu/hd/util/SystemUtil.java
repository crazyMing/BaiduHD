package com.baidu.hd.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class SystemUtil {

	public static String getEmid(Context context) {
		TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if(manager == null) {
			return "";
		}
		String ret =  manager.getDeviceId();
		if(StringUtil.isEmpty(ret)) {
			ret = getLocalMacAddress(context);
		}
		return (ret == null) ? "" : ret;
	}
	
	public static String getLocalMacAddress(Context context) {  
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(wifi == null) {
        	return "";
        }
        WifiInfo info = wifi.getConnectionInfo();
        if(info == null) {
        	return "";
        }
        return info.getMacAddress();  
    }  
	
	public static String getAppVerison(Context context) {
		try {
			return context.getPackageManager().getPackageInfo("com.baidu.hd", 0).versionName;
		} catch(PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/** 获取手机的硬件信息 */
	public static String getMobileInfo() {
		StringBuffer sb = new StringBuffer();
		// 通过反射获取系统的硬件信息
		try {

			Field[] fields = Build.class.getDeclaredFields();
			for (Field field : fields) {
				// 暴力反射 ,获取私有的信息
				field.setAccessible(true);
				String name = field.getName();
				String value = field.get(null).toString();
				sb.append(name + "=" + value);
				sb.append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public static String getCPUInfo() {
		try
		{
			byte[] bs = new byte[1024];
			RandomAccessFile reader = new RandomAccessFile("/proc/cpuinfo", "r");
			reader.read(bs);
			String ret = new String(bs);
			int index = ret.indexOf(0);
			if(index != -1) {
				return ret.substring(0, index);
			} else {
				return ret;
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return "";
	}
	
	public static Turple<Integer, Integer> getResolution(Context context) {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();
		d.getMetrics(metrics);
		return new Turple<Integer, Integer>(metrics.widthPixels, metrics.heightPixels);
	}
}
