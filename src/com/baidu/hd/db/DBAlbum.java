package com.baidu.hd.db;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.hd.log.Logger;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.AlbumFactory;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.VideoFactory;
import com.baidu.hd.module.album.AlbumFactory.AlbumType;
import com.baidu.hd.util.StringUtil;

// 添加修改时间，倒序排列
class DBAlbum {
	private Logger logger = new Logger("DBAlbum");
	
	// table
	private static final String T_NAME = "album";

	// field
	private static final String F_ID = "_ID";
	private static final String F_LISTID = "listid";
	private static final String F_LISTNAME = "listname";
	private static final String F_REFER = "refer";
	private static final String F_IMAGE = "image";
	private static final String F_SITE = "site";
	private static final String F_TYPE = "type";
	private static final String F_HAVENEW = "have_new";
	private static final String F_ISFINISH = "is_finish";
	private static final String F_CURRENTID = "current_id";
	private static final String F_CURRENTNAME = "current_name";
	private static final String F_CURRENTREFER = "current_refer";
	private static final String F_CURRENTTICK = "current_tick";
	private static final String F_NEWESTID = "newest_id";
	private static final String F_EXTERNAL = "external";
	private static final String F_VISITTICK = "visit_tick";

	// sql
	static final String CreateTabelSql = String.format(
			"CREATE TABLE %s(" +
			"%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"%s TEXT, " +
			"%s TEXT, " +
			"%s TEXT, " +
			"%s TEXT, " +
			"%s TEXT, " +
			"%s INTEGER, " +
			"%s INTEGER, " +
			"%s INTEGER, " +
			"%s TEXT, " +
			"%s TEXT, " + 
			"%s TEXT, " +
			"%s INTEGER, " +
			"%s TEXT, " +
			"%s TEXT, " +
			"%s INTEGER, " +
			"reserved TEXT)", 
			T_NAME, F_ID, F_LISTID, F_LISTNAME, F_REFER, F_IMAGE, F_SITE, F_TYPE, F_HAVENEW, F_ISFINISH, 
			F_CURRENTID, F_CURRENTNAME, F_CURRENTREFER, F_CURRENTTICK, F_NEWESTID, F_EXTERNAL, F_VISITTICK);
	static final String DeleteTableSql = "DROP TABLE " + T_NAME;
	
	// 删除触发器
	private static final String R_DELETE = "trigger_delete";
	// 当album表中删除一条记录时，删除net_video表中的相关记录，WHERE根据ID判断。
	static final String CreateDeleteTrigger = String.format(
			"CREATE TRIGGER " +
			"%s BEFORE DELETE ON " +
			"%s BEGIN DELETE FROM " +
			"%s WHERE " +
			"%s = old." +
			"%s; END;",
			R_DELETE, T_NAME, DBNetVideo.T_NAME, DBNetVideo.F_ALBUMID, F_ID);

	private SQLiteDatabase db = null;
	public DBAlbum(SQLiteDatabase db) {
		this.db = db;
	}
	
	public long add(Album value) {
		long ret = this.db.insert(T_NAME, null, this.getContentValues(value));
		value.setId(ret);
		return ret;
	}
	
	public void update(Album value) {
		db.update(T_NAME, this.getContentValues(value), 
				F_ID + "=?", new String[]{value.getId() + ""});
	}
	
	public void delete(Album value) {
		this.db.delete(T_NAME, F_ID + "=?", new String[]{value.getId() + ""});
	}
	
	public List<Album> getAll() {
		logger.d("getAll");
		
		List<Album> result = new ArrayList<Album>();
		Cursor c = null;
		try {
			// 创建事务
//			db.beginTransaction();
			
			c = this.db.query(T_NAME, 
				new String[]{F_ID, F_LISTID, F_LISTNAME, F_REFER, F_IMAGE, F_SITE, F_TYPE, F_HAVENEW, F_ISFINISH, 
					F_CURRENTID, F_CURRENTNAME, F_CURRENTREFER, F_CURRENTTICK, F_EXTERNAL, F_NEWESTID}, 
				null,
				null, 
				null, 
				null, 
				F_VISITTICK + " desc");
			result =  this.getModuleList(c);
			// 提交事务
//			db.setTransactionSuccessful();
//			db.endTransaction();
		}
		finally {
			if(c != null) {
				c.close();
			}
		}
		return result;
	}
	
	private ContentValues getContentValues(Album value) {
		
		ContentValues values = new ContentValues();
		NetVideo video = value.getCurrent();
		values.put(F_LISTID, value.getListId());
		values.put(F_LISTNAME, value.getListName());
		values.put(F_REFER, value.getRefer());
		values.put(F_IMAGE, value.getImage());
		values.put(F_SITE, value.getSite());
		values.put(F_TYPE, value.getType());
		values.put(F_HAVENEW, value.isHaveNew() ? 1 : 0);
		values.put(F_ISFINISH, value.isFinished() ? 1 : 0);
		if (video != null && video.getRefer() != null) {
			values.put(F_CURRENTID, video.getEpisode());
			values.put(F_CURRENTNAME, video.getName());
			values.put(F_CURRENTREFER, video.getRefer());
			values.put(F_CURRENTTICK, video.getPosition());
		}
		values.put(F_NEWESTID, value.getNewestId());
		values.put(F_VISITTICK, System.currentTimeMillis());
		
		try {
			JSONObject o = new JSONObject();
			o.put("push", value.isPush());
			o.put("personalHistory", value.isPersonalHistory());
			o.put("favorite", value.isFavorite());
			o.put("isHomeShow", value.isHomeShow());
			o.put("isDownload", value.isDownload());
			o.put("newestCount", value.getNewestCount());
			o.put("fromType", value.getFromType());
			
			if (video != null && video.getUrl()!=null && !video.getUrl().equals("")) {
				o.put("currentUrl", video.getUrl());
			}
			else {
				o.put("currentUrl", "");
			}
			
			values.put(F_EXTERNAL, o.toString());
		} catch(JSONException e) {
			e.printStackTrace();
		}
		
		return values;
	}
	
	private List<Album> getModuleList(Cursor c) {
		
		List<Album> result = new ArrayList<Album>();
		if(!c.moveToFirst()) {
			return result;
		}
		do{
			String external = c.getString(c.getColumnIndex(F_EXTERNAL));
			JSONObject o = null;
			int fromType = 2;
			try {
				o = new JSONObject(external);
				fromType = o.getInt("fromType");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			
			// 剧集来源 0--大站服务器，1--大站本地，2--小站服务器，3--小站本地
			Album value = null;
			switch(fromType) {
			case 0:
				value = AlbumFactory.getInstance().createAlbum(AlbumType.BIG_SERVER);
				break;
			case 1:
				value = AlbumFactory.getInstance().createAlbum(AlbumType.BIG_NATIVE);
				break;
			case 2:
				value = AlbumFactory.getInstance().createAlbum(AlbumType.SMALL_SERVER);
				break;
			case 3:
				value = AlbumFactory.getInstance().createAlbum(AlbumType.SMALL_NATIVE);
				break;
			default:
					return null;
			}
			value.setId(c.getLong(c.getColumnIndex(F_ID)));
			value.setListId(c.getString(c.getColumnIndex(F_LISTID)));
			value.setListName(c.getString(c.getColumnIndex(F_LISTNAME)));
			value.setRefer(c.getString(c.getColumnIndex(F_REFER)));
			value.setImage(c.getString(c.getColumnIndex(F_IMAGE)));
			value.setSite(c.getString(c.getColumnIndex(F_SITE)));
			value.setType(c.getInt(c.getColumnIndex(F_TYPE)));
			value.setHaveNew(c.getInt(c.getColumnIndex(F_HAVENEW)) == 1);
			value.setFinished(c.getInt(c.getColumnIndex(F_ISFINISH)) == 1);
			
			NetVideo video = VideoFactory.create(false).toNet();
			video.setEpisode(c.getString(c.getColumnIndex(F_CURRENTID)));
			video.setName(c.getString(c.getColumnIndex(F_CURRENTNAME)));
			video.setRefer(c.getString(c.getColumnIndex(F_CURRENTREFER)));
			video.setPosition(c.getInt(c.getColumnIndex(F_CURRENTTICK)));
			video.setAlbumRefer(value.getRefer());
			value.setCurrent(video);
			
			value.setNewestId(c.getString(c.getColumnIndex(F_NEWESTID)));
//			String external = c.getString(c.getColumnIndex(F_EXTERNAL));
			
			if(!StringUtil.isEmpty(external)) {
				try {
//					JSONObject o = new JSONObject(external);
					value.setPush(o.getBoolean("push"));
					value.setPersonalHistory(o.getBoolean("personalHistory"));
					value.setFavorite(o.getBoolean("favorite"));
					value.setHomeShow(o.getBoolean("isHomeShow"));
					value.setDownload(o.getBoolean("isDownload"));
					value.setNewestCount(o.getInt("newestCount"));
					value.setFromType(o.getInt("fromType"));
					if (video != null) {
						video.setUrl(o.getString("currentUrl"));
						video.setType(value.getType());
					}
					
				} catch(JSONException e) {
					e.printStackTrace();
				}
			}
			
			result.add(value);
		} while(c.moveToNext());
		
		return result;
	}
}
