package com.baidu.hd.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 用于获取各种格式的时间，单例模式实现
 * @author sunjianshun
 *
 */
public class TimeUtil {

	private static TimeUtil mInstance = null;
	public static TimeUtil getInstance() {
		if (mInstance == null) {
			mInstance = new TimeUtil();
		}
		return mInstance;
	}
	
	private TimeUtil() {		
	}
	
	///////////////////////////////////////////////////////////////
    /**
     * 返回"yyyy-MM-dd HH:mm:ss"格式
     */
	public String getCurrentTime() {
		
		long timemillis = System.currentTimeMillis();   
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date(timemillis));
	}
	
	/**
	 * 返回真：当天时间。以第一个参数为参照
	 * 参数格式:"yyyy-MM-dd HH:mm:ss"
	 */
	private boolean isToday(final String today, final String time) {
		if (getYear(today).equals(getYear(time)) && getMonth(today).equals(getMonth(time))) {
			String today_day = getDay(today);
			String time_day = getDay(time);
			return today_day.equals(time_day);
		}
		
		return false;
	}
	
	/**
	 * 返回真：当天或者未来时间。以第一个参数为参照
	 * 参数格式:"yyyy-MM-dd HH:mm:ss"
	 */
	public boolean isTodayOrFuture(final String today, final String time) {
		
		if (isToday(today, time)) return true;
		else if (today.compareTo(time) < 0) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * 以第一个参数为参照
	 * 参数格式:"yyyy-MM-dd HH:mm:ss"
	 */
	public boolean isYesterday(final String today, final String time) {
		// TODO 1.格式检查
		
		String yestoday = null;
		int iTodayYear = Integer.valueOf(getYear(today)).intValue();				
		int iTodayMonth = Integer.valueOf(getMonth(today)).intValue();
		int iTodayDay = Integer.valueOf(getDay(today)).intValue();
		
		// 月份起始从0开始
		iTodayMonth = (iTodayMonth -1 + 12)%12;
		
		// 用today设置cal
		Calendar cal = Calendar.getInstance();
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");	    
	    cal.set(iTodayYear, iTodayMonth, iTodayDay);
	    
	    // 计算得到yesterday
	    cal.add(Calendar.DATE, -1);
	    yestoday = dateFormat.format(cal.getTime());
	    
	    // 比较tiem与yesterday是否相等
	    String time_yesterday = time.split(" ")[0];
	    return yestoday.equals(time_yesterday);	
	}
	
	/**
	 * 以第一个参数为参照
	 * 参数格式:"yyyy-MM-dd HH:mm:ss"
	 */
	public boolean isEarliear(final String today, final String time) {
		// TODO 优化
		// 不是昨天，不是今天，并且值小于today
		if (!isToday(today, time) && !isYesterday(today, time)) {
			return today.compareTo(time) > 0 ? true : false; 
		}
		return false;
	}
	
	/**
	 * 提取 dd
	 * 参数格式:"yyyy-MM-dd HH:mm:ss"
	 */
	public String getDay(String time) {
		String[] data = time.split("-");
		if (data.length > 1) {
			data = data[2].split(" ");
			if (data.length > 0) {
				return data[0];
			}
		}
		return "";
	}
	
	/**
	 * 提取 yyyy
	 * 参数格式:"yyyy-MM-dd HH:mm:ss"
	 */
	public String getYear(String time) {
		String[] data = time.split("-");
		if (data.length > 1) {
			return data[0];
		}
		return "";
	}
	
	/**
	 * 提取 MM
	 * 参数格式:"yyyy-MM-dd HH:mm:ss"
	 */
	public String getMonth(String time) {
		String[] data = time.split("-");
		if (data.length > 1) {
			return data[1];
		}
		return "";
	}
	
	/**
	 * 判断是否为两周以内
	 * 以第一个参数为参照
	 * 参数格式:"yyyy-MM-dd HH:mm:ss"
	 */
	public boolean isInTwoWeeks(String today, String time) {
		
		String twoWeeks = null;
		int iTodayYear = Integer.valueOf(getYear(today)).intValue();				
		int iTodayMonth = Integer.valueOf(getMonth(today)).intValue();
		int iTodayDay = Integer.valueOf(getDay(today)).intValue();
		
		// 月份起始从0开始
		iTodayMonth = (iTodayMonth -1 + 12)%12;
		
		// 用today设置cal
		Calendar cal = Calendar.getInstance();
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");	    
	    cal.set(iTodayYear, iTodayMonth, iTodayDay);
	    
	    // 计算得到twoWeeks
	    cal.add(Calendar.DATE, -14);
	    twoWeeks = dateFormat.format(cal.getTime());
	    
	    // 比较tiem与twoWeeks是否相等
	    String time_twoWeeks = time.split(" ")[0];
	    if (twoWeeks.compareTo(time_twoWeeks) > 0) {
	    	return false;
	    }
	    return true;
	}
}
