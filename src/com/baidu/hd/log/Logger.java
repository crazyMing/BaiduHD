package com.baidu.hd.log;

import com.baidu.hd.util.Const;

public class Logger {

	private static ConsoleLogger consoleLogger = new ConsoleLogger();
	private static FileLogger fileLogger = new FileLogger();
	private static BufferLogger bufferLogger = new BufferLogger();
	
	static class Level {
		static final int verbose = 1;
		static final int debug = 2;
		static final int info = 3;
		static final int warn = 4;
		static final int error = 5;
	}
	
	private String tag = "default";
	
	public Logger(String tag) {
		this.tag = tag;
	}
	
	public void i(String msg) {
		this.write(Level.info, msg);
	}
	
	public void d(String msg) {
		this.write(Level.debug, msg);
	}
	
	public void w(String msg) {
		this.write(Level.warn, msg);
	}
	
	public void e(String msg) {
		this.write(Level.error, msg);
	}
	
	public void v(String msg) {
		this.write(Level.verbose, msg);
	}

	public static void init() {
		fileLogger.init();
	}
	
	public static void flush() {
		fileLogger.flush();
	}
	
	public static String getFileName() {
		return fileLogger.getFileName();
	}

	private void write(int level, String msg) {
		consoleLogger.write(level, this.tag, msg);
		if(Const.isDebug) {
			fileLogger.write(level, this.tag, msg);
		} else {
			bufferLogger.write(level, this.tag, msg);
		}
	}
}






