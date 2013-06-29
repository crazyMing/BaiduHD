package com.baidu.hd;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.baidu.hd.stat.CrashSavor;

/**
 * 自定义的 异常处理类 , 实现了 UncaughtExceptionHandler接口
 */
public class MyCrashHandler implements UncaughtExceptionHandler {

	private Thread.UncaughtExceptionHandler mDfltExceptionHandler;
	private Context mContext;
	
	public MyCrashHandler(Context context) {
		mDfltExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		mContext = context;
	}

	public void uncaughtException(Thread t, Throwable e) {
		
		Thread.setDefaultUncaughtExceptionHandler(mDfltExceptionHandler);

		System.out.println("程序挂掉了 ");

		// 1.获取当前程序的版本号. 版本的id
		String versioninfo = getVersionInfo();

		// 2.获取手机的硬件信息.
		String mobileInfo = getMobileInfo();

		// 3.把错误的堆栈信息 获取出来
		String errorinfo = getErrorInfo(e);
		
		String message = versioninfo + "\r\n" + 
				mobileInfo + "\r\n" + 
				errorinfo + "\r\n";

		// 4.把所有的信息 还有信息对应的时间 提交到服务器
		Log.e("Crash", message);
		CrashSavor.save(message);
		
		BaiduHD.cast(mContext).destroyService();
		
		mDfltExceptionHandler.uncaughtException(t, e);
	}

	/**
	 * 获取错误的信息
	 */
	private String getErrorInfo(Throwable arg1) {
		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		arg1.printStackTrace(pw);
		pw.close();
		String error = writer.toString();
		return error;
	}

	/**
	 * 获取手机的硬件信息
	 */
	private String getMobileInfo() {
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

	/**
	 * 获取手机的版本信息
	 */
	private String getVersionInfo() {
		try {
			PackageManager pm = mContext.getPackageManager();
			PackageInfo info = pm.getPackageInfo(mContext.getPackageName(), 0);
			return "ver:" + info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
			return "ver:unknown";
		}
	}
}