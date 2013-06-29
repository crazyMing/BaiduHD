package com.baidu.hd.player;

import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.LocalVideo;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.Video;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.service.ServiceFactory;

class PlayListHandler {	
	Logger logger = new Logger("PlayListHandler");
	
	private ServiceFactory mServiceFactory = null;
	
	private Video mVideo = null;
	private Album mAlbum = null;

	PlayListHandler(ServiceFactory factory) {
		this.mServiceFactory = factory;
	}

	public Video fromHolder(Video value) {
		
		if(value.isLocal()) {
	
			LocalVideo v = getManager().findLocal(value.toLocal().getFullName());
			if(v != null) {
				this.mVideo = v;
				return this.mVideo;
			}
		}
		this.mVideo = value;
		return this.mVideo;
	}
	
	public Album fromHolder(Album value) {
		
		if(value == null) {
			return null;
		}
//		Album a = getManager().findAlbum(value.getListId());
		Album a = getManager().findAlbum(value.getListId());
		if(a != null) {
			if(a.getSite() != value.getSite() || a.getYear() != value.getYear()) {
				a.setPulled(false);
				a.setSite(value.getSite());
				a.setYear(value.getYear());
				a.getVideos().clear();
			}
			this.mAlbum = a;
		} else {
			this.mAlbum = value;
		}
		
		return this.mAlbum;
	}
	
	public void init() {
		if(this.mAlbum != null) {
			//this.getManager().fillAlbumVideo(this.mAlbum);
			//add by juqiang start 
			PlayListManager playListManager = (PlayListManager)getManager();
			mAlbum.setVideos(playListManager.getNetVideos(mAlbum.getId(), mAlbum.getListId(), mAlbum.getRefer()));
			//add by juqiang end
			if(!this.mAlbum.getCurrent().isSame(this.mVideo)) {
				this.mAlbum.setCurrent(this.mVideo.toNet());
			}
			this.mVideo.setPosition(this.mAlbum.getCurrent().getPosition());
		}
	}
	
	public void destroy() {
		logger.d("destroy");
		// save last pos
		try{
		if(this.mVideo.isLocal()) {
			getManager().updateLocal(this.mVideo.toLocal());
			fireEvent();
		} else {
			if(this.mAlbum != null) {
				if(this.mVideo.getPosition()>0){
				this.mAlbum.getCurrent().setPosition(this.mVideo.getPosition());
				}
				if (mAlbum.getCurrent()==null){
				}
				else if (mAlbum.getCurrent().toNet() != null) {
				}
				
				getManager().playAlbum(this.mAlbum);
//				getManager().addNetVideo(value);
				fireEvent();
			}
		}}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Video next() {
		
		boolean found = false;
		if(this.mVideo!=null&&this.mVideo.isLocal()) {
			
			for(LocalVideo v: getManager().getAllLocal()) {
				if(found) {
					return v;
				}
				if(v.isSame(this.mVideo)) {
					found = true;
				}
			}

		} else {
			
			if(this.mAlbum == null) {
				return null;
			}
			
			for(NetVideo v: this.mAlbum.getVideos()) {
				if(found) {
					return v;
				}
				if(v.isSame(this.mVideo)) {
					found = true;
				}
			}
		}
		return null;
	}
	
	public Video last() {
		
		Video result = null;
		if(this.mVideo.isLocal()) {
			for(LocalVideo v: getManager().getAllLocal()) {
				if(v.isSame(this.mVideo)) {
					return result;
				}
				result = v;
			}			
		} else {
			if(this.mAlbum == null) {
				return null;
			}
			for(NetVideo v: this.mAlbum.getVideos()) {
				if(v.isSame(this.mVideo)) {
					return result;
				}
				result = v;
			}
		}
		return null;
	}

	private PlayListManager getManager() {
		return (PlayListManager)this.mServiceFactory.getServiceProvider(PlayListManager.class);
	}
	
	private void fireEvent() {
		logger.d("fireEvent ePlayListUpdate");
		EventCenter eventCenter = (EventCenter)mServiceFactory.getServiceProvider(EventCenter.class);
		eventCenter.fireEvent(EventId.ePlayListUpdate, new EventArgs());
	}
}
