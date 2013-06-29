package com.baidu.hd.task;

import com.baidu.hd.event.EventArgs;
import com.baidu.hd.module.Task;

/*
 * �����¼��Ĳ���
 * �����EventCenter
 */
public class TaskEventArgs extends EventArgs {

	private Task task = null;
	
	public Task getTask() {
		return task;
	}

	public TaskEventArgs(Task task) {
		this.task = task;
	}
}
