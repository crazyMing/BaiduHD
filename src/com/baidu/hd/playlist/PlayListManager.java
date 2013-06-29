package com.baidu.hd.playlist;

import java.util.List;

import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.LocalVideo;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.service.ServiceProvider;
import com.baidu.hd.sniffer.BigSiteSnifferResult;
import com.baidu.hd.sniffer.SmallSiteUrl;

/**
 * 历史记录管理器
 */
public interface PlayListManager extends ServiceProvider {

	/**
	 * 修改专辑
	 */
	void playAlbum(Album value);
	
	/**
	 * 设置离线任务中的标记
	 */
	void setDownload(Album album, boolean isDownload);
	void setDownload(long albumId, boolean isDownload);
	void setDownload(boolean isDownload);
	
	/**
	 * 删除历史列表中剧集
	 */
	void removePersonalHistoryAlbum(Album value);
	
	/**
	 * 删除收藏列表中剧集
	 */
	void removeFavoriteAlbum(Album value);

	/**
	 * 查找一个专辑
	 */
	Album findAlbum(String listId);
	
	/** 根据id查找专辑 */
	Album findAlbumById(long id);
	
	/**
	 * 根据refer查找
	 */
	Album findAlbumByRefer(String refer);
	
	/**
	 * 查找专辑
	 */
	public Album findAlbumByVideoRefer(String refer);
	
	/**
	 * 根据视频播放地址查询，主要用于支持本地构建的单集剧集
	 */
	public Album findAlbumByPlayUrl(String playUrl);
	
	/**
	 * 获得所有个人-历史记录列表中的专辑
	 */
	List<Album> getAllPersonalHistoryAlbums();
	
	/**
	 * 获得所有收藏列表中的专辑
	 */
	List<Album> getAllFavoriteAlbums();
	
	/**
	 * 添加本地视频
	 */
	boolean addLocal(String value);
	
	/**
	 * 刷新本地视频
	 * @param value 新检测到的视频路径
	 */
	void refreshLocal(List<String> value);
	
	/**
	 * 修改本地视频
	 */
	void updateLocal(LocalVideo value);

	/**
	 * 删除本地视频
	 */
	void removeLocal(LocalVideo value);
	
	/**
	 * 查找本地视频信息
	 */
	LocalVideo findLocal(String fullName);
	
	/**
	 * 获得所有本地视频
	 */
	List<LocalVideo> getAllLocal();
	
	/**
	 * 设置收藏
	 */
	void setFavorite(Album a);
	
	/**
	 * 修改网络视频
	 */
	void updateNetVideo(NetVideo value);
	
	/**
	 * 删除网络视频
	 */
	void removeNetVideo(NetVideo value);
	
	/**
	 * 查找网络视频
	 */
	NetVideo findNetVideo(String refer, String url);
	
	/**
	 * 根据剧集ID获取网络视频，
	 * 【注意】根据listId查询，暂未支持
	 */
	List<NetVideo> getNetVideos(long albumId, String listId, String albumRefer);
	
	/**
	 * 插入视频列表
	 * @return 插入数据库的数目
	 */
	int addVideos(SmallSiteUrl url, Album album);
	int addVideos(BigSiteSnifferResult result, Album album);
	
	/**
	 * 更新视频列表
	 */
	void updateVideos(SmallSiteUrl url, Album album);
	void updateVideos(BigSiteSnifferResult result, Album album);
	
	/**
	 * 获取所有在首页显示的播放历史
	 */
	List<Album> getHomeShowAlbums();
	
	/**
	 * 删除首页显示的播放历史
	 */
	void removeHomeShowAlbum(Album album);
	
	/**
	 * 删除首页所有播放历史
	 */
	void removeAllHomeShowAlbum();
	
	/**
	 * 追据
	 */
	void fetchNewestAlbum();
}
