package com.baidu.hd.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 首页推荐列表 
 * @author LEIKANG
 */
public class SuggestionProvider extends ContentProvider{
	
	//URI的指定，此处的字符串必须和声明的authorities一致
	public static final String AUTHORITIES = "com.baidu.hd.SuggestionContentProvider";
	//数据库名称
	public static final String DATABASE_NAME = "appsearch.db";
	//数据库的版本
	public static final int DATABASE_VERSION = 1;
	//表名 
	//public static final String USERS_TABLE_NAME = "suggestion";
	
	public static final class SuggestionTableMetaData implements BaseColumns {
		
		/**这里类型为首页推荐类型 1： 推荐区域 2：搜索排行榜 3：热词标签 */
		public static final String TYPE_PLAYERBOX = "1";
		public static final String TYPE_RANKING = "2";
		public static final String TYPE_HOT = "3";
		
		// 表名
		public static final String TABLE_NAME = "suggestion";
		// 访问该ContentProvider的URI
		public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITIES + "/suggestion");
		// 该ContentProvider所返回的数据类型的定义
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.suggestionprovider.suggestion";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.suggestionprovider.suggestion";
		
		public static final String _ID = "id";
		public static final String _NAME = "name";
		public static final String _URL = "url";
		public static final String _NUMBER = "number";
		public static final String _UP_OR_DOWN = "up_or_down";
		public static final String _TYPE = "type";
		
		// 默认的排序方法
		public static final String DEFAULT_SORT_ORDER = "_id desc";
	}
	
	static final String CreateTabelSql = String.format(
			"CREATE TABLE %s(" + "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +	"%s TEXT, " + "%s TEXT, " + "%s TEXT, " +	"%s TEXT, " +
			"reserved TEXT)", 	SuggestionTableMetaData._ID, SuggestionTableMetaData._NAME, SuggestionTableMetaData._URL, 
			SuggestionTableMetaData._NUMBER, SuggestionTableMetaData._UP_OR_DOWN, SuggestionTableMetaData._TYPE);
	static final String DeleteTableSql = "DROP TABLE IF EXIST " + SuggestionTableMetaData.TABLE_NAME;
	
	//访问表的所有列
	public static final int INCOMING_USER_COLLECTION = 1;
	
	//访问单独的列
	public static final int INCOMING_USER_SINGLE = 2;
	
	//操作URI的类
	public static final UriMatcher uriMatcher;
	
	//为UriMatcher添加自定义的URI
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITIES, "/suggestion", INCOMING_USER_COLLECTION);
		uriMatcher.addURI(AUTHORITIES, "/suggestion/#", INCOMING_USER_SINGLE);
	}
	 
	@Override
	public boolean onCreate() {
		return false;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return null;
	}

	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}
	
}
