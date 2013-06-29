package com.baidu.hd.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * ���ڻ�ȡ���ָ�ʽ��ʱ�䣬����ģʽʵ��
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
     * ����"yyyy-MM-dd HH:mm:ss"��ʽ
     */
	public String getCurrentTime() {
		
		long timemillis = System.currentTimeMillis();   
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date(timemillis));
	}
	
	/**
	 * �����棺����ʱ�䡣�Ե�һ������Ϊ����
	 * ������ʽ:"yyyy-MM-dd HH:mm:ss"
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
	 * �����棺�������δ��ʱ�䡣�Ե�һ������Ϊ����
	 * ������ʽ:"yyyy-MM-dd HH:mm:ss"
	 */
	public boolean isTodayOrFuture(final String today, final String time) {
		
		if (isToday(today, time)) return true;
		else if (today.compareTo(time) < 0) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * �Ե�һ������Ϊ����
	 * ������ʽ:"yyyy-MM-dd HH:mm:ss"
	 */
	public boolean isYesterday(final String today, final String time) {
		// TODO 1.��ʽ���
		
		String yestoday = null;
		int iTodayYear = Integer.valueOf(getYear(today)).intValue();				
		int iTodayMonth = Integer.valueOf(getMonth(today)).intValue();
		int iTodayDay = Integer.valueOf(getDay(today)).intValue();
		
		// �·���ʼ��0��ʼ
		iTodayMonth = (iTodayMonth -1 + 12)%12;
		
		// ��today����cal
		Calendar cal = Calendar.getInstance();
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");	    
	    cal.set(iTodayYear, iTodayMonth, iTodayDay);
	    
	    // ����õ�yesterday
	    cal.add(Calendar.DATE, -1);
	    yestoday = dateFormat.format(cal.getTime());
	    
	    // �Ƚ�tiem��yesterday�Ƿ����
	    String time_yesterday = time.split(" ")[0];
	    return yestoday.equals(time_yesterday);	
	}
	
	/**
	 * �Ե�һ������Ϊ����
	 * ������ʽ:"yyyy-MM-dd HH:mm:ss"
	 */
	public boolean isEarliear(final String today, final String time) {
		// TODO �Ż�
		// �������죬���ǽ��죬����ֵС��today
		if (!isToday(today, time) && !isYesterday(today, time)) {
			return today.compareTo(time) > 0 ? true : false; 
		}
		return false;
	}
	
	/**
	 * ��ȡ dd
	 * ������ʽ:"yyyy-MM-dd HH:mm:ss"
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
	 * ��ȡ yyyy
	 * ������ʽ:"yyyy-MM-dd HH:mm:ss"
	 */
	public String getYear(String time) {
		String[] data = time.split("-");
		if (data.length > 1) {
			return data[0];
		}
		return "";
	}
	
	/**
	 * ��ȡ MM
	 * ������ʽ:"yyyy-MM-dd HH:mm:ss"
	 */
	public String getMonth(String time) {
		String[] data = time.split("-");
		if (data.length > 1) {
			return data[1];
		}
		return "";
	}
	
	/**
	 * �ж��Ƿ�Ϊ��������
	 * �Ե�һ������Ϊ����
	 * ������ʽ:"yyyy-MM-dd HH:mm:ss"
	 */
	public boolean isInTwoWeeks(String today, String time) {
		
		String twoWeeks = null;
		int iTodayYear = Integer.valueOf(getYear(today)).intValue();				
		int iTodayMonth = Integer.valueOf(getMonth(today)).intValue();
		int iTodayDay = Integer.valueOf(getDay(today)).intValue();
		
		// �·���ʼ��0��ʼ
		iTodayMonth = (iTodayMonth -1 + 12)%12;
		
		// ��today����cal
		Calendar cal = Calendar.getInstance();
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");	    
	    cal.set(iTodayYear, iTodayMonth, iTodayDay);
	    
	    // ����õ�twoWeeks
	    cal.add(Calendar.DATE, -14);
	    twoWeeks = dateFormat.format(cal.getTime());
	    
	    // �Ƚ�tiem��twoWeeks�Ƿ����
	    String time_twoWeeks = time.split(" ")[0];
	    if (twoWeeks.compareTo(time_twoWeeks) > 0) {
	    	return false;
	    }
	    return true;
	}
}
