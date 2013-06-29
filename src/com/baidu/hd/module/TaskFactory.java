package com.baidu.hd.module;

public class TaskFactory {

	public static Task create(int type) {
		
		switch(type) {
		case Task.Type.Small:
			return new SmallSiteTask();
		case Task.Type.Big:
			return new BigSiteTask();
		default:
			return null;
		}
	}
}
