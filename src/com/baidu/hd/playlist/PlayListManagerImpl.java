package com.baidu.hd.playlist;

import java.util.List;

import android.content.Context;

import com.baidu.hd.log.Logger;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.LocalVideo;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.service.ServiceConsumer;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.sniffer.BigSiteSnifferResult;
import com.baidu.hd.sniffer.SmallSiteUrl;

public class PlayListManagerImpl implements PlayListManager, ServiceConsumer {
	
	private Logger logger = new Logger("PlayListManagerImpl");

	
	private Context mContext = null;
	private ServiceFactory mServiceFactory = null;
	
	private AlbumManager mAlbumManager = new AlbumManager();
	private LocalManager mLocalManager = new LocalManager();
	private NetVideoManager mNetVideoManager = new NetVideoManager();
	
	@Override
	public void setContext(Context ctx) {
		mContext = ctx;
	}

	@Override
	public void onCreate() {
		logger.d("onCreate");
		
		mAlbumManager.create(mContext, mServiceFactory);
		mLocalManager.create(mServiceFactory);
		mNetVideoManager.create(mServiceFactory);
	}

	@Override
	public void onDestory() {
	}

	@Override
	public void onSave() {
	}

	@Override
	public void setServiceFactory(ServiceFactory factory) {
		mServiceFactory = factory;
	}

	@Override
	public void playAlbum(Album value) {
		logger.d("playAlbum");
		
		mAlbumManager.playAlbum(value);
		
		// 更新视频播放时长字段
		if (mServiceFactory != null) {
			mNetVideoManager.updateNetVideo(value.getCurrent());
		}
	}
		
	@Override
	public void setDownload(Album value, boolean isDownload) {
		mAlbumManager.setDownload(value, isDownload);
	}
	
	@Override
	public void setDownload(long albumId, boolean isDownload) {
		mAlbumManager.setDownload(albumId, isDownload);
	}
	
	@Override
	public void setDownload(boolean isDownload) {
		mAlbumManager.setDownload(isDownload);
	}
	
	@Override
	public void removePersonalHistoryAlbum(Album value) {
		mAlbumManager.removePersonalHistoryAlbum(value);
	}

	@Override
	public void removeFavoriteAlbum(Album value) {
		mAlbumManager.removeFavoriteAlbum(value);
	}

	@Override
	public Album findAlbum(String listId) {
		return mAlbumManager.findAlbum(listId);
	}

	@Override
	public Album findAlbumById(long id) {
		return mAlbumManager.findAlbumById(id);
	}

	@Override
	public boolean addLocal(String value) {
		return mLocalManager.addLocal(value);
	}

	@Override
	public void refreshLocal(List<String> value) {
		mLocalManager.refresh(value);
	}

	@Override
	public void updateLocal(LocalVideo value) {
		mLocalManager.updateLocal(value);
	}

	@Override
	public void removeLocal(LocalVideo value) {
		mLocalManager.removeLocal(value);
	}

	@Override
	public LocalVideo findLocal(String fullName) {
		return mLocalManager.findLocal(fullName);
	}
	
	@Override
	public List<LocalVideo> getAllLocal() {
		return mLocalManager.getAllLocal();
	}
	
	@Override
	public List<NetVideo> getNetVideos(long albumId, String listId, String albumRefer) {
		return mNetVideoManager.getNetVideos(albumId, listId, albumRefer);
	}
	
	@Override
	public Album findAlbumByRefer(String refer) {
		return mAlbumManager.findAlbumByRefer(refer);
	}
	
	@Override
	public Album findAlbumByVideoRefer(String refer) {
		return mAlbumManager.findAlbumByVideoRefer(refer);
	}
	
	@Override
	public Album findAlbumByPlayUrl(String playUrl) {
		return mAlbumManager.findAlbumByPlayUrl(playUrl);
	}

	@Override
	public List<Album> getAllPersonalHistoryAlbums() {
		return mAlbumManager.getAllPersonalHistoryAlbums();
	}

	@Override
	public List<Album> getAllFavoriteAlbums() {
		return mAlbumManager.getAllFavoriteAlbums();
	}

	@Override
	public void setFavorite(Album a) {
		mAlbumManager.setFavorite(a);
	}

	@Override
	public void updateNetVideo(NetVideo value) {
		mNetVideoManager.updateNetVideo(value);
	}

	@Override
	public void removeNetVideo(NetVideo value) {
		mNetVideoManager.removeVideo(value);
	}

	@Override
	public NetVideo findNetVideo(String refer, String url) {
		return mNetVideoManager.findVideo(refer, url);
	}
	
	@Override
	public int addVideos(SmallSiteUrl url, Album album) {
		// 先插入剧集
		Album result = mAlbumManager.addAlbum(album);
		int count = mNetVideoManager.addVideos(url, result);
		
		List<NetVideo> videos = mNetVideoManager.getNetVideos(result.getId(), result.getListName(), result.getRefer());
		if (videos.size() > 0) {
			logger.e("video size = 0");
			mAlbumManager.setCurrent(result, videos.get(0));
		}
		return count;
	}
	
	@Override
	public int addVideos(BigSiteSnifferResult value, Album album) {
		// 先插入剧集
		Album result = mAlbumManager.addAlbum(album);
		int count = mNetVideoManager.addVideos(value, result);
		
		List<NetVideo> videos = mNetVideoManager.getNetVideos(result.getId(), result.getListName(), result.getRefer());
		if (videos.size() > 0) {
			logger.e("video size = 0");
			mAlbumManager.setCurrent(result, videos.get(0));
		}
		return count;
	}
	
	@Override
	public void updateVideos(SmallSiteUrl url, Album album) {
		mNetVideoManager.updateVideos(url, album);
	}
	
	@Override
	public void updateVideos(BigSiteSnifferResult result, Album album) {
		mNetVideoManager.updateVideos(result, album);
	}

	@Override
	public List<Album> getHomeShowAlbums() {
		return mAlbumManager.getHomeShowAlbums();
	}
	
	@Override
	public void removeHomeShowAlbum(Album album) {
		mAlbumManager.removeHomeShowAlbum(album);
	}

	@Override
	public void removeAllHomeShowAlbum() {
		mAlbumManager.removeAllHomeShowAlbum();
	}
	
	@Override
	public 	void fetchNewestAlbum() {
		mAlbumManager.fetchNewestList();
	}
}
