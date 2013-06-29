package com.baidu.hd.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import android.os.Environment;

class FileLogger {
	
	private static final int enableLevel = Logger.Level.verbose;
	private static final int logCount = 100;
	
	private String mFileName = "";
	private Queue<String> msgs = new LinkedList<String>();
	
	// 因为日志之在一个进程中，所以只用一个锁就可以了
	private Object mLock = new Object();
	
	public void init() {
		
		String path = Environment.getExternalStorageDirectory() + "/baidu/player/log/";

		SimpleDateFormat dateformat = new SimpleDateFormat("MMddHHmmss");
		String timestamp = dateformat.format(new Date());
		this.mFileName = path + timestamp + ".plog";
		
		File dir = new File(path);
		if(!dir.exists()) {
			dir.mkdirs();
		}
	}
	
	public void write(int level, String tag, String msg) {
	
		if(level < enableLevel) {
			return;
		}

		SimpleDateFormat timeFmt = new SimpleDateFormat("hh:mm:ss:SSS");
		String formatMsg = String.format("%s %s %s %s\r\n", 
				this.formatLevel(level),  
				timeFmt.format(new java.util.Date()).toString(), 
				this.formatTag(tag),
				msg);
		
		synchronized (this.msgs) {
			this.msgs.add(formatMsg);
		}
		
		if(this.msgs.size() >= logCount) {
			this.flush();
		}
	}
	
	public String getFileName() {
		return this.mFileName;
	}
	
	public void flush() {
		
		Queue<String> queue = new LinkedList<String>();
		
		synchronized (this.msgs) {
			queue.addAll(this.msgs);
			this.msgs.clear();
		}
		if(queue.isEmpty()) {
			return;
		}
		
		synchronized (mLock) {
			FileLock lock = null;
			FileWriter writer = null;
			try {
				writer = new FileWriter(this.mFileName, true);
				for(String s: queue) {
					writer.write(s);
				}
				writer.flush();
	
			} catch(FileNotFoundException e) {
				return;
			} catch(IOException e) {
				return;
			} finally {
				try {
					if(writer != null) {
						writer.close();
					}
				} catch(IOException e) {
					e.printStackTrace();
				}
				try {
					if(lock != null) {
						lock.release();
					}
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private String formatLevel(int level) {
		switch(level) {
		case Logger.Level.verbose:
			return "V";
		case Logger.Level.debug:
			return "D";
		case Logger.Level.info:
			return "I";
		case Logger.Level.warn:
			return "W";
		case Logger.Level.error:
			return "E";
		default:
			return "U";
		}
	}

	private String formatTag(String tag) {
		
		String result = tag;
		if(result.length() > 10) {
			result = tag.substring(0 ,10);
		}
		if(result.length() < 10) {
			for(int i = 0; i < 10 - result.length(); ++i) {
				result += " ";
			}
		}
		return result;
	}
}
