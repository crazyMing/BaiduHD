package com.baidu.browser.db;

import com.baidu.hd.log.Logger;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @ClassName: HistoryConfig 
 * @Description: 历史记录配置常量集合 
 * @author LEIKANG 
 * @date 2012-12-12 下午1:10:38
 */
public final class HistoryConfig {
	
	private static Logger logger = new Logger("HistoryConfig");
	
	public static final String PREFS_NAME = "settings";
	
	/** 隐私模式对应的preferebce key。 */
	public static final String PREFS_KEY_PRIVATE_MODE = "nohistory";
    
    /**一天的毫秒数。*/
    public static final long DAY_MILLIS = 86400000L;
    
    /**历史记录存在的时间期限（毫秒）。*/
    public static final long MAX_STAT_AGE_MILLIS = 30 * DAY_MILLIS;
    
    /**每秒的毫秒数。*/
    public static final long PER_SECOND = 1000;
    
    /**历史记录存在的时间期限（秒）。*/
    public static final long MAX_STAT_AGE_SECOND = MAX_STAT_AGE_MILLIS / PER_SECOND;
    
    /**点击记录的最低等级（点击次数）。*/
    public static final int MIN_CLICKS_FOR_SOURCE_RANKING = 1;
    
    /**所有资源类型的常量（查询时使用）。*/
    public static final String SOURCE_ALL = "all";
    
    /**所有本地资源类型的常量（查询时使用）。*/
    public static final String SOURCE_LOCAL = "local";
    
    /**应用资源类型的常量。*/
    public static final String SOURCE_APP = "app";
    
    /**Web资源类型的常量。*/
    public static final String SOURCE_WEB = "web";
    /** 记录设置是否更新。 */
    private static boolean settingsUpdated = true;
    /** 是否为隐私模式. */
    private static boolean isprivate = false;
    /** 隐藏构造方法，禁止获取实例 */
    private HistoryConfig() {
        
    }

    /**
     * 获取当前是否为隐私模式，如果是，将不显示历史记录，并且不会添加新的历史记录。
     * @param ctx context
     * @return true 是隐私模式，false 不是隐私模式。
     */
    public static boolean isPrivateMode(Context ctx) {
        if (settingsUpdated) {
            final SharedPreferences settings = ctx.getSharedPreferences(
            		PREFS_NAME, 0);
            isprivate = settings.getBoolean(PREFS_KEY_PRIVATE_MODE, false);
            settingsUpdated = false;
        }
        
        return isprivate;
    }

    /**
     * 设置是否为隐私模式。
     * @param ctx context
     * @param privateMode true 设置为隐私模式，false 设置为非隐私模式。
     */
    public static void setPrivateMode(Context ctx, boolean privateMode) {
    	logger.d("setPrivateMode privateMode = " + String.valueOf(privateMode));
    	
        final SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREFS_KEY_PRIVATE_MODE, privateMode);
        editor.commit();
        settingsUpdated = true;
    }
}
