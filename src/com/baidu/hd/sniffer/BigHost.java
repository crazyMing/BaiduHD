package com.baidu.hd.sniffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetManager;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.net.HttpComm;
import com.baidu.hd.net.HttpResultCallback;
import com.baidu.hd.net.HttpResultCallback.HttpDownloaderResult;
import com.baidu.hd.util.UrlUtil;

/**
 * ��Ҫ������̽�Ĵ�վ�б�
 * @author sunjianshun
 *
 */
public class BigHost {

	private Context mContext = null;
	
	private int mVersion = 0;
	private List<String> mHosts = new ArrayList<String>();
	
	public void create(Context context) {
		mContext = context;

		String content = null;
		try {
			InputStream is = null;
			String newFile = context.getFilesDir().getAbsolutePath() + "/bighost";
			if(new File(newFile).exists()) {
				is = context.openFileInput("bighost");
			} else {
		        AssetManager assetManager = mContext.getAssets();
		        is = assetManager.open("sniffer/bighost");	
			}

	        int length = is.available();
	        byte[] buffer = new byte[length];
	        is.read(buffer);
	        content = new String(buffer);
		} catch(IOException e) {
			e.printStackTrace();
		}
		if(content == null || "".equals(content)) {
			return;
		}
		try {
			JSONObject obj = new JSONObject(content);
			mVersion = obj.optInt("version");
			JSONArray array = obj.optJSONArray("site");
			for(int i = 0; i < array.length(); ++i) {
				String site = array.getString(i);
				mHosts.add(site);
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}

	public boolean isBigHost(String url) {
		String host = UrlUtil.getHost(url);
		for(String site: mHosts) {
			if(host.endsWith(site)) {
				return true;
			}
		}
		return false;
	}
	
	public String getBigHost(String url) {
		String host = UrlUtil.getHost(url);
		for (int i = 0; i < mHosts.size(); i++) {
			if (host.endsWith(mHosts.get(i))) {
				return mHosts.get(i);
			}
		}
		return "";
	}
	
	/**
	 * ��δʹ��
	 */
	public void upgrade() {
		String server = "";
		new HttpComm().get(server, new HttpResultCallback() {
			
			@Override
			public void onResponse(HttpDownloaderResult result, String url, String message) {
				if(isDestroyed()) {
					return;
				}
				if(HttpDownloaderResult.eSuccessful != result) {
					return;
				}
				try {
					JSONObject obj = new JSONObject(message);
					int version = obj.optInt("version");
					if(version > mVersion) {
						saveToFile(message);
					}
				} catch(JSONException e) {
					e.printStackTrace();
				}

			}
			
			@Override
			public void onProgress(String url, float rate) {				
			}
		});
	}
	
	private void saveToFile(String content) {
		FileOutputStream fos = null;
		try {
			String newFile = mContext.getFilesDir().getAbsolutePath() + "/spec";
			fos = this.mContext.openFileOutput(newFile, Context.MODE_WORLD_WRITEABLE);
			fos.write(content.getBytes(), 0, content.length());
			fos.flush();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != fos) {
					fos.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private boolean isDestroyed() {
		return mContext == null || 
				!BaiduHD.cast(mContext).getServiceContainer().isCreated();
	}
}
