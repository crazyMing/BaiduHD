package com.baidu.hd.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.baidu.hd.log.Logger;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;

class MediaPlayerCore implements PlayerCore {
	
	private static final int CMD_REFRESH = 1;
	private static final int CMD_COMPLETE = 2;
	private static final int CMD_PREPARED = 4;
	private static final int CMD_ERROR = 5;
	
	private Logger logger = new Logger("MediaPlayerCore");
	
	private MediaPlayer mPlayer = null; 
	
	/**
	 * 宿主
	 */
	private Activity mActivity = null;
	
	/**
	 * 是否已经准备完成
	 */
	private boolean mPrepared = false;
	
	/**
	 * surface 是否已经创建
	 */
	private boolean mSurfaceCreated = false;

	/**
	 * 视频地址
	 */
	private String mPath = "";
	
	/**
	 * 总时长缓存
	 */
	private int mDuration = 0;
	
	/**
	 * 退出缓存
	 */
	private int mLastPos = 0;
	
	/**
	 * 缓存开始时间
	 */
	private int mCacheStartPos = 0;
	
	private boolean mInActivtiyStop = false;
	private boolean mDestoryed = false;

	/**
	 * 回调
	 */
	private Callback mCallback = null;
	
	private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			logger.d("surfaceDestroyed");
		}
		
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			
			logger.d("surfaceCreated");
			if(mDestoryed) {
				return;
			}
			mSurfaceCreated = true;
			mPlayer.setDisplay(holder);
			start();
		}
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			logger.d("surfaceChanged");
		}
	};

	/**
	 * 播放器回调
	 */
	private class PlayerCallback implements MediaPlayer.OnCompletionListener,
	MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener {

		@Override
		public void onSeekComplete(MediaPlayer mp) {
			if(mDestoryed) {
				return;
			}
			mHandler.sendEmptyMessage(CMD_REFRESH);
		}

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			logger.d("onPlayerError " + what + " " + extra);
			mPrepared = false;
			if(mDestoryed) {
				return true;
			}
			mHandler.sendMessage(mHandler.obtainMessage(CMD_ERROR, ErrorCode.Unknown, 0));
			return true;
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			logger.d("onPlayerPrepared");
			mPrepared = true;
			if(mDestoryed) {
				return;
			}
			logger.d("starting mediaplayer");
			mPlayer.start();
			logger.d("started mediaplayer");
			mHandler.sendEmptyMessage(CMD_PREPARED);
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			logger.d("onPlayerCompletion");
			mPrepared = false;
			mLastPos = 0;
			if(mDestoryed) {
				return;
			}
			mHandler.sendEmptyMessage(CMD_COMPLETE);
		}
	}

	Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case CMD_REFRESH:
				
				if(mPlayer == null || mCallback == null)
					break;
				
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
				mCallback.onRefresh(mPlayer.getCurrentPosition() / 1000);
				mHandler.sendEmptyMessageDelayed(CMD_REFRESH, Const.Elapse.PlayerRefresh);
				break;

			case CMD_ERROR:
				if(mPlayer != null && mCallback != null) {
					mCallback.onError(msg.arg1);
				}
				break;

			case CMD_PREPARED:
				if(mPlayer != null && mCallback != null) {
					mDuration = mPlayer.getDuration() / 1000;
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
	
	MediaPlayerCore(Activity activity, Callback callback) {
		this.mActivity = activity;
		this.mCallback = callback;
	}

	@Override
	public boolean isCyberPlayer() {
		return false;
	}

	private SurfaceView surface;
	
	@Override
	public void create() {
		logger.d("create");
		
		this.mActivity.getWindow().setFormat(PixelFormat.UNKNOWN);
		
		// surface view
		surface = new SurfaceView(this.mActivity);

		RelativeLayout layout = (RelativeLayout)this.mActivity.findViewById(R.id.player_holder);
		layout.removeAllViews();
		layout.addView(surface, 
				new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT, 
						RelativeLayout.LayoutParams.WRAP_CONTENT));
		
		// surface holder
		SurfaceHolder holder = surface.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(this.mSurfaceCallback);

		// media player
		this.mPlayer = new MediaPlayer();
		this.mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		
		// player callback
		PlayerCallback callback = new PlayerCallback();
		this.mPlayer.setOnCompletionListener(callback);
		this.mPlayer.setOnErrorListener(callback);
		this.mPlayer.setOnPreparedListener(callback);
		this.mPlayer.setOnSeekCompleteListener(callback);
		
		this.mInActivtiyStop = false;
		
		this.mDestoryed = false;
	}

	@Override
	public void destroy() {
		logger.d("destroy");
		this.stopRefresh();
		this.stop();
		if(this.mPlayer != null) {
			logger.d("reseting mediaplayer");
			this.mPlayer.reset();
			logger.d("reseted and releasing mediaplayer");
			this.mPlayer.release();
			logger.d("released mediaplayer");
			this.mPlayer = null;
		}
		this.mDestoryed = true;
		this.mSurfaceCreated = false;
	}

	@Override
	public void onActivityStart() {
		logger.d("onActivityStart");
		if(this.mInActivtiyStop) {
			this.create();
			this.start(this.mPath, this.mLastPos);
			this.mInActivtiyStop = false;
		}
	}

	@Override
	public void onActivityStop() {
		logger.d("onActivityStop");
		if(this.mDestoryed) {
			return;
		}
		this.destroy();
		this.mInActivtiyStop = true;
	}

	@Override
	public void start(String src, int startPos) {
		logger.d("start");
		if(this.mPlayer == null) {
			this.logger.d("null player in start");
		}
		this.logger.d("start player");
		this.mPath = src;
		this.mCacheStartPos = startPos;
		this.start();
	}

	@Override
	public void stop() {
		logger.d("stop");
		if(this.mPlayer == null || !this.mPrepared) {
			this.logger.d("null player or not prepared in stop");
			return;
		}
		int pos = this.mPlayer.getCurrentPosition() / 1000;
		if(pos + Const.PlayerListRecordStopPos <= this.mDuration) {
			this.mLastPos = pos;
		}
		logger.d("stoping mediaplayer");
		this.mPlayer.stop();
		logger.d("stoped mediaplayer");
		this.mPrepared = false;
		this.mSurfaceCreated = false;
	}

	@Override
	public boolean pause() {
		if (this.mPlayer == null || !this.mPrepared) {
			this.logger.d("null player or not prepared in pause");
			return false;
		}
		if (this.mPlayer.isPlaying()) {
			this.logger.d("pausing mediaplayer");
			this.mPlayer.pause();
			logger.d("pausing mediaplayer");
			return false;
		}
		return true;
	}

	@Override
	public boolean pauseResume() {
		if (this.mPlayer == null || !this.mPrepared) {
			this.logger.d("null player or not prepared in pauseResume");
			return false;
		}
		if(this.mPlayer.isPlaying()) {
			this.logger.d("pausing mediaplayer");
			this.mPlayer.pause();
			logger.d("paused mediaplayer");
			return false;
		} else {
			this.logger.d("starting mediaplayer");
			this.mPlayer.start();
			logger.d("started mediaplayer");
			return true;
		}
	}

	@Override
	public void beginSeek() {
		if (this.mPlayer == null || !this.mPrepared) {
			this.logger.d("null player or not prepared in beginSeek");
			return;
		}
		this.stopRefresh();
	}

	@Override
	public void seeking(int pos) {
		if (this.mPlayer == null || !this.mPrepared) {
			this.logger.d("null player or not prepared in seeking");
			return;
		}
		mCallback.onRefresh(pos);
	}

	@Override
	public void endSeek(int pos) {
		if (this.mPlayer == null || !this.mPrepared) {
			this.logger.d("null player or not prepared in seekTo");
			return;
		}
		this.logger.d("seeking mediaplayer");
		this.mPlayer.seekTo(pos * 1000);
		logger.d("seeked mediaplayer");
	}

	@Override
	public int getLastPos() {
		return this.mLastPos;
	}

	@Override
	public String getDataSource() {
		return this.mPath;
	}

	@Override
	public int getDuration() {
		return mDuration;
	}
	
	@Override
	public int getCurrentPos() {
		return mPlayer.getCurrentPosition();
	}

	@Override
	public int getVideoWidth() {
		if(this.mPlayer != null) {
			return this.mPlayer.getVideoWidth();
		}
		return 0;
	}

	@Override
	public int getVideoHeight() {
		if(this.mPlayer != null) {
			return this.mPlayer.getVideoHeight();
		}
		return 0;
	}

	@Override
	public boolean setVideoSize(int width, int height) {
		if(this.mPlayer != null) {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)surface.getLayoutParams();
			if (0==width || 0==height) {
				DisplayMetrics metrics = new DisplayMetrics();
				WindowManager wm = (WindowManager) this.mActivity.getSystemService(Context.WINDOW_SERVICE);
				Display d = wm.getDefaultDisplay();
				d.getMetrics(metrics);
				width = metrics.widthPixels;
				height = metrics.heightPixels;
			}
			else {
				lp.height = height;
				lp.width = width;
			}
			surface.setLayoutParams(lp);
		}
		return true;
	}

	private void start() {

		if(StringUtil.isEmpty(mPath) || !this.mSurfaceCreated ) {
			this.logger.d("empty path or surface not created");
			return;
		}
		
		try {
			File file = new File(this.mPath.substring("file://".length())); 
			FileInputStream fis = new FileInputStream(file);
			logger.d("setDataSourcing mediaplayer");
			this.mPlayer.setDataSource(fis.getFD());
			logger.d("setDataSourced and preparing mediaplayer");
			this.mPlayer.prepare();
			logger.d("prepared and seeking mediaplayer");
			this.mPlayer.seekTo(this.mCacheStartPos * 1000);
			logger.d("seeked mediaplayer");
		} catch(IllegalStateException e) {
			this.logger.d("start exception " + e.getMessage());
			mHandler.sendMessage(mHandler.obtainMessage(CMD_ERROR, ErrorCode.Unknown, 0));			
		} catch(IOException e) {
			this.logger.d("start exception " + e.getMessage());
			mHandler.sendMessage(mHandler.obtainMessage(CMD_ERROR, ErrorCode.NotSupport, 0));
		}
	}

	private void stopRefresh() {
		this.mHandler.removeMessages(CMD_REFRESH);
	}
}
