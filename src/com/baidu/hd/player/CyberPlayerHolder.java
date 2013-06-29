package com.baidu.hd.player;

import android.content.Context;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.conf.Configuration;
import com.baidu.hd.log.Logger;
import com.baidu.hd.service.ServiceProvider;
import com.baidu.video.CyberPlayer;
import com.baidu.video.CyberPlayerConst;

public class CyberPlayerHolder implements ServiceProvider {

	private Logger logger = new Logger("CyberPlayerHolder");

	/** 是否正在使用 */
	private boolean mUsing = false;
	
	/** 是否发生了错误 */
	private boolean mError = false;
	
	/** 播放器内核 */
	private CyberPlayer mPlayer = null;

	private Context mContext = null;

	@Override
	public void setContext(Context ctx) {
		mContext = ctx;
	}

	@Override
	public void onCreate() {
	}

	@Override
	public void onDestory() {
		if (mPlayer != null && !mError) {
			logger.d("releasing cyberPlayer");
			mPlayer.release();
			mPlayer = null;
			logger.d("released cyberPlayer");
		}
	}

	@Override
	public void onSave() {
		// TODO 能做么？？
		// mPlayer.release();
		// mPlayer = null;
	}

	public CyberPlayer getPlayer() {

		if (mPlayer == null) {
			if(mContext != null) {
				logger.d("newing cyberPlayer");
				mPlayer = new CyberPlayer(mContext);
				logger.d("newed cyberPlayer");

				Configuration conf = (Configuration) BaiduHD
						.cast(mContext).getServiceFactory()
						.getServiceProvider(Configuration.class);
				CyberPlayer.setWebReferer_UserAgent(CyberPlayerConst.UA,
						conf.getPlayUA());
			}
		}
		return mPlayer;
	}
	
	public void reset() {
		if(mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
			mError = false;
			System.gc();
		}
	}
	
	public void setUsing(boolean value) {
		mUsing = value;
	}
	
	public boolean isUsing() {
		return mUsing;
	}

	public void setError() {
		mError = true;
	}
}
