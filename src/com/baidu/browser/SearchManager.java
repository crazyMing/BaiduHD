package com.baidu.browser;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.baidu.browser.db.HistoryConfig;
import com.baidu.hd.MainActivity;
import com.baidu.hd.VoiceSearchActivity;
import com.baidu.hd.module.IntentConstants;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.SystemUtil;
import com.baidu.hd.util.Turple;

/**
 * @ClassName: SearchManager 
 * @Description: 从其他位置调起浏览器，搜索页 或以后的 widget ，打开html文件等方式
 * @author LEIKANG 
 * @date 2012-12-12 下午5:50:41
 */
public final class SearchManager {
	
	/** 搜索或loadurl时 intent tag */
	public static final String TAG_KEY_URL = "key_url";
	
	public static final String searchUrl = "http://www.baidu.com/s?from=%s&tn=%s&word=%s";
	
	public static final String keywordUrl = "http://nssug.baidu.com/su?prod=iphone_video&callback=SuggestionService.defaultDataProcessor&wd=%s";
	
	
    /**
     * 发起搜索，调用浏览器发起网络搜索，或者调用本地搜索。
     * @param context context
     * @param query 要搜索的关键词
     * @param type 搜索类型 
     * @param voiceSuggestions 语音搜索建议列表
     * @param isFromWidgetVoiceSearch 是否是从widget的语音搜索发起
     * @param searchSource 搜索来源
     */
    public static void launchSearch(Context context, String query) {
        String url = null;
        if (!TextUtils.isEmpty(query)) 
            addWebSearchHistory(query, context);
        url = getSearchUrl(query, context);

        // 调用浏览器，发起搜索
        if (url != null) {
            Bundle extras = new Bundle();
            extras.putString(TAG_KEY_URL, url);
            startBrowser(context, extras);
        }        
    }
    
    /**
     * 语音识别结束后，发起语音搜索
     * @Title: launchVoiceSearch 
     * @Description: voiceFrom 标记语音从什么位置打开，便于分别处理
     * @param @param context
     * @param @param query
     * @param @param voiceFrom    设定文件 
     * @return void    返回类型 
     * @throws
     */
    public static void launchVoiceSearch(Context context, String query, String[] voiceSuggestions, int voiceFrom) {
        String url = null;
        if (!TextUtils.isEmpty(query)) 
            addWebSearchHistory(query, context);
        url = getSearchUrl(query, context);

        // 调用浏览器，发起搜索,标记语音位置
        if (url != null) {
            Bundle extras = new Bundle();
            extras.putString(TAG_KEY_URL, url);
            extras.putInt(VoiceSearchActivity.TAG_VOICE_START_FROM, voiceFrom);
            extras.putStringArray(VoiceSearchActivity.TAG_VOICE_RESULT_, voiceSuggestions);
            startBrowser(context, extras);
        } 
    }
    
    /**
     * @Title: launchURL 
     * @Description: 直接加載URL 
     * @param context
     * @param url   
     */
    public static void launchURL(Context context, String url) {
        if (url != null) {
            Bundle extras = new Bundle();
            extras.putString(TAG_KEY_URL, url);
            startBrowser(context, extras);
        } 
    }
    
    /**
     * @Title: startBrowser 
     * @Description:调起浏览器 
     * @param context
     * @param extras   
     */
    public static void startBrowser(Context context, Bundle extras) {
        Intent intent = getBrowserStartIntent(context, extras);
        if ((context instanceof MainActivity)) {
            MainActivity activity = (MainActivity) context;
            FragmentManager manager = activity.getSupportFragmentManager();
            BPBrowser searchBrowser = (BPBrowser) manager.findFragmentByTag(BPBrowser.FRAGMENT_TAG);
            if (searchBrowser != null) {
                searchBrowser.initFromIntent(intent);
            }
        } else {
            context.startActivity(intent);
        }
    }
    
    /**
     * 获取启动浏览页的intent.
     * @param extras Extras.
     * @return Intent.
     */
    public static Intent getBrowserStartIntent(Context context, Bundle extras) {
        Intent intent = new Intent(IntentConstants.ACTION_BROWSER);
        if (extras != null) {
            intent.putExtras(extras);
        }
        intent.setPackage(context.getPackageName());
        return intent;
    }
    
    /**
     * @Title: addWebSearchHistory 
     * @Description: 增加一个历史搜索记录
     * @param query
     * @param ctx   
     */
    public static void addWebSearchHistory(String query, Context ctx) {
        if (TextUtils.isEmpty(query.trim()) || HistoryConfig.isPrivateMode(ctx)) {
            return;
        }
        //Suggestion history = new Suggestion();
        //history.setText1(query);
        //history.setUserQuery(query);
        //history.setIntentQuery(query);
        //history.setSourceName(HistoryConfig.SOURCE_WEB);
        //history.setVersionCode("1");
        //HistoryControl.getInstance(ctx).insertHistoryInfo(history);
    }
    
    /**
     * add by LEIKANG 替换搜索地址为百度HD地址
     * @param word
     * @param context
     * @return
     */
	public static String getSearchUrl(String word, Context context) {
		Turple<Integer, Integer> size = SystemUtil.getResolution(context);
		//String ua = String.format("bdp_%d_%d_android_%s_a1", size.getX(), size.getY(), Build.VERSION.INCREMENTAL);
		//String ut = String.format("%s_%s_%d", Build.MODEL, Build.VERSION.RELEASE, Build.VERSION.SDK_INT);
		//String uid = String.format("bdp_%s|%s", "321323123", StringUtil.reverse(SystemUtil.getEmid(context)));
		//String pu = String.format("sz@401320_1001,osname@android,uid@%s,ua@%s,ut@%s",  StringUtil.encode(uid), StringUtil.encode(ua), StringUtil.encode(ut));
		String from = "1000324a";
		String tn = "baiduhd";
		
		String url = String.format(searchUrl, from, tn, StringUtil.encode(word));
		return url;
	}
	
}
