package com.baidu.hd.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.baidu.hd.log.Logger;

/**
 * 
 * version对应的版本
 * 4 -- 1.1
 * 5 -- 1.2
 */
public class DatabaseHelper extends SQLiteOpenHelper{

	private Logger logger = new Logger("DatabaseHelper");
	
	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	
	public DatabaseHelper(Context context, String name, int version){
		this(context, name, null, version);
	}
	
	public DatabaseHelper(Context context) {
		this(context, "bdplayer_database", 5);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		this.logger.i("onCreate");
		db.execSQL(DBTask.CreateTabelSql);
		db.execSQL(DBHistorySearch.CreateTabelSql);
		db.execSQL(DBAlbum.CreateTabelSql);
		db.execSQL(DBLocalVideo.CreateTabelSql);
		db.execSQL(DBImage.CreateTabelSql);
		db.execSQL(DBVisiteSite.CreateTabelSql);
		db.execSQL(DBSearchKeyword.CreateTabelSql);
		db.execSQL(DBNetVideo.CreateTabelSql);
		
		db.execSQL(DBAlbum.CreateDeleteTrigger);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		this.logger.i("onUpgrade oldVersion:" + oldVersion + " newVersion:" + newVersion);
		
		if (oldVersion == 4) {
			db.execSQL(DBVisiteSite.CreateTabelSql);
			db.execSQL(DBSearchKeyword.CreateTabelSql);
		}
	}

	@Override
	public synchronized void close() {
		super.close();
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}
