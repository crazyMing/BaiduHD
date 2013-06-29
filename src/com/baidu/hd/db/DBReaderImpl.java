package com.baidu.hd.db;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.browser.db.Suggestion;
import com.baidu.browser.visitesite.SearchKeyword;
import com.baidu.browser.visitesite.VisiteSite;
import com.baidu.hd.module.HistorySearch;
import com.baidu.hd.module.Image;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.LocalVideo;
import com.baidu.hd.module.album.NetVideo;

public class DBReaderImpl implements DBReader {
	
	/** 多线程同步锁 
	 * 【fix bug】MEDIA-5486 【崩溃】【取消法】小站嗅探，播放后返回播放列表页面程序崩溃database is locked（随机）
	 */
	public static Object LOCK = new Object();

	private SQLiteDatabase db = null;
	
	@Override
	public void setContext(Context ctx) {
		
		if(ctx != null) {
			if (db == null || !db.isOpen()) {
				DatabaseHelper helper = new DatabaseHelper(ctx);
				this.db = helper.getReadableDatabase();
			}
		} else {
			if (db!=null && db.isOpen()) {
				this.db.close();
			}
		}
	}
	
	@Override
	public void onCreate() {
	}

	@Override
	public void onDestory() {
		if (db != null && db.isOpen()) {
			db.close();
		}
	}

	@Override
	public void onSave() {
	}

	@Override
	public List<HistorySearch> getHistorySearchList(int count) {
		synchronized (LOCK) {
			return new DBHistorySearch(this.db).get(count);
		}
	}

	@Override
	public List<HistorySearch> historySearch(String keyword, int count) {
		synchronized (LOCK) {
			return new DBHistorySearch(this.db).search(keyword, count);
		}
	}

	@Override
	public List<Task> getAllTask() {
		synchronized (LOCK) {
			return new DBTask(this.db).getAll();
		}
	}

	@Override
	public List<Album> getAllAlbum() {
		synchronized (LOCK) {
			return new DBAlbum(this.db).getAll();
		}
	}	

	@Override
	public List<LocalVideo> getAllLocalVideo() {
		synchronized (LOCK) {
			return new DBLocalVideo(this.db).getAll();
		}
	}

	@Override
	public List<Image> getAllImage() {
		synchronized (LOCK) {
			return new DBImage(this.db).getAll();
		}
	}
	
	@Override
	public List<NetVideo> getAllNetVideos() {
		synchronized (LOCK) {
			return new DBNetVideo(this.db).getAllVideos();
		}
	}
	
	@Override
	public List<NetVideo> getNetVideosByAlbumRefer(String albumRefer) {
		synchronized (LOCK) {
			return new DBNetVideo(this.db).getVideosByAlbumRefer(albumRefer);
		}
	}

	@Override
	public List<VisiteSite> getAllVisitedSite() {
		synchronized (LOCK) {
			return new DBVisiteSite(this.db).getAll();
		}
	}

	@Override
	public List<Suggestion> getHistoryVisiteSiteLike(String query) {
		synchronized (LOCK) {
			return new DBVisiteSite(this.db).getHistoryVisiteSiteLike(query);
		}
	}

	@Override
	public List<SearchKeyword> getAllSearchKeyword() {
		synchronized (LOCK) {
			return new DBSearchKeyword(this.db).getAll();
		}
	}

	@Override
	public List<Suggestion> getHistorySearchKeywordLike(String query) {
		synchronized (LOCK) {
			return new DBSearchKeyword(this.db).getHistorySearchKeywordLike(query);
		}
	}
}
