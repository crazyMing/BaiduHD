package com.baidu.hd.module.album;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.baidu.hd.log.Logger;
import com.baidu.hd.module.AlbumInfo;
import com.baidu.hd.module.UpdateInfo;
import com.baidu.hd.module.AlbumInfo.DetailInfo;
import com.baidu.hd.module.UpdateInfo.Request;
import com.baidu.hd.module.UpdateInfo.Response;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.sniffer.BigSiteAlbumRequester;
import com.baidu.hd.sniffer.BigSiteSnifferResult;
import com.baidu.hd.sniffer.SmallSiteUrl;
import com.baidu.hd.sniffer.Sniffer;
import com.baidu.hd.sniffer.SnifferEntity;
import com.baidu.hd.sniffer.BigSiteSnifferResult.BigSiteAlbumResult;
import com.baidu.hd.sniffer.BigSiteSnifferResult.BigSiteVideoResult;
import com.baidu.hd.sniffer.SnifferHandler.SnifferType;
import com.baidu.hd.task.BigSiteTaskHandler;
import com.baidu.hd.util.StringUtil;

/**
 * 更新信息查询工具类
 * @author sunjianshun
 *
 */
public class FetchNewestHandler {
	
	/****************************************************************
	 * 更新回调
	 */
    public interface Callback {
    	void onCompelete(BigSiteSnifferResult result);
    	void onCompelete(SmallSiteUrl url);
    }
    private Callback mCallback = null;
	/****************************************************************/
	
	private Logger logger = new Logger(this.getClass().getSimpleName());
	
	/** 服务工厂*/
	private ServiceFactory mServiceFactory = null;
	/** 嗅探实体*/
	private SnifferEntity mSnifferEntity = null;
	
	public FetchNewestHandler(ServiceFactory factory, Callback callback) {
		this.mServiceFactory = factory;
		this.mCallback = callback;
	}
	
	public void destroy() {
		if (mSnifferEntity !=null) {
			mSnifferEntity.cancel();
		}
	}
	
	public void request() {
		PlayListManager playListManager = (PlayListManager)mServiceFactory.getServiceProvider(PlayListManager.class);
		List<Album> albumList = playListManager.getAllFavoriteAlbums();

		for (Album album : albumList) {
			if (album.asSmallServerAlbum() != null) {
				// 小站逐条判断
				smallSiteRequest(album.getRefer());
			}
		}
		
		// 大站批量查询
		bigSiteRequest(albumList);
	}
	
	/**
	 *  查询小站更新信息
	 */
	private void smallSiteRequest(String value) {

		if (StringUtil.isEmpty(value)) {
			return;
		}
		
		Sniffer sniffer = (Sniffer)mServiceFactory.getServiceProvider(Sniffer.class);
		mSnifferEntity = sniffer.createSmall(new Sniffer.SmallSiteCallback() {
			
			@Override
			public void onComplete(String refer, SmallSiteUrl url) {
				if (!refer.equals(refer)) { 
					return ;
				}
				if ( !url.getSnifferType()) {
					// TODO 更新信息
					if (mCallback != null) {
						mCallback.onCompelete(url);
					}
				}
			}
			
			@Override
			public void onCancel(String refer) {
			}
		}, null);
		mSnifferEntity.request(value);
	}
	
	/**
	 * 查询大站更新信息
	 */
	private void bigSiteRequest(final List<Album> albums) {
		// 设置回调
		BigSiteCallback bigCallback = new BigSiteCallback() {

			@Override
			public void onPostExcuete(AlbumInfo albumInfo, Response response) {
				// 构建BigSiteSnifferResult
				BigSiteSnifferResult result = new BigSiteSnifferResult();
				
				List<BigSiteVideoResult> videos = new ArrayList<BigSiteVideoResult>();
				for (DetailInfo detail : albumInfo.getDetailInfo()) {
					BigSiteVideoResult video = new BigSiteSnifferResult.BigSiteVideoResult();
					video.mPlayId = String.valueOf(detail.playId); 
					video.mName = detail.linkTitle; 
					video.mRefer = detail.orginUrl;
					videos.add(video);
				}
				
				BigSiteAlbumResult albumRelsult = new BigSiteAlbumResult();
				albumRelsult.mAlbumId = albumInfo.getAlbumId();
				albumRelsult.mTitel = albumInfo.getTitle();
				albumRelsult.mThumbnail = albumInfo.getPoster();
				albumRelsult.mDescription = albumInfo.getDesc();
				albumRelsult.mVideoList = videos;
				albumRelsult.mIsFinished = response.finish == 1;
				albumRelsult.mIsHaveNew = response.hasUpdate == 1;
				albumRelsult.mLastlatest = response.latest;
				
				result.setBigSiteAlbumResult(albumRelsult);
				
				if (mCallback != null) {
					mCallback.onCompelete(result);
				}
			}

			@Override
			public void onCancelled() {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		// 构建查询参数
		PlayListManager playListManager = (PlayListManager)mServiceFactory.getServiceProvider(PlayListManager.class);
		List<Request> requestList = new ArrayList<UpdateInfo.Request>();
		for (Album album : albums) {
			Request request = new Request();
			request.albumId = album.getListId();
			List<NetVideo> videos = playListManager.getNetVideos(album.getId(), album.getListId(), null);
			int index = videos.size() -1;
			request.playId = videos.get(index).getEpisode();
			requestList.add(request);
		}
		
		// 执行异步查询
		BigSiteUpdateInfoRequestTask task = new BigSiteUpdateInfoRequestTask(bigCallback);
		task.execute(requestList);
	}
	
	/*************************************************************************************
	 * 大站剧集更新查询异步任务
	 */
	// 异步查询回调
	interface BigSiteCallback {
		void onPostExcuete(AlbumInfo albumInfo, Response response);
		void onCancelled();
	}
	
	class BigSiteUpdateInfoRequestTask extends AsyncTask<List<Request>, Void, Void> {

		private BigSiteCallback mCallback = null;
		
		public BigSiteUpdateInfoRequestTask(BigSiteCallback callback) {
			mCallback = callback;
		}
		
		public void cancel() {
			this.cancel(true);
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (mCallback != null) {
				mCallback.onCancelled();
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(List<Request>... params) {
			if (isCancelled()) {
				return null;
			}
			
			if (params.length > 0) {
				
				List<UpdateInfo.Response> updateInfo = BigSiteAlbumRequester.getIntance(mServiceFactory).getUpdateInfo(params[0]);
				for (int i=0; i<updateInfo.size(); i++) {
					UpdateInfo.Response responce = updateInfo.get(i);
					if (responce.hasUpdate == 1) {
						String listId = params[0].get(i).albumId;
						PlayListManager playListManager = (PlayListManager)mServiceFactory.getServiceProvider(PlayListManager.class);
						Album album = null;
						String requestRefer = "";
						
						synchronized (playListManager) {
							album = playListManager.findAlbum(listId);
							// 取第一个视频的refer作为查询的refer
							List<NetVideo> videos = playListManager.getNetVideos(album.getId(), null, null);
							requestRefer = videos.get(0).getRefer();
						}
						
						AlbumInfo albumInfo = BigSiteAlbumRequester.getIntance(mServiceFactory).getAlbumInfo(requestRefer);
						if (mCallback != null && albumInfo != null) {
							mCallback.onPostExcuete(albumInfo, responce);
						}
					}
				}
			}
			return null;
		}
	}
	 /*************************************************************************************/
}
