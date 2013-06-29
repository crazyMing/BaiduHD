package com.baidu.hd.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/*
 * @ClassName   : ToastUtil 
 * @Description : ����ToastĬ��ʵ�ַ�ʽ �����е���ʽ�ظ���ʾ
 * @Author      : sunyimin 
 * @CreateDate  : 2012.8.7
 */

public class ToastUtil {
	private static Handler handler = new Handler(Looper.getMainLooper());
	private static Toast toast = null;
	private static Object synObj = new Object();
	
	/* Show Toast
	 * @param ctx ʹ��ʱ��������
	 * @param msg ��ʾ����
	 * @param len ��ʾʱ�䳤��
	 */
	public static void showMessage(final Context ctx, final String msg, final int len) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						synchronized(synObj) {
							if (toast != null) {
								toast.setText(msg);
								toast.setDuration(len);
							}
							else {
								toast = Toast.makeText(ctx, msg, len);
							}
							toast.show();
						}
					}
				});
			}
		}).start();
	}
	
}
