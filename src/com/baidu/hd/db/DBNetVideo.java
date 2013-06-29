package com.baidu.hd.db;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.hd.log.Logger;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.VideoFactory;

public class DBNetVideo {
	
	private Logger logger = new Logger("DBNetVideo");
	// table
	static final String T_NAME = "net_video";

	// field
	static final String F_ID = "_ID";
	static final String F_ALBUMID = "albumId";
	static final String F_NAME = "name";
	static final String F_TYPE = "type";				// ��Ƶ����
	static final String F_GRADE = "grade";				// ��Ƶ�ȼ�(ָ�����ȵȼ�)
	static final String F_REFER = "refer";				// ��Ƶ����ҳ��refer
	static final String F_ALBUMREFER = "album_refer";   // ��Ƶ�б�ҳ��refer
	static final String F_URL = "url";                  // bdhd��ַ
	static final String F_POSITION = "position";		// �Ѳ��ŵ�ʱ��
	static final String F_IMAGE = "image";
	static final String F_EXTERNAL = "external";
	
	// sql
	static final String CreateTabelSql = String.format("CREATE TABLE %s(" + 
			"%s INTEGER PRIMARY KEY AUTOINCREMENT, " + 
			"%s INTEGER, " + 
			"%s TEXT, " + 
			"%s INTEGER, " +
			"%s INTEGER, " +
			"%s TEXT, " + 
			"%s TEXT, " +
			"%s TEXT, " + 
			"%s INTEGER, " + 
			"%s TEXT, "	+ 
			"%s TEXT, " + 
			"reserved TEXT)",
			T_NAME, F_ID, F_ALBUMID, F_NAME, F_TYPE, F_GRADE, F_REFER, F_ALBUMREFER,
			F_URL, F_POSITION, F_IMAGE, F_EXTERNAL);
			
	static final String DeleteTableSql = "DROP TABLE " + T_NAME;

	private SQLiteDatabase db = null;

	public DBNetVideo(SQLiteDatabase db) {
		this.db = db;
	}

	public long add(NetVideo value) {
		long ret = this.db.insert(T_NAME, null, this.getContentValues(value));
		value.setId(ret);
		return ret;
	}

	public void update(NetVideo value) {
		logger.d("update");
		db.update(T_NAME, this.getContentValues(value), F_ID + "=?",
				new String[] { value.getId() + "" });
	}

	public void delete(NetVideo value) {
		logger.d("delete");
		this.db.delete(T_NAME, F_ID + "=?", new String[] { value.getId() + "" });
	}

	public void clear() {
		this.db.delete(T_NAME, null, null);
	}
	
	/** ��ȡ���е���Ƶ */
	public List<NetVideo> getAllVideos() {
		Cursor c = null;
		// ��������
//		db.beginTransaction();
		try {
			c = this.db.query(T_NAME, new String[] { F_ID, F_ALBUMID, F_NAME, F_TYPE, F_GRADE, F_REFER, F_ALBUMREFER, F_URL, F_POSITION, F_IMAGE, F_EXTERNAL},
					null, null, null, null, F_ID+" asc");
			return this.getModuleList(c);
		} catch (Exception e) {
			logger.e(e.toString());
			
			return null;
			
		} finally {
			if (c != null) {
				c.close();
			}
			// �ύ����
//			db.setTransactionSuccessful();
//			db.endTransaction();
		}
	}
	
	/** ������Ƶ */
	public List<NetVideo> getVideosByAlbumRefer(String albumRefer) {
		Cursor c = null;

		try {
			c = this.db.query(T_NAME, new String[] { F_ID, F_ALBUMID, F_NAME, F_TYPE, F_GRADE, F_REFER, F_ALBUMREFER, F_URL, F_POSITION, F_IMAGE, F_EXTERNAL},
					F_ALBUMREFER+"=?", new String[]{albumRefer}, null, null, F_ID+" asc");
			return this.getModuleList(c);
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}
	
	/** ��ȡ�����ղ��еľ缯����Ƶ */
/*	public List<NetVideo> getAllFavoriteVideos() {
		//
	}
*/
	private ContentValues getContentValues(NetVideo value) {
		
		ContentValues values = new ContentValues();
		values.put(F_ALBUMID, value.getAlbumId());
		values.put(F_NAME, value.getName());
		values.put(F_TYPE, value.getType());
		values.put(F_GRADE, value.getGrade());
		values.put(F_REFER, value.getRefer());
		values.put(F_ALBUMREFER, value.getAlbumRefer());
		values.put(F_URL, value.getUrl());
		values.put(F_POSITION, value.getPosition());
		values.put(F_IMAGE, value.getImage());
		
		try {
			JSONObject o = new JSONObject();
			o.put("episode", value.getEpisode());
			values.put(F_EXTERNAL, o.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return values;
	}

	private List<NetVideo> getModuleList(Cursor c) {

		List<NetVideo> result = new ArrayList<NetVideo>();
		if (!c.moveToFirst()) {
			return result;
		}
		do {
			NetVideo value = VideoFactory.create(false).toNet();
			value.setId(c.getLong(c.getColumnIndex(F_ID)));
			value.setAlbumId(c.getLong(c.getColumnIndex(F_ALBUMID)));
			value.setName(c.getString(c.getColumnIndex(F_NAME)));
			value.setType(c.getInt(c.getColumnIndex(F_TYPE)));
			value.setGrade(c.getInt(c.getColumnIndex(F_GRADE)));
			value.setRefer(c.getString(c.getColumnIndex(F_REFER)));
			value.setAlbumRefer(c.getString(c.getColumnIndex(F_ALBUMREFER)));
			value.setUrl(c.getString(c.getColumnIndex(F_URL)));
			value.setPosition(c.getInt(c.getColumnIndex(F_POSITION)));
			value.setImage(c.getString(c.getColumnIndex(F_IMAGE)));
			
			String external = c.getString(c.getColumnIndex(F_EXTERNAL));
			
			if (external != null) {
				try {
					JSONObject o = new JSONObject(external);
					value.setEpisode(o.getString("episode"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			result.add(value);
		} while (c.moveToNext());

		return result;
	}
}
