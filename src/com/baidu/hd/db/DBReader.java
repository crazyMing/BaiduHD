package com.baidu.hd.db;

import java.util.List;

import com.baidu.browser.db.Suggestion;
import com.baidu.browser.visitesite.SearchKeyword;
import com.baidu.browser.visitesite.VisiteSite;
import com.baidu.hd.module.HistorySearch;
import com.baidu.hd.module.Image;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.LocalVideo;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.service.ServiceProvider;

public interface DBReader extends ServiceProvider {

	/*
	 * �����ʷ�����б�
	 */
	List<HistorySearch> getHistorySearchList(int count);
	
	/*
	 * ���ݹؼ��ֲ�����ʷ�����б�
	 */
	List<HistorySearch> historySearch(String keyword, int count);
	
	/*
	 * �����������
	 */
	List<Task> getAllTask();
	
	/**
	 * �������ר����Ϣ
	 */
	List<Album> getAllAlbum();
	
	/**
	 * ������б�����Ƶ��Ϣ
	 */
	List<LocalVideo> getAllLocalVideo();
	
	/**
	 * �������ͼƬ
	 */
	List<Image> getAllImage();
	
	/**
	 * �������������Ƶ
	 */
	List<NetVideo> getAllNetVideos();
	
	/**
	 * ��ø����缯��������Ƶ
	 */
	List<NetVideo> getNetVideosByAlbumRefer(String albumRefer);
	
	/**
	 * ��ȡ��������з�����ʷ
	 */
	List<VisiteSite> getAllVisitedSite();
	
	/**
	 * ���ݹؼ������������¼
	 */
	List<Suggestion> getHistoryVisiteSiteLike(String query);
	
	/**
	 * ��ȡ�������عؼ���
	 */
	List<SearchKeyword> getAllSearchKeyword();
	
	/**
	 *���ݹؼ��ֻ�ȡ�ؼ���������¼ 
	 */
	List<Suggestion> getHistorySearchKeywordLike(String query);
	
}
