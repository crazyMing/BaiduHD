package com.baidu.hd.task;

interface ListStateChanger {

	public enum Type {
		eRegular,
		ePlay,
		ePause,
	}
	
	void change(Type type);
}
