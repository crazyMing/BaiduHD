package com.baidu.hd.upgrade;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.log.Logger;
import com.baidu.hd.personal.PersonalActivity;
import com.baidu.hd.personal.PersonalConst;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.ToastUtil;
import com.baidu.hd.R;

public class RemoteUpgradeImp implements RemoteUpgrade {
	Logger logger = new Logger("RemoteUpgradeImp");
	private static final int REMOTE_PROGRESS_SIZE = 100;
	private static final int REMOTE_FAILED_SIZE = -1;
	private static Context mContext = null;
	private Intent mServiceIntent = null;

	private DownLoadService mDownLoadService = null;

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			logger.d("onServiceConnected");
			mDownLoadService = ((DownLoadService.DownLoadServiceBinder) service)
					.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			logger.d("onServiceDisconnected");
			if (null != mDownLoadService) {
				mDownLoadService.uninit();
			}
			mDownLoadService = null;
		}
	};

	@Override
	public void setContext(Context ctx) {
		logger.d("setContext()");
		mContext = ctx;
		
		// 反初始化
		if(ctx == null) {
			stopRemoteUpgrade(Type.AppUpgrade);
			stopRemoteUpgrade(Type.PlayerCoreUpgrade);
			stopRemoteUpgrade(Type.DowndLoadTaskUpgrade);
		}
	}

	@Override
	public void onCreate() {
		logger.d("onCreate()");
		mServiceIntent = new Intent(mContext, DownLoadService.class);
		mContext.startService(mServiceIntent);
		mContext.bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onDestory() {
		mContext.unbindService(mServiceConnection);
		 mContext.stopService(mServiceIntent);
	}

	@Override
	public void onSave() {
	}

	@Override
	public void startRemoteUpgrade(int type, String title) {
		if (null != mDownLoadService) {
			mDownLoadService.startRemoteUpgrade(type, title);
		}
	}

	@Override
	public void stopRemoteUpgrade(int type) {
		if (null != mDownLoadService) {
			mDownLoadService.stopRemoteUpgrade(type);
		}
	}

	@Override
	public void updateRemoteUpgrade(int type, String update) {
		if (null != mDownLoadService) {
			mDownLoadService.updateRemoteUpgrade(type, update);
		}
	}

	public static class DownLoadService extends Service {
		private Logger logger = new Logger("DownLoadService");
		private NotificationManager mManager = null;
		private Map<Integer, Notification> mNotificationList = new HashMap<Integer, Notification>();
		private Binder serviceBinder = new DownLoadServiceBinder();

		@Override
		public void onCreate() {
			logger.d("onCreate()");
			super.onCreate();
			init();
		}

		@Override
		public void onDestroy() {
			logger.d("onDestroy()");
			super.onDestroy();
			uninit();
		}

		@Override
		public IBinder onBind(Intent intent) {
			return serviceBinder;
		}

		public void init() {
			mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			try {
				addNotification(Type.DowndLoadTaskUpgrade);
				addNotification(Type.AppUpgrade);
				addNotification(Type.PlayerCoreUpgrade);
				
			} catch (Exception e) {
				e.printStackTrace();
				logger.d("init " + e.toString());
			}
		}

		public void uninit() {
			mNotificationList.clear();
			mManager.cancel(Type.AppUpgrade);
			mManager.cancel(Type.PlayerCoreUpgrade);
			mManager.cancel(Type.DowndLoadTaskUpgrade);
		}

		private void addNotification(int type) {
			Notification notification = new Notification(	R.drawable.ic_download_titlebar_videoplayer_pressed, "", System.currentTimeMillis());
			Intent intent = null;
			if (Type.DowndLoadTaskUpgrade == type) {
				notification.contentView = new RemoteViews(getApplication().getPackageName(), R.layout.remote_upgrade_downloadtask);
				notification.contentView.setTextViewText(R.id.remote_progress_rate, "有0个下载缓存任务，点击查看");
				notification.flags = Notification.FLAG_ONGOING_EVENT;
				intent = new Intent(mContext, PersonalActivity.class);
				intent.putExtra(Const.IntentExtraKey.PersonalActivity, PersonalConst.PERSONAL_PAGE_BUFFER);
			} 
			//<add by sunjianshun 2012.11.04 BEGIN
			else if (Type.AppUpgrade == type) {
				notification.contentView = new RemoteViews(getApplication().getPackageName(), R.layout.remote_upgrade);
				notification.contentView.setProgressBar(R.id.remote_progressbar, REMOTE_PROGRESS_SIZE, 0, false);
				notification.contentView.setTextViewText(R.id.remote_progress_rate, "%");
				notification.flags = Notification.FLAG_ONGOING_EVENT;
//				intent = new Intent(Intent.ACTION_VIEW);
//				intent.setDataAndType(Uri.fromFile(new File(Const.Path.AppUpgradeFileName)), "application/vnd.android.package-archive");
//				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent = new Intent();
			}
			//add by sunjianshun 2012.11.04 END>
			else {
				notification.contentView = new RemoteViews(getApplication().getPackageName(), R.layout.remote_upgrade);
				notification.contentView.setProgressBar(R.id.remote_progressbar, REMOTE_PROGRESS_SIZE, 0, false);
				notification.contentView.setTextViewText(R.id.remote_progress_rate, "%");
				intent = new Intent(mContext, BaiduHD.class);
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			notification.contentIntent = PendingIntent.getActivity(this, 0,  intent, PendingIntent.FLAG_UPDATE_CURRENT);
			mNotificationList.put(type, notification);
		}

		public void startRemoteUpgrade(int type, String title) {
			logger.d("startRemoteUpgrade()");
			if (null != mNotificationList.get(type)) {
				mNotificationList.get(type).contentView.setTextViewText(R.id.remote_progress_title, title);
				mManager.notify(type, mNotificationList.get(type));
				Message msg = handler.obtainMessage();
				msg.obj = "0";
				msg.arg1 = 0;
				msg.arg2 = type;
				msg.sendToTarget();
			}
		}

		public void stopRemoteUpgrade(int type) {
			logger.d("stopRemoteUpgrade()");
			if (null != mNotificationList.get(type)) {
				mManager.cancel(type);
			}
		}

		public void updateRemoteUpgrade(int type, String update) {
			Message msg = handler.obtainMessage();
			msg.obj = update;
			if (Type.DowndLoadTaskUpgrade != type) {
				try {
					msg.arg1 = (int) Double.parseDouble(update);
				} catch (NumberFormatException ef) {
					try {
						msg.arg1 = (int) Integer.parseInt(update);
					} catch (NumberFormatException ei) {
						ei.printStackTrace();
					}
				}
				msg.obj = msg.arg1 + "%";
			}
			msg.arg2 = type;
			
			//<add by sunjianshun 2012.11.24 BEGIN
			//【fix bug]】【MEDIA-4703】【升级】【场景插入法】升级版本的时候，点击通知栏里面的进度条提示解析包失败
			if (msg.arg1 == 100) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(Const.Path.AppUpgradeFileName)), "application/vnd.android.package-archive");
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				Notification notification = mNotificationList.get(Type.AppUpgrade );
				if (notification != null) {
					notification.contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				}
			}
			//add by sunjianshun 2012.11.24 END>
			
			msg.sendToTarget();
		}

		public class DownLoadServiceBinder extends Binder {
			
			public DownLoadService getService() {
				return DownLoadService.this;
			}
		}

		public Handler handler = new Handler() {
			
			@Override
			public void handleMessage(Message msg) {
				Notification notification = mNotificationList.get(msg.arg2);
				if (null != notification) {
					if (Type.DowndLoadTaskUpgrade != msg.arg2) {
						notification.contentView.setProgressBar(
								R.id.remote_progressbar, REMOTE_PROGRESS_SIZE,
								msg.arg1, false);
					}
					notification.contentView.setTextViewText(	R.id.remote_progress_rate, msg.obj.toString());
					
					// APK升级
					if (msg.arg2 == Type.AppUpgrade) {
						if (msg.arg1 == REMOTE_PROGRESS_SIZE) {
							notification.contentView.setTextViewText(R.id.remote_progress_title, getText(R.string.upgrade_remote_apk_downloaded_title));
							notification.contentView.setViewVisibility(R.id.remote_downloaded_rate, View.VISIBLE);
							notification.contentView.setViewVisibility(R.id.remote_progress_rate, View.GONE);
						}
						else {
							notification.contentView.setTextViewText(R.id.remote_progress_title, getText(R.string.upgrade_remote_apk_downloading_title));
							notification.contentView.setViewVisibility(R.id.remote_downloaded_rate, View.GONE);
							notification.contentView.setViewVisibility(R.id.remote_progress_rate, View.VISIBLE);
						}
					}
					// 内核升级
					else if (msg.arg2 == Type.PlayerCoreUpgrade){
						
						if (msg.arg1 == REMOTE_PROGRESS_SIZE) {
							mManager.cancel(msg.arg2);
							return;
						}
						else if (msg.arg1 == REMOTE_FAILED_SIZE) {
							notification.contentView.setViewVisibility(R.id.remote_downloaded_rate, View.VISIBLE);
							notification.contentView.setViewVisibility(R.id.remote_progress_rate, View.GONE);
							notification.contentView.setViewVisibility(R.id.remote_progressbar, View.GONE);
							notification.contentView.setTextViewText(R.id.remote_downloaded_rate, getText(R.string.upgrade_remote_playercore_downloaded_failed));
							ToastUtil.showMessage(mContext, getText(R.string.upgrade_remote_playercore_downloaded_failed).toString(), Toast.LENGTH_LONG);
						}
						else {
							notification.contentView.setViewVisibility(R.id.remote_downloaded_rate, View.GONE);
							notification.contentView.setViewVisibility(R.id.remote_progress_rate, View.VISIBLE);
							notification.contentView.setViewVisibility(R.id.remote_progressbar, View.VISIBLE);
						}
					}
					// 下载，在其他的XML中
					else {
						if (msg.arg1 == REMOTE_PROGRESS_SIZE) {
							mManager.cancel(msg.arg2);
							return;
						}
					}
					
					// 通知通知栏
					mManager.notify(msg.arg2, notification);
				}
			}
		};
	}

}
