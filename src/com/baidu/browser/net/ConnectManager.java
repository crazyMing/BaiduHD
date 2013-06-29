package com.baidu.browser.net;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

/**
 * @ClassName: ConnectManager 
 * @Description: 用于判断当前网络连接类型，接入点，代理服务器信息。 
 * @author LEIKANG 
 * @date 2012-12-12 下午1:51:31
 */
public class ConnectManager {
    
    /** log tag. */
    private static final String TAG = ConnectManager.class.getSimpleName();
    
    /** log debug on/off .*/
    private static final boolean DEBUG = false;
    
    /** current apn. */
    private String mApn;
    
    /** apn proxy. */
    private String mProxy;
    
    /** proxy port. */
    private String mPort;
    
    /** 网络类型字符串.*/
    private String mNetType; 
    
    /** 当前网络是否使用wap。
     *  wifi 或者 cmnet等为不使用 wap.
     */
    private boolean mUseWap;
    
    /** prefered apn url. */
    public static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
    
    /**
     * constructor.
     * @param context  context
     */
    public ConnectManager(Context context) {
        checkNetworkType(context);
    }
    
    /**
     * 检查当前系统apn 设置 状态.
     * @param context context
     */
    private void checkApn(Context context) {
        Cursor cursor = context.getContentResolver().query(
                PREFERRED_APN_URI,
                new String[] { "_id", "apn", "proxy", "port" }, null, null,
                null);
        
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int anpIndex = cursor.getColumnIndex("apn");
                int proxyIndex = cursor.getColumnIndex("proxy");
                int portIndex = cursor.getColumnIndex("port");
                
                mApn = cursor.getString(anpIndex);
                mProxy = cursor.getString(proxyIndex);
                mPort = cursor.getString(portIndex);
                
                mNetType = mApn;
                
                if (DEBUG) {
                    Log.d(TAG, "apn: " + " " + mApn + " " + mProxy + " " + mPort);
                }
                
                if (mProxy != null && mProxy.length() > 0) {
                    //如果设置了代理
                    
                    if ("10.0.0.172".equals(mProxy.trim())) {
                        // 当前网络连接类型为cmwap || uniwap
                        mUseWap = true;
                        mPort = "80";
                    } else if ("10.0.0.200".equals(mProxy.trim())) {
                        mUseWap = true;
                        mPort = "80";
                    } else {
                        // 否则为net
                        mUseWap = false;
                    }

                } else if (mApn != null) {
                    // 如果用户只设置了apn，没有设置代理，我们自动补齐
                    
                    String strApn = mApn.toUpperCase();
                    if (strApn.equals("CMWAP") || strApn.equals("UNIWAP")
                            || strApn.equals("3GWAP")) {
                        mUseWap = true;
                        mProxy = "10.0.0.172";
                        mPort = "80";
                    } else if (strApn.equals("CTWAP")) {
                        mUseWap = true;
                        mProxy = "10.0.0.200";
                        mPort = "80";
                    }

                } else {
                    // 其它网络都看作是net
                    mUseWap = false;
                }
                if (DEBUG) {
                    Log.d(TAG, "adjust apn: " + " " + mApn + " " + mProxy + " " + mPort);
                }
            }
            
            cursor.close();
        }        
    }
    
    /**
     * 检查当前网络类型。
     * @param context context
     */
    private void checkNetworkType(Context context) {
        
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null) {
            
            if (DEBUG) {
                Log.d(TAG, "network type : " + activeNetInfo.getTypeName().toLowerCase());
            }
            
            if ("wifi".equals(activeNetInfo.getTypeName().toLowerCase())) {
            	mNetType = "wifi";
                mUseWap = false;
            } else {
                checkApn(context);
            }
        }
    }
    
    /**
     * 当前网络连接是否是wap网络。
     * @return  cmwap 3gwap ctwap 返回true
     */
    public boolean isWapNetwork() {
        return mUseWap;
    }
    
    /**
     * 获取当前网络连接apn.
     * @return apn
     */
    public String getApn() {
        return mApn;
    }
    
    /**
     * 获得当前网络类型
     * @return wifi, cmnet, uninet, ctnet, cmwap ……
     */
    public String getNetType() {
    	return mNetType;
    }
    
    /**
     * 获取当前网络连接的代理服务器地址，比如 cmwap 代理服务器10.0.0.172.
     * @return 获取当前网络连接的代理服务器地址，比如 cmwap 代理服务器10.0.0.172
     */
    public String getProxy() {
        return mProxy;
    }
    
    /**
     * 获取当前网络连接的代理服务器端口，比如 cmwap 代理服务器端口 80.
     * @return 获取当前网络连接的代理服务器端口，比如 cmwap 代理服务器端口 80
     */
    public String getProxyPort() {
        return mPort;
    }
    
	/**
	 * 网络是否可用。
	 * @param context Context
	 * @return 连接并可用返回 true
	 */
	public static boolean isNetworkConnected(Context context) {
		NetworkInfo networkInfo = getActiveNetworkInfo(context);
		return networkInfo != null && networkInfo.isConnected();
	}

	/**
	 * 获取活动的连接。
	 * @param context Context
	 * @return 当前连接
	 */
	private static NetworkInfo getActiveNetworkInfo(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return null;
		}
		return connectivity.getActiveNetworkInfo();
	}
}
