package com.baidu.hd.db;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;

import com.baidu.browser.visitesite.SearchKeyword;
import com.baidu.browser.visitesite.VisiteSite;
import com.baidu.hd.module.HistorySearch;
import com.baidu.hd.module.Image;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.LocalVideo;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.service.ServiceProvider;

public interface DBWriter extends ServiceProvider {
	
	public enum Action {
		Add,
		Update,
		Delete,
		Clear,
	}

	/**
	 * 更新或添加历史搜索记录
	 */
	boolean updateHistorySearch(HistorySearch value);
	
	/**
	 * 删除历史搜索记录
	 */
	boolean deleteHistorySearch(HistorySearch value);
	
	/** 批量添加大站子任务 */
	boolean batchAddSubTask(List<Task> value);
	
	/**
	 * 添加任务
	 */
	boolean addTask(Task value);
	
	/**
	 * 更新任务
	 */
	boolean updateTask(Task value);
	
	/**
	 * 删除任务
	 * 按照key删除，所以小站删除一条记录，大站删除此任务所有记录
	 */
	boolean removeTask(Task value);

	boolean modifyAlbum(Album value, Action action);
	boolean modifyLocalVideo(LocalVideo value, Action action);
	boolean modifyImage(Image value, Action action);
	boolean modifyNetVideo(NetVideo value, Action action);
	boolean modifyVisiteSite(VisiteSite visiteSite, Action action);
	boolean modifySearchKeyword(SearchKeyword mSearchKeyword, Action update);
	
	boolean deleteAllHistorys();
	boolean deleteAllBookmarks();
	
	
	/**  获取db */
	SQLiteDatabase getDatabase();
}
