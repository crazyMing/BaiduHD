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
	 * ���»������ʷ������¼
	 */
	boolean updateHistorySearch(HistorySearch value);
	
	/**
	 * ɾ����ʷ������¼
	 */
	boolean deleteHistorySearch(HistorySearch value);
	
	/** ������Ӵ�վ������ */
	boolean batchAddSubTask(List<Task> value);
	
	/**
	 * �������
	 */
	boolean addTask(Task value);
	
	/**
	 * ��������
	 */
	boolean updateTask(Task value);
	
	/**
	 * ɾ������
	 * ����keyɾ��������Сվɾ��һ����¼����վɾ�����������м�¼
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
	
	
	/**  ��ȡdb */
	SQLiteDatabase getDatabase();
}
