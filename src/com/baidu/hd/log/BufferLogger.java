package com.baidu.hd.log;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Queue;

class BufferLogger {
	
	private static final int enableLevel = Logger.Level.debug;
	private static final int logCount = 1000;
	
	private SimpleDateFormat mTimeFmt = new SimpleDateFormat("hh:mm:ss");
	private Queue<String> mMsgs = new LinkedList<String>();
	
	public void write(int level, String tag, String msg) {
		
		if(level < enableLevel) {
			return;
		}
		
		synchronized (this.mMsgs) {
			this.mMsgs.offer(this.getLogMsg(level, tag, msg));
			if(this.mMsgs.size() > logCount) {
				this.mMsgs.poll();
			}
		}
	}

	public String getAllLogs() {
		
		Queue<String> queue = new LinkedList<String>();
		synchronized (this.mMsgs) {
			queue.addAll(this.mMsgs);
		}
		
		StringBuilder sb = new StringBuilder();
		while(true) {
			String msg = queue.poll();
			if(msg == null) {
				break;
			}
			sb.append(msg);
		}
		return sb.toString();
	}
	
	private String getLogMsg(int level, String tag, String msg) {
		
		return String.format("%s %s %s %s\r\n", 
				this.formatLevel(level),  
				mTimeFmt.format(new java.util.Date()).toString(), 
				this.formatTag(tag),
				msg);
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
