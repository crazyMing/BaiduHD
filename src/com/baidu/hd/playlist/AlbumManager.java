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
	
	// ��¼�Ѹ��µľ缯B
	private List<Album> mNewestedAlbums = new ArrayList<Album>();
	
	// �缯��ʷ�б�
	private List<Album> mPersonalHistoryAlbums = new ArrayList<Album>();
	
	// ���ղصľ缯�б�
	private List<Album> mFavoriteAlbums = new ArrayList<Album>();
	
	// ��ҳ��ʾ�ľ缯�б�
	private List<Album> mHomeShowAlbums = new ArrayList<Album>();
	
	// ����30����¼
	private final int ALBUM_MAX = 30;
	
	public void create(Context context, ServiceFactory serviceFactory) {
		mContext = context;
		mServiceFactory = serviceFactory;
		
		refresh();
		
		// �����������
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
			// �����˾�û�и��±�־��
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

		// TODO ��ʱ�򻯴������򣬻�øո���ӵ�id
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
	
	// ��ʱֻ֧�ֱ��ع����ľ缯��ѯ
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
	 * ��������ھ缯��ʷ�б��е�ר��
	 */
	public List<Album> getAllPersonalHistoryAlbums() {
		return mPersonalHistoryAlbums;
	}
	
	/**
	 * ��������ھ缯�ղ��б��е�ר��
	 */
	public List<Album> getAllFavoriteAlbums() {
		return mFavoriteAlbums;
	}
	
	/**
	 * �����Ƿ�Ϊ�ղ�
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

		// TODO ��ʱ�򻯴������򣬻�øո���ӵ�id
		refresh();
	}

	/**
	 * ������б����¹���ר��
	 */
	public List<Album> getNewestedAlbum() 
	{
		return mNewestedAlbums;
	}
	
	/**
	 * ���������Ҫ���͵ľ缯��Ŀ
	 */
	// TODO ���Ͳ��Ա�����ú����Ѳ��ٺ���Ӧ�޸�ΪgetNeedPushFavoriteAlbumCount()
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
	 * �����Ҫ���;缯���
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
	 * ��ȡ������Ϣ
	 */
	public void fetchNewestList() {
		logger.d("fetchNewestList");
		
		FetchNewestHandler handler = new FetchNewestHandler(mServiceFactory, new Callback() {
			PlayListManager playListManager = (PlayListManager)mServiceFactory.getServiceProvider(PlayListManager.class);
			@Override
			public void onCompelete(BigSiteSnifferResult result) {
				// ��վֻ��ȷʵ�и��µĲŻ᷵��ֵ
				synchronized (playListManager) {
					Album album = playListManager.findAlbum(result.getAlbumId());
					playListManager.updateVideos(result, album);
				}
			}

			@Override
			public void onCompelete(SmallSiteUrl url) {
				// Сվ�������Ƿ��и���ȫ������ֵ
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
	
	/** �����ݿ�������ȡһ������ */
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
		
		// �������ݿ�
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
		// ��վ���ؾ缯
		if (value.asBigNativeAlbum()!=null) {
			
			if (value.getId() < 0) {
				dbWriter.modifyAlbum(value, DBWriter.Action.Add);
				refresh();
				result = findAlbum(value.getListId());
			}
			else result = value;
		}
		// Сվ���ؾ缯
		else if (value.asSmallNativeAlbum() != null) {
			if (value.getId() < 0) {
				dbWriter.modifyAlbum(value, DBWriter.Action.Add);
				refresh();
				result = findAlbum(value.getListId());
			}
			else result = value;
		}
		// ���������������缯
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
		
		// ������ݿ��е���������
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
			// ��������
			db.beginTransaction();
			for(Album a: values) {
				dbWriter.modifyAlbum(a, DBWriter.Action.Delete);
			}
			// �ύ����
			db.setTransactionSuccessful();
			db.endTransaction();
		}
	}
	
	private Stat getStat() {
		return (Stat) mServiceFactory.getServiceProvider(Stat.class);
	}
}
