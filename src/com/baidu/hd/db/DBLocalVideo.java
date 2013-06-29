package com.baidu.hd.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.hd.log.Logger;
import com.baidu.hd.module.album.LocalVideo;
import com.baidu.hd.module.album.VideoFactory;

class DBLocalVideo {
	
	private Logger logger = new Logger(DBLocalVideo.class.getSimpleName());
	
	// table
	private static final String T_NAME = "local_video";

	// field	
	private static final String F_ID = "_ID";
	private static final String F_FULLNAME = "fullname";
	private static final String F_TICK = "tick";
	private static final String F_TOTALSIZE = "totalsize";
	private static final String F_DURATION = "duration";

	// sql
	static final String CreateTabelSql = String.format(
			"CREATE TABLE %s(" +
			"%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"%s TEXT, " + 
			"%s INTEGER, " +
			"%s INTEGER, " +
			"%s INTEGER, " +
			"reserved TEXT)", 
			T_NAME, F_ID, F_FULLNAME, F_TICK, F_TOTALSIZE, F_DURATION);
	static final String DeleteTableSql = "DROP TABLE " + T_NAME;
		
	private SQLiteDatabase db = null;	
	public DBLocalVideo(SQLiteDatabase db) {
		this.db = db;
	}
	
	public long add(LocalVideo value) {
		long ret = this.db.insert(T_NAME, null, this.getContentValues(value));
		value.setId(ret);
		return ret;
	}
	
	public void update(LocalVideo value) {
		if (db == null) {
			logger.e("db is null");
			return;
		}
		else if (!db.isOpen()) {
			logger.e("db is closed");
			return;
		}
		
		db.update(T_NAME, this.getContentValues(value), F_ID + "=?", new String[]{value.getId() + ""});
	}
	
	public void delete(LocalVideo value) {
		this.db.delete(T_NAME, F_ID + "=?", new String[]{value.getId() + ""});
	}
	
	public void clear() {
		this.db.delete(T_NAME, null, null);
	}
	
	public List<LocalVideo> getAll() {
		
		Cursor c = null;
		
		try {
			c = this.db.query(T_NAME, 
				new String[]{F_ID, F_FULLNAME, F_TICK, F_TOTALSIZE, F_DURATION}, 
				null,
				null, 
				null, 
				null, 
				F_ID + " desc");
			return this.getModuleList(c);
		}
		finally {
			if(c != null) {
				c.close();
			}
		}
	}
	
	private ContentValues getContentValues(LocalVideo value) {
		ContentValues values = new ContentValues();
		values.put(F_FULLNAME, value.getFullName());
		values.put(F_TICK, value.getPosition());
		values.put(F_TOTALSIZE, value.getTotalSize());
		values.put(F_DURATION, value.getDuration());
		return values;
	}
	
	private List<LocalVideo> getModuleList(Cursor c) {
		
		List<LocalVideo> result = new ArrayList<LocalVideo>();
		if(!c.moveToFirst()) {
			return result;
		}
		do{
			LocalVideo value = VideoFactory.create(true).toLocal();
			value.setId(c.getLong(c.getColumnIndex(F_ID)));
			value.setFullName(c.getString(c.getColumnIndex(F_FULLNAME)));
			value.setPosition(c.getInt(c.getColumnIndex(F_TICK)));
			value.setTotalSize(c.getLong(c.getColumnIndex(F_TOTALSIZE)));
			value.setDuration(c.getInt(c.getColumnIndex(F_DURATION)));
			result.add(value);
		} while(c.moveToNext());
		
		return result;
	}
}
