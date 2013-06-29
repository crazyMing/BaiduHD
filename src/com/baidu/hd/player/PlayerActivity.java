package com.baidu.hd.player;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.BaseActivity;
import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.Video;
import com.baidu.hd.module.album.NetVideo.NetVideoType;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.sniffer.SnifferHandler;
import com.baidu.hd.sniffer.SnifferResult;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId.PlCtr;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.MediaFile;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.TimePowerUtility;
import com.baidu.hd.util.ToastUtil;
import com.baidu.hd.util.Turple;
import com.baidu.hd.util.UrlUtil;
import com.baidu.hd.R;

public class PlayerActivity extends BaseActivity {
	private static final int CMD_HIDECONTROL = 1;
	private static final int CMD_HIDELOCKSCREEN = 2;
	private static final int CMD_HIDEPLAYLIST = 3;
	private Logger logger = new Logger("PlayerActivity");

	/**
	 * 状态变化器
	 */
	private SizeChanger mSizeChanger = null;
	private VoiceView mVoiceView = null;
	private BrightView mBrightView = null;
	private int mScreenMode = 0;
	private CacheView mCacheView = null;
	private PlayListView mPlayListView = null;
	private TimePowerUtility mTimePowerUtility = null;
	private CacheErrorView mCacheErrorView = null;
	private DebugView mDebugView = null;
	private boolean mBInNativeFile = false;

	/*
	 * 手势监听相关
	 */
	private GestureDetector mGestureDetector = null;
	private TextView mGestrueBrightVolume = null; // 手势亮度音量显示
	private TextView mGestrueProgress = null; // 手势进度显示
	private int mOldY = -1;
	private boolean mBGestureStart = false; // 需要刷新标志
	private boolean mBGestureProgress = false; // 处于划动进度状态
	private int mGestureCurrentPos = -1;
	private int mGestureFinalPos = -1;
	private int mTotalpos = -1;
	private boolean mBGesturePort = false; // 处于音量、亮度状态
	// 记录两次滑动的时间间隔，在上层解金来播放内核的bug
	private long mOldMills = 0;
	private long mNewMills = 0;

	// 锁屏软件控制
	private ImageView mLockScreen = null;
	private boolean mBLockScreen = false;

	// 引导页相关
	private RelativeLayout mPlayerMainLayout = null;
	private boolean mBFirstPlay = true;
	private Button mBtnGestureClose = null;
	private AudioManager mAudioManager = null;
	private int mCurrentVolume = 0;

	// 锁屏键
	private boolean mBPressLock = false;

	/** 播放内核控制器 */
	private PlayerCore mPlayerCore = null;

	/**
	 * 锁控制器 使用另一种方法实现
	 * */
	// private LockWrapper mLock = null;

	/**
	 * 控件
	 */
	private ImageButton mBtnPlayPause = null;
	private SeekBar mProgress = null;

	private TextView mDuration = null;
	private TextView mCurrPostion = null;

	private TextView mCurrentVideoName = null;
	private TextView mCurrentVideoOrigin = null;

	private List<View> mControlView = new ArrayList<View>();

	private EventListener eventListener = new EventListener() {

		@Override
		public void onEvent(EventId id, EventArgs args) {
			switch (id) {
			case eAlbumComplete:
				refreshControl();
				break;
			}
		}
	};

	/**
	 * 播放任务及回调
	 */
	private Scheduler mScheduler = null;
	private Scheduler.Callback mSchedulerCallback = new Scheduler.Callback() {

		@Override
		public void onSetTitle(String value) {
			updateTitleInfo();
			mCacheErrorView.updateName();
		}

		@Override
		public void onSniffer() {
			mCacheErrorView.showParse();
		}

		@Override
		public void onPlay(String url, final Video video) {
			logger.d("onTaskPlay " + url);
			if (mPlayerCore != null) {
				// Log.e("qq", "on play " + mPro + ":"
				// + mScheduler.getVideo().getPosition());
				mPro = loadMpro(mScheduler.getVideo());
				if (!video.isLocal()
						&& (video.toNet().getType() == NetVideoType.BIGSITE)
						&& (video.toNet().getUrl() == null || video.toNet()
								.getUrl().equals(""))) {
					//Log.e("qq", video.toNet().getRefer());
					mCacheErrorView.showParse(video.toNet());
					SnifferHandler sniffer = new SnifferHandler(
							PlayerActivity.this, getPlayerApp()
									.getServiceFactory(),
							new SnifferHandler.CallBack() {

								@Override
								public void onComplete(SnifferResult result) {
									//Log.e("qq", (result == null) + "");
									String url = "";
									try {
										url = result.getBigSiteResult()
												.getCurrentPlayUrl();
										doOnplay(url);
									} catch (Exception e) {
										e.printStackTrace();
										logger.e("sniffer failed");
										error(ErrorCode.SnifferFail);
									}
									// video.toNet().setUrl(url);
								}

								@Override
								public void onCancel(SnifferResult result) {
									//Log.e("qq", "canceled");
								}
							}, null, null);
					sniffer.requestM3U8(video.toNet().getRefer());
				} else {
					doOnplay(url);
				}
			}

		}

		private void doOnplay(String url) {
			//Log.e("qq", "on Play url is " + url);
			if (mScheduler.getVideo() != null) {
				try {
					if (mScheduler.getAlbum().asBigServerAlbum() != null
							&& mScheduler.getVideo().toNet() != null) {
						mScheduler.getVideo().toNet().setUrl(url);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				mPlayerCore.start(url, mPro > 0 ? mPro : mScheduler.getVideo()
						.getPosition());
			} else {
				mPlayerCore.start(url, mPro > 0 ? mPro : 0);
			}
			mCacheErrorView.showCache();
		}

		@Override
		public void onError(int errorCode) {
			logger.d("onTaskError " + errorCode);
			error(errorCode);
		}

		@Override
		public void onNew(final Video video, final Album album) {
			if (mPlayerCore != null) {
				mPlayerCore.destroy();
				try {
					mScheduler.getVideo().setPosition(mPlayerCore.getLastPos());
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
			mScheduler.destroy(false);
			mPlayListView.destroy();
			mCacheView.destroy();

			mScheduler = new Scheduler(PlayerActivity.this, mSchedulerCallback);
			if (!video.isLocal()
					&& (video.toNet().getType() == NetVideoType.BIGSITE)
					&& (video.toNet().getUrl() == null || video.toNet()
							.getUrl().equals(""))) {
				//Log.e("qq", video.toNet().getRefer());
				mCacheErrorView.showParse(video.toNet());
				SnifferHandler sniffer = new SnifferHandler(
						PlayerActivity.this,
						getPlayerApp().getServiceFactory(),
						new SnifferHandler.CallBack() {

							@Override
							public void onComplete(SnifferResult result) {
								try {
									String url = result.getBigSiteResult()
											.getCurrentPlayUrl();
									video.toNet().setUrl(url);
									doOnNew(video, album);
								} catch (Exception e) {
									onError(ErrorCode.SnifferFail);
								}
							}

							@Override
							public void onCancel(SnifferResult result) {
								//Log.e("qq", "canceled");
							}
						}, null, null);
				sniffer.requestM3U8(video.toNet().getRefer());
			} else {
				doOnNew(video, album);
			}
		}

		public void doOnNew(Video video, Album album) {
			mScheduler.create(video, album);
			initPlayerCore();
			mCacheErrorView.showPrepare();
			mSizeChanger = new SizeChanger(mAccessor, mPlayerCore);
			mCacheView.create();
			mPro = 0;
			// Log.e("qq","onnew mpro is "+mPro);
			updateTitleInfo();
			refreshControl();
		}

		@Override
		public void onRetry() {
			mCacheErrorView.hide();
			Video video = mScheduler.getVideo();
			Album album = mScheduler.getAlbum();
			mScheduler = new Scheduler(PlayerActivity.this, mSchedulerCallback);
			mScheduler.create(video, album);
			initPlayerCore();
			mSizeChanger = new SizeChanger(mAccessor, mPlayerCore);

			mCacheErrorView.showPrepare();
			refreshControl();
			mCacheView.create();
		}
	};

	/**
	 * 播放器回调
	 */
	boolean prograssChangeable = true;
	private PlayerCore.Callback mPlayerCallback = new PlayerCore.Callback() {

		@Override
		public void onCache(int percent) {
			mCacheView.onCache(percent);
			mCacheErrorView.onCache(percent);
		}

		@Override
		public void onError(int errorCode) {
			error(errorCode);
		}

		@Override
		public void onPrepare(int totalPos) {

			mTotalpos = totalPos;
			if (totalPos < 0) {
				totalPos = 0;
			}
			updatePlayButton(true);
			mCacheErrorView.hide();
			mCacheView.onPrepare();
			mSizeChanger.setVideoSize();
			mProgress.setMax(totalPos);

			if (mBFirstPlay) {
				mPlayerMainLayout
						.setBackgroundResource(R.drawable.gestrue_guide);
				mBtnGestureClose.setVisibility(View.VISIBLE);
				mPlayerCore.pause();
			}

			// 如果是锁屏后进来的
			if (mBPressLock) {
				mPlayerCore.pause();
				mBPressLock = false;
			}
			// Log.e("qq", "mpro is : "+mPro);
			int duration = mPlayerCore.getDuration();
			// Log.e("qq", "duration is : "+duration);
			// if(mProgress.getProgress()!=mPro&&mPro>0){
			// mProgress.setProgress(mPro);
			// mPlayerCore.endSeek(mPro);
			// }
			mScheduler.setVideoDuration(duration);
			mDuration.setText(StringUtil.formatTime(duration));
		}

		@Override
		public void onComplete() {
			if (mScheduler.isCanNext()) {
				mScheduler.next();
				return;
			}

			if (mPlayerCore != null) {
				mPlayerCore.destroy();
				mScheduler.getVideo().setPosition(mPlayerCore.getLastPos());
				mPlayerCore = null;
			}

			mCacheView.destroy();
			finish();
			System.gc();
		}

		@Override
		public void onRefresh(int currentPos) {
			// need refresh in old version
			if (currentPos < 0) {
				currentPos = 0;
				mProgress.setEnabled(false);
				mGestrueProgress.setVisibility(View.GONE);
				// mCurrPostion.setVisibility(View.GONE);
				// mDuration.setVisibility(View.GONE);
				prograssChangeable = false;
				findViewById(R.id.btn_download).setEnabled(false);
				((TextView) findViewById(R.id.btn_download))
						.setTextColor(Color.GRAY);
			}
			if (mBGestureStart || isControlShow()) {
				mCurrPostion.setText(StringUtil.formatTime(currentPos));
				mGestrueProgress.setText(StringUtil.formatTime(currentPos));
				if (mProgress.getProgress() != currentPos) {
					mProgress.setProgress(currentPos);
				}
			} // else {
			mPro = currentPos < 3 ? mPro : currentPos;
			// Log.e("qq","onrefresh mpro is "+mPro);
			// Log.e("jq", "current pos is "+currentPos);
			// }
		}

		@Override
		public boolean needRefresh() {
			/*
			 * changed by juqiang in v.14994, at 2012.11.25-18:33 if
			 * (mBGestureStart || isControlShow()) return true; return false;
			 */
			return true;
		}
	};

	private class PlayerAccessorImp implements PlayerAccessor {

		@Override
		public Turple<Integer, Integer> getScreenSize() {
			return mSizeChanger.getScreenSize();
		}

		@Override
		public Turple<Integer, Integer> getVideoSize() {
			return mSizeChanger.getVideoSize();
		}

		@Override
		public void playMedia(Video video) {
			mScheduler.playNewVideo(video);
		}

		@Override
		public Album getAlbum() {
			return mScheduler.getAlbum();
		}

		@Override
		public Video getVideo() {
			return mScheduler.getVideo();
		}

		@Override
		public Task getTask() {
			return mScheduler.getTask();
		}

		@Override
		public Activity getHost() {
			return PlayerActivity.this;
		}

		@Override
		public View getControlHolder() {
			return findViewById(R.id.player_holder);
		}

		@Override
		public ServiceFactory getServiceFactory() {
			return getPlayerApp().getServiceFactory();
		}

		// 包括锁屏按钮
		@Override
		public void startHideControl() {
			mUIHandler.sendEmptyMessageDelayed(CMD_HIDECONTROL, 5000);
			mUIHandler.sendEmptyMessageDelayed(CMD_HIDELOCKSCREEN, 5000);
		}

		// 包括锁屏按钮
		@Override
		public void stopHideControl() {
			mUIHandler.removeMessages(CMD_HIDECONTROL);
			mUIHandler.removeMessages(CMD_HIDELOCKSCREEN);
		}

		@Override
		public void startHideRightBar() {
			stopHideRightBar();
			mUIHandler.sendEmptyMessageDelayed(CMD_HIDEPLAYLIST, 5000);
		}

		@Override
		public void stopHideRightBar() {
			mUIHandler.removeMessages(CMD_HIDEPLAYLIST);
		}
	}

	private PlayerAccessor mAccessor = new PlayerAccessorImp();

	private Handler mUIHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CMD_HIDECONTROL:
				if (isControlShow() || mVoiceView.isShow()) {
					setViewVisible(false);
				}
				break;

			case CMD_HIDELOCKSCREEN:
				if (mLockScreen.getVisibility() == View.VISIBLE) {
					setLockScreenVisible(false);
				}
				break;
			case CMD_HIDEPLAYLIST:
				hideRightBar();
				break;
			default:
				break;
			}
		}
	};
	int mPro = 0;

	protected void onSaveInstanceState(Bundle outState) {
		try {
			outState.putBoolean("lock", mBLockScreen);
			outState.putInt("position", mPro);
			// Log.e("qq","onsave mpro is "+mPro);
		} finally {
			super.onSaveInstanceState(outState);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		logger.d("onCreate");

		// 对于本地文件关联打开的视频，要先初始化service
		if (!isServiceCreated()) {
			getPlayerApp().getServiceContainer().createDirect(this);
			mBInNativeFile = true;
		}
		/*
		 * if (savedInstanceState != null) { mBLockScreen =
		 * savedInstanceState.getBoolean("lock"); mPro =
		 * savedInstanceState.getInt("position");
		 * Log.e("qq","oncreate recover mpro is "+mPro);
		 * 
		 * }else{ Log.e("qq", "oncreate savedInstance is null"); }
		 * Log.e("qq","oncreate mpro is "+mPro);
		 */
		super.onCreate(savedInstanceState);

		// 防止自动锁屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.player);

		// super.setBackExitFlag(false);

		mScheduler = new Scheduler(this, mSchedulerCallback);
		mScheduler.create(getIntent());

		initControls();
		initPlayerCore();

		// mLock = new LockWrapper(this);
		// mLock.onCreate();

		mGestureDetector = new GestureDetector(this, new GestureListener());
		mGestrueBrightVolume = (TextView) findViewById(R.id.gestrue_bright_vol);
		mGestrueProgress = (TextView) findViewById(R.id.time_current_big);

		EventCenter eventCenter = (EventCenter) getServiceProvider(EventCenter.class);
		eventCenter.addListener(eventListener);

		mDebugView = new DebugView(mAccessor);
		mSizeChanger = new SizeChanger(mAccessor, mPlayerCore);
		mVoiceView = new VoiceView(mAccessor);
		mBrightView = new BrightView(mAccessor);
		mCacheView = new CacheView(mAccessor);
		mPlayListView = new PlayListView(mAccessor);
		mTimePowerUtility = new TimePowerUtility(
				((TextView) findViewById(R.id.play_tv_cur_time)),
				((RelativeLayout) findViewById(R.id.play_rl_power_info)), this);
		mCacheErrorView = new CacheErrorView(mAccessor);
		mCacheErrorView.setCallBack(new CacheErrorView.CallBack() {

			@Override
			public void onRetry() {
				try {
					mCacheErrorView.hide();
					Video video = mScheduler.getVideo();
					Album album = mScheduler.getAlbum();
					mScheduler = new Scheduler(PlayerActivity.this,
							mSchedulerCallback);
					mScheduler.create(video, album);
					initPlayerCore();
					mSizeChanger = new SizeChanger(mAccessor, mPlayerCore);
					mCacheErrorView.showPrepare();

					refreshControl();

					mCacheView.create();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onBack() {
				try {
					if (mPlayerCore != null) {
						mPlayerCore.destroy();
						try {
							mScheduler.getVideo().setPosition(
									mPlayerCore.getLastPos());
						} catch (NullPointerException e) {
							e.printStackTrace();
						}
						mPlayerCore = null;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				finish();
				System.gc();
			}
		});

		mPlayListView.setCallBack(new PlayListView.CallBack() {

			@Override
			public void onShowCtrl() {
				mUIHandler.removeMessages(CMD_HIDELOCKSCREEN);
				mUIHandler.removeMessages(CMD_HIDECONTROL);
				setViewVisible(true);
				setLockScreenVisible(true);
				mUIHandler.sendEmptyMessageDelayed(CMD_HIDECONTROL, 5000);
				mUIHandler.sendEmptyMessageDelayed(CMD_HIDELOCKSCREEN, 5000);
			}
		});

		SharedPreferences preferences = getSharedPreferences("application",
				Context.MODE_WORLD_READABLE);
		this.mBFirstPlay = preferences.getBoolean("first_play", true);
		if (this.mBFirstPlay) {
			preferences = getSharedPreferences("application",
					Context.MODE_WORLD_WRITEABLE);
			Editor editor = preferences.edit();
			editor.putBoolean("first_play", false);
			editor.commit();
			this.mAudioManager = (AudioManager) this.mAccessor.getHost()
					.getSystemService(Context.AUDIO_SERVICE);
			this.mCurrentVolume = mAudioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
					AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		}

		mCacheErrorView.showPrepare();

		// 刷新控件
		refreshControl();

	}

	@Override
	protected void onDestroy() {

		logger.d("onDestroy");
		mScheduler.destroy(mCacheErrorView.getmErrorCode() != -1);
		mVoiceView.destroy();
		mBrightView.destroy();
		mCacheView.destroy();
		mPlayListView.destroy();
		mCacheErrorView.hide();
		mDebugView.destroy();

		if (mBInNativeFile) {
			getPlayerApp().destroyService();
			getPlayerApp().toForceExitApp();
		}

		if (!mBInNativeFile && mScheduler.isCreateByIntentData()) {
			getPlayerApp().onDestroy();
		}

		super.onDestroy();
	}

	@Override
	protected void onStart() {
		logger.d("onStart");
		super.onStart();
	}

	@Override
	protected void onStop() {
		logger.d("onStop");

		super.onStop();
	}

	@Override
	protected void onResume() {
		logger.d("onResume");
		super.onResume();

		if (mVoiceView != null) {
			mVoiceView.updateCurrentVolume();
		}

		if (mPlayerCore != null) {
			mPlayerCore.onActivityStart();
			if (mPro > 3) {
				try {
					loadMpro(mScheduler.getVideo());
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		}

		mCacheView.create();
		// mLock.onResume();
		mTimePowerUtility.startWork();

		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		current = 0;
		mScreenMode = getScreenMode();
		setScreenMode(0);
	}

	@Override
	protected void onPause() {
		logger.d("onPause");
		super.onPause();

		mBrightView.storeBrightValue();

		// mLock.onPause();
		mTimePowerUtility.stopWork();

		if (mPlayerCore != null) {
			updatePlayButton(mPlayerCore.pause());

		}

		if (mPlayerCore != null) {
			mPlayerCore.onActivityStop();
			if (mPro > 3) {
				try {
					saveMpro(mScheduler.getVideo());
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		}
		setScreenMode(mScreenMode);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// Log.e("qq", "onConfig changed "+mPro);
		super.onConfigurationChanged(newConfig);
		try {
			// 转入横屏是进行操作
			if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				if (this.mPlayerCore != null) {
					mPlayerCore.onActivityStop();
					mPlayerCore.onActivityStart();
					// 硬锁屏时暂停后恢复播放时恢复声音
					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
							this.mCurrentVolume,
							AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
					if (mVoiceView != null) {
						mVoiceView.updateCurrentVolume();
					}
				}
			}

			// 以此判断是否进行硬锁屏
			if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				mBPressLock = true; // 按了硬锁屏键

				// 锁屏避免暂停前还有声音
				this.mAudioManager = (AudioManager) this.mAccessor.getHost()
						.getSystemService(Context.AUDIO_SERVICE);
				this.mCurrentVolume = mAudioManager
						.getStreamVolume(AudioManager.STREAM_MUSIC);
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
						AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			}
			super.onConfigurationChanged(newConfig);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {

		logger.d("onBackPressed");
		if (mBFirstPlay) {
			mBFirstPlay = false;
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					mCurrentVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		}
		if (mBLockScreen) {
			ToastUtil.showMessage(PlayerActivity.this, getResources()
					.getString(R.string.lock_screen_back), Toast.LENGTH_SHORT);
			if (mLockScreen.getVisibility() == View.GONE
					|| mLockScreen.getVisibility() == View.INVISIBLE) {
				mUIHandler.removeMessages(CMD_HIDELOCKSCREEN);
				setLockScreenVisible(true);
				mUIHandler.sendEmptyMessageDelayed(CMD_HIDELOCKSCREEN, 5000);
			}
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// Log.e("qq", "on key up");
		if (event.getAction() == KeyEvent.ACTION_UP
				&& keyCode == KeyEvent.KEYCODE_BACK) {
			logger.d("onKeyUp");
			if (!mBLockScreen) {
				if (mBrightView.isShow()) {
					mBrightView.destroy();

					mUIHandler.removeMessages(CMD_HIDECONTROL);
					mUIHandler.removeMessages(CMD_HIDELOCKSCREEN);
					setViewVisible(true);
					setLockScreenVisible(true);
					mUIHandler.sendEmptyMessageDelayed(CMD_HIDECONTROL, 5000);
					mUIHandler
							.sendEmptyMessageDelayed(CMD_HIDELOCKSCREEN, 5000);

					return true;
				} else if (mPlayListView.isShow()) {
					mPlayListView.destroy();
					findViewById(R.id.rightbar).setVisibility(View.GONE);
					resetRightBar();
					return true;
				} else if (mPlayerCore != null) {
					mPlayerCore.destroy();
					if (mScheduler.getVideo() != null) {
						mScheduler.getVideo().setPosition(
								mPlayerCore.getLastPos());
					}
					mPlayerCore = null;
				}
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent) {
		logger.d("onKeyDown:keyCode=" + paramInt);
		boolean bAvailability = false;

		// 音量键响应
		if (4 != paramInt) {
			boolean isMusicActive = ((AudioManager) getSystemService("audio"))
					.isMusicActive();

			if ((!isMusicActive) || (paramInt != 24)) {
				if ((isMusicActive) && (paramInt == 25)) {
					if (mBLockScreen || mBFirstPlay) {
						mUIHandler.removeMessages(CMD_HIDECONTROL);
					}
					mVoiceView.show();
					mVoiceView.onVoiceDown();
					if (mBFirstPlay && mAudioManager != null) {
						this.mCurrentVolume = mAudioManager
								.getStreamVolume(AudioManager.STREAM_MUSIC);
					}
					bAvailability = true;
					if (mBLockScreen || mBFirstPlay) {
						mUIHandler.sendEmptyMessageDelayed(CMD_HIDECONTROL,
								5000);
					}
				}
			} else {
				if (mBLockScreen || mBFirstPlay) {
					mUIHandler.removeMessages(CMD_HIDECONTROL);
				}
				mVoiceView.show();
				mVoiceView.onVoiceUp();
				if (mBFirstPlay && mAudioManager != null) {
					this.mCurrentVolume = mAudioManager
							.getStreamVolume(AudioManager.STREAM_MUSIC);
				}
				bAvailability = true;
				if (mBLockScreen || mBFirstPlay) {
					mUIHandler.sendEmptyMessageDelayed(CMD_HIDECONTROL, 5000);
				}
			}

			if (bAvailability && !mBLockScreen && !mBFirstPlay) {
				mUIHandler.removeMessages(CMD_HIDECONTROL);
				mUIHandler.removeMessages(CMD_HIDELOCKSCREEN);
				setViewVisible(true);
				setLockScreenVisible(true);
				findViewById(R.id.rightbar).setVisibility(View.GONE);
				mUIHandler.sendEmptyMessageDelayed(CMD_HIDECONTROL, 5000);
				mUIHandler.sendEmptyMessageDelayed(CMD_HIDELOCKSCREEN, 5000);
			}
		}

		if (!bAvailability) {
			return super.onKeyDown(paramInt, paramKeyEvent);
		} else {
			return true;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (mGestureDetector.onTouchEvent(event)) {
			return true;
		}

		if (!mBLockScreen) {
			switch (event.getAction()) {
			// 处理手势结束
			case MotionEvent.ACTION_UP:
				mOldY = -1;
				mGestrueBrightVolume.setVisibility(View.INVISIBLE);
				mGestrueProgress.setVisibility(View.INVISIBLE);
				if (mBGestureProgress) {
					mBGestureProgress = false;
					if (mPlayerCore != null) {
						mPlayerCore.endSeek(mGestureFinalPos);
					}

					mOldMills = mNewMills;
					mGestureCurrentPos = mGestureFinalPos;

				}
				if (mBGestureStart) {
					mBGestureStart = false;
				}

				mBGesturePort = false;
				break;

			// 取巧 手势开始
			case MotionEvent.ACTION_DOWN:
				mBGestureStart = true;

				// 引导页相关去效果
				if (mBFirstPlay) {
					mPlayerMainLayout
							.setBackgroundColor(R.color.transparent_background);
					mBtnGestureClose.setVisibility(View.INVISIBLE);
					if (mPlayerCore != null) {
						mPlayerCore.pauseResume();
						if (mAudioManager != null) {
							mAudioManager.setStreamVolume(
									AudioManager.STREAM_MUSIC, mCurrentVolume,
									AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
						}
						if (mVoiceView != null) {
							mVoiceView.updateCurrentVolume();
						}
					}
					mBFirstPlay = false;
				}
				break;
			}
		}

		return super.onTouchEvent(event);
	}

	private void initControls() {

		mPlayerMainLayout = (RelativeLayout) findViewById(R.id.player_main);
		mBtnGestureClose = (Button) findViewById(R.id.btn_gesture_guide_close);
		mBtnGestureClose.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mBFirstPlay) {
					mPlayerMainLayout
							.setBackgroundColor(R.color.transparent_background);
					mBtnGestureClose.setVisibility(View.INVISIBLE);
					if (mPlayerCore != null) {
						mPlayerCore.pauseResume();
						mAudioManager.setStreamVolume(
								AudioManager.STREAM_MUSIC, mCurrentVolume,
								AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
						if (mVoiceView != null) {
							mVoiceView.updateCurrentVolume();
						}
					}
					mBFirstPlay = false;
					mUIHandler.removeMessages(CMD_HIDECONTROL);
					mUIHandler.removeMessages(CMD_HIDELOCKSCREEN);
					setViewVisible(true);
					setLockScreenVisible(true);
					mUIHandler.sendEmptyMessageDelayed(CMD_HIDECONTROL, 5000);
					mUIHandler
							.sendEmptyMessageDelayed(CMD_HIDELOCKSCREEN, 5000);
				}
			}
		});

		mBtnPlayPause = (ImageButton) findViewById(R.id.btn_playpause);
		mBtnPlayPause.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mPlayerCore != null) {
					boolean isPlay = mPlayerCore.pauseResume();
					updatePlayButton(isPlay);
				}
				getStat().incEventCount(PlCtr.Name, PlCtr.Btn_play);
			}
		});

		final ImageButton btnLast = (ImageButton) findViewById(R.id.btn_last);
		btnLast.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mScheduler.last();
				getStat().incEventCount(PlCtr.Name, PlCtr.Btn_last);

			}
		});

		final ImageButton btnNext = (ImageButton) findViewById(R.id.btn_next);
		btnNext.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mScheduler.next();
				getStat().incEventCount(PlCtr.Name, PlCtr.Btn_next);

			}
		});

		final ImageButton btnFullScreen = (ImageButton) findViewById(R.id.btn_fullscreen);
		btnFullScreen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSizeChanger.change();
			}
		});

		final ImageButton btnVoice = (ImageButton) findViewById(R.id.btn_voice);
		btnVoice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mVoiceView.isShow()) {
					mVoiceView.hide();

				} else {
					mVoiceView.show();

				}
				getStat().incEventCount(PlCtr.Name, PlCtr.Btn_voice);

			}
		});

		final ImageButton btnBack = (ImageButton) findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if (mPlayerCore != null) {
						mPlayerCore.destroy();
						mScheduler.getVideo().setPosition(
								mPlayerCore.getLastPos());
						mPlayerCore = null;
					}
					finish();
					System.gc();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});

		final ImageButton btnBright = (ImageButton) findViewById(R.id.btn_bright);
		btnBright.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mVoiceView.hide();
				setViewVisible(false);
				setLockScreenVisible(false);
				mBrightView.show();
				getStat().incEventCount(PlCtr.Name, PlCtr.Btn_bright);

			}
		});

		final Button btnDownload = (Button) findViewById(R.id.btn_download);
		final Button btnPlayList = (Button) findViewById(R.id.btn_episode);

		btnDownload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mPlayListView.isShow() && mPlayListView.isDownloadMode()) {
					mPlayListView.destroy();
					findViewById(R.id.rightbar).setVisibility(View.GONE);
					resetRightBar();
					return;
				}
				// 没有剧集列表的直接下载
				if (!(mScheduler.isCanLast() || mScheduler.isCanNext())) {
					boolean ret = mScheduler.download();
					Toast.makeText(
							PlayerActivity.this,
							ret ? R.string.download_tip
									: R.string.download_exist_tip,
							Toast.LENGTH_LONG).show();
				} else {
					setViewVisible(false);
					setLockScreenVisible(false);
					mPlayListView.show(true);
					PlayerActivity.this.findViewById(R.id.rightbar)
							.setVisibility(View.VISIBLE);
					setBlueState(false);

				}
				getStat().incEventCount(PlCtr.Name, PlCtr.Btn_download);

			}
		});

		btnPlayList.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mPlayListView.isShow() && !mPlayListView.isDownloadMode()) {
					mPlayListView.destroy();
					findViewById(R.id.rightbar).setVisibility(View.GONE);
					resetRightBar();
					return;
				}
				try {
					if (mScheduler.getVideo().isLocal() == false
							&& mScheduler.getAlbum() != null) {
						setViewVisible(false);
						setLockScreenVisible(false);
						mPlayListView.show(false);
						PlayerActivity.this.findViewById(R.id.rightbar)
								.setVisibility(View.VISIBLE);
						setBlueState(true);

					}
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});

		mCurrPostion = (TextView) findViewById(R.id.time_current);
		mDuration = (TextView) findViewById(R.id.time);

		mProgress = (SeekBar) findViewById(R.id.mediacontroller_progress);
		mProgress.setMax(100);
		mProgress
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						if (mPlayerCore != null) {

							mPlayerCore.endSeek(mProgress.getProgress());
							mPro = mProgress.getProgress() == 0 ? mPro
									: mProgress.getProgress();
							// Log.e("qq","onstoptrack mpro is "+mPro);
							if (!mAccessor.getVideo().isLocal()) {
								updatePlayButton(true);
							}
							mGestrueProgress.setVisibility(View.GONE);
							getStat().incEventCount(PlCtr.Name,
									PlCtr.Btn_prograss);

						}
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						if (mPlayerCore != null) {
							mPlayerCore.beginSeek();
							mGestrueProgress.setVisibility(View.VISIBLE);

						}
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (fromUser) {
							if (mPlayerCore != null) {
								mPro = progress == 0 ? mPro : progress;
								// Log.e("qq","onprogressChanged mpro is "+mPro);
								mPlayerCore.seeking(progress);
							}
						}
					}
				});

		mCurrentVideoName = (TextView) findViewById(R.id.play_title_info_name);
		mCurrentVideoOrigin = (TextView) findViewById(R.id.play_title_info_origin);
		updateTitleInfo();

		mLockScreen = (ImageView) findViewById(R.id.lock_screen);
		mLockScreen.setImageDrawable(mBLockScreen ? getResources().getDrawable(
				R.drawable.lock_lock) : getResources().getDrawable(
				R.drawable.lock_unlock));
		mLockScreen.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mBLockScreen) {
					logger.d("unlock screen");
					mBLockScreen = false;
					mLockScreen.setImageDrawable(getResources().getDrawable(
							R.drawable.lock_unlock));
					mUIHandler.removeMessages(CMD_HIDECONTROL);
					mUIHandler.removeMessages(CMD_HIDELOCKSCREEN);
					if (!isControlShow()) {
						setViewVisible(true);
					}
					ToastUtil.showMessage(PlayerActivity.this, getResources()
							.getString(R.string.lock_screen_unlock),
							Toast.LENGTH_SHORT);
					mUIHandler.sendEmptyMessageDelayed(CMD_HIDECONTROL, 5000);
					mUIHandler
							.sendEmptyMessageDelayed(CMD_HIDELOCKSCREEN, 5000);
				} else {
					logger.d("lock screen");
					mBLockScreen = true;
					mLockScreen.setImageDrawable(getResources().getDrawable(
							R.drawable.lock_lock));
					if (isControlShow()) {
						setViewVisible(false);
					}
					ToastUtil.showMessage(PlayerActivity.this, getResources()
							.getString(R.string.lock_screen_lock),
							Toast.LENGTH_SHORT);
					mUIHandler
							.sendEmptyMessageDelayed(CMD_HIDELOCKSCREEN, 5000);
				}
				getStat().incEventCount(PlCtr.Name, PlCtr.Btn_lock);

			}
		});

		mControlView.add(findViewById(R.id.bottom_bar));
		mControlView.add(findViewById(R.id.top_bar));
		mControlView.add(findViewById(R.id.rightbar));
	}

	private void initPlayerCore() {
		Video video = mScheduler.getVideo();
		if (video.isLocal()
				&& MediaFile.getFileType(video.toLocal().getFullName()) != null) {
			mPlayerCore = new MediaPlayerCore(this, mPlayerCallback);
		} else {
			mPlayerCore = new CyberPlayerCore(mPlayerCallback, mAccessor);
		}
		mPlayerCore.create();
	}

	private void error(int errorCode) {
		try {
			// 默认播放内核不支持，切换为CyberPlayer
			if (errorCode == ErrorCode.NotSupport) {
				String dataSource = mPlayerCore.getDataSource();
				mPlayerCore.destroy();
				mPlayerCore = new CyberPlayerCore(mPlayerCallback, mAccessor);
				mPlayerCore.create();
				mPlayerCore.start(dataSource, mScheduler.getVideo()
						.getPosition());
				return;
			}

			EventCenter eventCenter = (EventCenter) this
					.getServiceProvider(EventCenter.class);
			eventCenter.fireEvent(EventId.ePlayFail, new PlayerEventArgs(
					mScheduler.getVideo(), errorCode));

			if (mPlayerCore != null) {
				mPlayerCore.destroy();
				try {
					mScheduler.getVideo().setPosition(mPlayerCore.getLastPos());
				} catch (Exception e) {
					e.printStackTrace();
				}
				mPlayerCore = null;
			}
		} catch (NullPointerException nulle) {
			nulle.printStackTrace();
		}

		mScheduler.destroy(true);
		mVoiceView.destroy();
		mBrightView.destroy();
		mCacheView.destroy();
		mCacheErrorView.hide();

		setViewVisible(false);

		mCacheErrorView.showError(errorCode);
	}

	private void setViewVisible(boolean bflag) {
		if (bflag && !isControlShow()) {
			for (View v : mControlView) {
				// 三星9100兼容性问题 如果为INVISIBLE 在本地视频暂停自己内核时不调起
				if (v != null) {
					v.setVisibility(View.GONE);
					v.setVisibility(View.VISIBLE);
				}
			}
		} else if (!bflag && isControlShow()) {
			for (View v : mControlView) {
				// 三星9100兼容性问题 如果为GONE 在本地视频系统内核时 不能消失
				if (v != null) {
					v.setVisibility(View.INVISIBLE);
				}
			}
		}

		if (!bflag) {
			mVoiceView.hide();
			findViewById(R.id.rightbar).setVisibility(View.GONE);
			resetRightBar();
		}
		if (mScheduler.getVideo() != null && mScheduler.getVideo().isLocal()) {
			findViewById(R.id.rightbar).setVisibility(View.GONE);
		}
	}

	private void resetRightBar() {
		Button btnDownload = (Button) findViewById(R.id.btn_download);
		Button btnPlayList = (Button) findViewById(R.id.btn_episode);
		btnPlayList.setCompoundDrawablesWithIntrinsicBounds(
				null,
				getResources().getDrawable(
						R.drawable.ic_episode_titlebar_videoplayer_style),
				null, null);
		btnPlayList.setTextColor(btnPlayList.isEnabled() ? Color.WHITE
				: Color.GRAY);
		// btnPlayList.setImageResource(R.drawable.ic_episode_titlebar_videoplayer_style);
		btnDownload.setCompoundDrawablesWithIntrinsicBounds(
				null,
				getResources().getDrawable(
						R.drawable.ic_download_titlebar_videoplayer_style),
				null, null);
		btnDownload.setTextColor(btnDownload.isEnabled() ? Color.WHITE
				: Color.GRAY);
		// btnDownload.setImageResource(R.drawable.ic_download_titlebar_videoplayer_style);
	}

	private void setBlueState(boolean isUp) {
		resetRightBar();
		Button btnDownload = (Button) findViewById(R.id.btn_download);
		Button btnPlayList = (Button) findViewById(R.id.btn_episode);
		if (isUp) {
			btnPlayList.setTextColor(0xff058bfc);
			btnPlayList.setCompoundDrawablesWithIntrinsicBounds(null,
					getResources().getDrawable(R.drawable.ic_album_selected),
					null, null);
		} else {
			btnDownload.setTextColor(0xff058bfc);
			btnDownload
					.setCompoundDrawablesWithIntrinsicBounds(
							null,
							getResources().getDrawable(
									R.drawable.ic_download_selected), null,
							null);
		}
	}

	private void setLockScreenVisible(boolean bFlag) {
		if (mLockScreen != null) {
			if (bFlag) {
				/*
				 * 三星兼容性，不设为GONE在锁屏界面呼不出锁屏
				 */
				if (mPlayerCore instanceof MediaPlayerCore) {
					mLockScreen.setVisibility(View.GONE);
					mLockScreen.setVisibility(View.VISIBLE);
				} else {
					mLockScreen.setVisibility(View.VISIBLE);
				}
			} else {
				// 三星兼容性
				if (mBLockScreen) {
					// INVISIBLE不能使其消失
					if (mPlayerCore instanceof CyberPlayerCore) {
						mLockScreen.setVisibility(View.GONE);
					}
					// GONE不能使其消失
					else if (mPlayerCore instanceof MediaPlayerCore) {
						mLockScreen.setVisibility(View.INVISIBLE);
					}
				} else {
					// GONE不能使其消失
					mLockScreen.setVisibility(View.INVISIBLE);
				}
			}
		}
	}

	private void updateTitleInfo() {
		try {
			if (null != mScheduler && null != mScheduler.getVideo()) {
				mCurrentVideoName.setText(StringUtil.getVideoName(
						mScheduler.getVideo(), mScheduler.getAlbum()));
				if (mScheduler.getVideo().isLocal()) {
					mCurrentVideoOrigin.setVisibility(View.GONE);
				} else {
					mCurrentVideoOrigin.setVisibility(View.VISIBLE);
					NetVideo netVideo = (NetVideo) mScheduler.getVideo();
					if (null != netVideo) {
						mCurrentVideoOrigin.setText(UrlUtil.getHost(netVideo
								.getRefer()));
					}
				}
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	private void refreshControl() {

		if (mBFirstPlay) {
			setViewVisible(false);
			setLockScreenVisible(false);
		} else {
			if (mLockScreen.getVisibility() == View.VISIBLE) {
				mUIHandler.sendEmptyMessageDelayed(CMD_HIDELOCKSCREEN, 5000);
			}
			if (!mBLockScreen) {
				setViewVisible(true);
			}
			if (isControlShow()) {
				mUIHandler.sendEmptyMessageDelayed(CMD_HIDECONTROL, 5000);
			}

		}

		Button btnDownload = (Button) findViewById(R.id.btn_download);
		btnDownload.setEnabled(!mScheduler.getVideo().isLocal());
		if (mScheduler.getVideo().isLocal()) {
			btnDownload.setVisibility(View.GONE);
			this.mCurrentVideoOrigin.setVisibility(View.GONE);
			this.mCurrentVideoName.setPadding(
					this.mCurrentVideoName.getPaddingLeft(), 12,
					this.mCurrentVideoName.getPaddingRight(),
					this.mCurrentVideoName.getPaddingBottom());
		}

		final ImageButton btnLast = (ImageButton) findViewById(R.id.btn_last);
		btnLast.setEnabled(mScheduler.isCanLast());

		final ImageButton btnNext = (ImageButton) findViewById(R.id.btn_next);
		btnNext.setEnabled(mScheduler.isCanNext());

		final Button btnPlayList = (Button) findViewById(R.id.btn_episode);
		btnPlayList
				.setEnabled((mScheduler.getVideo().isLocal() == false)
						&& (mScheduler.getAlbum() != null)
						&& (mScheduler.getAlbum().asBigServerAlbum() != null || mScheduler
								.getAlbum().asSmallServerAlbum() != null));
		btnPlayList.setTextColor(btnPlayList.isEnabled() ? Color.WHITE
				: Color.GRAY);
		if (mScheduler.getVideo().isLocal()) {
			btnPlayList.setVisibility(View.GONE);
			findViewById(R.id.rightbar).setVisibility(View.GONE);
		}
	}

	private void updatePlayButton(boolean isPlaying) {
		if (!isPlaying) {
			mBtnPlayPause.setImageResource(R.drawable.ic_play_media_style);
		} else {
			mBtnPlayPause.setImageResource(R.drawable.ic_stop_media_style);
		}
	}

	private boolean isControlShow() {
		return mControlView.get(0).getVisibility() == View.VISIBLE;
	}

	/*
	 * 手势监听
	 */
	class GestureListener extends SimpleOnGestureListener {

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {

			if (mBLockScreen) {
				return super.onScroll(e1, e2, distanceX, distanceY);
			}

			int originalX = (int) e1.getX();
			int originalY = (int) e1.getY();

			if (mOldY == -1) {
				mOldY = originalY;
			}

			Display disp = getWindowManager().getDefaultDisplay();

			int windowWidth = disp.getWidth();
			int windowHeight = disp.getHeight();

			if (Math.abs(e2.getY() - originalY)
					/ Math.abs(e2.getX() - originalX) < 1
					&& !mBGesturePort && prograssChangeable) { // 调节进度
				mNewMills = Calendar.getInstance().getTimeInMillis();

				mBGestureProgress = true;
				if (mPlayerCore != null) {
					mPlayerCore.beginSeek();
				}
				mGestrueProgress.setVisibility(View.VISIBLE);
				int increaseProgress = 180 * (int) (e2.getX() - originalX)
						/ windowWidth;

				if (mPlayerCore != null) {

					if (mGestureCurrentPos == -1
							|| mNewMills - mOldMills >= 500) {
						if (mPlayerCore instanceof MediaPlayerCore) {
							mGestureFinalPos = increaseProgress
									+ mPlayerCore.getCurrentPos() / 1000;
						} else {
							mGestureFinalPos = increaseProgress
									+ mPlayerCore.getCurrentPos();
						}
					} else {
						mGestureFinalPos = increaseProgress
								+ mGestureCurrentPos;
					}

				}

				if (mGestureFinalPos < 0) {
					mGestureFinalPos = 0;
				}
				if (mTotalpos != -1 && mGestureFinalPos > mTotalpos) {
					mGestureFinalPos = mTotalpos;
				}
				if (mPlayerCore != null) {
					mPlayerCore.seeking(mGestureFinalPos);
				}
				if (!mAccessor.getVideo().isLocal()) {
					updatePlayButton(true);
				}
				mProgress.setProgress(mGestureFinalPos);
				getStat().incEventCount(PlCtr.Name, PlCtr.Hand_prograss);

			} else { // 调节亮度或声音
				if (!isControlShow()) {
					if (originalX <= windowWidth / 4 && !mBGestureProgress) { // 左边滑动
																				// 调节亮度
						mBGesturePort = true;
						int increaseBright = Const.BrightVolume.BrightMax
								* (int) (e2.getY() - mOldY) / windowHeight;
						mBrightView.setIncreaseBright(-increaseBright);
						float ratio = (float) mBrightView.getBrightValue()
								/ Const.BrightVolume.BrightMax * 100;
						String format = getString(R.string.gestrue_bright);
						String info = String.format(format, ratio);
						mGestrueBrightVolume.setText(info);
						mGestrueBrightVolume.setVisibility(View.VISIBLE);
						mOldY = (int) e2.getY();
						getStat().incEventCount(PlCtr.Name, PlCtr.Hand_bright);

					} else if (originalX >= windowWidth / 4 * 3
							&& !mBGestureProgress) { // 右边滑动 调节声音
						mBGesturePort = true;
						int increaseVol = Const.BrightVolume.GestureVoiceMax
								* (int) (e2.getY() - mOldY)
								/ (windowHeight / 2);
						mVoiceView.setIncreaseVol(-increaseVol);
						float ratio = (float) mVoiceView.getGestureVol()
								/ Const.BrightVolume.GestureVoiceMax * 100;
						String format = getString(R.string.gestrue_volume);
						String info = String.format(format, ratio);
						mGestrueBrightVolume.setText(info);
						mGestrueBrightVolume.setVisibility(View.VISIBLE);
						mOldY = (int) e2.getY();
						getStat().incEventCount(PlCtr.Name, PlCtr.Hand_voice);

					}
				}
			}
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {

			if (mBLockScreen) {
				if (mLockScreen.getVisibility() == View.VISIBLE) {
					setLockScreenVisible(false);
				} else {
					mUIHandler.removeMessages(CMD_HIDELOCKSCREEN);
					setLockScreenVisible(true);
					mUIHandler
							.sendEmptyMessageDelayed(CMD_HIDELOCKSCREEN, 5000);
				}
				return super.onSingleTapUp(e);
			} else {
				if (mBrightView.isShow()) {
					mBrightView.destroy();

					mUIHandler.removeMessages(CMD_HIDECONTROL);
					mUIHandler.removeMessages(CMD_HIDELOCKSCREEN);
					setViewVisible(true);
					setLockScreenVisible(true);
					mUIHandler.sendEmptyMessageDelayed(CMD_HIDECONTROL, 5000);
					mUIHandler
							.sendEmptyMessageDelayed(CMD_HIDELOCKSCREEN, 5000);

					return super.onSingleTapUp(e);
				}

				if (mPlayListView.isShow()) {
					mPlayListView.destroy();
					findViewById(R.id.rightbar).setVisibility(View.GONE);
					resetRightBar();
					return super.onSingleTapUp(e);
				}

				mVoiceView.hide();

				mUIHandler.removeMessages(CMD_HIDECONTROL);
				mUIHandler.removeMessages(CMD_HIDELOCKSCREEN);
				if (mLockScreen.getVisibility() == View.VISIBLE) {
					setLockScreenVisible(false);
				} else {
					setLockScreenVisible(true);
					mUIHandler
							.sendEmptyMessageDelayed(CMD_HIDELOCKSCREEN, 5000);
				}
				if (isControlShow()) {
					setViewVisible(false);
				} else {
					setViewVisible(true);
					mUIHandler.sendEmptyMessageDelayed(CMD_HIDECONTROL, 5000);
				}

				return super.onSingleTapUp(e);
			}
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////
	// debug window
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (Const.isDebug) {
			mDebugView.createMenu(menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (Const.isDebug) {
			mDebugView.clickMenu(item.getItemId());
		}
		return true;
	}

	/**
	 * 获取当前屏幕亮度调节模式
	 * 
	 * @return
	 */
	private int getScreenMode() {
		int screenMode = 0;
		try {
			screenMode = Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE);
		} catch (Exception localException) {
			localException.printStackTrace();
		}
		return screenMode;
	}

	/**
	 * 设置屏幕亮度调节模式
	 * 
	 * @param paramInt
	 *            0手动，1自动
	 */
	private void setScreenMode(int paramInt) {
		try {
			Settings.System.putInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE, paramInt);
		} catch (Exception localException) {
			// do nothing
		}
	}

	private void saveMpro(Video v) {
		if (mPro == 0) {
			return;
		}
		SharedPreferences preferences = this.mAccessor.getHost()
				.getSharedPreferences("tempplay", Context.MODE_WORLD_WRITEABLE);
		Editor editor = preferences.edit();
		editor.putInt("mpro", mPro);
		editor.putString("vname", v.getName());
		editor.commit();
		// Log.e("qq", "save "+v.getName()+mPro);
	}

	private int loadMpro(Video v) {
		SharedPreferences preferences = this.mAccessor.getHost()
				.getSharedPreferences("tempplay", Context.MODE_WORLD_WRITEABLE);
		String name = preferences.getString("vname", null);
		if (name != null && name.equals(v.getName())) {
			return preferences.getInt("mpro", 0);
		}
		return 0;
	}

	public Scheduler getScheduler() {
		return mScheduler;
	}

	public void hideRightBar() {
		if (mPlayListView != null) {
			mPlayListView.destroy();
			findViewById(R.id.rightbar).setVisibility(View.GONE);
			resetRightBar();
		}
	}

	Stat getStat() {
		return (Stat) getServiceProvider(Stat.class);
	}
}