package com.baidu.browser.ui;

import java.net.URISyntaxException;
import java.net.URLDecoder;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * 一个工程中所有的 webview 都需要继承此类，或者调用 setSupportHtml5() 函数，所有用到的webview都需要支持html5.
 * 因为如果有部分没有支持html5的话，会存在 localstorage失败的问题。
 * 表现为：程序进程被杀死后，再起来，之前存储的locaostorage读取失败。
 * 重要：这个类中不要添加和业务逻辑相关的东西。
 * 
 * @author LEIKANG
 * @since 2012-12-04
 */
public class BaseWebView extends WebView {
    
    /** 缓存目录。 */
    public static final String APP_CACHE_PATH = "appcache";
    /** 数据库目录。 */
    public static final String APP_DATABASE_PATH = "databases";
    /** 地理位置定位信息目录。 */
    public static final String APP_GEO_PATH = "geolocation";

    /**
     * constructor.
     * @param context Context
     * @param attrs AttributeSet
     * @param defStyle int
     */
    public BaseWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        init(context);
    }

    /**
     * constructor.
     * @param context Context
     * @param attrs AttributeSet
     */
    public BaseWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(context);
    }

    /**
     * constructor. 
     * @param context Context
     */
    public BaseWebView(Context context) {
        super(context);
        
        init(context);
    }
    
    /**
     * init 
     * @param context Context
     */
    private void init(Context context) {
        setDefaultWebSettings(context, this);
        setSupportHtml5(context, this);
        removeInitialFocus(context, this);
        setWebViewClient(new BaseWebViewClient());
    }
    
    /**
     * 去掉初始的焦点，解决焦点乱窜的问题
     * @param context Context
     * @param webView WebView
     */
    public static void removeInitialFocus(Context context, WebView webView) {
        WebSettings s = webView.getSettings();
        if (s != null)
        	s.setNeedInitialFocus(false);
    }

    /**
     * 设置支持 html 5
     * 
     * 一个工程中所有的 webview，都需要调用此函数，或者集成此类。所有用到的webview都需要支持html5.
     * 因为如果有部分没有支持html5的话，会存在 localstorage失败的问题。
     * 
     * @param context Context
     * @param webview WebView
     */
    public static void setSupportHtml5(Context context, WebView webview) {
        WebSettings s = webview.getSettings();
        if (s!=null) {
        s.setAppCacheEnabled(true);
        s.setDatabaseEnabled(true);
        s.setDomStorageEnabled(true);
        s.setGeolocationEnabled(true);
        s.setJavaScriptEnabled(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        
        String databasePath = context.getDir(APP_DATABASE_PATH, 0).getPath();
        String geolocationDatabasePath = context.getDir(APP_GEO_PATH, 0).getPath();
        String appCachePath = context.getDir(APP_CACHE_PATH, 0).getPath();
        s.setGeolocationDatabasePath(geolocationDatabasePath);
        s.setDatabasePath(databasePath);
        s.setAppCachePath(appCachePath);
        
        s.setSupportZoom(true);
        }
    }
    
    
    
    @Override
	public boolean onTouchEvent(MotionEvent event) {
        if (!this.isFocused()) {
            this.requestFocus();
        }
		return super.onTouchEvent(event);
	}

	/**
     * 设置默认的WebSettings支持
     * 
     * 一般性的WebView的设置，比如js支持，localstorage支持等
     * 
     * @param context Context
     * @param webview WebView
     */
    public static void setDefaultWebSettings(Context context, WebView webview) {
        WebSettings s = webview.getSettings();
        if (s!=null) {
        	// 开启js功能
        	s.setJavaScriptEnabled(true);
        	// 开启js localstorage 功能
        	s.setDomStorageEnabled(true);
        }
    }
    
    /**
     * 处理特殊协议，比如拨电话，发短信等
     * @param context context
     * @param url 
     * @return 是否处理
     * @throws ActivityNotStartedException 启动Activity错误
     */
    public static boolean handleSpecialScheme(Context context, String url) throws ActivityNotStartedException {
        if (url.startsWith("wtai://")) {
            // 处理wap电话链接
            int start = "wtai://wp/wc;".length();
            if (url.length() > start) {
                String tel = url.substring(start);
                startDialer(context, tel);
                
                return true;
            }
        } else if (url.startsWith("sms:") || url.startsWith("smsto:")) {
            // 部分机型的短信处理有问题，自己处理
            sendSms(context, url);
            return true;
            
        } else if (startActivityFromUrl(context, url)) {
            // bugfix#SEARHBOX-907
            return true;
        }
        
        return false;
    }
    
    /**
     * 调用Dialer
     * 
     * @param phoneNumber
     *            电话号码
     * @throws ActivityNotStartedException 启动Activity错误
     */
    private static void startDialer(Context context, String phoneNumber) throws ActivityNotStartedException {
        phoneNumber = "tel:" + phoneNumber;
        Intent dialerIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber));
        dialerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(context, dialerIntent);
    }
    
    /**
     * 发送信息。
     * @param activity Activity
     * @param url 格式为 sms:xxxxx?body= 的URL
     * @throws ActivityNotStartedException 启动Activity异常
     * */
    private static void sendSms(Context context, String url) throws ActivityNotStartedException {
        try {
            String tel = null, body = null;
            if (url.startsWith("sms:")) {
                int index = url.indexOf('?');
                if (index < "sms:?".length()) {
                    tel = url.substring("sms:".length());
                    body = "";
                    
                } else {
                    tel = url.substring("sms:".length(), index);
                    index = url.indexOf("body=");
                    if (index > -1) {
                        body = url.substring(index + "body=".length());
                        if (!TextUtils.isEmpty(body)) {
                            body = URLDecoder.decode(body, "UTF-8");
                        }
                    }
                }
                
            } else if (url.startsWith("smsto:")) {
                int index = url.indexOf('?');
                if (index < "smsto:?".length()) {
                    tel = url.substring("smsto:".length());
                    body = "";
                    
                } else {
                    tel = url.substring("smsto:".length(), index);
                    index = url.indexOf("body=");
                    if (index > -1) {
                        body = url.substring(index + "body=".length());
                        if (!TextUtils.isEmpty(body)) {
                            body = URLDecoder.decode(body, "UTF-8");
                        }
                    }
                }
            }
            
            Intent it = new Intent(Intent.ACTION_VIEW);
            it.putExtra("address", tel);
            it.putExtra("sms_body", body);
            it.setType("vnd.android-dir/mms-sms");
            startActivity(context, it);
            
        } catch (ActivityNotStartedException e) {
            throw e;
            
        } catch (Throwable t) {
            t.printStackTrace();
        }   
    }
    
    /**
     * 安全启动应用程序，截获Exception
     * 
     * @param activity
     *            Context
     * @param intent
     *            Intent
     * @throws ActivityNotStartedException 启动Activity错误
     * */
    private static void startActivity(Context context, Intent intent) throws ActivityNotStartedException {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ActivityNotStartedException ae = new ActivityNotStartedException();
            ae.initCause(e);
            throw ae;
        } catch (SecurityException e) {
            ActivityNotStartedException ae = new ActivityNotStartedException();
            ae.initCause(e);
            throw ae;
        }
    }
    
    /**
     * 通过url打开应用程序，处理类似 gel:xx, 或者 intent: xxx 的url. tel:, sms:, mailto
     * 
     * @param url
     *            要打开的url
     * @return 是否能够处理。
     * @throws ActivityNotStartedException 
     */
    private static boolean startActivityFromUrl(Context context, String url) throws ActivityNotStartedException {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        
        // http https自己消化，不让别人处理。这样也有个弊端，别人注册的这种intent，无法得到处理。
        if (url.startsWith("http:") || url.startsWith("https:")) {
            return false;
        }
        
        Intent intent;
        // perform generic parsing of the URI to turn it into an Intent.
        try {
            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
        } catch (URISyntaxException ex) {
            Log.w("Browser", "Bad URI " + url + ": " + ex.getMessage());
            return false;
        }
        
        // whether we can download it from the Market.
        ResolveInfo ri = context.getPackageManager().resolveActivity(intent, 0);
        if (ri == null) {
            String packagename = intent.getPackage();
            if (packagename != null) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:"
                        + packagename));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                // modify begin by caohaitao 20120727 , 解决 本地没有market程序导致crash.
                try {
                    context.startActivity(intent);
                    return true;
                } catch (ActivityNotFoundException ex) {
                    ActivityNotStartedException ae = new ActivityNotStartedException();
                    ae.initCause(ex);
                    throw ae;
                }
                // modify end by caohaitao
                
            } else {
                return false;
            }
        }
        
        // sanitize the Intent, ensuring web pages can not bypass browser
        // security (only access to BROWSABLE activities).
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setComponent(null);
        try {
            if (context instanceof Activity) {
                Activity act = (Activity) context;
                if (act.startActivityIfNeeded(intent, -1)) {
                    return true;
                }
            }
        } catch (ActivityNotFoundException ex) {
            ActivityNotStartedException ae = new ActivityNotStartedException();
            ae.initCause(ex);
            throw ae;
            // ignore the error. If no application can handle the URL,
            // eg about:blank, assume the browser can handle it.
        }
        
        return false;
    }
    
    /**
     * @param client 需传入{@link BaseWebViewClient}的对象
     */
    @Override
    public void setWebViewClient(WebViewClient client) {
        
        // check
        if (client == null || !(client instanceof BaseWebViewClient)) {
            throw new RuntimeException("WebViewClient must be extended from BaseWebViewClient!!");
        }
        
        super.setWebViewClient(client);
    }
    
    /**
     * 默认的WebViewClient实现基类，默认对https请求的非授信SSL证书采取信任的处理
     */
    public static class BaseWebViewClient extends WebViewClient {
        
        /**
         * 默认信任ssl证书
         */
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
        
        /**
         * 默认添加对拨电话，发短信等特殊协议的支持
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                return BaseWebView.handleSpecialScheme(view.getContext(), url);
            } catch (ActivityNotStartedException e) {
                // 交给webview默认处理
                return false;
            }
        }
        
    }
    
    /**
     * 启动Activity异常
     */
    public static class ActivityNotStartedException extends Exception {
        /** serialVersionUID */
        private static final long serialVersionUID = 1L;
        /**
         * 启动Activity异常
         */
        public ActivityNotStartedException() {
            super();
        }
    }
}
