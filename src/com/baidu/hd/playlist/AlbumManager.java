package com.baidu.hd.playlist;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.Toast;

import com.baidu.hd.db.DBReader;
import com.baidu.hd.db.DBWriter;
import com.baidu.hd.log.DebugLogger;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.FetchNewestHandler;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.FetchNewestHandler.Callback;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.sniffer.BigSiteSnifferResult;
import com.baidu.hd.sniffer.SmallSiteUrl;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.ToastUtil;
import com.baidu.hd.R;

class AlbumManager {
	
	private Logger logger = new Logger("AlbumManager");
	
	private Context mContext = null;
	private ServiceFactory mServiceFactory = null;
	
	private List<Album> mAlbums = new ArrayList<Album>();
	private Handler mHandler = null;
	
	// 记录已更新的剧集B
	private List<Album> mNewestedAlbums = new ArrayList<Album>();
	
	// 剧集历史列表
	private List<Album> mPersonalHistoryAlbums = new ArrayList<Album>();
	
	// 被收藏的剧集列表
	private List<Album> mFavoriteAlbums = new ArrayList<Album>();
	
	// 首页显示的剧集列表
	private List<Album> mHomeShowAlbums = new ArrayList<Album>();
	
	// 保留30个记录
	private final int ALBUM_MAX = 30;
	
	public void create(Context context, ServiceFactory serviceFactory) {
		mContext = context;
		mServiceFactory = serviceFactory;
		
		refresh();
		
		// 清除垃圾数据
		clearDirtyData();
	}
	
	public void playAlbum(Album value) {
		logger.d("playAlbum");
		
		if(mServiceFactory == null) {
			logger.e("playAlbum null factory");
			return;
		}
		DBWriter dbWriter = (DBWriter)mServiceFactory.getServiceProvider(DBWriter.class);
		Album a = null;
		if (null != value.getRefer() && !"".equals(value.getRefer())){
			a = findAlbumByRefer(value.getRefer());
		}
		else {
			a = findAlbum(value.getListId());
		}
		
		if(a != null) {
			// 看过了就没有更新标志了
//			a.setHaveNew(false);
			a.setCurrent(value.getCurrent());
			a.setType(value.getType());
			if (value.asBigNativeAlbum()!=null || value.asSmallNativeAlbum() != null) {
				value.setPersonalHistory(false);
			}
			else {
				value.setPersonalHistory(true);
			}
			dbWriter.modifyAlbum(a, DBWriter.Action.Update);
		} else {
			value.setHaveNew(false);
			value.handleName(value.getCurrent());
			if (value.asBigNativeAlbum()!=null || value.asSmallNativeAlbum() != null) {
				value.setPersonalHistory(false);
			}
			else {
				value.setPersonalHistory(true);
			}
			mAlbums.add(value);
			dbWriter.modifyAlbum(value, DBWriter.Action.Add);
		}

		// TODO 暂时简化处理，排序，获得刚刚添加的id
		DBReader dbReader = (DBReader)mServiceFactory.getServiceProvider(DBReader.class);
		mAlbums = dbReader.getAllAlbum();
		
		// TODO 10 magic
		if(mPersonalHistoryAlbums.size() >= ALBUM_MAX) {
			Album album = mPersonalHistoryAlbums.get(mPersonalHistoryAlbums.size() - 1);
			album.setPersonalHistory(false);
			if (album.isFavorite()) {
				dbWriter.modifyAlbum(album, DBWriter.Action.Update);
			}
			else {
				dbWriter.modifyAlbum(album, DBWriter.Action.Delete);
			}
		}
		
		refresh();
	}
	
	public void removePersonalHistoryAlbum(Album value) {
		if(mServiceFactory == null) {
			logger.e("removeAlbum null factory");
			return;
		}
		
		Album album = null;
		album = findAlbum(value.getListId());
		
		if(album == null) {
			return;
		}
		
		if (value.isPersonalHistory()) {
			value.setPersonalHistory(false);
			value.setHomeShow(false);
			album.setPersonalHistory(false);
			album.setHomeShow(false);
			mPersonalHistoryAlbums.remove(album);
			mHomeShowAlbums.remove(album);
			
			DBWriter dbWriter = (DBWriter)mServiceFactory.getServiceProvider(DBWriter.class);
			dbWriter.modifyAlbum(album, DBWriter.Action.Update);
		}
	}
	
	public void removeFavoriteAlbum(Album value) {
		if(mServiceFactory == null) {
			logger.e("removeAlbum null factory");
			return;
		}
		
		Album album = null;
		album = findAlbum(value.getListId());
		
		if(album == null) {
			return;
		}
		
		if (value.isFavorite()) {
			value.setFavorite(false);
			album.setFavorite(false);
			mFavoriteAlbums.remove(album);
			
			DBWriter dbWriter = (DBWriter)mServiceFactory.getServiceProvider(DBWriter.class);
			dbWriter.modifyAlbum(album, DBWriter.Action.Update);
		}
	}

	public Album findAlbum(String listId) {
		if (listId == null) return null;
		
		if ("".equals(listId)) return null;
		
		for(Album a: mAlbums) {
			if(a.getListId().equals(listId)) {
				return a;
			}
		}
		return null;
	}
	
	public Album findAlbumById(long id) {
		for(Album a: mAlbums) {
			if(a.getId() == id) {
				return a;
			}
		}
		return null;
	}
	
	public Album findAlbumByRefer(String refer) {
		
		for(Album a: mAlbums) {
			if(a.getRefer().equals(refer)) {
				return a;
			}
		}
		return null;
	}
	
	public Album findAlbumByVideoRefer(String refer) {
		
		for(Album a : mAlbums) {
			if (a.getCurrent()==null || a.getCurrent().getRefer()==null) {
				continue;
			}
			else if (a.getCurrent().getRefer().equals(refer)) {
				return a;
			}
		}
		return null;
	}
	
	// 暂时只支持本地构建的剧集查询
	public Album findAlbumByPlayUrl(String playUrl) {
		if (StringUtil.isEmpty(playUrl)) return null;
		
		for(Album a : mAlbums) {
			if (a.asBigNativeAlbum().getVideo().getUrl().equals(playUrl)) {
				return a;
			}
			else if (a.asSmallNativeAlbum().getVideo().equals(playUrl)) {
				return a;
			}
		}
		return null;
	}
	
	
	/**
	 * 获得所有在剧集历史列表中的专辑
	 */
	public List<Album> getAllPersonalHistoryAlbums() {
		return mPersonalHistoryAlbums;
	}
	
	/**
	 * 获得所有在剧集收藏列表中的专辑
	 */
	public List<Album> getAllFavoriteAlbums() {
		return mFavoriteAlbums;
	}
	
	/**
	 * 设置是否为收藏
	 */
	public void setFavorite(Album value) {
		if(mServiceFactory == null) {
			logger.e("playAlbum null factory");
			return;
		}
		DBWriter dbWriter = (DBWriter)mServiceFactory.getServiceProvider(DBWriter.class);
		Album a = null;
		if (null != value.getRefer() && !"".equals(value.getRefer())) {
			a = findAlbumByRefer(value.getRefer());
		}
		else {
			a = findAlbum(value.getListId());
		}
		
		if(a != null) {
			a.setFavorite(!a.isFavorite());
			dbWriter.modifyAlbum(a, DBWriter.Action.Update);
			
		} else {
			value.setFavorite(true);
			value.setPersonalHistory(false);
			dbWriter.modifyAlbum(value, DBWriter.Action.Add);
		}
		
		// Toast
		Album forToast = a != null ? a : value;
		String message = forToast.isFavorite() ? mContext.getString(R.string.favorite_info_add) : mContext.getString(R.string.favorite_info_remove);
		ToastUtil.showMessage(mContext, message, Toast.LENGTH_LONG);

		// TODO 暂时简化处理，排序，获得刚刚添加的id
		refresh();
	}

	/**
	 * 获得所有被更新过的专辑
	 */
	public List<Album> getNewestedAlbum() 
	{
		return mNewestedAlbums;
	}
	
	/**
	 * 获得所有需要推送的剧集数目
	 */
	// TODO 推送策略变更，该函数已不再合理。应修改为getNeedPushFavoriteAlbumCount()
	/*
	public int getNeedPushAlbumCount() {
		int count = 0; 
		for (Album album : mAlbums) {
			if (album.isPush()) {
				count++;
			}
		}
		return count;
	}
	*/
	/**
	 * 清除需要推送剧集标记
	 */
	public void clearPushForAllAlbum() {
		for (Album album : mAlbums) {
			if (album.isPush())	{
				album.setPush(false);
				DBWriter dbWriter = (DBWriter)mServiceFactory.getServiceProvider(DBWriter.class);
				dbWriter.modifyAlbum(album, DBWriter.Action.Update);
			}
		}
	}
	
	/**
	 * 获取更新信息
	 */
	public void fetchNewestList() {
		logger.d("fetchNewestList");
		
		FetchNewestHandler handler = new FetchNewestHandler(mServiceFactory, new Callback() {
			PlayListManager playListManager = (PlayListManager)mServiceFactory.getServiceProvider(PlayListManager.class);
			@Override
			public void onCompelete(BigSiteSnifferResult result) {
				// 大站只有确实有更新的才会返回值
				synchronized (playListManager) {
					Album album = playListManager.findAlbum(result.getAlbumId());
					playListManager.updateVideos(result, album);
				}
			}

			@Override
			public void onCompelete(SmallSiteUrl url) {
				// 小站，不管是否有更新全部返回值
				synchronized (playListManager) {
					Album album = playListManager.findAlbumByRefer(url.getRefer());
					playListManager.updateVideos(url, album);
				}
			}
			
			
		});
		
		handler.request();
	}
	
	private void prompt(String msg) {
		if(Const.isDebug) {
			Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
		}
	}
	
	/** 从数据库中重新取一次数据 */
	private void refresh() {
		logger.d("read albums from database");
		
		DBReader dbReader = (DBReader)mServiceFactory.getServiceProvider(DBReader.class);
		mAlbums = dbReader.getAllAlbum();
		
		mFavoriteAlbums.clear();
		mPersonalHistoryAlbums.clear();
		mHomeShowAlbums.clear();
		for(Album album : mAlbums) {
			if (album.isFavorite()) {
				mFavoriteAlbums.add(album);
			}
			if(album.isPersonalHistory()) {
				mPersonalHistoryAlbums.add(album);
				if (album.isHomeShow()) {
					mHomeShowAlbums.add(album);
				}
			}
		}
	}
	
	public List<Album> getHomeShowAlbums() {
		return mHomeShowAlbums;
	}
	
	public void removeAllHomeShowAlbum(){
		final List<Album> homeShowAlbums = new ArrayList<Album>();
		for (Album album: mHomeShowAlbums)
			homeShowAlbums.add(album);
		
		for (Album album: homeShowAlbums) {
			removeHomeShowAlbum(album);
		}
	}
	
	
	public void removeHomeShowAlbum(Album album) {
		if(mServiceFactory == null) {
			logger.e("removeHomeShowAlbum null factory");
			return;
		}
		
		Album found = findAlbum(album.getListId());
		
		if(found == null) {
			return;
		}
		
		// 处理数据库
		if (found.isHomeShow()) {
			found.setHomeShow(false);
			album.setHomeShow(false);
			mHomeShowAlbums.remove(album);

			DBWriter dbWriter = (DBWriter)mServiceFactory.getServiceProvider(DBWriter.class);
			dbWriter.modifyAlbum(found, DBWriter.Action.Update);
		}
	}
	
	 Album addAlbum(Album value) {
		logger.d("addAlbum");
		Album result = null;
		
		if(mServiceFactory == null) {
			logger.e("addAlbum null factory");
			return result;
		}
		DBWriter dbWriter = (DBWriter)mServiceFactory.getServiceProvider(DBWriter.class);
		Album a = null;
		// 大站本地剧集
		if (value.asBigNativeAlbum()!=null) {
			
			if (value.getId() < 0) {
				dbWriter.modifyAlbum(value, DBWriter.Action.Add);
				refresh();
				result = findAlbum(value.getListId());
			}
			else result = value;
		}
		// 小站本地剧集
		else if (value.asSmallNativeAlbum() != null) {
			if (value.getId() < 0) {
				dbWriter.modifyAlbum(value, DBWriter.Action.Add);
				refresh();
				result = findAlbum(value.getListId());
			}
			else result = value;
		}
		// 服务器来的真正剧集
		else {
			if (null != value.getRefer() && !"".equals(value.getRefer())){
				a = findAlbumByRefer(value.getRefer());
			}
			else {
				a = findAlbum(value.getListId());
			}
			
			if(a == null) {
				dbWriter.modifyAlbum(value, DBWriter.Action.Add);
				refresh();
				result = findAlbum(value.getListId());
			}
			else {
				result = a;
			}
		}

		return result;
	}
	
	public void setDownload(Album value, boolean isDownload) {
		DBWriter dbWriter = (DBWriter)mServiceFactory.getServiceProvider(DBWriter.class);
		Album a = null;
		if (null != value.getRefer() && !"".equals(value.getRefer())){
			a = findAlbumByRefer(value.getRefer());
		}
		else {
			a = findAlbum(value.getListId());
		}
		
		if(a != null && a.isDownload()!=isDownload) {
			a.setDownload(isDownload);
			dbWriter.modifyAlbum(a, DBWriter.Action.Update);
		}
	}
	
	public void setDownload(long albumId, boolean isDownload) {
		DBWriter dbWriter = (DBWriter)mServiceFactory.getServiceProvider(DBWriter.class);
		Album a = null;
		a = findAlbumById(albumId);
		
		if(a != null && a.isDownload()!=isDownload) {
			a.setDownload(isDownload);
			dbWriter.modifyAlbum(a, DBWriter.Action.Update);
		}
	}
	
	public void setDownload(boolean isDownload) {
		synchronized (mAlbums) {
			for (int i = 0; i < mAlbums.size(); i++) {
				setDownload(mAlbums.get(i), isDownload);
			}
		}
	}

	public void setCurrent(Album album, NetVideo video) {
		if (album.getId() < 0) {
			return;
		}
		
		album.setCurrent(video);
		DBWriter dbWriter = (DBWriter)mServiceFactory.getServiceProvider(DBWriter.class);
		dbWriter.modifyAlbum(album, DBWriter.Action.Update);
	}
	
	private void clearDirtyData() {
		HandlerThread thread = new HandlerThread("dbRemoveNetVidoes");
		thread.start();
		this.mHandler = new Handler(thread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				removeAlbums(((Package)msg.obj).getAlbums());
			}
		};
		
		// 清除数据库中的垃圾数据
		List<Album> removed = new ArrayList<Album>();
		for(Album v: mAlbums) {
			if (!v.isDownload() && 
				!v.isPersonalHistory() && 
				!v.isFavorite()) {
				removed.add(v);
			}
		}
		synchronized (this.mAlbums) {
			this.mAlbums.removeAll(removed);
		}
		
		Package pack = new Package();
		pack.setAlbums(removed);
		this.mHandler.sendMessage(this.mHandler.obtainMessage(0,  pack));
	}
	
	private class Package {
		private List<Album> albums = null;

		public List<Album> getAlbums() {
			return albums;
		}

		public void setAlbums(List<Album> values) {
			this.albums = values;
		}
	}
	
	private void removeAlbums(List<Album> values) {
		DBWriter dbWriter = (DBWriter)this.mServiceFactory.getServiceProvider(DBWriter.class);
		SQLiteDatabase db = dbWriter.getDatabase();
		if (db != null && db.isOpen()) {
			// 创建事务
			db.beginTransaction();
			for(Album a: values) {
				dbWriter.modifyAlbum(a, DBWriter.Action.Delete);
			}
			// 提交事务
			db.setTransactionSuccessful();
			db.endTransaction();
		}
	}
	
	private Stat getStat() {
		return (Stat) mServiceFactory.getServiceProvider(Stat.class);
	}
}
