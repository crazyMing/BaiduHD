package com.baidu.hd.upgrade;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

import com.baidu.hd.R;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.log.Logger;
import com.baidu.hd.player.CyberPlayerHolder;
import com.baidu.hd.service.ServiceFactory;

/**
 * 
 * 
 * 简化设计，通过定时器来重命名播放内核
 */
class PlayerCoreUpgrade {

	private final static int DownLoadSuccessful = 2;
	private final static int UpgradeComplete = 3;
	private final static int TryToRename = 4;
	private final static int UpgradeCompleteToRename = 5;

	private Logger logger = new Logger("PlayerCoreUpgrade");
	private boolean mCheckWorking = false;
	private boolean mDownloadWorking = false;
	private boolean mCancel = false;
	private boolean mIsForce = false;
	private boolean mIsSilence = false;

	private Context mContext = null;
	private ServiceFactory mServiceFactory = null;

	private PlayerCoreTask mTask = null;
	private RemoteUpgrade mUpgrade = null;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == DownLoadSuccessful) {
				logger.d("download player core successed");
			} else if (msg.what == UpgradeComplete) {
				onComplete();
			} else if (msg.what == TryToRename) {
				tryToRename();
			} else if (msg.what == UpgradeCompleteToRename) {
				int textID = 0;
				if (getHolder().isUsing()) {
					textID = R.string.dialog_upgrade_copy_playercore_successed;
				} else if (!mIsSilence) {
					textID = R.string.dialog_upgrade_copy_playercore_successed;
				}
				if (!mIsSilence) {
					if (msg.arg1 == 1) {
						textID = R.string.dialog_upgrade_rename_playercore_using_faild;
					} else if (msg.arg1 == 2) {
						textID = R.string.dialog_upgrade_rename_playercore_other_faild;
					}
				}
				if (0 != textID) {
					Toast toast = Toast.makeText(mContext, textID, Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			}
		}
	};

	public void create(Context context, ServiceFactory factory) {
		mContext = context;
		mServiceFactory = factory;
		mUpgrade = (RemoteUpgrade) this.mServiceFactory
				.getServiceProvider(RemoteUpgrade.class);
	}

	public void destroy() {
		mHandler.removeMessages(DownLoadSuccessful);
		mHandler.removeMessages(UpgradeComplete);
		mHandler.removeMessages(TryToRename);
		mHandler.removeMessages(UpgradeCompleteToRename);
	}

	public void cancel() {
		mCancel = true;
	}

	public int getStatus() {
		if (this.mCheckWorking) {
			return 1;
		} else if (this.mDownloadWorking) {
			return 2;
		} else
			return 0;
	}

	public void check(boolean isForce, boolean isSilence) {
		this.mIsForce = isForce;
		this.mIsSilence = isSilence;
		this.mCancel = false;
		if (this.mDownloadWorking || this.mCheckWorking) {
			if (this.mDownloadWorking) {
				new Callback().onStartRemote(!this.mIsSilence);
				logger.d("isworking, not upgrade");
				EventCenter center = (EventCenter) this.mServiceFactory.getServiceProvider(EventCenter.class);
				center.fireEvent(EventId.ePlayCoreCheckComplete, new UpgradeEventArgs(false, true));
			}
			return;
		}
		this.mCheckWorking = true;
		mTask = new PlayerCoreTask(new Callback(), mContext);
		mTask.start();
	}

	private void tryToRename() {
		logger.d("tryToRename() " + mTask + mDownloadWorking + mCheckWorking);
		// 解决性能差的手机，在第一次快速切换到更多标签时，不能正常关闭loading的问题。
		EventCenter center = (EventCenter) mServiceFactory.getServiceProvider(EventCenter.class);
		center.fireEvent(EventId.ePlayCoreCheckComplete, new UpgradeEventArgs(false, false));

		if ((mTask == null) || mDownloadWorking || mCheckWorking) {
			return;
		}
		if(getHolder().isUsing()) {
			mHandler.sendEmptyMessageDelayed(TryToRename, 5000);
			return;
		}
		getHolder().reset();
		int ret = mTask.rename();
		if (0 == ret) {
			logger.d("tryToRename succeed");
			mTask = null;
		} else if (1 == ret) {
			logger.e("tryToRename failed by using");
		} else if (2 == ret) {
			logger.e("tryToRename failed by permission is required");
		}
		mHandler.sendMessage(mHandler.obtainMessage(UpgradeCompleteToRename, ret, 0));
	}

	public class Callback {
		public ServiceFactory getServiceFactory() {
			return mServiceFactory;
		}

		public Context getContext() {
			return mContext;
		}

		public boolean isForce() {
			return mIsForce;
		}

		public boolean isSilence() {
			return mIsSilence;
		}

		public boolean isCancel() {
			return mCancel;
		}

		public void onStartRemote(boolean bStart) {
			mDownloadWorking = true;
			if (bStart) {
				mUpgrade.startRemoteUpgrade(RemoteUpgrade.Type.PlayerCoreUpgrade, mContext.getText(R.string.settings_other_update_playercore_name).toString());
			}
		}

		public void onStopRemote(boolean bStop) {
			mDownloadWorking = false;
			if (bStop) {
				mUpgrade.stopRemoteUpgrade(RemoteUpgrade.Type.PlayerCoreUpgrade);
			}
		}

		public void onDownloadComplete(boolean isSucceed) {
			if (isSucceed) {
				mHandler.sendEmptyMessage(DownLoadSuccessful);
			}
		}

		public void onProgressRemote(float rate) {
			mUpgrade.updateRemoteUpgrade(RemoteUpgrade.Type.PlayerCoreUpgrade, rate * 100 + "");
		}

		public void onCheckComplete() {
			mCheckWorking = false;
		}

		public void onComplete() {
			mHandler.sendEmptyMessage(UpgradeComplete);
		}
	}

	public void onComplete() {
		logger.d("onComplete()");
		if (null == mTask) {
			return;
		}

		mDownloadWorking = false;
		if (mTask.isUserCancel() || mTask.isFail() || !mTask.needRename()) {
			logger.d("isUserCancel = " + mTask.isUserCancel() + "   isFail = "
					+ mTask.isFail() + "   needRename = " + mTask.needRename());
			
			// <sunjianshun 2013.01.06 BEGIN
			// 【fix bug】33% 66%
			if (!mTask.isUserCancel() && !mIsSilence && mTask.isFail()) {
				if (mIsForce && !mTask.isAllFileIsDownloaded()) {
					mUpgrade.updateRemoteUpgrade(RemoteUpgrade.Type.PlayerCoreUpgrade,  "-1");
				}
			}
			// sunjianshun 2013.01.06 END>
			
			mTask = null;
			return;
		}

		tryToRename();
	}
	
	private CyberPlayerHolder getHolder() {
		return (CyberPlayerHolder)mServiceFactory.getServiceProvider(CyberPlayerHolder.class);
	}
}
