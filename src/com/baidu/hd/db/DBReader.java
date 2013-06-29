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
	 * 获得历史搜索列表
	 */
	List<HistorySearch> getHistorySearchList(int count);
	
	/*
	 * 根据关键字查找历史搜索列表
	 */
	List<HistorySearch> historySearch(String keyword, int count);
	
	/*
	 * 获得所有任务
	 */
	List<Task> getAllTask();
	
	/**
	 * 获得所有专辑信息
	 */
	List<Album> getAllAlbum();
	
	/**
	 * 获得所有本地视频信息
	 */
	List<LocalVideo> getAllLocalVideo();
	
	/**
	 * 获得所有图片
	 */
	List<Image> getAllImage();
	
	/**
	 * 获得所有网络视频
	 */
	List<NetVideo> getAllNetVideos();
	
	/**
	 * 获得给定剧集的所有视频
	 */
	List<NetVideo> getNetVideosByAlbumRefer(String albumRefer);
	
	/**
	 * 获取浏览器所有访问历史
	 */
	List<VisiteSite> getAllVisitedSite();
	
	/**
	 * 根据关键字搜索浏览记录
	 */
	List<Suggestion> getHistoryVisiteSiteLike(String query);
	
	/**
	 * 获取所有搜素关键字
	 */
	List<SearchKeyword> getAllSearchKeyword();
	
	/**
	 *根据关键字获取关键字搜索记录 
	 */
	List<Suggestion> getHistorySearchKeywordLike(String query);
	
}
