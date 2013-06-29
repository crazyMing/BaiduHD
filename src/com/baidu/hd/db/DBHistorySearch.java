package com.baidu.hd.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.hd.module.HistorySearch;

class DBHistorySearch {

	// table
	private static final String T_NAME = "history_search";

	// field
	private static final String F_ID = "_ID";
	private static final String F_TEXT = "text";
	private static final String F_TICK = "tick";
	private static final String F_ISURL = "isurl";
	
	// sql
	static final String CreateTabelSql = String.format(
			"CREATE TABLE %s(" +
			"%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"%s TEXT, " +
			"%s INTEGER, " +
			"%s INTEGER, " +
			"reserved TEXT)",  
			T_NAME, F_ID, F_TEXT, F_TICK, F_ISURL);
	static final String DeleteTableSql = "DROP TABLE " + T_NAME;
	
	private SQLiteDatabase db = null;
	public DBHistorySearch(SQLiteDatabase db) {
		this.db = db;
	}

	public boolean update(HistorySearch value) 
	{
		Cursor c = null;
		
		try {
			c = this.db.query(T_NAME, 
				new String[]{F_ID}, 
				F_TEXT + "=?", 
				new String[]{value.getText()}, 
				null, 
				null, 
				null);
			boolean exist = c.moveToFirst();
			
			//更新时，要删除老的，插入新的，而不是直接更新，这样是为了能把最新的放在顶端
			if(exist) 
			{
				String id = c.getLong(c.getColumnIndex(F_ID)) + "";
				ContentValues values = new ContentValues();
				values.put(F_TICK, new Date().getTime() + "");
	//			return this.db.update(T_NAME, values, F_ID + "=?", new String[]{id}) == 1;
				this.db.delete(T_NAME, F_ID + "=?", new String[]{id});
			} 
	//		else 
			{
				ContentValues values = new ContentValues();
				values.put(F_TEXT, value.getText());
				values.put(F_TICK, new Date().getTime() + "");
				values.put(F_ISURL, value.isUrl() ? "1" : "0");
				return this.db.insert(T_NAME, null, values) != 0;
			}
		}
		finally {
			if(c != null) {
				c.close();
			}
		}
	}

	public boolean delete(HistorySearch value) 
	{
		if (null != value)
		{
			Cursor c = null;
			try {
				c = this.db.query(T_NAME, 
					new String[]{F_ID}, 
					F_TEXT + "=?", 
					new String[]{value.getText()}, 
					null, 
					null, 
					null);
				
				if(c.moveToFirst()) 
				{
					String id = c.getLong(c.getColumnIndex(F_ID)) + "";
					ContentValues values = new ContentValues();
					values.put(F_TICK, new Date().getTime() + "");
					int i = this.db.delete(T_NAME, F_ID + "=?", new String[]{id});
					return i >= 1;
				} 
			}
			finally {
				if(c != null) {
					c.close();
				}
			}
		}
		
		int i = this.db.delete(T_NAME, null, null);
		return i >= 1;
	}
	
	public List<HistorySearch> get(int count) {
		
		Cursor c = null;
		
		try {
			c = this.db.query(T_NAME, 
					new String[]{F_ID, F_TEXT, F_ISURL}, 
					null,
					null, 
					null, 
					null, 
					F_ID + " desc", 
					"" + count);
	
			return this.getModuleList(c);
		}
		finally {
			if(c != null) {
				c.close();
			}
		}
	}
	
	public List<HistorySearch> search(String keyword, int count)
	{		
		if (null == keyword) {
			keyword = new String();
		}
		
		//过滤掉'单引号字符
		keyword = keyword.replace('\'', ' ');
		
		Cursor c = null;
		
		try {
			c = this.db.query(T_NAME, 
					new String[]{F_ID, F_TEXT, F_ISURL}, 
					String.format("%s like '%%%s%%'", F_TEXT, keyword), 
					null,
					null, 
					null,
					F_ID + " DESC", 
					count + "");
			return this.getModuleList(c);
		}
		finally {
			if(c != null) {
				c.close();
			}
		}
	}
	
	private List<HistorySearch> getModuleList(Cursor c) 
	{
		List<HistorySearch> result = new ArrayList<HistorySearch>();
		
		if(!c.moveToFirst()) 
		{
			return result;
		}
		
		do
		{
			HistorySearch value = new HistorySearch();
			value.setId(c.getLong(c.getColumnIndex(F_ID)));
			value.setText(c.getString(c.getColumnIndex(F_TEXT)));
			value.setUrl(c.getInt(c.getColumnIndex(F_ISURL)) == 1);
			value.setHistory(true);
			result.add(value);
		} 
		while(c.moveToNext());
		
		return result;
	}
}
