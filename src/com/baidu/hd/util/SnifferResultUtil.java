package com.baidu.hd.util;

import com.baidu.hd.log.Logger;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.AlbumFactory;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.AlbumFactory.AlbumType;
import com.baidu.hd.module.album.NetVideo.NetVideoType;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.sniffer.BigSiteSnifferResult;
import com.baidu.hd.sniffer.SmallSiteUrl;
import com.baidu.hd.sniffer.SnifferResult;
import com.baidu.hd.sniffer.SnifferHandler.SnifferType;

/**
 * �����վ��̽���
 * @author sunjianshun
 */
public class SnifferResultUtil {
	
	private Logger logger = new Logger(this.getClass().getSimpleName());
	
	public class SaveResultArgs {
		public int number = 0;
		public Album album = null;
		public NetVideo currentVideo = null;
	}
	
	private static SnifferResultUtil mIntance = null;
	public static SnifferResultUtil getInstance() {
		if (mIntance == null) {
			mIntance = new SnifferResultUtil();
		}
		return mIntance;
	}
	
	/**
	 * �������ݣ������ر�����
	 * @param serviceFactory
	 * @param value
	 * @return
	 */
	public SaveResultArgs saveToDatabase(ServiceFactory serviceFactory, SnifferResult value, String listName) {
		if (StringUtil.isEmpty(listName)) {
			logger.e("listName = " + listName);
		}
		
		if (value == null) {
			logger.e("SnifferResult = null");
			return null;
		}
		if (serviceFactory == null) {
			logger.e("ServiceFactory = null");
			return null;
		}
		
		if (value.getType() == SnifferType.BIG) {
			return bigSiteHandler(serviceFactory, value, listName);
		}
		else {
			return smallSiteHandler(serviceFactory, value, listName);
		}
	}
	
	public boolean updateM3U8(ServiceFactory serviceFactory, SnifferResult value) {
		if (value == null) {
			logger.e("SnifferResult = null");
			return false;
		}
		if (serviceFactory == null) {
			logger.e("ServiceFactory = null");
			return false;
		}
		if (value.getType() == SnifferType.BIG && value.getBigSiteResult() != null) {
			BigSiteSnifferResult result = value.getBigSiteResult();
			String playUrl = result.getCurrentPlayUrl();
			String playRefer = result.getCurrentPlayRefer();
			PlayListManager playListManager = (PlayListManager)serviceFactory.getServiceProvider(PlayListManager.class);
			NetVideo video = playListManager.findNetVideo(playRefer, playUrl);
			video.setUrl(playUrl);
			playListManager.updateNetVideo(video);
			return true;
		}
		return false;
	}
	
	/**
	 * �����վ��Ϣ
	 */
	private SaveResultArgs bigSiteHandler(ServiceFactory serviceFactory, SnifferResult value, String listName) {
		PlayListManager playListManager = (PlayListManager)serviceFactory.getServiceProvider(PlayListManager.class);
		SaveResultArgs args = new SaveResultArgs();
		Album album = null;
		String playRefer = null;
		String playUrl = null;
		
		BigSiteSnifferResult big = value.getBigSiteResult();
		if (big == null) return null;
		
		playRefer = big.getCurrentPlayRefer();
		playUrl = big.getCurrentPlayUrl();
		// �޲��ŵ�ֱַ�ӷ��ؿ�ֵ
		if (StringUtil.isEmpty(playUrl)) {
			return null;
		}
		
		// �о缯��Ϣʱ������һ�����������صľ缯
		if (big.getBigSiteAlbumResult() != null) {
			album = playListManager.findAlbum(big.getAlbumId());
			if (album == null) {
				album = AlbumFactory.getInstance().createAlbum(AlbumType.BIG_SERVER);
				album.setListId(big.getAlbumId());
				album.setListName(big.getTitel());
				album.setImage(big.getThumbnail());
				album.setType(NetVideoType.BIGSITE);
				album.setSite(UrlUtil.getHost(playRefer));
				args.number = playListManager.addVideos(big, album);
			}
			else {
				playListManager.updateVideos(big, album);
				args.number = playListManager.getNetVideos(album.getId(), album.getListId(), null).size();
			}
		}
		// �޾缯��Ϣʱ������һ��������̽�õ��ľ缯
		else {
			NetVideo video = playListManager.findNetVideo(big.getCurrentPlayRefer(), big.getCurrentPlayUrl());
			if (video != null) {
				album = playListManager.findAlbumById(video.getAlbumId());
			}
			if (album == null) {
				album = AlbumFactory.getInstance().createAlbum(AlbumType.BIG_NATIVE);
				album.setListId(StringUtil.createUUID());
				album.setListName(listName);
				album.setType(NetVideoType.BIGSITE);
				album.setSite(UrlUtil.getHost(playRefer));
			}
			args.number = playListManager.addVideos(big, album);
		}
		
		// �����ݿ��ȡ��Ϣ
		Album album_fromDB = playListManager.findAlbum(album.getListId());
		NetVideo currentVideo = playListManager.findNetVideo(playRefer, playUrl);
		if (currentVideo != null) {
			if (StringUtil.isEmpty(currentVideo.getUrl())) {
				currentVideo.setUrl(playUrl);
			}
			if (StringUtil.isEmpty(currentVideo.getName())){
				currentVideo.setName(listName);
			}
		}
		args.album = album_fromDB;
		args.currentVideo = currentVideo;
		return args;
	}
	
	/**
	 * ����Сվ��Ϣ
	 */
	private SaveResultArgs smallSiteHandler(ServiceFactory serviceFactory, SnifferResult value, String listName) {
		logger.d("smallSiteHandler");
		
		PlayListManager playListManager = (PlayListManager)serviceFactory.getServiceProvider(PlayListManager.class);
		SaveResultArgs args = new SaveResultArgs();
		Album album = null;
		String playUrl = null;
		SmallSiteUrl small = value.getSmallSiteUrl();
		if (small == null) {
			logger.d("value.getSmallSiteUrl = null" );
			return null;
		}
		
		// ������̽���޾缯
		if (small.getSnifferType()) {
			logger.d("is small native");
			
			playUrl = small.getBdhd();
			// �޲��ŵ�ַ����ֱ�ӷ��ؿ�ֵ
			if (StringUtil.isEmpty(playUrl)) {
				return null;
			}
			// �в��ŵ�ַ���򹹽�һ��������̽�õ��ľ缯
			else {
				logger.d("bdhd=" + small.getBdhd());
				NetVideo video = playListManager.findNetVideo("", small.getBdhd());
				if (video != null ) {
					album = playListManager.findAlbumById(video.getAlbumId());
				}
				if (album == null) {
					album = AlbumFactory.getInstance().createAlbum(AlbumType.SMALL_NATIVE);
					album.setListId(StringUtil.createUUID());
					album.setType(NetVideoType.P2P_STREAM);
					album.setSite(UrlUtil.getHost(small.getRefer()));
				}
				args.number = playListManager.addVideos(small, album);
			}
		}
		
		// ��������̽���о缯
		else {
			logger.d("is small server");
			
			playUrl = small.getLink();
			// �޽����ֱ�ӷ��ؿ�ֵ
			if (small.getPlayUrls().size() < 1) {
				return null;
			}
			album = playListManager.findAlbumByRefer(small.getRefer());
			if (album == null) {
				album = AlbumFactory.getInstance().createAlbum(AlbumType.SMALL_SERVER);
				album.setListId(StringUtil.createUUID());
				album.setListName(listName);
	//			album.setImage(small);
				album.setRefer(small.getRefer());
				album.setType(NetVideoType.P2P_STREAM);
				album.setSite(UrlUtil.getHost(small.getRefer()));
				args.number = playListManager.addVideos(small, album);
			}
			else {
				playListManager.updateVideos(small, album);
				args.number = playListManager.getNetVideos(album.getId(), album.getListId(), null).size();
			}
		}
		
		// �����ݿ��ȡ��Ϣ
		Album album_fromDB = playListManager.findAlbum(album.getListId());
		NetVideo currentVideo = playListManager.findNetVideo(null, playUrl);
		if (currentVideo != null) {
			if (StringUtil.isEmpty(currentVideo.getUrl())) {
				currentVideo.setUrl(playUrl);
			}
			if (StringUtil.isEmpty(currentVideo.getName())) {
				currentVideo.setName(listName);
			}
		}
		args.album = album_fromDB;
		args.currentVideo = currentVideo;
		return args;		
	}
	
	/**
	 * �����ַ�������缯
	 * playUrl ����Ϊ��ֵ��playRefer����Ϊ��ֵ��
	 * ��playUrlΪ�գ��򷵻ؿ�ֵ
	 */
	public SaveResultArgs saveToDatabase(ServiceFactory serviceFactory, String playUrl, String playRefer, String pageTitle, final int netVideoType) {
		if (StringUtil.isEmpty(playUrl)) {
			logger.e("playUrl is empty");
			return null;
		}
		
		if (StringUtil.isEmpty(playRefer)) {
			playRefer = playUrl;
		}
		
		SnifferResult result = new SnifferResult();
		
		if (netVideoType == NetVideoType.P2P_STREAM) {
			SmallSiteUrl url = new SmallSiteUrl();
			url.setBdhd(playUrl);
			url.setSnifferType(true);
			
			result.setSucceed(true);
			result.setSmallSiteUrl(url);
			
			result.setRefer(playRefer);
			result.setType(SnifferType.SMALL);
		}
		else {
			BigSiteSnifferResult bigResult = new BigSiteSnifferResult();
			bigResult.setCurrentPlayUrl(playUrl);
			bigResult.setCurrentPlayRefer(playRefer);
			
			result.setSucceed(true);
			result.setBigSiteResult(bigResult);
			
			result.setRefer(playRefer);
			result.setType(SnifferType.BIG);
			
		}
		return saveToDatabase(serviceFactory, result, pageTitle);
	}
}
