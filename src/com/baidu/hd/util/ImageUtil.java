package com.baidu.hd.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.baidu.hd.R;

public class ImageUtil {
	/**
	 * @Title: createAlbumIcon 
	 * @Description: 根据数字，创建一张带有数字的图片
	 * @param @param number
	 * @param @param bitmap
	 * @param @param context
	 * @param @return    设定文件 
	 * @return Bitmap    返回类型 
	 * @throws
	 */
	public static Bitmap createAlbumIcon(int number, Bitmap bitmap, Context context) {
		if (number == 0) {
			return bitmap;
		}
		int len = getNumberLength(number);
		int textSize = (int)context.getResources().getDimension(R.dimen.sniffer_text_size);
		int x = len*(textSize);		
		int y = textSize;
		
    	int width = bitmap.getWidth();
    	int height = bitmap.getHeight();
    	Bitmap contactIcon=Bitmap.createBitmap(width, height, Config.ARGB_8888);
    	Canvas canvas=new Canvas(contactIcon);
    	
    	Paint iconPaint=new Paint();
    	iconPaint.setDither(true);	
    	iconPaint.setFilterBitmap(true);
    	Rect src1=new Rect(0, 0, width, height);
    	Rect dst1=new Rect(0, 0, width, height);
    	canvas.drawBitmap(bitmap, src1, dst1, iconPaint);
    	Paint bgPaint=new Paint();
    	bgPaint.setColor(Color.RED);
    	canvas.drawRect(width - x/2-6, 0, width + x/2+6, y, bgPaint);
    	Paint countPaint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DEV_KERN_TEXT_FLAG);
    	countPaint.setColor(Color.WHITE);
    	countPaint.setTextSize(textSize);
    	countPaint.setTypeface(Typeface.DEFAULT_BOLD);
    	canvas.drawText(String.valueOf(number), width - x/2 - 5, y-2 , countPaint);
    	return contactIcon;
	}
	
	/**
	 * 统计数字位数
	 */
	private static int getNumberLength(int number) {
		int count = 0;
		while (number != 0) {
			number = number / 10;
			count++;
		}
		return count;
	}
}
