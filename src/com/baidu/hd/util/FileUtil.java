package com.baidu.hd.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

public class FileUtil {
	
	private static final String TAG = "FileUtil";

	public static String read(String fileFullName) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileFullName));
			String line = "";
			while((line = br.readLine()) != null) {
				sb.append(line + "\r\n");
			}
			br.close();
			
		} catch(FileNotFoundException e) {
			Log.d(TAG, "FileNotFoundException " + fileFullName);
		} catch(IOException e) {
			Log.d(TAG, "IOException " + fileFullName);
		}
		return sb.toString().trim();
	}
	
	public static void write(String fileFullName, String message, boolean append) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(fileFullName, append);
			writer.write(message);
			writer.flush();

		} catch(FileNotFoundException e) {
			Log.d(TAG, "FileNotFoundException " + fileFullName);
			return;
		} catch(IOException e) {
			Log.d(TAG, "IOException " + fileFullName);
			return;
		} finally {
			try {
				if(writer != null) {
					writer.close();
				}
			} catch(IOException e) {
				Log.d(TAG, "IOException " + fileFullName);
			}
		}
	}
	
	public static void removePathAsync(String path) {
		new FileDeleter().execute(path);
	}
	
	public static void removePathSync(String path) {
		removePath(path);
	}
	
	public static String filterName(String value) {
		String ret = value.replaceAll("[: /\\*?\"<>|]*", "");
		if("".equals(ret)) {
			return "null";
		} else {
			return ret;
		}
	}
	
	private static void removePath(String value) {
		try {
			File path = new File(value);
			if(!path.exists()) {
				return;
			}
			for(File file: path.listFiles()) {
				file.delete();
			}
			path.delete();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	static class FileDeleter extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... arg0) {
			removePath(arg0[0]);
			return null;
		}	
	}
}
