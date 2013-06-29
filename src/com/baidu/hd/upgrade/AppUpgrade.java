package com.baidu.hd.upgrade;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.Gravity;
import android.widget.Toast;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.conf.Configuration;
import com.baidu.hd.ctrl.PopupDialog;
import com.baidu.hd.ctrl.PopupDialog.ReturnType;
import com.baidu.hd.detect.Detect;
import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.log.Logger;
import com.baidu.hd.net.HttpComm;
import com.baidu.hd.net.HttpDownloader;
import com.baidu.hd.net.HttpResultCallback;
import com.baidu.hd.net.HttpResultCallback.HttpDownloaderResult;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.stat.MarketChannelHelper;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.PlayerTools;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.SystemUtil;
import com.baidu.hd.R;

class AppUpgrade
{
	private Logger logger = new Logger("AppUpgrade");
	
	private LocalInfo mLocalInfo = null;
	private RemoteInfo mRemoteInfo = null;

	private Context mContext = null;
	private ServiceFactory mServiceFactory = null;

	private boolean mCheckWorking = false;
	private boolean mDownloadWorking = false;
	private boolean mCancel = false;
	private boolean mIsForce = false;
	private boolean mIsSilence = false;
	private RemoteUpgrade mUpgrade = null;

//	private Handler mHandler = new Handler()
//	{
//		@Override
//		public void handleMessage(Message msg)
//		{
//			check();
//		}
//	};

	private HttpResultCallback mHttpCallback = new HttpResultCallback()
	{
		float mark = 0;
		
		@Override
		public void onResponse(HttpDownloaderResult result, String url, String message)
		{
			if(mContext == null || !BaiduHD.cast(mContext).getServiceContainer().isCreated()) {
				return;
			}
			if (mRemoteInfo != null && url.equalsIgnoreCase(mRemoteInfo.getUrl()))
			{
				logger.d(result.toString());
				onDownloadFinish(result);
			}
			else
			{
				onCheck(HttpDownloaderResult.eSuccessful == result, message);
			}
		}
		
		@Override
		public void onProgress(String url, float rate)
		{
			if(mContext == null || !BaiduHD.cast(mContext).getServiceContainer().isCreated()) {
				return;
			}
			if (mRemoteInfo != null && url.equalsIgnoreCase(mRemoteInfo.getUrl()))
			{
				if(rate > mark + 0.1 || rate == 1.0)
				{	
					mark = rate;
					mUpgrade.updateRemoteUpgrade(RemoteUpgrade.Type.AppUpgrade, rate*100 + "");
					if (rate == 1.0)
					{
						mark = 0;
					}
				}
			}
		}
	};

	public void create(Context context, ServiceFactory factory)
	{
		this.mContext = context;
		this.mServiceFactory = factory;

		this.mLocalInfo = new LocalInfo(this.mContext);
		this.mUpgrade = (RemoteUpgrade) this.mServiceFactory.getServiceProvider(RemoteUpgrade.class);
	}

	public void destroy()
	{
//		this.mHandler.removeMessages(0);
		mDownloadWorking = false;
//		mCheckWorking = false;
	}

	public void cancel()
	{
		this.mCancel = true;
	}
	
	public int getStatus()
	{
		if (this.mCheckWorking)
		{
			return 1;
		}
		else if (this.mDownloadWorking)
		{
			return 2;
		}
		else return 0;
	}

	public void check(boolean isForce, boolean isSilence)
	{
		logger.d("check mDownloadWorking = " + mDownloadWorking + "; mCheckWorking = " + mCheckWorking);
		
		this.mIsForce = isForce;
		this.mIsSilence = isSilence;
		this.mCancel = false;
		if (this.mDownloadWorking || this.mCheckWorking)
		{
			if (this.mDownloadWorking)
			{
				logger.d("isworking, not upgrade");
				EventCenter center = (EventCenter) this.mServiceFactory.getServiceProvider(EventCenter.class);
				center.fireEvent(EventId.eUpgradeCheckComplete, new UpgradeEventArgs(false, true));
			}
			return;
		}
		this.mCheckWorking = true;
		String urlFormat = this.getConf().getAppUpgradeUrl();
		String url = String.format(urlFormat, SystemUtil.getEmid(mContext), SystemUtil.getAppVerison(mContext), MarketChannelHelper.getInstance(mContext).getChannelID());
		new HttpComm().get(url, this.mHttpCallback);
	}

	private void onCheck(boolean success, String message)
	{
		logger.d("onCheck");
		
		if (this.mCancel)
		{
			logger.d("cancelled task");
			return;
		}

		UpgradeEventArgs args = null;
		if (success)
		{
			mRemoteInfo = new RemoteInfo(message);
			boolean haveNew = (this.mLocalInfo.compare(this.mRemoteInfo.getVersion()) == -1);
			if (!mIsForce && haveNew)
			{
				SharedPreferences pre = mContext.getSharedPreferences(Key.FileName, Context.MODE_WORLD_WRITEABLE);
				String ignoreVersion = pre.getString(Key.AppIgnoreVerstion, "");
				if (!StringUtil.isEmpty(ignoreVersion))
				{
					haveNew = (this.mRemoteInfo.getVersion().compareToIgnoreCase(ignoreVersion) != 0);
				}
			}
			args = new UpgradeEventArgs(true, haveNew);
			logger.d(String.valueOf( haveNew));
			if (haveNew)
			{
				this.showDlg(mRemoteInfo.getComment());
			}
		}
		else
		{
			this.logger.e("check net fail");
			args = new UpgradeEventArgs(false, false);
			if (this.mIsForce)
			{
				Toast toast = Toast.makeText(mContext, R.string.dialog_upgrade_check_error, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		}
		EventCenter center = (EventCenter) this.mServiceFactory.getServiceProvider(EventCenter.class);
		center.fireEvent(EventId.eUpgradeCheckComplete, args);
		this.mCheckWorking = false;
	}

	private void showDlg(String message)
	{
		BaiduHD playerApp = BaiduHD.cast(mContext);
		if (playerApp.getCurrentActivity() == null)
		{
			return;
		}
		//wifi下直接下载，不弹对话框，弹toast提示
		Detect detect = (Detect)this.mServiceFactory.getServiceProvider(Detect.class);
		if (null != detect && detect.isNetAvailabeWithWifi() && mIsForce)
		{
			Toast toast = Toast.makeText(mContext, R.string.dialog_app_upgrade_downloading, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			downloadApk();
		}
		else 
		{
			PopupDialog dialog = new PopupDialog(playerApp.getCurrentActivity(), new PopupDialog.Callback()
			{
				@Override
				public void onReturn(ReturnType type, boolean checked)
				{
					if (type == PopupDialog.ReturnType.OK)
					{
						downloadApk();
					}
					else {
						//<add by sunjianshun 2012.11.24 BEGIN
						// 检查内核更新
						EventCenter center = (EventCenter) AppUpgrade.this.mServiceFactory.getServiceProvider(EventCenter.class);
						center.fireEvent(EventId.eUpgradeCheckCancel, new EventArgs());
						//add by sunjianshun 2012.11.24 END>
					}
					if (checked)
					{
						SharedPreferences pre = mContext.getSharedPreferences(Key.FileName, Context.MODE_WORLD_WRITEABLE);
						pre.edit().putString(Key.AppIgnoreVerstion, mRemoteInfo.getVersion()).commit();
					}
				}
			});
			dialog.setTitle(dialog.createText(R.string.dialog_app_upgrade_title)).setScrollMessage(dialog.createText(message)).setPositiveButton(dialog.createText(R.string.dialog_upgrade_ok))
			.setNegativeButton(dialog.createText(R.string.dialog_upgrade_cancel));
			if (!mIsForce)
			{
				dialog.setCheckBox(dialog.createText(R.string.dialog_upgrade_cancel_current));
			}
			dialog.show();
		}
	}

	private void downloadApk()
	{
		logger.d("downloadApk");
		
		this.mDownloadWorking = true;
		if (!new File(Const.Path.AppUpgradeFilePath).exists())
		{
			new File(Const.Path.AppUpgradeFilePath).mkdirs();
		}
		mUpgrade.startRemoteUpgrade(RemoteUpgrade.Type.AppUpgrade, mContext.getText(R.string.settings_other_update_name).toString());
		new HttpDownloader().download(this.mRemoteInfo.getUrl(), Const.Path.AppUpgradeFileName, this.mHttpCallback);
	}

	private void onDownloadFinish(HttpDownloaderResult result)
	{
		this.mDownloadWorking = false;
//		this.mUpgrade.stopRemoteUpgrade(RemoteUpgrade.Type.AppUpgrade);
		if (HttpDownloaderResult.eSuccessful != result)
		{
			this.logger.e("download apk fail");
			int messageID = R.string.dialog_upgrade_checknosdcard_faild;
			//检查SD卡是否可用，空间是否足够
			if (PlayerTools.checkSDPath())
			{
				if(HttpDownloaderResult.eWriteError == result)
				{
					messageID = R.string.dialog_upgrade_checksdcard_noenoughspace_faild;
				}
				else if(HttpDownloaderResult.eReadError == result)
				{
					messageID = R.string.dialog_upgrade_check_error;
				}
				else
				{
					messageID = R.string.dialog_upgrade_failed;
				}
			}
			
			Toast toast = Toast.makeText(mContext, messageID, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			
			new FileHandler(mContext).deleteAppSDCard();
			mUpgrade.stopRemoteUpgrade(RemoteUpgrade.Type.AppUpgrade);
			return;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(new File(Const.Path.AppUpgradeFileName)), "application/vnd.android.package-archive");
		this.mContext.startActivity(intent);
		this.logger.i("setup new package");
	}

	private Configuration getConf()
	{
		return (Configuration) this.mServiceFactory.getServiceProvider(Configuration.class);
	}
}
