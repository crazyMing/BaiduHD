package com.baidu.hd.sniffer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.baidu.hd.log.Logger;
import com.baidu.hd.module.AlbumInfo;
import com.baidu.hd.module.AlbumInfo.DetailInfo;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.sniffer.BigSiteSnifferResult.BigSiteAlbumResult;
import com.baidu.hd.sniffer.BigSiteSnifferResult.BigSiteVideoResult;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.StringUtil;

/**
 * 大站剧集信息服务器支持处理器
 * @author sunjianshun
 *
 */
public class BigSiteAlbumHandler extends BaseHandler {

	private Logger logger = new Logger(BigSiteAlbumHandler.class.getSimpleName());
	
	private BigSiteAlbumResult mResult = null;
	private ServiceFactory mServiceFactory = null;
	
	public void onCreate(OnCompleteListener listener, ServiceFactory serviceFactory, Context context) {
		super.onCreate(listener, context);
		mServiceFactory = serviceFactory;
	}
	
	@Override
	protected void start() {
		if (StringUtil.isEmpty(mRefer)) {
			snifferComplete();
			return;
		}
		
		if (mServiceFactory == null) {
			logger.e("have no servicefactory");
			snifferComplete();
			return;
		}
		
		RequestTask task = new RequestTask(new Callback() {
			
			@Override
			public void onPostExcuete(AlbumInfo info) {
				if (info != null) {
					 List<BigSiteVideoResult> videos = new ArrayList<BigSiteVideoResult>();
					for (DetailInfo detail : info.getDetailInfo()) {
						BigSiteVideoResult video = new BigSiteSnifferResult.BigSiteVideoResult();
						video.mPlayId = String.valueOf(detail.playId); 
						video.mName = detail.linkTitle; 
						video.mRefer = detail.orginUrl;
						videos.add(video);
					}
					
					mResult = new BigSiteSnifferResult.BigSiteAlbumResult();
					mResult.mAlbumId = info.getAlbumId();
					mResult.mTitel = info.getTitle();
					mResult.mThumbnail = info.getPoster();
					mResult.mDescription = info.getDesc();
					mResult.mVideoList = videos;
					mResult.mIsFinished = info.getFinish() == 1;
					mResult.mIsHaveNew = false;
					mResult.mLastlatest = -1;
				}
				
				snifferComplete();
			}
			
			@Override
			public void onCancelled() {
			}
		});
		task.execute(mRefer);
	}

	@Override
	protected void stop() {
	}

	@Override
	String getType() {
		return this.getClass().getSimpleName();
	}

	@Override
	protected void snifferComplete() {
		log();
    	isComplete = true;
    	if(!mCancel) {
    		mListener.onComplete(mRefer, mUrl, mResult, this);
    	}
	}
	
	public BigSiteAlbumResult getAlbumResult() {
		return mResult;
	}
	
	public void setBigSiteAlbumResult(BigSiteAlbumResult result) {
		mResult = result;
	}
	
	// 异步查询回调
	interface Callback {
		void onPostExcuete(AlbumInfo result);
		void onCancelled();
	}
	// 异步查询
	// Params, Progress, Result
	class RequestTask extends AsyncTask<String, Void, AlbumInfo> {

		private Callback mCallback = null;
		
		public RequestTask(Callback callback) {
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
		protected void onPostExecute(AlbumInfo result) {
			if (mCallback != null && result != null) {
				mCallback.onPostExcuete(result);
			}
			super.onPostExecute(result);
		}

		@Override
		protected AlbumInfo doInBackground(String... params) {
			if (isCancelled()) {
				return null;
			}
			
			if (params.length > 0) {
				return BigSiteAlbumRequester.getIntance(mServiceFactory).getAlbumInfo(params[0]);
			}
			return null;
		}
	}
}
