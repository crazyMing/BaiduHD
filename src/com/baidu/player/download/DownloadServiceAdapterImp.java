package com.baidu.player.download;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.log.Logger;
import com.baidu.hd.service.ServiceConsumer;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.SystemUtil;

public class DownloadServiceAdapterImp implements DownloadServiceAdapter, ServiceConsumer {

	private Logger logger = new Logger("JNIP2P");

	private Context mContext = null;
	private ServiceFactory mServiceFactory = null;
	
	/** 创建锁 */
	private Object mLock = new Object();
	
	/** 是否服务已经创建 */
	private boolean mCreated = false;

	/** 服务接口 */
	private DownloadServiceInterface mServiceInterface = null;

	/** 服务链接 */
	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			logger.d("disconnected");
			mCreated = false;
			mServiceInterface = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			logger.d("connected");
			mServiceInterface = DownloadServiceInterface.Stub.asInterface(service);
			mHandler.sendEmptyMessageDelayed(0, Const.Elapse.DownloadServiceCheck);
			netInit();
			synchronized (mLock) {
				mCreated = true;
				mLock.notify();
			}
			EventCenter eventCenter = (EventCenter)mServiceFactory.getServiceProvider(EventCenter.class);
			eventCenter.fireEvent(EventId.eDownloadServiceDie, new EventArgs());
		}
	};
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (mServiceInterface == null) {
				logger.e("checked null service, fatal");
				EventCenter eventCenter = (EventCenter)mServiceFactory.getServiceProvider(EventCenter.class);
				eventCenter.fireEvent(EventId.eDownloadServiceDie, new EventArgs());
			} else {
				sendEmptyMessageDelayed(0, Const.Elapse.DownloadServiceCheck);
			}
		}
	};

	@Override
	public void setContext(Context ctx) {
		mContext = ctx;
	}

	@Override
	public void onCreate() {
		startService();
	}

	@Override
	public void onDestory() {
		netUninit();
		mContext.unbindService(mServiceConnection);
		mContext.stopService(new Intent(mContext, DownloadService.class));
	}

	@Override
	public void onSave() {
	}

	@Override
	public void setServiceFactory(ServiceFactory factory) {
		mServiceFactory = factory;
	}

	@Override
	public int create(JNITaskCreateParam param) {
		try {
			logger.d(String.format("netCreate(), %s", param.toString()));
			int ret = getService().create(param);
			log(ret, String.format("netCreate()=%d", ret));
			return ret;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int start(long handle) {
		try {
			logger.d(String.format("netStart(handle=%d)", handle));
			int ret = getService().start(handle);
			log(ret, String.format("netStart(handle=%d)=%d", handle, ret));
			return ret;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int stop(long handle) {
		try {
			logger.d(String.format("netStop(handle=%d)", handle));
			int ret = getService().stop(handle);
			log(ret, String.format("netStop(handle=%d)=%d", handle, ret));
			return ret;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int delete(long handle) {
		try {
			logger.d(String.format("netDelete(handle=%d)", handle));
			int ret = getService().delete(handle);
			log(ret, String.format("netDelete(handle=%d)=%d", handle, ret));
			return ret;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int query(long handle, JNITaskInfo jniTaskInfo) {
		try {
			if(mServiceInterface == null) {
				startService();
				return -1;
			}
			logger.v(String.format("netQueryTaskInfo(handle=%d)", handle));
			int ret = mServiceInterface.query(handle, jniTaskInfo);

			String msg = String.format("netQueryTaskInfo(handle=%d)=%d, %s",
					handle, ret, jniTaskInfo.toString());
			if (ret >= 0) {
				logger.v(msg);
			} else {
				logger.e(msg);
			}
			return ret;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int parseUrl(String url, JNITaskInfo jniTaskInfo) {
		try {
			if(mServiceInterface == null) {
				startService();
				return -1;
			}
			logger.d(String.format("netParseUrl(url=%s)", url));
			int ret = mServiceInterface.parseUrl(url, jniTaskInfo);
			logger.d(String.format("netParseUrl(url=%s)=%d, %s", url, ret,
					jniTaskInfo.toString()));
			return ret;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public boolean isFileExist(String path, String fileFullName, long fileSize) {
		try {
			if(mServiceInterface == null) {
				startService();
				return false;
			}
			logger.d(String.format("netFileExist(fileFullName=%s, fileSize=%d)", 
					fileFullName, fileSize));
			int ret = mServiceInterface.isFileExist(path, fileFullName,
					fileSize);
			log(ret, String.format(
					"netFileExist(fileFullName=%s, fileSize=%d)=%d",
					fileFullName, fileSize, ret));
			return ret == 1;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int setSpeedLimit(int value) {
		try {
			logger.d(String.format("netSetSpeedLimit(value=%d)", value));
			int ret = getService().setSpeedLimit(value);
			log(ret,
					String.format("netSetSpeedLimit(value=%d)=%d", value, ret));
			return ret;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int getBlock(long handle, JNITaskBuffer buffer) {
		try {
			if(mServiceInterface == null) {
				startService();
				return -1;
			}
			return mServiceInterface.getBlock(handle, buffer);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int setLogLevel(int value) {
		try {
			logger.d(String.format("setLogLevel(value=%d)", value));
			int ret = getService().setLogLevel(value);
			logger
					.d(String.format("setLogLevel(value=%d)=%d", value, ret));
			return ret;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int setPlaying(long handle, boolean playing) {
		try {
			logger.d(String.format("setPlaying(value=%d, playing=%b)", handle, playing));
			int ret = getService().setPlaying(handle, playing);
			logger
					.d(String.format("setPlaying(value=%d, playing=%b)=%d", handle, playing, ret));
			return ret;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int setMediaTime(long handle, int value) {
		try {
			logger.d(String.format("setPlayingRate(handle=%d, value=%d)", handle, value));
			int ret = getService().setMediaTime(handle, value);
			logger.d(String.format("setPlayingRate(handle=%d, value=%d)=%d", handle, value, ret));
			return ret;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int getRedirectUrl(long handle, JNITaskInfo jniTaskInfo) {
		try {
			if(mServiceInterface == null) {
				startService();
				return -1;
			}
			logger.d(String.format("getRedirectUrl(handle=%d)", handle));
			int ret = mServiceInterface.getRedirectUrl(handle, jniTaskInfo);
			logger.d(String.format("getRedirectUrl(handle=%d)=%d", handle, ret));
			return ret;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int statReport(String key, String value) {
		try {
			if(mServiceInterface == null) {
				startService();
				return -1;
			}
			logger.d(String.format("statReport(key=%s, value=%s)", key, value));
			int ret = mServiceInterface.statReport(key, value);
			logger.d(String.format("statReport(key=%s, value=%s)=%d", key, value, ret));
			return ret;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public boolean exist(long handle) {
		return handle != 0;
	}

	@Override
	public int destroy() {
		netUninit();
		return 0;
	}

	private void netInit() {
		try {
			logger.d("setDeviceId");
			int ret = getService().setDeviceId(SystemUtil.getEmid(mContext));
			log(ret, String.format("setDeviceId()=%d", ret));
			logger.d("netInit");
			ret = getService().init();
			log(ret, String.format("netInit()=%d", ret));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void netUninit() {
		try {
			if (mServiceInterface != null) {
				logger.d("netUninit");
				int ret = mServiceInterface.uninit();
				log(ret, String.format("netUninit()=%d", ret));
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void log(int ret, String msg) {
		if (ret >= 0) {
			logger.d(msg);
		} else {
			logger.e(msg);
		}
	}
	
	private DownloadServiceInterface getService() {
		if(mServiceInterface == null) {
			logger.e("null service, restart");
			synchronized (mLock) {
				if(mCreated) {
					return mServiceInterface;
				}
				startService();
				try {
					mLock.wait();
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}			
		}
		return mServiceInterface;
	}
	
	private void startService() {
		Intent intent = new Intent(mContext, DownloadService.class);
		mContext.bindService(intent, mServiceConnection,
				Context.BIND_AUTO_CREATE);
	}
}
