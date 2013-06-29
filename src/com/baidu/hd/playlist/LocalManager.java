package com.baidu.hd.playlist;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.baidu.hd.db.DBReader;
import com.baidu.hd.db.DBWriter;
import com.baidu.hd.module.album.LocalVideo;
import com.baidu.hd.module.album.VideoFactory;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId;

class LocalManager {

	private ServiceFactory mServiceFactory = null;
	
	private List<LocalVideo> mLocalVideos = new ArrayList<LocalVideo>();
	
	private Handler mHandler = null;
	
	public void create(ServiceFactory serviceFactory) {
		this.mServiceFactory = serviceFactory;
		
		DBReader dbReader = (DBReader)this.mServiceFactory.getServiceProvider(DBReader.class);
		this.mLocalVideos = dbReader.getAllLocalVideo();
		
		Stat stat = (Stat)this.mServiceFactory.getServiceProvider(Stat.class);
		stat.setLocalVideoCount(this.mLocalVideos.size());
		stat.incEventValue(StatId.Startup.Name, StatId.Startup.LocalCount, mLocalVideos.size());
		
		HandlerThread thread = new HandlerThread("dbRemoveLocal");
		thread.start();
		
		this.mHandler = new Handler(thread.getLooper()) {

			@Override
			public void handleMessage(Message msg) {
				removeVideos(((Package)msg.obj).getVideos());
			}
		};
	}
	
	public boolean addLocal(String value) {
		DBWriter dbWriter = (DBWriter)this.mServiceFactory.getServiceProvider(DBWriter.class);
		if(this.findLocal(value) == null) {
			LocalVideo v = VideoFactory.create(true).toLocal();
			v.setFullName(value);
			dbWriter.modifyLocalVideo(v, DBWriter.Action.Add);
			synchronized (this.mLocalVideos) {
				this.mLocalVideos.add(v);
				return true;
			}
		}
		
		return false;
	}

	public void updateLocal(LocalVideo value) {
		LocalVideo v = this.findLocal(value.getFullName());
		if(v == null) {
			return;
		}
		
		v.setPosition(value.getPosition());
		DBWriter dbWriter = (DBWriter)this.mServiceFactory.getServiceProvider(DBWriter.class);
		dbWriter.modifyLocalVideo(v, DBWriter.Action.Update);
	}
	
	/**
	 * 若mLocalVideos中存在，而在values中不存在，则删除mLocalVideos中的值
	 * 就是说，mLocalVideos中只保留values中存在的值
	 * @param values
	 */
	public void refresh(List<String> values) {
		List<LocalVideo> removed = new ArrayList<LocalVideo>();
		for(LocalVideo v: this.mLocalVideos) {
			boolean found = false;
			for(String fullName: values) {
				if(fullName.equalsIgnoreCase(v.getFullName())) {
					found = true;
					break;
				}
			}
			if(!found) {
				removed.add(v);
			}
		}
		synchronized (this.mLocalVideos) {
			this.mLocalVideos.removeAll(removed);
		}
		
		Package pack = new Package();
		pack.setVideos(removed);
		this.mHandler.sendMessage(this.mHandler.obtainMessage(0,  pack));
	}

	public void removeLocal(LocalVideo value) {
		LocalVideo video = this.findLocal(value.getFullName());
		if(video == null) {
			return;
		}
		
		synchronized (this.mLocalVideos) {
			this.mLocalVideos.remove(video);
		}
		DBWriter dbWriter = (DBWriter)this.mServiceFactory.getServiceProvider(DBWriter.class);
		dbWriter.modifyLocalVideo(video, DBWriter.Action.Delete);
	}

	public LocalVideo findLocal(String fullName) {
		synchronized (this.mLocalVideos) {
			for(LocalVideo value: this.mLocalVideos) {
				if(value.getFullName().equals(fullName)) {
					return value;
				}
			}
			return null;
		}
	}

	public List<LocalVideo> getAllLocal() {
		List<LocalVideo> result = new ArrayList<LocalVideo>();
		synchronized (this.mLocalVideos) {
			for(LocalVideo v: this.mLocalVideos) {
				result.add(v);
			}
		}
		return result;
	}
	
	private void removeVideos(List<LocalVideo> values) {
		DBWriter dbWriter = (DBWriter)this.mServiceFactory.getServiceProvider(DBWriter.class);
		for(LocalVideo v: values) {
			dbWriter.modifyLocalVideo(v, DBWriter.Action.Delete);
		}
	}
	
	private class Package {
		private List<LocalVideo> videos = null;

		public List<LocalVideo> getVideos() {
			return videos;
		}

		public void setVideos(List<LocalVideo> values) {
			this.videos = values;
		}
	}
}
