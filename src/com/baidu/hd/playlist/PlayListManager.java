package com.baidu.hd.playlist;

import java.util.List;

import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.LocalVideo;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.service.ServiceProvider;
import com.baidu.hd.sniffer.BigSiteSnifferResult;
import com.baidu.hd.sniffer.SmallSiteUrl;

/**
 * ��ʷ��¼������
 */
public interface PlayListManager extends ServiceProvider {

	/**
	 * �޸�ר��
	 */
	void playAlbum(Album value);
	
	/**
	 * �������������еı��
	 */
	void setDownload(Album album, boolean isDownload);
	void setDownload(long albumId, boolean isDownload);
	void setDownload(boolean isDownload);
	
	/**
	 * ɾ����ʷ�б��о缯
	 */
	void removePersonalHistoryAlbum(Album value);
	
	/**
	 * ɾ���ղ��б��о缯
	 */
	void removeFavoriteAlbum(Album value);

	/**
	 * ����һ��ר��
	 */
	Album findAlbum(String listId);
	
	/** ����id����ר�� */
	Album findAlbumById(long id);
	
	/**
	 * ����refer����
	 */
	Album findAlbumByRefer(String refer);
	
	/**
	 * ����ר��
	 */
	public Album findAlbumByVideoRefer(String refer);
	
	/**
	 * ������Ƶ���ŵ�ַ��ѯ����Ҫ����֧�ֱ��ع����ĵ����缯
	 */
	public Album findAlbumByPlayUrl(String playUrl);
	
	/**
	 * ������и���-��ʷ��¼�б��е�ר��
	 */
	List<Album> getAllPersonalHistoryAlbums();
	
	/**
	 * ��������ղ��б��е�ר��
	 */
	List<Album> getAllFavoriteAlbums();
	
	/**
	 * ��ӱ�����Ƶ
	 */
	boolean addLocal(String value);
	
	/**
	 * ˢ�±�����Ƶ
	 * @param value �¼�⵽����Ƶ·��
	 */
	void refreshLocal(List<String> value);
	
	/**
	 * �޸ı�����Ƶ
	 */
	void updateLocal(LocalVideo value);

	/**
	 * ɾ��������Ƶ
	 */
	void removeLocal(LocalVideo value);
	
	/**
	 * ���ұ�����Ƶ��Ϣ
	 */
	LocalVideo findLocal(String fullName);
	
	/**
	 * ������б�����Ƶ
	 */
	List<LocalVideo> getAllLocal();
	
	/**
	 * �����ղ�
	 */
	void setFavorite(Album a);
	
	/**
	 * �޸�������Ƶ
	 */
	void updateNetVideo(NetVideo value);
	
	/**
	 * ɾ��������Ƶ
	 */
	void removeNetVideo(NetVideo value);
	
	/**
	 * ����������Ƶ
	 */
	NetVideo findNetVideo(String refer, String url);
	
	/**
	 * ���ݾ缯ID��ȡ������Ƶ��
	 * ��ע�⡿����listId��ѯ����δ֧��
	 */
	List<NetVideo> getNetVideos(long albumId, String listId, String albumRefer);
	
	/**
	 * ������Ƶ�б�
	 * @return �������ݿ����Ŀ
	 */
	int addVideos(SmallSiteUrl url, Album album);
	int addVideos(BigSiteSnifferResult result, Album album);
	
	/**
	 * ������Ƶ�б�
	 */
	void updateVideos(SmallSiteUrl url, Album album);
	void updateVideos(BigSiteSnifferResult result, Album album);
	
	/**
	 * ��ȡ��������ҳ��ʾ�Ĳ�����ʷ
	 */
	List<Album> getHomeShowAlbums();
	
	/**
	 * ɾ����ҳ��ʾ�Ĳ�����ʷ
	 */
	void removeHomeShowAlbum(Album album);
	
	/**
	 * ɾ����ҳ���в�����ʷ
	 */
	void removeAllHomeShowAlbum();
	
	/**
	 * ׷��
	 */
	void fetchNewestAlbum();
}
