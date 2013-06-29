package com.baidu.player.download;

import android.os.Parcel;
import android.os.Parcelable;

public class JNITaskBuffer implements Parcelable {
	
	public static final int BufferSize = 16 * 1024;
	
	public byte[] szBuffer = new byte[BufferSize];
	private int nBlockSize = 0;
	
	public JNITaskBuffer() {
	}

	public byte[] getBuffer() {
		return szBuffer;
	}

	public void setBuffer(byte[] szBuffer) {
		this.szBuffer = szBuffer;
	}
	
	public int getBlockSize() {
		return nBlockSize;
	}

	private JNITaskBuffer(Parcel source) {
		this.readFromParcel(source);
	}

	public static final Creator<JNITaskBuffer> CREATOR = new Creator<JNITaskBuffer>() {
		
		@Override
		public JNITaskBuffer[] newArray(int size) {
			return new JNITaskBuffer[size];
		}
		
		@Override
		public JNITaskBuffer createFromParcel(Parcel source) {
			return new JNITaskBuffer(source);
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.nBlockSize);
		dest.writeByteArray(this.szBuffer);
	}
	
	public void readFromParcel(Parcel source) {
		this.nBlockSize = source.readInt();
		source.readByteArray(this.szBuffer);	
	}
}
