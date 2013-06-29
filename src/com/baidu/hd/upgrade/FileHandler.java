package com.baidu.hd.upgrade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.baidu.hd.log.Logger;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.Security;

class FileHandler {
	
	private Logger logger = new Logger("FileHandler");
	
	private Context mContext = null;
	private Map<String, String> mInfo = new HashMap<String, String>();
	
	public FileHandler(Context context) {
		mContext = context;
	}

	public void set(Map<String, String> info) {
		mInfo = info;
	}
	
	public boolean checkLib() {
		return this.check(mContext.getFilesDir().getAbsolutePath() + "/../lib/");
	}
	
	public boolean checkFile() {
		return this.check(mContext.getFilesDir().getAbsolutePath() + "/");
	}
	
	public boolean checkFileTmp() {
		return this.check(mContext.getFilesDir().getAbsolutePath() + "/", Const.PlayerCoreTmpFileNameSubfix);
	}
	
	public boolean checkSDCard() {
		return check(Const.Path.PlayerCoreUpgradeFilePath);
	}
	
	public void deleteFile() {
		for(String name: mInfo.keySet()) {
			mContext.deleteFile(name);
		}
	}
	
	public void deleteFileTmp() {
		for(String name: mInfo.keySet()) {
			mContext.deleteFile(name + Const.PlayerCoreTmpFileNameSubfix);
		}
	}
	
	public void deleteSDCard() {
		File path = new File(Const.Path.PlayerCoreUpgradeFilePath);
		if(!path.exists()) {
			return;
		}
		for(File file: path.listFiles()) {
			file.delete();
		}
	}
	
	public void deleteAppSDCard() {
		File path = new File(Const.Path.AppUpgradeFilePath);
		if(!path.exists()) {
			return;
		}
		for(File file: path.listFiles()) {
			file.delete();
		}
	}
	
	public void createSDCardDir() {
		File path = new File(Const.Path.PlayerCoreUpgradeFilePath);
		if(!path.exists()) {
			if(!path.mkdirs()) {
				logger.d("create dir fail");
			}
		}
	}
	
	public boolean copySDCard() {
		String srcPath = Const.Path.PlayerCoreUpgradeFilePath;
		for(String name: mInfo.keySet()) {
			String srcFileFullName = srcPath + name;
			String destFileName = name + Const.PlayerCoreTmpFileNameSubfix;
			if(!this.copyFile(srcFileFullName, destFileName)) {
				return false;
			}
		}
		return true;
	}
	
	public int rename()
	{
		String path = mContext.getFilesDir().getAbsolutePath() + "/";
		for (Map.Entry<String, String> entry : mInfo.entrySet())
		{
			String name = entry.getKey();
			String srcFileName = path + name + Const.PlayerCoreTmpFileNameSubfix;
			String destFileName = path + name;
			if (new File(destFileName).exists())
			{
				if (!mContext.deleteFile(name))
				{
					logger.e("rename() delete fail " + name);
					return 1;
				}
			}
			File file = new File(srcFileName);
			if (!file.renameTo(new File(destFileName)))
			{
				logger.e("rename() rename fail " + name);
				return 2;
			}
		}
		return 0;
	}
	
	private boolean copyFile(String srcFileFullName, String destFileName) {
		InputStream inStream = null;
		FileOutputStream fos = null;

		try {
			File srcFile = new File(srcFileFullName);
			if (srcFile.exists()) {
				inStream = new FileInputStream(srcFile);
				fos = this.mContext.openFileOutput(destFileName, Context.MODE_WORLD_WRITEABLE);
				byte[] buffer = new byte[8192];
				int readed = 0;
				while ((readed = inStream.read(buffer)) != -1) {
					fos.write(buffer, 0, readed);
				}
				fos.flush();
				buffer = null;
			}
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != inStream) {
					inStream.close();
				}
				if (null != fos) {
					fos.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}
	
	private boolean check(String path) {
		return check(path, "");
	}
	
	private boolean check(String path, String subfix) {
		for(Map.Entry<String, String> entry: mInfo.entrySet()) {
			String name = entry.getKey();
			String md5 = entry.getValue();
			String fileFullName = path + name + subfix;
			
			if(!new File(fileFullName).exists()) {
				return false;
			}
			String md5_ = Security.getFileMD5(fileFullName);
			logger.d("md5 = " + md5);
			logger.d("md5_ = " + md5_);
			if(!md5.equalsIgnoreCase(md5_)) {
				return false;
			}
		}
		return true;
	}
}
