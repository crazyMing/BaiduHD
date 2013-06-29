package com.baidu.hd.personal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;
import android.os.StatFs;

public class SDCardUtil
{
	public interface ScanEvent
	{
		void found(String path);

		boolean isCancel();
	}

	private static final String RMVB = "rmvb";
	private static final String MP4 = "mp4";
	private static final String THREEGP = "3gp";
	private static final String M3U8 = "m3u8";
	private static final String MKV = "mkv";
	private static final String MOV = "mov";
	private static final String RM = "rm";
	
	/*
	 * �жϸ��ļ������Ƿ�֧��
	 */
	public boolean isSupported(String fullName) {
		
		int index = fullName.lastIndexOf('.');
		String extString = fullName.substring(index + 1);
		if (extString.equalsIgnoreCase(MP4)  	|| 
			extString.equalsIgnoreCase(RMVB) 	|| 
			extString.equalsIgnoreCase(THREEGP) || 
			extString.equalsIgnoreCase(M3U8)	|| 
			extString.equalsIgnoreCase(MKV) 	|| 
			extString.equalsIgnoreCase(MOV) 	||  
			extString.equalsIgnoreCase(RM)) 
		{
			return true;
		}
		return false;
	}
	
	/*
	 * ��ȡ��չ��
	 */
	public String getFileNameExtra(final File file) {
		
		if (file == null) return null;
		
		String fullName = file.getPath();
		int index = fullName.lastIndexOf('.');
		String extString = fullName.substring(index + 1);
		extString.toUpperCase();
		return extString;
	}
	
	private String SDPath = "";

	private static SDCardUtil instance = new SDCardUtil();

	public static SDCardUtil getInstance()
	{
		return instance;
	}

	private SDCardUtil()
	{
		SDPath = Environment.getExternalStorageDirectory() + "/";
	}
	
	public String getSDCardRootDir() {
		return Environment.getExternalStorageDirectory().toString();
	}

	public List<String> getAllFiles(ScanEvent event)
	{
		List<String> result = new ArrayList<String>();
		this.getAllFiles(new File(SDPath), result, event);
		return result;
	}
	
	public List<String> getAllFiles(ScanEvent event, List<String> dirPathList)
	{
		if (dirPathList == null) {
			return getAllFiles(event);
		}
		else if (dirPathList.size() == 0) {
			return null;
		}
		else {
			List<String> result = new ArrayList<String>();
			for (String path : dirPathList) {
				File file = new File(path);
				if (!file.exists()) continue;
				else if (file.isFile() && isSupported(path)) { 
					// ������ļ������
					result.add(path);
					if (event != null) {
						event.found(path);
					}
				}
				else { 
					// ������ļ����г��ļ�
					this.getAllFiles(new File(path), result, event);
				}
			}
			return result;
		}
	}

	/*
	 * �ж��ļ��Ƿ����
	 */
	public boolean isFileExist(String name)
	{
		File file = new File(name);
		return file.exists();
	}

	/*
	 * ɾ���ļ�
	 */
	public boolean delFiles(String name)
	{
		File file = new File(name);
		if (file.exists()) {
			return file.delete();
		}
		else {
			return false;
		}
	}

	/*
	 * ����SD��
	 */
	private void getAllFiles(File root, List<String> item, ScanEvent event)
	{
		File files[] = root.listFiles();
		if (files != null) {
			for (File f : files) {
				if (event != null) {
					if (event.isCancel()) {
						break;
					}
				}
				if (f.isDirectory()) {
					getAllFiles(f, item, event);
				}
				else {
					String fileFullName = f.toString();
					int index = fileFullName.lastIndexOf('.');
					String extString = fileFullName.substring(index + 1);
					if (extString.equalsIgnoreCase(MP4) || extString.equalsIgnoreCase(RMVB) || extString.equalsIgnoreCase(THREEGP) || extString.equalsIgnoreCase(M3U8)
							|| extString.equalsIgnoreCase(MKV) || extString.equalsIgnoreCase(MOV) ||  extString.equalsIgnoreCase(RM))
					{
						item.add(fileFullName);
						if (event != null)
						{
							event.found(fileFullName);
						}
					}
				}
			}
		}
	}
	
	/**
	 * ���SD���Ƿ����
	 */
	public boolean isMediaMounted() {
		boolean mounted = false;
		try {
			mounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mounted;
	}
	
	/*
	 * ����SD�����ÿռ� GB ��λ
	 * -1 �������ʧ��  
	 */
	public double getAvailableSize(){
		
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath()); 
			long blockSize = stat.getBlockSize(); 
			long availableBlocks = stat.getAvailableBlocks();
			return (double)availableBlocks * blockSize / 1024 /1024 /1024;
		}
		
		return -1;
	}
	
	/*
	 * ����SD���ܴ�С GB ��λ 
	 * -1 �������ʧ��
	 */
	public double getAllSize(){
		
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath()); 
			long blockSize = stat.getBlockSize(); 
			long allBlocks = stat.getBlockCount();
			return (double)allBlocks * blockSize / 1024 /1024 /1024;
		}
		
		return -1;
		
	}
}
