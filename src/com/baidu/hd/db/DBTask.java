package com.baidu.hd.db;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.hd.module.BigSiteTask;
import com.baidu.hd.module.SmallSiteTask;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.TaskFactory;
import com.baidu.hd.module.Task.Type;

class DBTask {
	
	// table
	private static final String T_NAME = "task";

	// field	
	private static final String F_ID = "_ID";
	private static final String F_URL = "url";
	private static final String F_REFER = "refer";
	private static final String F_NAME = "name";
	private static final String F_FILENAME = "filename";
	private static final String F_FOLDERNAME = "foldername";
	private static final String F_TOTALSIZE = "totalsize";
	private static final String F_DOWNLOADEDSIZE = "downloadedsize";
	private static final String F_PERCENT = "percent";
	private static final String F_STATE = "state";
	private static final String F_TYPE = "type";
	private static final String F_VIDEOTYPE = "videotype";
	private static final String F_EXTERNAL = "external";

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
			"%s INTEGER, " + 
			"%s INTEGER, " +
			"%s INTEGER, " + 
			"%s TEXT, " +
			"reserved TEXT)", 
			T_NAME, F_ID, F_URL, F_REFER, F_NAME, F_FILENAME, F_FOLDERNAME, F_TOTALSIZE, 
			F_DOWNLOADEDSIZE, F_PERCENT, F_STATE, F_TYPE, F_VIDEOTYPE, F_EXTERNAL);
	static final String DeleteTableSql = "DROP TABLE " + T_NAME;
		
	private SQLiteDatabase db = null;	
	public DBTask(SQLiteDatabase db) {
		this.db = db;
	}
	
	public long add(Task value) {
		
		ContentValues values = new ContentValues();
		values.put(F_URL, value.getUrl());
		values.put(F_REFER, value.getRefer());
		values.put(F_NAME, value.getName());
		values.put(F_FILENAME, value.getFileName());
		values.put(F_FOLDERNAME, value.getFolderName());
		values.put(F_TOTALSIZE, value.getTotalSize());
		values.put(F_DOWNLOADEDSIZE, value.getDownloadSize());
		values.put(F_PERCENT, value.getPercent());
		values.put(F_STATE, value.getState());
		values.put(F_TYPE, value.getType());
		values.put(F_VIDEOTYPE, value.getVideoType());
		values.put(F_EXTERNAL, this.getExternal(value));
		long ret = this.db.insert(T_NAME, null, values);
		value.setId(ret);
		return ret;
	}
	
	public void update(Task value) {
		
		// F_TYPE²»ÄÜÐÞ¸Ä
		ContentValues values = new ContentValues();
		values.put(F_URL, value.getUrl());
		values.put(F_REFER, value.getRefer());
		values.put(F_NAME, value.getName());
		values.put(F_FILENAME, value.getFileName());
		values.put(F_FOLDERNAME, value.getFolderName());
		values.put(F_TOTALSIZE, value.getTotalSize());
		values.put(F_DOWNLOADEDSIZE, value.getDownloadSize());
		values.put(F_PERCENT, value.getPercent());
		values.put(F_STATE, value.getState());
		values.put(F_VIDEOTYPE, value.getVideoType());
		values.put(F_EXTERNAL, this.getExternal(value));
		db.update(T_NAME, values, F_ID + "=?", new String[]{value.getId() + ""});
	}
	
	public void delete(Task value) {
		if(value.getType() == Type.Big) {
			this.db.delete(T_NAME, F_REFER + "=?", new String[]{value.getKey()});
		} else {
			this.db.delete(T_NAME, F_URL + "=?", new String[]{value.getKey()});
		}
	}
	
	public List<Task> getAll() {
		
		Cursor c = null;
		
		try {
			c = this.db.query(T_NAME, 
				new String[]{F_ID, F_URL, F_REFER, F_NAME, F_FILENAME, F_FOLDERNAME, 
					F_TOTALSIZE, F_DOWNLOADEDSIZE, F_PERCENT, F_STATE, F_TYPE, F_VIDEOTYPE, F_EXTERNAL}, 
				null,
				null, 
				null, 
				null, 
				F_ID);
			return this.getModuleList(c);
		}
		finally {
			if(c != null) {
				c.close();
			}
		}
	}
	
	private List<Task> getModuleList(Cursor c) {
		
		List<Task> result = new ArrayList<Task>();
		if(!c.moveToFirst()) {
			return result;
		}
		do{
			int type = c.getInt(c.getColumnIndex(F_TYPE));
			Task value = TaskFactory.create(type);
			
			value.setId(c.getLong(c.getColumnIndex(F_ID)));
			value.setUrl(c.getString(c.getColumnIndex(F_URL)));
			value.setRefer(c.getString(c.getColumnIndex(F_REFER)));
			value.setName(c.getString(c.getColumnIndex(F_NAME)));
			value.setFileName(c.getString(c.getColumnIndex(F_FILENAME)));
			value.setFolderName(c.getString(c.getColumnIndex(F_FOLDERNAME)));
			value.setTotalSize(c.getLong(c.getColumnIndex(F_TOTALSIZE)));
			value.setDownloadSize(c.getLong(c.getColumnIndex(F_DOWNLOADEDSIZE)));
			value.setPercent(c.getInt(c.getColumnIndex(F_PERCENT)));
			value.setState(c.getInt(c.getColumnIndex(F_STATE)));
			value.setVideoType(c.getInt(c.getColumnIndex(F_VIDEOTYPE)));
			
			this.handleResult(value, c.getString(c.getColumnIndex(F_EXTERNAL)), result);
			
		} while(c.moveToNext());
		
		return result;
	}
	
	private String getExternal(Task task) {
		
		JSONObject o = new JSONObject();
		try {
			o.put("ai", task.getAlbumId());
			if(task.getType() == Task.Type.Big) {
				BigSiteTask bigTask = task.toBig();
				o.put("pa", bigTask.getParent() == null ? -1 : bigTask.getParent().getId());
				o.put("bt", bigTask.getSubType());
				o.put("du", bigTask.getDuration());
				o.put("df", bigTask.getDiskFile());
				if(bigTask.getSubType() == BigSiteTask.SubType.Whole) {
					o.put("pc", task.toBig().isParseComplete());
					o.put("ct", bigTask.isComputeTotalSize());
					o.put("pt", bigTask.getPlayType());
				}
			}
		} catch(JSONException e ){
			e.printStackTrace();
		}
		return o.toString();
	}
	
	private void handleResult(Task task, String external, List<Task> result) {

		if(task.getType() == Task.Type.Small) {
			try {
				JSONObject o = new JSONObject(external);
				SmallSiteTask smallTask = task.toSmall();
				smallTask.setDiskFile(o.optInt("df"));
				smallTask.setAlbumId(o.optLong("ai"));
			} catch(JSONException e) {
				e.printStackTrace();
			}
			result.add(task);
		}
		if(task.getType() == Task.Type.Big) {
			
			BigSiteTask bigTask = task.toBig();

			try {
				JSONObject o = new JSONObject(external);
				
				bigTask.setDuration(o.optInt("du"));
				bigTask.setDiskFile(o.optInt("df"));
				bigTask.setAlbumId(o.optLong("ai"));
				
				int bigType = o.getInt("bt");
				bigTask.setSubType(bigType);
				
				if(bigType == BigSiteTask.SubType.Whole) {
					bigTask.setParseComplete(o.optBoolean("pc"));
					bigTask.setPlayType(o.optInt("pt"));
					bigTask.setComputeTotalSize(o.optBoolean("ct"));
					result.add(task);
					return;
				}
				Task parent = this.findTask(o.getLong("pa"), result);
				if(parent == null || parent.toBig() == null) {
					return;
				}
				BigSiteTask bigParent = parent.toBig();
				bigTask.setParent(bigParent);
				if(bigType == BigSiteTask.SubType.First) {
					bigParent.setFirstTask(bigTask);
				}
				if(bigType == BigSiteTask.SubType.Second) {
					bigParent.getSecondTasks().add(bigTask);
				}
				
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private Task findTask(long id, List<Task> tasks) {
		for(Task t: tasks) {
			if(t.getId() == id) {
				return t;
			}
		}
		return null;
	}
}











