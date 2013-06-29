package com.baidu.browser.net;

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/**
 * @ClassName: ProxyHttpClient 
 * @Description: 
 * 提供一个anroid平台自动填充apn proxy的 HttpClient.
 * 自动填充 proxy 设置，不需要使用者再次关心wap net情况。
 * @author LEIKANG 
 * @date 2012-12-12 下午1:52:54
 */
public class ProxyHttpClient extends DefaultHttpClient {
    
    /** log tag. */
    private static final String TAG = ProxyHttpClient.class.getSimpleName();
    
    /** log debug on/off .*/
    private static final boolean DEBUG = false;
    
    /** apn proxy. */
    private String mProxy;
    
    /** proxy port. */
    private String mPort;
    
    /** 当前网络是否使用wap。
     *  wifi 或者 cmnet等为不使用 wap.
     */
    private boolean mUseWap;
    
    /** http 超时。 */
    private static final int HTTP_TIMEOUT_MS = 30000;

    /** 用于检查是否发生没有释放(close)的情况. */
    private RuntimeException mLeakedException = new IllegalStateException(
                                                        "ProxyHttpClient created and never closed");
    
    /**
     * constructor.
     * @param context  context
     */
    public ProxyHttpClient(Context context) {
        this(context, null, null);
    }
    
    /**
     * 构造函数.
     * @param context application context
     * @param userAgent  useragent you want to set 
     */
    public ProxyHttpClient(Context context, String userAgent) {
        this(context, userAgent, null);
    }
    
    /**
     * 构造函数.
     * @param context application context
     * @param connectManager ConnectManager
     */
    public ProxyHttpClient(Context context, ConnectManager connectManager) {
        this(context, null, connectManager);
    }
    
    /**
     * 构造函数.
     * @param context application context
     * @param userAgent  useragent you want to set 
     * @param connectManager ConnectManager
     */
    public ProxyHttpClient(Context context, String userAgent, ConnectManager connectManager) {
        
        // 检查网络类型 wifi /mobile & apn proxy
        ConnectManager cm = connectManager;
        if (cm == null) {
            cm = new ConnectManager(context);
        }
        
        mUseWap = cm.isWapNetwork();
        mProxy = cm.getProxy();
        mPort = cm.getProxyPort();
        
        if (DEBUG) {
            Log.d(TAG, "wap :" + mUseWap + " " + mProxy + " " + mPort);
        }
        
        if (mUseWap) {
            //如果使用wap 网络，需要设置代理，没有的话直连.
            HttpHost proxy = new HttpHost(mProxy, Integer.valueOf(mPort));
            getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
        }

        HttpConnectionParams.setConnectionTimeout(getParams(), HTTP_TIMEOUT_MS);
        HttpConnectionParams.setSoTimeout(getParams(), HTTP_TIMEOUT_MS);
        HttpConnectionParams.setSocketBufferSize(getParams(), 8192); // SUPPRESS CHECKSTYLE
        
        if (!TextUtils.isEmpty(userAgent)) {
            HttpProtocolParams.setUserAgent(getParams(), userAgent);
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mLeakedException != null) {
            Log.e(TAG, "Leak found", mLeakedException);
        }
    }
    
    /**
     * Release resources associated with this client.  You must call this,
     * or significant resources (sockets and memory) may be leaked.
     */
    public void close() {
        if (mLeakedException != null) {
            getConnectionManager().shutdown();
            mLeakedException = null;
        }
    }
    
    @Override
    protected HttpParams createHttpParams() {
        HttpParams params = super.createHttpParams();
        HttpProtocolParams.setUseExpectContinue(params, false);
        return params;
    }
}
