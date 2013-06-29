package com.baidu.hd.module;

import java.io.File;

import android.content.Context;

import com.baidu.hd.R;

/**
 * 文件夹ListView数据项
 * @author sunjianshun
 *
 */
public class FolderItemPackage extends ItemPackage {

	private Context mContext = null;
	private File mFile = null;
	private String mFileName = null;
	private int mIconResId;
	
	public FolderItemPackage(Context context) {
		mContext = context;
		mIconResId = R.drawable.ic_folder;
	}
	
	public void setFile(File file) {
		mFile = file;
		
		if (mFile == null) {
			mFileName = mContext.getString(R.string.local_folder_to_parent);
		}
		else {
			mFileName = mFile.getName();
		}
		
	}
	
	public String getFolderName() {
		if (mFile!=null && mFile.isFile()) return null;
		return mFileName;
	}
	
	public String getFileName(){
		return mFileName;
	}
	
	public int getIconResId() {
		return mIconResId;
	}
	
	public boolean isToParent() {
		if (mFile == null) {
			return true;
		}
		return false;
	}
	
	public File getFile() {
		return mFile;
	}
}
