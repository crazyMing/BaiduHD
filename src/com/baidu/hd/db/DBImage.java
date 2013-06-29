package com.baidu.hd.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.hd.module.Image;

class DBImage {

	// table
	private static final String T_NAME = "image";

	// field	
	private static final String F_ID = "_ID";
	private static final String F_URL = "url";
	private static final String F_PATH = "path";
	private static final String F_TYPE = "type";

	// sql
	static final String CreateTabelSql = String.format(
			"CREATE TABLE %s(" +
			"%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"%s TEXT, " +
			"%s TEXT, " +
			"%s INTEGER, " +
			"reserved TEXT)", 
			T_NAME, F_ID, F_URL, F_PATH, F_TYPE);
	static final String DeleteTableSql = "DROP TABLE " + T_NAME;
		
	private SQLiteDatabase db = null;	
	public DBImage(SQLiteDatabase db) {
		this.db = db;
	}
	
	public long add(Image value) {
		
		ContentValues values = new ContentValues();
		values.put(F_URL, value.getUrl());
		values.put(F_PATH, value.getPath());
		values.put(F_TYPE, value.getType());
		long ret = this.db.insert(T_NAME, null, values);
		value.setId(ret);
		return ret;
	}
	
	public void update(Image value) {
		
		// ²»ÄÜÐÞ¸Ä F_FULLNAME
		ContentValues values = new ContentValues();
		values.put(F_PATH, value.getPath());
		db.update(T_NAME, values, F_ID + "=?", new String[]{value.getId() + ""});
	}
	
	public void delete(Image value) {
		this.db.delete(T_NAME, F_ID + "=?", new String[]{value.getId() + ""});
	}

	public List<Image> getAll() {
		
		Cursor c = null;
		
		try {
			c = this.db.query(T_NAME, 
				new String[]{F_ID, F_URL, F_PATH, F_TYPE}, 
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
	
	private List<Image> getModuleList(Cursor c) {
		
		List<Image> result = new ArrayList<Image>();
		if(!c.moveToFirst()) {
			return result;
		}
		do{
			Image value = new Image();
			value.setId(c.getLong(c.getColumnIndex(F_ID)));
			value.setUrl(c.getString(c.getColumnIndex(F_URL)));
			value.setPath(c.getString(c.getColumnIndex(F_PATH)));
			value.setType(c.getInt(c.getColumnIndex(F_TYPE)));
			result.add(value);
		} while(c.moveToNext());
		
		return result;
	}
}
