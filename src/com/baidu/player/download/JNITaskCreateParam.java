package com.baidu.player.download;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;


public class JNITaskCreateParam implements Parcelable {
	
	public class FlagType {
		
		// 普通不分块
		public static final int NormalNoSplit = 1;
		
		// 流式（不区分分块）到文件
		public static final int StreamFile = 2;
		
		// 普通分块
		public static final int NormalSplit = 3;
		
		// 流式（不区分分块）到内存
		public static final int StreamMemory = 4;
	}

	private long nHandle = 0;
	private String strUrl = "";
	private String strRefer = "";
	private String strSavePath = "";
	
	// 仅文件名
	private String strFileName;
	
	private int nFlag;
	
	public JNITaskCreateParam() {
	}

	public long getHandle() {
		return nHandle;
	}

	public void setHandle(long nHandle) {
		this.nHandle = nHandle;
	}

	public String getUrl() {
		return strUrl;
	}

	public void setUrl(String strUrl) {
		this.strUrl = strUrl;
	}
	
	public String getRefer() {
		return strRefer;
	}

	public void setRefer(String strRefer) {
		this.strRefer = strRefer;
	}

	public String getSavePath() {
		return strSavePath;
	}

	public void setSavePath(String strSavePath) {
		this.strSavePath = strSavePath;
	}

	public String getFileName() {
		return strFileName;
	}

	public void setFileName(String strFileName) {
		this.strFileName = strFileName;
	}

	public int getFlag() {
		return nFlag;
	}

	public void setFlag(int nFlag) {
		this.nFlag = nFlag;
	}

	@Override
	public String toString() {
		
		try {
			JSONObject o = new JSONObject();
			o.put("fileName", this.strFileName);
			o.put("savePath", this.strSavePath);
			o.put("url", this.strUrl);
			o.put("flag", this.nFlag);
			o.put("handle", this.nHandle);
			return o.toString();
			
		} catch(JSONException e ){
			e.printStackTrace();
		}
		return "";
	}

	private JNITaskCreateParam(Parcel source) {
		this.readFromParcel(source);
	}

	public static final Creator<JNITaskCreateParam> CREATOR = new Creator<JNITaskCreateParam>() {
		
		@Override
		public JNITaskCreateParam[] newArray(int size) {
			return new JNITaskCreateParam[size];
		}
		
		@Override
		public JNITaskCreateParam createFromParcel(Parcel source) {
			return new JNITaskCreateParam(source);
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.nHandle);
		dest.writeString(this.strUrl);
		dest.writeString(this.strRefer);
		dest.writeString(this.strSavePath);
		dest.writeString(this.strFileName);
		dest.writeInt(this.nFlag);
	}
	
	public void readFromParcel(Parcel source) {
		this.nHandle = source.readLong();
		this.strUrl = source.readString();
		this.strRefer = source.readString();
		this.strSavePath = source.readString();
		this.strFileName = source.readString();
		this.nFlag = source.readInt();
	}
}
