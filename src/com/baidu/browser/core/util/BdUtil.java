package com.baidu.browser.core.util;

import android.content.Context;

import com.baidu.browser.core.BdCore;

/**
 * @ClassName: BdUtil 
 * @Description: BdUtil
 * @author LEIKANG 
 * @date 2012-12-11 下午5:20:45
 */
public final class BdUtil {

	/**
	 * Constructor
	 */
	private BdUtil() {
	}

	/**
	 * 获取dip对应的pix值
	 * 
	 * @param aContext
	 *            Context
	 * @param aValue
	 *            dip值
	 * @return dip对应的pix值
	 */
	public static int dip2pix(Context aContext, float aValue) {
		return Math.round(aValue * BdCore.getScreenDensity(aContext));
	}
	
	/**
	 * 从一个url中获取一个参数。
	 * 
	 * @param key 想要获得的参数的key。
	 * @param url 被查询的url 
	 * @return 返回原始参数。没有找到返回null.
	 */
    public static String getQueryParameter(String url, String key) {

        final String encodedKey = key;
        final int length = url.length();
        
        int start = 0;
        start = url.indexOf("?") + 1;
        
        do {
            int nextAmpersand = url.indexOf('&', start);
            int end = nextAmpersand != -1 ? nextAmpersand : length;

            int separator = url.indexOf('=', start);
            if (separator > end || separator == -1) {
                separator = end;
            }

            if (separator - start == encodedKey.length()
                    && url.regionMatches(start, encodedKey, 0, encodedKey.length())) {
                if (separator == end) {
                  return "";
                } else {
                  return (url.substring(separator + 1, end));
                }
            }

            if (nextAmpersand != -1) {
                start = nextAmpersand + 1;
            } else {
                break;
            }
        } while (true);
        
        return null;
    }
}
