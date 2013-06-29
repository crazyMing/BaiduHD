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
 * �Զ���� �쳣������ , ʵ���� UncaughtExceptionHandler�ӿ�
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

		System.out.println("����ҵ��� ");

		// 1.��ȡ��ǰ����İ汾��. �汾��id
		String versioninfo = getVersionInfo();

		// 2.��ȡ�ֻ���Ӳ����Ϣ.
		String mobileInfo = getMobileInfo();

		// 3.�Ѵ���Ķ�ջ��Ϣ ��ȡ����
		String errorinfo = getErrorInfo(e);
		
		String message = versioninfo + "\r\n" + 
				mobileInfo + "\r\n" + 
				errorinfo + "\r\n";

		// 4.�����е���Ϣ ������Ϣ��Ӧ��ʱ�� �ύ��������
		Log.e("Crash", message);
		CrashSavor.save(message);
		
		BaiduHD.cast(mContext).destroyService();
		
		mDfltExceptionHandler.uncaughtException(t, e);
	}

	/**
	 * ��ȡ�������Ϣ
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
	 * ��ȡ�ֻ���Ӳ����Ϣ
	 */
	private String getMobileInfo() {
		StringBuffer sb = new StringBuffer();
		// ͨ�������ȡϵͳ��Ӳ����Ϣ
		try {

			Field[] fields = Build.class.getDeclaredFields();
			for (Field field : fields) {
				// �������� ,��ȡ˽�е���Ϣ
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
	 * ��ȡ�ֻ��İ汾��Ϣ
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