package com.baidu.hd.db;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.browser.visitesite.SearchKeyword;
import com.baidu.browser.visitesite.VisiteSite;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.HistorySearch;
import com.baidu.hd.module.Image;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.LocalVideo;
import com.baidu.hd.module.album.NetVideo;

public class DBWriterImpl implements DBWriter {
	private Logger logger = new Logger("DBWriterImpl");

	private SQLiteDatabase db = null;
	
	@Override
	public SQLiteDatabase getDatabase() {
		return this.db;
	}
	
	/** 批量添加子任务 */
	private boolean mCancel = false;
	private boolean mBatchAdding = false;
	private Object mLock = new Object();
	
	@Override
	public void setContext(Context ctx) {
		logger.d("setContext");
		
		if(ctx != null) {
			logger.d("getWritableDatabase");
			if (db == null || !db.isOpen()) {
				DatabaseHelper helper = new DatabaseHelper(ctx);
				this.db = helper.getWritableDatabase();
			}
		}
		else {
			synchronized (mLock) {
				if(mBatchAdding) {
					mCancel = true;
					try {
						mLock.wait();
					} catch(InterruptedException e)  {
						e.printStackTrace();
					}
				}
			}
			if (db!=null && db.isOpen()) {
				this.db.close();
			}
			logger.d("db close");
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

	/*
	 * 历史搜索记录
	 */
	@Override
	public boolean updateHistorySearch(HistorySearch value) {
		return new DBHistorySearch(this.db).update(value);
	}
	
	@Override
	public boolean deleteHistorySearch(HistorySearch value) {
		return new DBHistorySearch(this.db).delete(value);
	}
	
	@Override
	public boolean batchAddSubTask(List<Task> value) {
		synchronized (mLock) {
			mBatchAdding = true;
			
			db.beginTransaction();
			for(Task t: value) {
				if(mCancel) {
					db.endTransaction();
					mLock.notify();
					return false;
				}
				addTask(t);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			mBatchAdding = false;
		}
		return true;
	}

	@Override
	public boolean addTask(Task value) {
		return new DBTask(this.db).add(value) != -1;
	}

	@Override
	public boolean updateTask(Task value) {
		new DBTask(this.db).update(value);
		return true;
	}

	@Override
	public boolean removeTask(Task value) {
		new DBTask(this.db).delete(value);
		return false;
	}

	@Override
	public boolean modifyAlbum(Album value, Action action) {
		
		DBAlbum handler = new DBAlbum(this.db);
		switch(action) {
		case Add:
			return handler.add(value) != -1;
		case Update:
			handler.update(value);
			return true;
		case Delete:
			handler.delete(value);
			return true;
		case Clear:
			return false;
		default:
			return false;
		}
	}	

	@Override
	public boolean modifyLocalVideo(LocalVideo value, Action action) {
		
		DBLocalVideo handler = new DBLocalVideo(this.db);
		switch(action) {
		case Add:
			return handler.add(value) != -1;
		case Update:
			handler.update(value);
			return true;
		case Delete:
			handler.delete(value);
			return true;
		case Clear:
			handler.clear();
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean modifyImage(Image value, Action action) {

		DBImage handler = new DBImage(this.db);
		switch(action) {
		case Add:
			return handler.add(value) != -1;
		case Update:
			handler.update(value);
			return true;
		case Delete:
			handler.delete(value);
			return true;
		default:
			return false;
		}
	}
	
	@Override
	public boolean modifyNetVideo(NetVideo value, Action action) {

		DBNetVideo handler = new DBNetVideo(this.db);
		switch(action) {
		case Add:
			return handler.add(value) != -1;
		case Update:
			handler.update(value);
			return true;
		case Delete:
			handler.delete(value);
			return true;
		case Clear:
			handler.clear();
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean modifyVisiteSite(VisiteSite value, Action action) {
		DBVisiteSite handler = new DBVisiteSite(this.db);
		switch(action) {
		case Add:
			return handler.add(value) != -1;
		case Update:
			handler.update(value);
			return true;
		case Delete:
			handler.delete(value);
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean modifySearchKeyword(SearchKeyword value,
			Action action) {
		DBSearchKeyword handler = new DBSearchKeyword(this.db);
		switch(action) {
		case Add:
			return handler.add(value) != -1;
		case Update:
			handler.update(value);
			return true;
		case Delete:
			handler.delete(value);
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean deleteAllHistorys() {
		DBVisiteSite handler = new DBVisiteSite(this.db);
		return handler.deleteAllHistorys();
	}

	@Override
	public boolean deleteAllBookmarks() {
		DBVisiteSite handler = new DBVisiteSite(this.db);
		return handler.deleteAllBookmarks();
	}
}






