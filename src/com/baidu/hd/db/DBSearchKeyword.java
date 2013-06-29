package com.baidu.hd.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.browser.db.Suggestion;
import com.baidu.browser.visitesite.SearchKeyword;
import com.baidu.hd.ui.SuggestionAdapter.SuggestionType;

/**
 * @ClassName: DBSearchKeyword 
 * @Description: �����ؼ��ֲ�����
 * @author LEIKANG 
 * @date 2012-12-19 ����1:10:45
 */
public class DBSearchKeyword {
	
	// table
	private static final String T_NAME = "searchkeyword";
	
	// field
	private static final String F_ID = "_id";
	private static final String F_KEYWORD = "keyword";
	private static final String F_SEARCHTIME = "searchtime";
  
	
	// sql
	static final String CreateTabelSql = String.format(
			"CREATE TABLE %s(" +
			"%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"%s TEXT, " +
			"%s INTEGER, " +
			"reserved TEXT)", 
			T_NAME, F_ID, F_KEYWORD, F_SEARCHTIME);
	static final String DeleteTableSql = "DROP TABLE " + T_NAME;
	
	private SQLiteDatabase db = null;
	
	public DBSearchKeyword(SQLiteDatabase db) {
		this.db = db;
	}
	
	public long add(SearchKeyword value) {

		long ret = this.db.insert(T_NAME, null, this.getContentValues(value));
		value.id = (ret);
		return ret;
	}
	
	public void update(SearchKeyword value) {
		db.update(T_NAME, this.getContentValues(value), 
				F_ID + "=?", new String[]{value.id + ""});
	}
	
	public void delete(SearchKeyword value) {
		this.db.delete(T_NAME, F_ID + "=?", new String[]{value.id + ""});
	}
	
	public List<SearchKeyword> getAll() {
		
		Cursor c = null;
		
		try {
			c = this.db.query(T_NAME, 
				new String[]{F_ID, F_KEYWORD, F_SEARCHTIME}, 
				null,
				null, 
				null, 
				null, 
				F_SEARCHTIME + " desc");
			return this.getModuleList(c);
		}
		finally {
			if(c != null) {
				c.close();
			}
		}
	}
	
	private ContentValues getContentValues(SearchKeyword value) {
		
		ContentValues values = new ContentValues();
		values.put(F_KEYWORD, value.keyword);
		values.put(F_SEARCHTIME, value.searchTime);
 
		
		return values;
	}
	
	private List<SearchKeyword> getModuleList(Cursor c) {
		
		List<SearchKeyword> result = new ArrayList<SearchKeyword>();
		if(!c.moveToFirst()) {
			return result;
		}
		do{
			SearchKeyword value = new SearchKeyword();
			value.id = (c.getLong(c.getColumnIndex(F_ID)));
			value.keyword = (c.getString(c.getColumnIndex(F_KEYWORD)));
			value.searchTime = (c.getLong(c.getColumnIndex(F_SEARCHTIME)));
			result.add(value);
		} while(c.moveToNext());
		
		return result;
	}

	/**
	 * @Title: getHistorySearchKeywordLike 
	 * @Description: ��ȡ ��ѯ ��ʷ��¼
	 * @param  query
	 * @param     �趨�ļ� 
	 * @return List<Suggestion>  �������� 
	 * @throws
	 */
	public List<Suggestion> getHistorySearchKeywordLike(String query) {
		
        query = query.replaceAll("\'", "\"");
        
		Cursor c = null;
		
		try {
			c = this.db.query(T_NAME, 
				new String[]{F_ID, F_KEYWORD, F_SEARCHTIME}, 
				F_KEYWORD + " like '%" + query + "%'",
				null, 
				null, 
				null, 
				F_SEARCHTIME + " desc");
			return this.getSearchKeywordList(c);
		}
		finally {
			if(c != null) {
				c.close();
			}
		}
		
	}
	
	private List<Suggestion> getSearchKeywordList(Cursor c) {
		
		List<Suggestion> result = new ArrayList<Suggestion>();
		if(!c.moveToFirst()) {
			return result;
		}
		do{
			Suggestion value = new Suggestion();
			value.title = c.getString(c.getColumnIndex(F_KEYWORD));
			//���ｫ�ؼ�������Ϊurl������null
			value.url = c.getString(c.getColumnIndex(F_KEYWORD));
			value.type = SuggestionType.HISTORY;
			result.add(value);
		} while(c.moveToNext());
		
		return result;
	}
}
