package com.baidu.hd.player;


import android.os.Handler;
import android.os.Message;
import android.widget.RelativeLayout;

import com.baidu.hd.log.Logger;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId;
import com.baidu.hd.util.Const;
import com.baidu.hd.R;
import com.baidu.video.CyberPlayer;
import com.baidu.video.CyberPlayerSurface;

class CyberPlayerCore implements PlayerCore {

	private static final int CMD_REFRESH = 1;
	private static final int CMD_COMPLETE = 2;
	private static final int CMD_CACHE = 3;
	private static final int CMD_PREPARED = 4;
	private static final int CMD_ERROR = 5;
	
	private Logger logger = new Logger("CyberPlayerCore");

	private PlayerAccessor mAccessor = null;

	/** 播放内核 */
	private CyberPlayer mPlayer = null;
	
	/** 回调 */
	private Callback mCallback = null;
	
	/** 是否已经准备完成 */
	private boolean mPrepared = false;
	
	/** 是否已经准备好消息循环 */
	private boolean mEventLoop = false; 
	
	/** 底层通知完成，包括complete和error */
	private boolean mCompete = false;
	
	/** 是否已经调用了start函数 */
	private boolean mStarted = false;

	/** 总时长缓存 */
	private int mDuration = 0;
	
	/** 退出缓存 */
	private int mLastPos = 0;
	
	/** 首次缓冲时间 */
	private long mCacheTime = 0;
	
	/** 是否上报了首次缓冲时间 */
	private boolean mReportedCacheTime = false;
	
	/** 同步停止锁 */
	private Object mStopLock = new Object();
	
	/** 获取消息循环锁 */
	private Object mEventLoopLock = new Object();

	/** 缓存的datasource，用于activity的start和stop */
	private String mDataSource = "";
	
	private boolean mInActivtiyStop = false;
	private boolean mDestoryed = false;
	
	/**
	 * 播放器回调
	 */
	private class PlayerCallback implements CyberPlayer.OnCompletionListener,
			CyberPlayer.OnPreparedListener, CyberPlayer.OnErrorListener,
			CyberPlayer.OnBufferingUpdateListener, CyberPlayer.OnSeekCompleteListener, 
			CyberPlayer.OnEventLoopPreparedListener{

		@Override
		public void onBufferingUpdate(int percent) {
			logger.v("onBufferingUpdate " + percent);
			mHandler.sendMessage(mHandler.obtainMessage(CMD_CACHE, percent, 0));
		}

		@Override
		public void onEventLoopPrepared() {
			logger.d("onPlayerEventLoopPrepared");
			synchronized (mEventLoopLock) {
				mEventLoop = true;
				mEventLoopLock.notify();
			}
		}

		@Override
		public void onPrepared() {
			logger.d("onPlayerPrepared");
			mPrepared = true;
			mHandler.sendEmptyMessage(CMD_PREPARED);
		}

		@Override
		public void onCompletion() {
			logger.d("onPlayerCompletion");
			complete();
			if(!mDestoryed) {
				mLastPos = 0;
				mHandler.sendEmptyMessage(CMD_COMPLETE);				
			}
		}

		@Override
		public void onError(int what, int extra) {
			logger.d("onPlayerError " + what + " " + extra);
			complete();
			if(!mDestoryed) {
				mHandler.sendMessage(mHandler.obtainMessage(CMD_ERROR, ErrorCode.PlayerCoreBase + what, 0));				
			}
		}

		@Override
		public void onSeekComplete() {
			logger.d("onPlayerSeekComplete");
			mHandler.sendEmptyMessageDelayed(CMD_REFRESH, 500);
		}

		private void complete() {
			synchronized (mStopLock) {
				mPrepared = false;
				mEventLoop = false;
				mCompete = true;
				mStopLock.notify();
			}
		}
	}

	Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case CMD_REFRESH:
				if(mPlayer == null || mCallback == null) {
					break;
				}
				if (!mCallback.needRefresh()) {
					mHandler.sendEmptyMessageDelayed(
							CMD_REFRESH, Const.Elapse.PlayerRefresh);
					break;
				}
				if(!mPlayer.isPlaying()){
					mHandler.sendEmptyMessageDelayed(
							CMD_REFRESH, Const.Elapse.PlayerRefresh);
					break;
				}
				if (0 == mDuration) {
					mDuration = mPlayer.getDuration();
				}
				mCallback.onRefresh(mPlayer.getCurrentPosition());
				mHandler.sendEmptyMessageDelayed(CMD_REFRESH, Const.Elapse.PlayerRefresh);
				break;
				
			case CMD_CACHE:
				if(mPlayer != null && mCallback != null) {
					boolean isCacheing = mPlayer.isCaching();
					if(mPrepared) {
						if(isCacheing) {
							if(msg.arg1 == 0) {
								mPlayer.pause();
							}
						} else {
							mPlayer.resume();
							if(!mReportedCacheTime) {
								mReportedCacheTime = true;
								try {
									if(mAccessor.getTask() != null) {
										long time = System.currentTimeMillis() - mCacheTime;
										Stat stat = (Stat)mAccessor.getServiceFactory().getServiceProvider(Stat.class);
										stat.incUdpValue(StatId.Playing.FirstBufferTime, time);
										stat.incUdpValue(StatId.Playing.TaskHandle, mAccessor.getTask().getHandle());
										stat.sendUdp();
									}
								} catch(Exception e){
									e.printStackTrace();
								}
							}
						}
					}
					mCallback.onCache(isCacheing ? msg.arg1 : 100);
				}
				break;
				
			case CMD_ERROR:
				if(mPlayer != null && mCallback != null) {
					mCallback.onError(msg.arg1);
				}
				break;

			case CMD_PREPARED:
				if(mPlayer != null && mCallback != null) {
					
					if (0 == mDuration) {
						mDuration = mPlayer.getDuration();
					}
					
					mCallback.onPrepare(mDuration);
					mHandler.sendEmptyMessage(CMD_REFRESH);
				}
				break;
				
			case CMD_COMPLETE:
				if(mPlayer != null && mCallback != null) {
					mCallback.onComplete();
				}
				break;

			default:
				break;
			}
		}
	};
	
	CyberPlayerCore(Callback callback, PlayerAccessor accessor) {
		mCallback = callback;
		mAccessor = accessor;
	}

	@Override
	public boolean isCyberPlayer() {
		return true;
	}

	@Override
	public void create() {
		
		logger.d("create");
		
		getHolder().setUsing(true);
		mPlayer = getHolder().getPlayer();
		logger.d("reseting cyberPlayer");
		mPlayer.reset();
		logger.d("reseted cyberPlayer");
		
		CyberPlayerSurface surface = new CyberPlayerSurface(mAccessor.getHost());
		
		RelativeLayout layout = (RelativeLayout)mAccessor.getHost().findViewById(R.id.player_holder);
		layout.removeAllViews();
		layout.addView(surface, 
				new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT, 
						RelativeLayout.LayoutParams.WRAP_CONTENT));

		mPlayer.setSurface(surface);

		PlayerCallback callback = new PlayerCallback();
		mPlayer.setOnCompletionListener(callback);
		mPlayer.setOnErrorListener(callback);
		mPlayer.setOnPreparedListener(callback);
		mPlayer.setOnBufferingUpdateListener(callback);
		mPlayer.setOnSeekCompleteListener(callback);
		mPlayer.setOnEventLoopPreparedListener(callback);
		
		mDestoryed = false;
	}

	@Override
	public void destroy() {
		logger.d("destroy");
		mDestoryed = true;
		stopRefresh();
		stopCache();
		stop();
		mPlayer = null;
		mPrepared = false;
		mEventLoop = false;
		mStarted = false;
		mCompete = false;
		getHolder().setUsing(false);
	}

	@Override
	public void onActivityStart() {
		if(mInActivtiyStop) {
			mInActivtiyStop = false;
			create();
			start(mDataSource, mLastPos);
			mDataSource = "";
		}
	}

	@Override
	public void onActivityStop() {
		if(mDestoryed) {
			return;
		}
		mDataSource = getDataSource();
		destroy();
		mInActivtiyStop = true;
	}

	@Override
	public void start(String src, int startPos) {
		logger.d("start");
		
		if(mInActivtiyStop) {
			logger.d("already activtiy stop in start");
			mDataSource = src;
			return;
		}
		if(mPlayer == null) {
			logger.d("null player in start");
			return;
		}
		logger.d("starting cyberPlayer");
		mPlayer.setDataSource(src);
		mPlayer.start(startPos);
		logger.d("started cyberPlayer");
		mCacheTime = System.currentTimeMillis();
		mStarted = true;
	}

	@Override
	public void stop() {
		logger.d("stop");
		
		if(mPlayer == null) {
			logger.d("null player in stop");
			return;
		}
		
		// 已经自然结束
		if(mCompete || !mStarted) {
			logger.d("alreay complete or not start in stop");
			return;
		}

		// 只有prepare完成才能调用getCurrentPosition，否则就不记录了
		if(mPrepared) {
			
			// 缓存最后播放位置，stop后再调用就不正确了
			int pos = mPlayer.getCurrentPosition();
			if(mDuration == 0) {
				mDuration = mPlayer.getDuration();
			}
			if(pos + Const.PlayerListRecordStopPos <= mDuration) {
				mLastPos = pos;
			} else {
				mLastPos = 0;
			}
		}
		
		// 只有eventloop后才能调用stop
		synchronized (mEventLoopLock) {
			try {
				if(!mEventLoop) {
					logger.d("wait eventloop lock");
					mEventLoopLock.wait(Const.Timeout.PlayerEventLoop);
					if(!mEventLoop) {
						throw new InterruptedException();
					}
					logger.d("get eventloop lock");
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
				getHolder().setError();
				return;
			}
		}
		
		logger.d("stoping cyberPlayer");
		mPlayer.stop();
		logger.d("stoped cyberPlayer");
		
		synchronized (mStopLock) {
			try {
				if(!mCompete) {
					logger.d("wait stop lock");
					mStopLock.wait(Const.Timeout.PlayerComplete);
					if(!mCompete) {
						throw new InterruptedException();
					}
					logger.d("get stop lock");
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
				getHolder().setError();
				return;
			}
		}
	}
	
	@Override
	public boolean pause() {
		
		if (mPlayer == null || !mPrepared) {
			logger.d("null player or not prepared in pause");
			return false;
		}
		if (mPlayer.isPlaying()) {
			logger.d("pause player");
			mPlayer.pause();
			return false;
		}
		return true;
	}

	@Override
	public boolean pauseResume() {
		
		if (mPlayer == null || !mPrepared) {
			logger.d("null player or not prepared in pauseResume");
			return false;
		}
		if (mPlayer.isPlaying()) {
			logger.d("pause player");
			mPlayer.pause();
			return false;
		} else {
			logger.d("resume player");
			mPlayer.resume();
			return true;
		}
	}

	@Override
	public void beginSeek() {
		if (mPlayer == null || !mPrepared) {
			logger.d("null player or not prepared in beginSeek");
			return;
		}
		stopRefresh();
	}
	
	@Override
	public void seeking(int pos) {
		if (mPlayer == null || !mPrepared) {
			logger.d("null player or not prepared in seeking");
			return;
		}
		mCallback.onRefresh(pos);
	}

	@Override
	public void endSeek(int pos) {
		if (mPlayer == null || !mPrepared) {
			logger.d("null player or not prepared in seekTo");
			return;
		}
		logger.d("seek player to " + pos);
		mPlayer.seekTo(pos);
	}
	
	@Override
	public int getLastPos() {
		return mLastPos;
	}
	
	@Override
	public String getDataSource() {
		return mPlayer.getDataSource();
	}

	@Override
	public int getDuration() {
		return mDuration;
	}
	
	@Override
	public int getCurrentPos() {
		if(mPlayer != null) {
			return mPlayer.getCurrentPosition();
		}
		return 0;
	}

	@Override
	public int getVideoWidth() {
		if(mPlayer != null) {
			return mPlayer.getVideoWidth();
		}
		return 0;
	}

	@Override
	public int getVideoHeight() {
		if(mPlayer != null) {
			return mPlayer.getVideoHeight();
		}
		return 0;
	}

	@Override
	public boolean setVideoSize(int width, int height) {
		if(mPlayer != null) {			
			mPlayer.setVideoSize(width, height);
			return true;
		}
		return false;
	}

	private void stopRefresh() {
		mHandler.removeMessages(CMD_REFRESH);
	}

	private void stopCache() {
		mHandler.removeMessages(CMD_CACHE);
	}
	
	private CyberPlayerHolder getHolder() {
		return (CyberPlayerHolder)mAccessor.getServiceFactory().getServiceProvider(CyberPlayerHolder.class);
	}
}
