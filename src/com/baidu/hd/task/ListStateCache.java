package com.baidu.hd.task;

import java.util.ArrayList;
import java.util.List;

import com.baidu.hd.module.Task;

class ListStateCache {

	private List<Package> mCacheTask = new ArrayList<Package>();

	public void add(Task value) {
		if(!contain(value)) {
			mCacheTask.add(new Package(value, value.getState()));
		}
	}
	
	public boolean contain(Task value) {
		for(Package pack: mCacheTask) {
			if(pack.getTask().isSame(value)) {
				return true;
			}
		}
		return false;
	}
	
	public void remove(Task value) {
		for(Package pack: mCacheTask) {
			if(pack.getTask().isSame(value)) {
				mCacheTask.remove(pack);
				break;
			}
		}
	}
	
	public void clear() {
		mCacheTask.clear();
	}
	
	public List<Package> getAll() {
		return mCacheTask;
	}
	
	public class Package {
		private int state = Task.getDefaultState();
		private Task task = null;
		public Package(Task task, int state) {
			this.task = task;
			this.state = state;
		}
		public int getState() {
			return state;
		}
		public Task getTask() {
			return task;
		}
	}
}
