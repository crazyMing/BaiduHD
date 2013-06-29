package com.baidu.hd.task;

import com.baidu.hd.module.P2PBlock;
import com.baidu.hd.module.Task;

class EmptyTaskHandler implements TaskHandler {

	@Override
	public void create(TaskManagerAccessor accessor) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void clearHandle(Task task) {
	}

	@Override
	public void forceStart(Task task) {
	}

	@Override
	public void create(Task task) {
	}

	@Override
	public void remove(Task task) {
	}

	@Override
	public void start(Task task) {
	}

	@Override
	public void stop(Task task) {
	}

	@Override
	public void query(Task task) {
	}

	@Override
	public void error(Task task) {
	}

	@Override
	public void queue(Task task) {
	}

	@Override
	public void startPlay(Task task) {
	}

	@Override
	public void endPlay(Task task) {
	}

	@Override
	public boolean needRealStart(Task task) {
		return false;
	}

	@Override
	public boolean isFileExist(Task task) {
		return false;
	}

	@Override
	public P2PBlock getBlock(Task task) {
		return null;
	}

	@Override
	public void startup(Task task) {
	}

	@Override
	public void shutdown(Task task) {
	}

	@Override
	public void onSuccess(int eventId, Task task) {
	}

	@Override
	public void onError(int eventId, Task task, int errorCode) {
	}

	@Override
	public void setProperty(Task inTask, Task paramTask) {
	}

	@Override
	public void setPostEventEnable(boolean value) {
	}
}
