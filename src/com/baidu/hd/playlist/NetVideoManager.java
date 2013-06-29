package com.baidu.hd.playlist;

import java.util.ArrayList;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.baidu.hd.db.DBReader;
import com.baidu.hd.db.DBWriter;
import com.baidu.hd.db.DBWriter.Action;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.VideoFactory;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.sniffer.BigSiteSnifferResult;
import com.baidu.hd.sniffer.SmallSiteUrl;
import com.baidu.hd.sniffer.BigSiteSnifferResult.BigSiteVideoResult;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;

public class NetVideoManager {
	
	private Logger logger = new Logger("NetVideoManager");
	private Object mLock = new Object();

	private ServiceFactory mServiceFactory = null;
	
	private List<NetVideo> mNetVideos = new ArrayList<NetVideo>();
	private Handler mHandler = null;
	
	public void create(ServiceFactory serviceFactory) {
		logger.d("create");
		
		this.mServiceFactory = serviceFactory;
		refresh();
		
		// 清除垃圾数据
		clearDirtyData();
	}
	
	/**
	 * DEMO(使用示例):若无Album，在调用之前先创建剧集
	 * NetVideo video = VideoFactory.create(false).toNet();
	 * video.set*  set 视频相关的所有信息，必须要有AlbumId
	 * video = addNetVideo(video); 由该函数判断该视频是否已经存在，注意一定要用返回值替换原有变量
	 */
	/*public*/private NetVideo addNetVideo(NetVideo value) {
		DBWriter dbWriter = (DBWriter)this.mServiceFactory.getServiceProvider(DBWriter.class);
		NetVideo video = this.findVideo(value.getRefer(), value.getUrl());
		
		// 找到直接返回找到的值
		if (video != null) return video;
		
		// 没找到插入数据库
		dbWriter.modifyNetVideo(value, DBWriter.Action.Add);
		synchronized (this.mNetVideos) {
			this.mNetVideos.add(value);
			// 插入之后再查找一次，回设信息
			value.setId(this.findVideo(value.getRefer(), value.getUrl()).getId());
			return value;
		}
	}

	/**
	 * 更新需要的字段，当用到某字段时，再过来添加
	 * @param value
	 */
	public void updateNetVideo(NetVideo value) {
		NetVideo v = findVideo(value.getRefer(), value.getUrl());
		if(v == null) {
			return;
		}
		
		v.setPosition(value.getPosition());
		v.setUrl(value.getUrl());
		DBWriter dbWriter = (DBWriter)this.mServiceFactory.getServiceProvider(DBWriter.class);
		dbWriter.modifyNetVideo(v, DBWriter.Action.Update);
	}
	
	public void removeVideo(NetVideo value) {
		NetVideo video = findVideo(value.getRefer(), value.getUrl());
		if(video == null) {
			return;
		}
		
		synchronized (this.mNetVideos) {
			this.mNetVideos.remove(video);
		}
		DBWriter dbWriter = (DBWriter)this.mServiceFactory.getServiceProvider(DBWriter.class);
		dbWriter.modifyNetVideo(video, DBWriter.Action.Delete);
	}

	public NetVideo findVideo(String refer, String url) {
		synchronized (this.mNetVideos) {
			for(NetVideo value: this.mNetVideos) {
				if (!StringUtil.isEmpty(url) && value.getUrl().equals(url)) {
					return value;
				}
				else if (!StringUtil.isEmpty(refer) && value.getRefer().equals(refer)) {
					return value;
				}
			}
			return null;
		}
	}

	public List<NetVideo> getNetVideos(long albumId, String listId, String albumRefer) {
//		logger.d("albumRefer = " + albumRefer + "\n mNetVideos size = " + mNetVideos.size());

		//		refresh();
		int count = 0;
		List<NetVideo> result = new ArrayList<NetVideo>();
		if (albumId < 0 && StringUtil.isEmpty(listId) && StringUtil.isEmpty(albumRefer)) {
			return result;
		}

		synchronized (this.mNetVideos) {
			for(NetVideo v: this.mNetVideos) {
				// 按albumId查找
				if (albumId > -1) {
					if ( v.getAlbumId() == albumId) {
						count++;
						if (StringUtil.isEmpty(v.getEpisode())){
							v.setEpisode(String.valueOf(count));
						}
						result.add(v);
					}
				}
				// 按albumRefer查找
				else if (!StringUtil.isEmpty(albumRefer)) {
					if (v.getAlbumRefer().equals(albumRefer)) {
						count++;
						if (StringUtil.isEmpty(v.getEpisode())) {
							v.setEpisode(String.valueOf(count));
						}
						result.add(v);
					}
				}
				// 按listId查找
				else if (!StringUtil.isEmpty(listId)) {
					// TODO
				}
				
			}
		}
		return result;
	}
	
	private void removeVideos(List<NetVideo> values) {
		DBWriter dbWriter = (DBWriter)this.mServiceFactory.getServiceProvider(DBWriter.class);
		SQLiteDatabase db = dbWriter.getDatabase();
		if(db != null && db.isOpen() ) {
			synchronized (mLock) {
				// 创建事务
				db.beginTransaction();
				
				for(NetVideo v: values) {
					dbWriter.modifyNetVideo(v, DBWriter.Action.Delete);
				}
				// 提交事务
				db.setTransactionSuccessful();
				db.endTransaction();
			}
		}
		else {
			logger.e("removeVideos db is null or is not open");
		}
	}

	public int addVideos(SmallSiteUrl url, Album album) {
		logger.d("addVideos");
		
		int iInserted = 0;
		
		// 小站本地构建剧集
		if (album.asSmallNativeAlbum() != null) {
			NetVideo video = VideoFactory.create(false).toNet();
			video.setType(NetVideo.NetVideoType.P2P_STREAM);
			video.setAlbumRefer(url.getRefer());
			video.setUrl(url.getBdhd());
			video.setAlbum(album);
			video.setAlbumId(album.getId());

			NetVideo result = addNetVideo(video);
			if (result == video) {
				iInserted++;
			}
		}
		// 来自服务器的剧集
		else {
			synchronized (mNetVideos) {
				
				DBWriter dbWriter = (DBWriter)this.mServiceFactory.getServiceProvider(DBWriter.class);
				SQLiteDatabase db = dbWriter.getDatabase();
				
				if(db != null && db.isOpen() ) {
					// 创建事物
					db.beginTransaction();
					for (int i = 0; i < url.getPlayUrls().size(); i++) {
						
						NetVideo video = VideoFactory.create(false).toNet();
						video.setType(NetVideo.NetVideoType.P2P_STREAM);
						video.setName(parseUrl(url.getPlayUrls().get(i).getY()));
						video.setRefer(parseUrl(url.getPlayUrls().get(i).getX()));
						video.setAlbumRefer(url.getRefer());
						video.setUrl(url.getPlayUrls().get(i).getY());
						video.setEpisode(1+i+"");
						video.setAlbum(album);
						video.setAlbumId(album.getId());
						
						NetVideo result = addNetVideo(video);
						if (result == video) {
							iInserted++;
						}
					}
					
					// 处理事物
					db.setTransactionSuccessful();
					db.endTransaction();
				}
				else {
					return iInserted;
				}
			}
		}
		
		// 从数据库中重新读取
		refresh();
		return iInserted;
	}
	
	public int addVideos(BigSiteSnifferResult value, Album album) {
		logger.d("addVideos");
		int iInserted = 0;
		
		// 大站本地构建的剧集
		if (album.asBigNativeAlbum() != null) {
			NetVideo video = VideoFactory.create(false).toNet();
			video.setType(NetVideo.NetVideoType.BIGSITE);
			video.setRefer(value.getCurrentPlayRefer());
			video.setAlbumRefer(value.getCurrentPlayRefer());
			video.setUrl(value.getCurrentPlayUrl());
			video.setAlbum(album);
			video.setAlbumId(album.getId());

			NetVideo result = addNetVideo(video);
			if (result == video) {
				iInserted++;
			}
		}
		// 来自服务器的剧集
		else {
			List<BigSiteVideoResult> vList = value.getVideoList();
			if (vList==null || vList.size() < 1) {
				return iInserted;
			}
			
			synchronized (mNetVideos) {
				
				DBWriter dbWriter = (DBWriter)this.mServiceFactory.getServiceProvider(DBWriter.class);
				SQLiteDatabase db = dbWriter.getDatabase();
				
				if(db != null && db.isOpen() ) {
					// 创建事物
					db.beginTransaction();
					for (int i = 0; i < vList.size(); i++) {
						
						NetVideo video = VideoFactory.create(false).toNet();
						video.setType(NetVideo.NetVideoType.BIGSITE);
						video.setName(vList.get(i).mName);
						video.setRefer(vList.get(i).mRefer);
						video.setAlbumRefer(album.getRefer());
						video.setUrl(vList.get(i).mPlayUrl);
						video.setEpisode(vList.get(i).mPlayUrl);
						video.setAlbum(album);
						video.setAlbumId(album.getId());

						NetVideo result = addNetVideo(video);
						if (result == video) {
							iInserted++;
						}
					}

					// 处理事物
					db.setTransactionSuccessful();
					db.endTransaction();
				} else {
					return iInserted;
				}
			}
		} // end 服务器剧集
		
		// 从数据库中重新读取
		refresh();
		return iInserted;
	}
	
	// 小站更新
	public boolean updateVideos(SmallSiteUrl url, Album album) {
		logger.d("updateVideos");
		if (album == null) {
			logger.d("album is null");
			return false;
		}
		
		boolean isHaveNew = false;
		int newestCount = 0;
		
		DBWriter dbWriter = (DBWriter) this.mServiceFactory.getServiceProvider(DBWriter.class);
		SQLiteDatabase db = dbWriter.getDatabase();
		if (db != null && db.isOpen()) {
			// 创建事物
			db.beginTransaction();

			for (int i = 0; i < url.getPlayUrls().size(); i++) {

				NetVideo video = VideoFactory.create(false).toNet();
				video.setType(NetVideo.NetVideoType.P2P_STREAM);
				video.setName(parseUrl(url.getPlayUrls().get(i).getY()));
				video.setRefer(parseUrl(url.getPlayUrls().get(i).getX()));
				video.setAlbumRefer(url.getRefer());
				video.setUrl(url.getPlayUrls().get(i).getY());
				video.setEpisode(1 + i + "");
				video.setAlbum(album);
				video.setAlbumId(album.getId());

				if (findVideo(video.getRefer(), video.getUrl()) == null) {
					addNetVideo(video);
					isHaveNew = true;
					newestCount++;
				}
			}

			// 更新剧集信息]
			album.setHaveNew(isHaveNew);
			album.setNewestCount(newestCount);
			dbWriter.modifyAlbum(album, Action.Update);

			// 处理事物
			db.setTransactionSuccessful();
			db.endTransaction();
		}
		
		// 重新从数据库中读取
		refresh();
		return isHaveNew;
	}
	
	// 大站更新
	public boolean updateVideos(BigSiteSnifferResult result, Album album) {
		logger.d("updateVideos");
		if (album == null) {
			logger.e("album is null");
			return false;
		}
		
		boolean isHaveNew = false;
		int newestCount = 0;
		List<BigSiteVideoResult> vResults = result.getVideoList();
		
		
		synchronized (mNetVideos) {
			
			DBWriter dbWriter = (DBWriter) this.mServiceFactory.getServiceProvider(DBWriter.class);
			SQLiteDatabase db = dbWriter.getDatabase();
			if (db != null && db.isOpen()) {
				// 创建事物
				db.beginTransaction();

				for (int i = 0; i < vResults.size(); i++) {

					NetVideo video = VideoFactory.create(false).toNet();
					video.setType(NetVideo.NetVideoType.BIGSITE);
					video.setName(vResults.get(i).mName);
					video.setRefer(vResults.get(i).mRefer);
					video.setAlbumRefer(album.getRefer());
					video.setUrl(vResults.get(i).mPlayUrl);
					video.setEpisode(vResults.get(i).mPlayId);
					video.setAlbum(album);
					video.setAlbumId(album.getId());

					if (findVideo(video.getRefer(), video.getUrl()) == null) {
						addNetVideo(video);
						isHaveNew = true;
						newestCount++;
					}
				}

				// 更新剧集信息]
				album.setHaveNew(isHaveNew);
				album.setNewestCount(newestCount);
				album.setNewestId(String.valueOf(result.getLastlatest()));
				dbWriter.modifyAlbum(album, Action.Update);

				// 处理事物
				db.setTransactionSuccessful();
				db.endTransaction();
			}
		}

		// 重新从数据库中读取
		refresh();
		return isHaveNew;
	}
	
	/**
	 * 从数据库中重新读取视频信息
	 */
	private void refresh() {
		DBReader dbReader = (DBReader)mServiceFactory.getServiceProvider(DBReader.class);
		List<NetVideo>  videos = dbReader.getAllNetVideos();
		if (videos != null) {
			mNetVideos = dbReader.getAllNetVideos();
		}
		else {
			mNetVideos.clear();
		}
	}

	private void clearDirtyData() {
		logger.d("clearDirtyData");
		
		HandlerThread thread = new HandlerThread("dbRemoveNetVidoes");
		thread.start();
		
		this.mHandler = new Handler(thread.getLooper()) {

			@Override
			public void handleMessage(Message msg) {
				removeVideos(((Package)msg.obj).getVideos());
			}
		};
		
		// 清除数据库中的垃圾数据
		List<NetVideo> removed = new ArrayList<NetVideo>();
		for(NetVideo v: mNetVideos) {
			if (v.getAlbumId() == -1) {
				removed.add(v);
			}
		}
		synchronized (this.mNetVideos) {
			this.mNetVideos.removeAll(removed);
		}
		
		Package pack = new Package();
		pack.setVideos(removed);
		this.mHandler.sendMessage(this.mHandler.obtainMessage(0,  pack));
	}
	
	private String parseUrl(String bdhd) {
		return StringUtil.getNameForUrl(bdhd);
	}
	
	private class Package {
		private List<NetVideo> videos = null;

		public List<NetVideo> getVideos() {
			return videos;
		}

		public void setVideos(List<NetVideo> values) {
			this.videos = values;
		}
	}
}
