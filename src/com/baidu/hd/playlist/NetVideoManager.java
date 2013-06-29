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
		
		// �����������
		clearDirtyData();
	}
	
	/**
	 * DEMO(ʹ��ʾ��):����Album���ڵ���֮ǰ�ȴ����缯
	 * NetVideo video = VideoFactory.create(false).toNet();
	 * video.set*  set ��Ƶ��ص�������Ϣ������Ҫ��AlbumId
	 * video = addNetVideo(video); �ɸú����жϸ���Ƶ�Ƿ��Ѿ����ڣ�ע��һ��Ҫ�÷���ֵ�滻ԭ�б���
	 */
	/*public*/private NetVideo addNetVideo(NetVideo value) {
		DBWriter dbWriter = (DBWriter)this.mServiceFactory.getServiceProvider(DBWriter.class);
		NetVideo video = this.findVideo(value.getRefer(), value.getUrl());
		
		// �ҵ�ֱ�ӷ����ҵ���ֵ
		if (video != null) return video;
		
		// û�ҵ��������ݿ�
		dbWriter.modifyNetVideo(value, DBWriter.Action.Add);
		synchronized (this.mNetVideos) {
			this.mNetVideos.add(value);
			// ����֮���ٲ���һ�Σ�������Ϣ
			value.setId(this.findVideo(value.getRefer(), value.getUrl()).getId());
			return value;
		}
	}

	/**
	 * ������Ҫ���ֶΣ����õ�ĳ�ֶ�ʱ���ٹ������
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
				// ��albumId����
				if (albumId > -1) {
					if ( v.getAlbumId() == albumId) {
						count++;
						if (StringUtil.isEmpty(v.getEpisode())){
							v.setEpisode(String.valueOf(count));
						}
						result.add(v);
					}
				}
				// ��albumRefer����
				else if (!StringUtil.isEmpty(albumRefer)) {
					if (v.getAlbumRefer().equals(albumRefer)) {
						count++;
						if (StringUtil.isEmpty(v.getEpisode())) {
							v.setEpisode(String.valueOf(count));
						}
						result.add(v);
					}
				}
				// ��listId����
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
				// ��������
				db.beginTransaction();
				
				for(NetVideo v: values) {
					dbWriter.modifyNetVideo(v, DBWriter.Action.Delete);
				}
				// �ύ����
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
		
		// Сվ���ع����缯
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
		// ���Է������ľ缯
		else {
			synchronized (mNetVideos) {
				
				DBWriter dbWriter = (DBWriter)this.mServiceFactory.getServiceProvider(DBWriter.class);
				SQLiteDatabase db = dbWriter.getDatabase();
				
				if(db != null && db.isOpen() ) {
					// ��������
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
					
					// ��������
					db.setTransactionSuccessful();
					db.endTransaction();
				}
				else {
					return iInserted;
				}
			}
		}
		
		// �����ݿ������¶�ȡ
		refresh();
		return iInserted;
	}
	
	public int addVideos(BigSiteSnifferResult value, Album album) {
		logger.d("addVideos");
		int iInserted = 0;
		
		// ��վ���ع����ľ缯
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
		// ���Է������ľ缯
		else {
			List<BigSiteVideoResult> vList = value.getVideoList();
			if (vList==null || vList.size() < 1) {
				return iInserted;
			}
			
			synchronized (mNetVideos) {
				
				DBWriter dbWriter = (DBWriter)this.mServiceFactory.getServiceProvider(DBWriter.class);
				SQLiteDatabase db = dbWriter.getDatabase();
				
				if(db != null && db.isOpen() ) {
					// ��������
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

					// ��������
					db.setTransactionSuccessful();
					db.endTransaction();
				} else {
					return iInserted;
				}
			}
		} // end �������缯
		
		// �����ݿ������¶�ȡ
		refresh();
		return iInserted;
	}
	
	// Сվ����
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
			// ��������
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

			// ���¾缯��Ϣ]
			album.setHaveNew(isHaveNew);
			album.setNewestCount(newestCount);
			dbWriter.modifyAlbum(album, Action.Update);

			// ��������
			db.setTransactionSuccessful();
			db.endTransaction();
		}
		
		// ���´����ݿ��ж�ȡ
		refresh();
		return isHaveNew;
	}
	
	// ��վ����
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
				// ��������
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

				// ���¾缯��Ϣ]
				album.setHaveNew(isHaveNew);
				album.setNewestCount(newestCount);
				album.setNewestId(String.valueOf(result.getLastlatest()));
				dbWriter.modifyAlbum(album, Action.Update);

				// ��������
				db.setTransactionSuccessful();
				db.endTransaction();
			}
		}

		// ���´����ݿ��ж�ȡ
		refresh();
		return isHaveNew;
	}
	
	/**
	 * �����ݿ������¶�ȡ��Ƶ��Ϣ
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
		
		// ������ݿ��е���������
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
