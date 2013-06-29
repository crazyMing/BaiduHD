package com.baidu.player.download;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.baidu.hd.stat.MarketChannelHelper;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.SystemUtil;

public class DownloadService extends Service {
	
	private static final int CheckRunning = 0;
	private static final int Quit = 1;
	private static final int killProcess = 2;
	
	private boolean isInited = false;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == CheckRunning) {
				//Log.d(":download", "running");
				//sendEmptyMessageDelayed(0, 1000);	
			} else if(msg.what == Quit) {
				JNIP2P.getInstance().uninit();
				sendEmptyMessageDelayed(killProcess, 500);
			} else if (msg.what == killProcess) {
				android.os.Process.killProcess(android.os.Process.myPid());
				System.exit(0);
			}
		}
	};

	private final DownloadServiceInterface.Stub mBinder = new DownloadServiceInterface.Stub() {

		@Override
		public int init() throws RemoteException {
			int ret = JNIP2P.APIErrorCode.API_SUCCESS;
			if(!isInited) {
				isInited = true;
				if(!Const.isDebug) {
					JNIP2P.getInstance().setLogLevel(5);
				}
				ret = JNIP2P.getInstance().init();
				//mHandler.sendEmptyMessageDelayed(0, 1000);
			}
			return ret;
		}

		@Override
		public int uninit() throws RemoteException {
			mHandler.sendEmptyMessageDelayed(Quit, 0);
			return 0;
		}

		@Override
		public int setDeviceId(String value) throws RemoteException {
			return JNIP2P.getInstance().setDeviceId(value);
		}

		@Override
		public int create(JNITaskCreateParam param) throws RemoteException {
			return JNIP2P.getInstance().create(param);
		}

		@Override
		public int start(long handle) throws RemoteException {
			return JNIP2P.getInstance().start(handle);
		}

		@Override
		public int stop(long handle) throws RemoteException {
			return JNIP2P.getInstance().stop(handle);
		}

		@Override
		public int delete(long handle) throws RemoteException {
			return JNIP2P.getInstance().delete(handle);
		}

		@Override
		public int query(long handle, JNITaskInfo jniTaskInfo)
				throws RemoteException {
			return JNIP2P.getInstance().query(handle, jniTaskInfo);
		}

		@Override
		public int parseUrl(String url, JNITaskInfo jniTaskInfo)
				throws RemoteException {
			return JNIP2P.getInstance().parseUrl(url, jniTaskInfo);
		}

		@Override
		public int isFileExist(String path, String fileFullName,
				long fileSize) throws RemoteException {
			return JNIP2P.getInstance().isFileExist(path, fileFullName, fileSize);
		}

		@Override
		public int setSpeedLimit(int value) throws RemoteException {
			return JNIP2P.getInstance().setSpeedLimit(value);
		}

		@Override
		public int getBlock(long handle, JNITaskBuffer buffer)
				throws RemoteException {
			return JNIP2P.getInstance().getBlock(handle, buffer);
		}

		@Override
		public int setLogLevel(int value) throws RemoteException {
			return JNIP2P.getInstance().setLogLevel(value);
		}

		@Override
		public int setPlaying(long handle, boolean playing) throws RemoteException {
			return JNIP2P.getInstance().setPlaying(handle, playing);
		}

		@Override
		public int setMediaTime(long handle, int value) throws RemoteException {
			return JNIP2P.getInstance().setMediaTime(handle, value);
		}

		@Override
		public int getRedirectUrl(long handle, JNITaskInfo jniTaskInfo)
				throws RemoteException {
			return JNIP2P.getInstance().getRedirectUrl(handle, jniTaskInfo);
		}

		@Override
		public int statReport(String key, String value)
				throws RemoteException {
			return JNIP2P.getInstance().statReport(
					SystemUtil.getAppVerison(DownloadService.this), 
					MarketChannelHelper.getInstance(DownloadService.this).getChannelID(), 
					key, value);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return this.mBinder;
	}

}
