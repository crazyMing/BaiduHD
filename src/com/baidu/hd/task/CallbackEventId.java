package com.baidu.hd.task;

public class CallbackEventId {

	// 不向外抛事件
	public static final int OnQuery = 1;

	// 向外抛事件
	public static final int OnCreate = 10;
	public static final int OnRemove = 11;
	public static final int OnStart = 12;
	public static final int OnStop = 13;
	public static final int OnComplete = 14;
	public static final int OnQueue = 15;
	public static final int OnPlay = 16;
	public static final int OnError = 17;	// 只是下载过程中出错了才使用(eg:onQuery)，执行结果错了不使用(eg:start)
}
