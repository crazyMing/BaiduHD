package com.baidu.hd.stat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.content.res.AssetManager;

/**
 * 更新记录
 * 
 * 1.2 获取到渠道号后，进行了trim()处理
 * 1.1 增加了对channel id的缓存，避免了频繁的文件读写
 * @author WangYSH
 * @date 2011-11-17
 * */
public class MarketChannelHelper {
    
    private static String CHANNEL_PATH; /** 渠道号存储在本地安装目录的路*/
    private static final String FILE_NAME = "channel";  /** 存储渠道号的文件*/
    private Context mContext;
    private static MarketChannelHelper mInstance;
    private static String mChannelId = null;
    
    public static synchronized MarketChannelHelper getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new MarketChannelHelper(context);
            mChannelId = null;
        }
        
        return mInstance;
    }
    
    private MarketChannelHelper(Context context) {
        CHANNEL_PATH = "/data/data/"+context.getPackageName()+"/";
        mContext = context;
    }
    
    public String getChannelID() {
        if(mChannelId != null) {
            return mChannelId;
        }
        
        File f = new File(CHANNEL_PATH + FILE_NAME);
        if (f.exists()) {
            mChannelId = getChannelIDFromLocal().trim();
        } else {
            mChannelId = getChannelIDFromPackage().trim();
        }

        if("".equals(mChannelId)) {
        	mChannelId = "757b";
        }
        return mChannelId;
    }

    /**
     * 浠庡畨瑁呭簲鐢ㄧ殑APK鍖呴噷璇诲彇娓犻亾鍙�?     * 骞跺皢娓犻亾鍙蜂俊鎭啓鍒版湰鍦扮殑瀹夎鐩綍涓�
     */
    private String getChannelIDFromPackage() {

        String channel = "";
        try {
            AssetManager assetManager = mContext.getAssets();
            InputStream is = assetManager.open(FILE_NAME);

            int length = is.available();
            byte[] buffer = new byte[length];
            is.read(buffer);
            
            /** 浠嶢PK涓鍒跺埌瀹夎鐩綍涓�*/
            File file = new File(CHANNEL_PATH + FILE_NAME);
            FileOutputStream os = new FileOutputStream(file);
            os.write(buffer);
            
            channel = EncodingUtils.getString(buffer, "UTF-8");
            
            os.close();
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return channel;
    }

    /**
     * 浠庢湰鍦板畨瑁呯洰褰曚笅鑾峰彇娓犻亾鍙�
     */
    private String getChannelIDFromLocal() {

        String channel = null;
        File f = new File(CHANNEL_PATH + FILE_NAME);
        try {
            InputStream in = new FileInputStream(f);
            BufferedReader bin = new BufferedReader(new InputStreamReader(in));
            channel = bin.readLine().toString();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (channel == null) {
            /** this should not be happened */
            return getChannelIDFromPackage();
        } else {
            return channel;
        }
    }
    
}
