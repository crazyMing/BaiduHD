package com.baidu.video;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public class CyberPlayer{
	
	//add for test
	public static final int TEST_ERROR_NO_INPUTFILE 			= 0;
	public static final int TEST_ERROR_OPEN_INPUTFILE 			= 1;
	public static final int TEST_ERROR_NO_SUPPORT_CODEC 		= 2;
	public static final int TEST_ERROR_NO_AUDIO_STREAM			= 3;
	public static final int TEST_ERROR_NO_VIDEO_STREAM			= 4;
	public static final int TEST_ERROR_SET_VIDEO_MODE_FAIL		= 5;
	public static final int TEST_ERROR_SET_VIDEO_SIZE_FAIL		= 6;
	public static final int TEST_ERROR_CREATEYUVOVERLAY_FAIL 	= 7;
	public static final int TEST_ERROR_OPEN_AUDIODEVICE_FAIL 	= 8;
	//end
	
	
	public static final int ERROR_NO_INPUTFILE 			= 1;
	public static final int ERROR_INVALID_INPUTFILE 	= 2;
	public static final int ERROR_NO_SUPPORTED_CODEC 	= 3;
	public static final int ERROR_SET_VIDEOMODE			= 4;
	
	private static final String TAG = "CyberPlayer";

	private static volatile int msiCurrentPosition = 0;
	private static volatile int msiDuration = 0;
	private static volatile int msiVideoWidth = 0;
	private static volatile int msiVideoHeight = 0;
	private static volatile int msiErrorCode = 0;
	private static volatile boolean msbIsPlaying = false;
	private static volatile boolean msbIsStart = false;
	private static volatile boolean msbIsError = false;
	private static volatile boolean msbIsStop = false;
	private static volatile boolean msbIsCache = false;
	
    // This is what SDL runs in. It invokes SDL_main(), eventually
    private Thread mSDLThread = null;

    //called by native for create the construct
    private static Context mNativeContext = null;
    private int miStartPos = 0;
    
	private String mstrVideoPath = null;
	private static String mstrUA = null;
	private static String mstrReferer = null;
	
    // Audio; called by native
    private static Object mbuf = null;
    private static Thread mAudioThread = null;
    private static AudioTrack mAudioTrack = null;    
    
    private static CyberPlayerSurface mSurface = null;
    private boolean mSurfaceCreated = false;
    
    private SurfaceHolder.Callback msfCallback = new SurfaceHolder.Callback(){

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Log.v(TAG, "surfaceChanged() width:" + width + " height:" + height);

			int sdlFormat = 0x85151002; // SDL_PIXELFORMAT_RGB565 by default
			switch (format) {
			case PixelFormat.A_8:
				Log.v(TAG, "pixel format A_8");
				break;
			case PixelFormat.LA_88:
				Log.v(TAG, "pixel format LA_88");
				break;
			case PixelFormat.L_8:
				Log.v(TAG, "pixel format L_8");
				break;
			case PixelFormat.RGBA_4444:
				Log.v(TAG, "pixel format RGBA_4444");
				sdlFormat = 0x85421002; // SDL_PIXELFORMAT_RGBA4444
				break;
			case PixelFormat.RGBA_5551:
				Log.v(TAG, "pixel format RGBA_5551");
				sdlFormat = 0x85441002; // SDL_PIXELFORMAT_RGBA5551
				break;
			case PixelFormat.RGBA_8888:
				Log.v(TAG, "pixel format RGBA_8888");
				sdlFormat = 0x86462004; // SDL_PIXELFORMAT_RGBA8888
				break;
			case PixelFormat.RGBX_8888:
				Log.v(TAG, "pixel format RGBX_8888");
				sdlFormat = 0x86262004; // SDL_PIXELFORMAT_RGBX8888
				break;
			case PixelFormat.RGB_332:
				Log.v(TAG, "pixel format RGB_332");
				sdlFormat = 0x84110801; // SDL_PIXELFORMAT_RGB332
				break;
			case PixelFormat.RGB_565:
				Log.v(TAG, "pixel format RGB_565");
				sdlFormat = 0x85151002; // SDL_PIXELFORMAT_RGB565
				break;
			case PixelFormat.RGB_888:
				Log.v(TAG, "pixel format RGB_888");
				// Not sure this is right, maybe SDL_PIXELFORMAT_RGB24 instead?
				sdlFormat = 0x86161804; // SDL_PIXELFORMAT_RGB888
				break;
			default:
				Log.v(TAG, "pixel format unknown " + format);
				break;
			}

			videoPara.put(CyberPlayerConst.VIDEO_WIDTH, width);
			videoPara.put(CyberPlayerConst.VIDEO_HEIGHT, height);
			videoPara.put(CyberPlayerConst.VIDEO_FORMAT, sdlFormat);

			Log.v(TAG, "videoPara.put value");

			holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);

			synchronized (CyberPlayerConst.SYNC_SURFACE_RESIZE) {
				CyberPlayerConst.SYNC_SURFACE_RESIZE.notify();
			}
			mSurfaceCreated = true;
		}

		public void surfaceCreated(SurfaceHolder holder) {
			Log.v(TAG, "surfaceCreated");
			
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			//BaiduMediaPlayer.this.release();
			Log.v(TAG, "surfaceDestoryed");
		}
    	
    };
    
    
    //need define this variable as static 
    //for avoiding it release before the player thread exit
    private static ContentValues videoPara = new ContentValues();
    
    //native function
    private native void nativeInit();   
    private native void nativeQuit();    
    private native void onNativeKeyDown(int keycode);
    private native void onNativeKeyUp(int keycode);
    private native void onNativeTouch(int touchDevId, int pointerFingerId,
                                            int action, float x, float y, float p);
    private native void onNativeAccel(float x, float y, float z);
    private native void onNativeMsgSend(int iMsgID, int iParam);
    private native void onNativeResize(int x, int y, int format);
    private native void nativeInitpath(int iStartPos, String strPath, String strUA, String strReferer);
    private native static void nativeRunAudioThread();
    private native int hasNEON();    
    private native int hasVFPv3();    
    private native int isARMCPUV7();
    private native static int nativeGetDuration(String mediaFile);
    
    public CyberPlayer() {
    }
    
    // Startup    
    public CyberPlayer(Context context) {
        init(context);
    }

	private void init(Context context) {
		if (null != mNativeContext) {
			return;
		}
		mNativeContext = context;
		
		String libPath = context.getFilesDir().getAbsolutePath() + "/";
		if(new File(libPath + "libgetcpuspec.so").exists() && 
				new File(libPath + "libffmpeg.so").exists() &&
				new File(libPath + "libcyberplayer.so").exists()) {
			Log.d(TAG, "load new kernel");
		} else {
			libPath = context.getFilesDir().getParent() + "/lib/";
			Log.d(TAG, "load default kernel");
		}

		System.load(libPath + "libgetcpuspec.so");
		System.load(libPath + "libffmpeg.so");
		System.load(libPath + "libcyberplayer.so");
	}


	public void setSurface(CyberPlayerSurface surface){
			mSurface = surface;
			mSurface.getHolder().addCallback(msfCallback);
	}
	
	public void reset(){
		if (mSDLThread != null) {
			try {
				Log.v(TAG, "SDL thread will exit");
				if (!msbIsStop) {
					onNativeMsgSend(CyberPlayerConst.CMD_PLAYEREXIT, 0);
				}
				mSDLThread.join();
			} catch (Exception e) {
				Log.v(TAG, "Problem stopping thread: " + e);
			}
			
			mSDLThread = null;			
		}
		
		mbuf = null;
		mAudioThread = null;
		mAudioTrack = null;
		msiCurrentPosition = 0;
		msiDuration = 0;
		msiErrorCode = 0;
		msbIsPlaying = false;
		msbIsStart = false;
		msbIsError = false;
		msbIsStop = false;
		mstrVideoPath = null;
		mSurfaceCreated = false;
		//reset閺冩湹绻氶悾娆忣渾娑撳淇婇幁锟�?//mNativeContext = null;
		//mSurface = null;
		//msiVideoWidth = 0;
		//msiVideoHeight = 0;
		//mstrUA = null;
		//mstrReferer = null;
		

	}
	public void release(){
		if (mSDLThread != null) {
			try {
				Log.v(TAG, "SDL thread will exit");
				if (!msbIsStop) {
					Log.v(TAG, "SDL thread does not reciever exit msg. so stop it");
					onNativeMsgSend(CyberPlayerConst.CMD_PLAYEREXIT, 0);
				}
				mSDLThread.join();
				Log.v(TAG, "SDL thread exit");
			} catch (Exception e) {
				Log.v(TAG, "Problem stopping thread: " + e);
			}
			
			mSDLThread = null;
			mSurface = null;
			mNativeContext = null;
			mbuf = null;
			mAudioThread = null;
			mAudioTrack = null;
			msiCurrentPosition = 0;
			msiDuration = 0;
			msiVideoWidth = 0;
			msiVideoHeight = 0;
			msiErrorCode = 0;
			msbIsPlaying = false;
			msbIsStart = false;
			msbIsError = false;
			msbIsStop = false;
			mstrVideoPath = null;
			mstrUA = null;
			mstrReferer = null;
			mSurfaceCreated = false;
			Log.v(TAG, "Finished waiting for SDL thread");
		}
	}
	
	
    //called by native
	public static Object audioInit(int sampleRate, boolean is16Bit,
			boolean isStereo, int desiredFrames) {
		int channelConfig = isStereo ? AudioFormat.CHANNEL_CONFIGURATION_STEREO
				: AudioFormat.CHANNEL_CONFIGURATION_MONO;
		int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT
				: AudioFormat.ENCODING_PCM_8BIT;
		int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);

		if (is16Bit) {
			mbuf = new short[desiredFrames * (isStereo ? 2 : 1)];
		} else {
			mbuf = new byte[desiredFrames * (isStereo ? 2 : 1)];
		}
		
		Log.v(TAG, "SDL audio: wanted " + (isStereo ? "stereo" : "mono")
				+ " " + (is16Bit ? "16-bit" : "8-bit") + " "
				+ ((float) sampleRate / 1000f) + "kHz, " + desiredFrames
				+ " frames buffer");

		// Let the user pick a larger buffer if they really want -- but ye
		// gods they probably shouldn't, the minimums are horrifyingly high
		// latency already
		desiredFrames = Math.max(desiredFrames,
				(AudioTrack.getMinBufferSize(sampleRate, channelConfig,
				audioFormat) + frameSize - 1)/ frameSize);

		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
				channelConfig, audioFormat, desiredFrames * frameSize, AudioTrack.MODE_STREAM);

		audioStartThread();

		Log.v(TAG,"SDL audio: got " + ((mAudioTrack.getChannelCount() >= 2) ? "stereo" : "mono")	+ " "
			+ ((mAudioTrack.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) ? "16-bit"
			: "8-bit") + " " + ((float) mAudioTrack.getSampleRate() / 1000f) + "kHz, " + desiredFrames + " frames buffer");

		/*try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		
		/*if (is16Bit) {
			mbuf = new short[desiredFrames * (isStereo ? 2 : 1)];
		} else {
			mbuf = new byte[desiredFrames * (isStereo ? 2 : 1)];
		}*/
		return mbuf;
	}
    
	
    public static void audioStartThread() {
        mAudioThread = new Thread(new Runnable() {
            public void run() {
                mAudioTrack.play();
                nativeRunAudioThread();
            }
        });
        
        // I'd take REALTIME if I could get it!
        mAudioThread.setPriority(Thread.MAX_PRIORITY);
        mAudioThread.start();
    }
    
    //called by native
    public static void audioWriteShortBuffer(short[] buffer) {
    	if(buffer == null || mAudioTrack == null)
    		return;
        for (int i = 0; i < buffer.length; ) {
            int result = mAudioTrack.write(buffer, i, buffer.length - i);
            if (result > 0) {
                i += result;
            } else if (result == 0) {
                try {
                    Thread.sleep(1);
                } catch(InterruptedException e) {
                    // Nom nom
                }
            } else {
                Log.w(TAG, "SDL audio: error return from write(short)");
                return;
            }
        }
    }
    
    
    //called by native
    public static void audioWriteByteBuffer(byte[] buffer) {
    	if(buffer == null || mAudioTrack == null)
    		return;
        for (int i = 0; i < buffer.length; ) {
            int result = mAudioTrack.write(buffer, i, buffer.length - i);
            if (result > 0) {
                i += result;
            } else if (result == 0) {
                try {
                    Thread.sleep(1);
                } catch(InterruptedException e) {
                    // Nom nom
                }
            } else {
                Log.w(TAG, "SDL audio: error return from write(short)");
                return;
            }
        }
    }

    
    
    
    
    //called by native
    public static void audioQuit() {
        if (mAudioThread != null) {
            try {
                mAudioThread.join();
                Log.v(TAG, "audio thread exit");
            } catch(Exception e) {
                Log.v(TAG, "Problem stopping audio thread: " + e);
            }
            mAudioThread = null;
        }

        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack = null;
        }
    }
   
    //video; called by native
    public static boolean createGLContext(int majorVersion, int minorVersion) {
    	
    	if(mSurface != null){
    		return mSurface.initEGL(majorVersion, minorVersion);
    	}
        return false;
    }

    //called by native
    public static void flipBuffers() {
    	if(mSurface != null){
    		mSurface.flipEGL();
    	}
    }
    
    public static void setActivityTitle(String title) {
        // Called from SDLMain() thread and can't directly affect the view
    }

    public static Context getAppContext() {
        return mNativeContext;
    }

    
    
    // jni call for giving value which you need
 	static int ReceiverValue_callback(int iGet, int iIndex) {
 		switch (iIndex) {
 		case CyberPlayerConst.CURRPOSITON_0:
 			synchronized (CyberPlayerConst.SYNC_CURRENTPOSTIION) {
 				msiCurrentPosition = iGet;
 				CyberPlayerConst.SYNC_CURRENTPOSTIION.notify();
 			}
 			break;
 		case CyberPlayerConst.DURATION_1:
 			synchronized (CyberPlayerConst.SYNC_Duration) {
 				msiDuration = iGet;
 				CyberPlayerConst.SYNC_Duration.notify();
 				Log.v(TAG, "DURATION_1, msiDuration: " + msiDuration);
 			}
 			break;
 		case CyberPlayerConst.VIDEOWIDTH_2:
 			synchronized (CyberPlayerConst.SYNC_VIDEOWIDTH) {
 				msiVideoWidth = iGet;
 				CyberPlayerConst.SYNC_VIDEOWIDTH.notify();
 			}
 			Log.v(TAG, "VIDEOWIDTH_2, msiVideoWidth: " + msiVideoWidth);
 			break;
 		case CyberPlayerConst.VIDEOHEIGH_3:
 			synchronized (CyberPlayerConst.SYNC_VIDEOHEIGHT) {
 				msiVideoHeight = iGet;
 				CyberPlayerConst.SYNC_VIDEOHEIGHT.notify();
 			}
 			Log.v(TAG, "VIDEOHEIGH_3, msiVideoHeight: " + msiVideoHeight);
 			break;
 		case CyberPlayerConst.ISPLAYING_4:
 			synchronized (CyberPlayerConst.SYNC_ISPLAYING) {
 				msbIsPlaying = (iGet==0x00)?true:false;
 				CyberPlayerConst.SYNC_ISPLAYING.notify();
 			}
 			//Log.v(TAG, "ISPLAYING_4, msbIsPlaying: " + msbIsPlaying);
 			break;
 		case CyberPlayerConst.START_5:
 			msbIsStart = true;
 			msbIsStop = false;
 			msbIsError = false;
 			if(mOnPreparedListener != null){
 				mOnPreparedListener.onPrepared();
 			}
 			break;
 		case CyberPlayerConst.ERROR_6:
 			msbIsError = true;
 			msiErrorCode = iGet;
 			if(mOnErrorListener != null){
 				mOnErrorListener.onError(iGet, 0);
 			}
 			break;
 		case CyberPlayerConst.STOP_7:
 			msbIsStop = true;
 			
 			if(mOnCompletionListener != null){
 				mOnCompletionListener.onCompletion();
 			}
 			break;
 		case CyberPlayerConst.CACHE_8:
 			msbIsCache = (iGet==0x01)?true:false;
 			if(mOnBufferingUpdate != null){
 				mOnBufferingUpdate.onBufferingUpdate(0); //initial the cache percent value
 			}
 			break;
 		case CyberPlayerConst.CACHE_PERCENT_9:
 			if(mOnBufferingUpdate != null){
 				mOnBufferingUpdate.onBufferingUpdate(iGet);
 			}
 			
 			break;
 		case CyberPlayerConst.ONSEEK_10:
 			if(mOnSeekCompleteListener != null){
 				mOnSeekCompleteListener.onSeekComplete();
 			}
 			break;
 		case CyberPlayerConst.ONEVENTLOOPPREPARED_11:
 			if(mOnEventLoopPreparedListener != null){
 				mOnEventLoopPreparedListener.onEventLoopPrepared();
 			}
 			break;
 		default:
 			break;
 		}
 		return 0;
 	}
 
	public int setStreamVolume(int iVolume) {
		AudioManager audioManager = 
				(AudioManager) mNativeContext.getSystemService(Context.AUDIO_SERVICE);
		if (null != audioManager) {
			int iMaxVolume = audioManager
					.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			if (iVolume > iMaxVolume) {
				iVolume = iMaxVolume;
			}
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					iVolume, AudioManager.FLAG_PLAY_SOUND);
			return 0;
		}
		return -1;
	}

	public int getStreamVolume() {
		int iVolume = -1;

		AudioManager audioManager = 
				(AudioManager) mNativeContext.getSystemService(Context.AUDIO_SERVICE);
		if (null != audioManager) {
			iVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		}
		
		return iVolume;
	}
/*
	public int getMaxStreamVolume() {
		AudioManager audioManager = 
				(AudioManager) mNativeContext.getSystemService(Context.AUDIO_SERVICE);
		if (null != audioManager) {
			int iMaxVolume = audioManager
					.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			return iMaxVolume;
		}
		return -1;
	}
*/

	public int getCurrentPosition() {
		onNativeMsgSend(CyberPlayerConst.CMD_GETCURRPOSITION, 0);

		synchronized (CyberPlayerConst.SYNC_CURRENTPOSTIION) {
			try {
				CyberPlayerConst.SYNC_CURRENTPOSTIION.wait(CyberPlayerConst.mWAIT1000MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return msiCurrentPosition;
	}

	//before call it, we must new Cyberplayer with Context, 
	//because this function will call native function for getting the duration 
	public static int getDurationForFile(String mediaFile){
		if(mediaFile != null)
			return nativeGetDuration(mediaFile);
		return -1;
	}
	
	public int getDuration() {
		onNativeMsgSend(CyberPlayerConst.CMD_GETDURATION, 0);

		synchronized (CyberPlayerConst.SYNC_Duration) {
			try {

				CyberPlayerConst.SYNC_Duration.wait(CyberPlayerConst.mWAIT1000MS);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}

		return msiDuration;
	}

	public int getVideoWidth() {
		onNativeMsgSend(CyberPlayerConst.CMD_GETVIDEOWIDTH, 0);

		synchronized (CyberPlayerConst.SYNC_VIDEOWIDTH) {
			try {

				CyberPlayerConst.SYNC_VIDEOWIDTH.wait(CyberPlayerConst.mWAIT1000MS);
				return msiVideoWidth;
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
		return 0;
	}

	public int getVideoHeight() {
		onNativeMsgSend(CyberPlayerConst.CMD_GETVIDEOHEIGHT, 0);

		synchronized (CyberPlayerConst.SYNC_VIDEOHEIGHT) {
			try {

				CyberPlayerConst.SYNC_VIDEOHEIGHT.wait(CyberPlayerConst.mWAIT1000MS);
				return msiVideoHeight;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return 0;
	}

	
	
	
	public boolean isPlaying() {
		onNativeMsgSend(CyberPlayerConst.CMD_ISPLAYING, 0);

		synchronized (CyberPlayerConst.SYNC_ISPLAYING) {
			try {
				CyberPlayerConst.SYNC_ISPLAYING.wait(CyberPlayerConst.mWAIT1000MS);
				return msbIsPlaying;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	public void seekTo(int iCurrPos) {
		onNativeMsgSend(CyberPlayerConst.CMD_SETVIDEOSEEKTO, iCurrPos);
	}

	public void setVideoSize(int iWidth, int iHeight) {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) mNativeContext.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();
		d.getMetrics(metrics);
		int screenWidth = metrics.widthPixels;
		int screenHeight = metrics.heightPixels;
		int videoWidth = iWidth;
		int videoHeight = iHeight;
		int width = 0;
		int height = 0;

		// calculate the screen and video width and height to match
	/*	if (screenWidth < videoWidth) {
			width = screenWidth;
			height = screenWidth * videoHeight / videoWidth;
			if (height > screenHeight) {
				int tmp = height;
				height = screenHeight;
				width = screenHeight * screenWidth / tmp;
			}
		} else if (screenHeight < videoHeight) {
			height = screenHeight;
			width = screenHeight * videoWidth / videoHeight;
		} else {			
			width = videoWidth;
			height = videoHeight;
		}*/
		width = videoWidth;
		height = videoHeight;


		int screenSize = (height << 16) | width;
		onNativeMsgSend(CyberPlayerConst.CMD_SETVIDEOSIZE, screenSize);
	}

	public void setDataSource(String strPath) {
 		mstrVideoPath = strPath;
 	}
 	
 	public String getDataSource(){
 		
 		return mstrVideoPath;
 	}
 	
 	public void start() {
 		start(0);
 	}
 	
 	public void start(int iStartPos) { 
 		if (mSDLThread == null) {
 			
 			videoPara.put(CyberPlayerConst.VIDEO_PATH, mstrVideoPath);
 			videoPara.put(CyberPlayerConst.UA, mstrUA);
 			videoPara.put(CyberPlayerConst.RERERER, mstrReferer);
 			videoPara.put(CyberPlayerConst.VIDEO_POSITON, iStartPos);
 			
			mSDLThread = new Thread(new SDLMainThread(videoPara), "SDLThread");
			mSDLThread.start();
			
			
		}
 	} 	

 	public boolean isStart() {
 		return msbIsStart;
 	}
 	
 	
 	
	public void stop() {
		onNativeMsgSend(CyberPlayerConst.CMD_PLAYEREXIT, 0);		
	}

 	public boolean isStop() {
 		return msbIsStop;
 	}
 	
 	public boolean isCaching() {
 		return msbIsCache;
 	}
 	
	public void pause() {
		if (isPlaying()) {
			onNativeMsgSend(CyberPlayerConst.CMD_PLAYERPAUSE, 0);
		}
	}

	public void prepare(){
		//nothing to do 
	}
	public void resume() {
		if (!isPlaying()) {
			onNativeMsgSend(CyberPlayerConst.CMD_PLAYERPAUSE, 0);
		}
	}
/*
	public void forward15s() {
		onNativeKeyDown(BaiduConst.CMD_VIDEO_FORWARD15s);
		onNativeKeyUp(BaiduConst.CMD_VIDEO_FORWARD15s);
	}

	public void back5s() {
		onNativeKeyDown(BaiduConst.CMD_VIDEO_BACK5s);
		onNativeKeyUp(BaiduConst.CMD_VIDEO_BACK5s);
	}
*/
	public static void setWebReferer_UserAgent(String strKey, String strValue) {
		
		if (strKey.equals(CyberPlayerConst.RERERER)) {
			mstrReferer = strValue;
		}
		
		if (strKey.equals(CyberPlayerConst.UA)) {
			mstrUA = strValue;
		}
	}	
	
	public interface OnPreparedListener {
		void onPrepared();
	}
	public void setOnPreparedListener(OnPreparedListener listener) {
		mOnPreparedListener = listener;
	}
	private static OnPreparedListener mOnPreparedListener;

	
	public interface OnCompletionListener {
		void onCompletion();
	}
	public void setOnCompletionListener(OnCompletionListener listener) {
		mOnCompletionListener = listener;
	}
	static OnCompletionListener mOnCompletionListener;

	
	public interface OnSeekCompleteListener {
		public void onSeekComplete();
	}
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mOnSeekCompleteListener = listener;
	}	
	private static OnSeekCompleteListener mOnSeekCompleteListener;

	
	public interface OnErrorListener {
		void onError(int what, int extra);
	}
	public void setOnErrorListener(OnErrorListener listener) {
		mOnErrorListener = listener;
	}
	private static OnErrorListener mOnErrorListener;

	
	public interface OnBufferingUpdateListener {
		void onBufferingUpdate(int percent);
	}
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {

		mOnBufferingUpdate = listener;
	}
	private static OnBufferingUpdateListener mOnBufferingUpdate;
	
	
	
	public interface OnEventLoopPreparedListener {
		void onEventLoopPrepared();
	}
	public void setOnEventLoopPreparedListener(OnEventLoopPreparedListener listener) {
		mOnEventLoopPreparedListener = listener;
	}
	private static OnEventLoopPreparedListener mOnEventLoopPreparedListener;
	
	/**
	Simple nativeInit() runnable
	*/
	private class SDLMainThread implements Runnable {
		private Handler mUIHandle = null;
		private ContentValues mVideoPara = null; 

		public SDLMainThread(ContentValues videoPara) {
			mVideoPara = videoPara;
		}
		
		public void run() {
			// Runs SDL_main()
			if (null != mVideoPara) {
				if(!mSurfaceCreated){
					//wait the surface create finish and start this thread
					if(CyberPlayer.this.mSurface != null){
						synchronized (CyberPlayerConst.SYNC_SURFACE_RESIZE) {
							try {
								CyberPlayerConst.SYNC_SURFACE_RESIZE.wait();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						}
					}
				}
				int iStartPos = mVideoPara.getAsInteger(CyberPlayerConst.VIDEO_POSITON);
				int iWidth = mVideoPara.getAsInteger(CyberPlayerConst.VIDEO_WIDTH);
				int iHeight = mVideoPara.getAsInteger(CyberPlayerConst.VIDEO_HEIGHT);
				int iFormat = mVideoPara.getAsInteger(CyberPlayerConst.VIDEO_FORMAT);	
				String strPath = mVideoPara.getAsString(CyberPlayerConst.VIDEO_PATH);
				String strUA = mVideoPara.getAsString(CyberPlayerConst.UA);
				String strReferer = mVideoPara.getAsString(CyberPlayerConst.RERERER);
	 			
				CyberPlayer.this.onNativeResize(iWidth, iHeight, iFormat);
				Log.v(CyberPlayer.mSurface.TAG, "SDL thread nativeInitpath: " + strPath);
				if (null != strPath) {
					CyberPlayer.this.nativeInitpath(iStartPos, strPath, strUA, strReferer);
				}else{
					if(CyberPlayer.mOnErrorListener != null){
						CyberPlayer.mOnErrorListener.onError(ERROR_NO_INPUTFILE, 0);
					}
				}
			}
			//release CyberPlayerSurface.mEGLContext from CyberPlayerSurface.mEGLDisplay  at current thread
			CyberPlayer.this.mSurface.releaseEGLContextFromThread();
			Log.v(TAG, "SDLMainThread exit");
		}
	}
}



