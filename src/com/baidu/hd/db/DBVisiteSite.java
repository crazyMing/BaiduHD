package com.baidu.hd.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.browser.db.Suggestion;
import com.baidu.browser.visitesite.VisiteSite;
import com.baidu.hd.ui.SuggestionAdapter.SuggestionType;

/**
 * @ClassName: DBBrowserHistoryRecord 
 * @Description: 浏览器模式_历史记录_数据操作类
 * @date 2012-12-17 下午1:49:53
 */
public class DBVisiteSite {
	
	public static final int NORMAL = 0;
	
	public static final int BOOKMARK = 1;
	
	public static final int HISTORY = 1;
	
	// table
	private static final String T_NAME = "visitesite";
	
	// field
	private static final String F_ID = "_id";
	private static final String F_SITE_TITLE = "site_title";
	private static final String F_SITE_URL = "site_url";
	private static final String F_CREATE_TIME = "create_time";
	private static final String F_VISITED_TIME = "visited_time";
	private static final String F_IS_BOOKMARK = "is_bookmark";
	private static final String F_IS_HISTORY = "is_history";
	private static final String F_ICON = "icon";
	private static final String F_EXTERNAL = "external";
	
	static final String CreateTabelSql = String.format(
			"CREATE TABLE %s(" +
			"%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"%s TEXT, " +
			"%s TEXT, " +
			"%s TEXT, " +
			"%s TEXT, " +
			"%s INTEGER, " +
			"%s INTEGER, " +
			"%s BLOB, " +
			"%s TEXT, " +
			"reserved TEXT)", 
			T_NAME, F_ID, F_SITE_TITLE, F_SITE_URL, F_CREATE_TIME, F_VISITED_TIME, F_IS_BOOKMARK, F_IS_HISTORY, F_ICON, F_EXTERNAL);
	static final String DeleteTableSql = "DROP TABLE " + T_NAME;
	
	private SQLiteDatabase db = null;
	
	public DBVisiteSite(SQLiteDatabase db) {
		this.db = db;
		
		//this.db.execSQL(sqlDeleteUnused);
	}
	
	public long add(VisiteSite value) {

		long ret = this.db.insert(T_NAME, null, this.getContentValues(value));
		value.setId(ret);
		return ret;
	}
	
	public void update(VisiteSite value) {
		db.update(T_NAME, this.getContentValues(value), 
				F_ID + "=?", new String[]{value.getId() + ""});
	}
	
	public void delete(VisiteSite value) {
		this.db.delete(T_NAME, F_ID + "=?", new String[]{value.getId() + ""});
	}
	
	public List<VisiteSite> getAll() {
		
		Cursor c = null;
		
		try {
			c = this.db.query(T_NAME, 
				new String[]{F_ID, F_SITE_TITLE, F_SITE_URL, F_CREATE_TIME, F_VISITED_TIME, F_IS_BOOKMARK, F_IS_HISTORY, F_ICON, F_EXTERNAL}, 
				null,
				null, 
				null, 
				null, 
				F_VISITED_TIME + " desc");
			return this.getModuleList(c);
		}
		finally {
			if(c != null) {
				c.close();
			}
		}
	}
	
	private ContentValues getContentValues(VisiteSite value) {
		
		ContentValues values = new ContentValues();
		values.put(F_SITE_TITLE, value.getSiteTitle());
		values.put(F_SITE_URL, value.getSiteUrl());
		values.put(F_CREATE_TIME, value.getCreateTime());
		values.put(F_VISITED_TIME, value.getVisitedTime());
		values.put(F_IS_BOOKMARK, value.getIsBookMark());
		values.put(F_IS_HISTORY, value.getIsHistory());
		values.put(F_ICON, value.getIcon());
		values.put(F_EXTERNAL, "");
		
		return values;
	}
	
	private List<VisiteSite> getModuleList(Cursor c) {
		
		List<VisiteSite> result = new ArrayList<VisiteSite>();
		if(!c.moveToFirst()) {
			return result;
		}
		do{
			VisiteSite value = new VisiteSite();
			value.setId(c.getLong(c.getColumnIndex(F_ID)));
			value.setSiteTitle(c.getString(c.getColumnIndex(F_SITE_TITLE)));
			value.setSiteUrl(c.getString(c.getColumnIndex(F_SITE_URL)));
			value.setCreateTime(c.getLong(c.getColumnIndex(F_CREATE_TIME)));
			value.setVisitedTime(c.getLong(c.getColumnIndex(F_VISITED_TIME)));
			value.setIsBookMark(c.getInt(c.getColumnIndex(F_IS_BOOKMARK)));
			value.setIsHistory(c.getInt(c.getColumnIndex(F_IS_HISTORY)));
			value.setIcon(c.getBlob(c.getColumnIndex(F_ICON)));
			result.add(value);
		} while(c.moveToNext());
		
		return result;
	}

	/**
	 * @Title: getHistoryVisiteSiteLike 
	 * @Description: 获取 查询 浏览历史记录
	 * @param  query
	 * @param     设定文件 
	 * @return List<Suggestion>  返回类型 
	 * @throws
	 */
	public List<Suggestion> getHistoryVisiteSiteLike(String query) {
		
        query = query.replaceAll("\'", "\"");
        
		Cursor c = null;
		
		try {
			c = this.db.query(T_NAME, 
				new String[]{F_ID, F_SITE_TITLE, F_SITE_URL, F_CREATE_TIME, F_VISITED_TIME, F_IS_BOOKMARK, F_IS_HISTORY, F_ICON, F_EXTERNAL}, 
				F_SITE_URL + " like '%" + query + "%' and " + F_IS_HISTORY + " == " + HISTORY,
				null, 
				null, 
				null, 
				F_VISITED_TIME + " desc");
			return this.getHistoryList(c);
		}
		finally {
			if(c != null) {
				c.close();
			}
		}
		
	}
	
	private List<Suggestion> getHistoryList(Cursor c) {
		
		List<Suggestion> result = new ArrayList<Suggestion>();
		if(!c.moveToFirst()) {
			return result;
		}
		do{
			Suggestion value = new Suggestion();
			value.title = c.getString(c.getColumnIndex(F_SITE_TITLE));
			value.url = c.getString(c.getColumnIndex(F_SITE_URL));
			value.type =  SuggestionType.HISTORY;
			result.add(value);
		} while(c.moveToNext());
		
		return result;
	}

	
	public boolean deleteAllHistorys() {
	
		String sql = "update " + T_NAME +" set " + F_IS_HISTORY + " = " + NORMAL + " where " + F_IS_HISTORY + " = " + HISTORY;
		this.db.execSQL(sql);
		return true;
	}

	public boolean deleteAllBookmarks() {
		String sql = "update " + T_NAME +" set " + F_IS_BOOKMARK + " = " + NORMAL + " where " + F_IS_BOOKMARK + " = " + BOOKMARK;
		this.db.execSQL(sql);
		return true;
	}
}
