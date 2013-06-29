package com.baidu.player.download;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.baidu.hd.util.StringUtil;

public class JNITaskInfo implements Parcelable {

	public byte[] szUrl = null;
	public byte[] szFileName = null; //文件名
	public long nFileLen = 0;      //文件大小
	
	public int nErrorCode = 0;   //错误码
	public int nStatus = 0;      //任务状态 
	public long nDownloadLen = 0;   //已下载大小
	public int nDownloadRate = 0;  //下载速度
	public int nDiskFiles = 0; // 文件分块数
	
	public JNITaskInfo() {
	}

	public String getUrl() {
		return StringUtil.bytes2String(szUrl);
	}
	public String getFileName() {
		return StringUtil.bytes2String(szFileName);
	}
	public int getState() {
		return nStatus;
	}
	public long getTotalSize() {
		return nFileLen;
	}
	public long getDownloadedSize() {
		return nDownloadLen;
	}
	public int getErrorCode() {
		return nErrorCode;
	}
	public int getSpeed() {
		return nDownloadRate;
	}
	public int getDiskFiles() {
		return nDiskFiles;
	}
	
	@Override
	public String toString() {
		
		try {
			JSONObject o = new JSONObject();
			o.put("url", getUrl());
			o.put("fileName", getFileName());
			o.put("state", nStatus);
			o.put("errorCode", nErrorCode);
			o.put("totalSize", nFileLen);
			o.put("downloadedSize", nDownloadLen);
			o.put("speed", nDownloadRate);
			o.put("diskFiles", nDiskFiles);
			return o.toString();
			
		} catch(JSONException e ){
			e.printStackTrace();
		}
		return "";
	}

	private JNITaskInfo(Parcel source) {
		readFromParcel(source);
	}

	public static final Creator<JNITaskInfo> CREATOR = new Creator<JNITaskInfo>() {
		
		@Override
		public JNITaskInfo[] newArray(int size) {
			return new JNITaskInfo[size];
		}
		
		@Override
		public JNITaskInfo createFromParcel(Parcel source) {
			return new JNITaskInfo(source);
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		String strUrl = StringUtil.bytes2String(szUrl);
		String strFileName = StringUtil.bytes2String(szFileName);
		dest.writeString(strUrl);
		dest.writeString(strFileName);
		dest.writeLong(nFileLen);
		dest.writeInt(nErrorCode);
		dest.writeInt(nStatus);
		dest.writeLong(nDownloadLen);
		dest.writeInt(nDownloadRate);
		dest.writeInt(nDiskFiles);
	}
	
	public void readFromParcel(Parcel source) {
		String strUrl = source.readString();
		String strFileName = source.readString();
		nFileLen = source.readLong();
		nErrorCode = source.readInt();
		nStatus = source.readInt();
		nDownloadLen = source.readLong();
		nDownloadRate = source.readInt();
		nDiskFiles = source.readInt();
		szUrl = strUrl.getBytes();
		szFileName = strFileName.getBytes();
	}
}
