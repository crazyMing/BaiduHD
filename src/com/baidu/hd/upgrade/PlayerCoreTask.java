package com.baidu.hd.upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.conf.Configuration;
import com.baidu.hd.ctrl.PopupDialog;
import com.baidu.hd.ctrl.PopupDialog.ReturnType;
import com.baidu.hd.detect.Detect;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.log.DebugLogger;
import com.baidu.hd.log.Logger;
import com.baidu.hd.net.HttpComm;
import com.baidu.hd.net.HttpDownloader;
import com.baidu.hd.net.HttpResultCallback;
import com.baidu.hd.net.HttpResultCallback.HttpDownloaderResult;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.PlayerTools;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.SystemUtil;
import com.baidu.hd.R;

class PlayerCoreTask extends Thread
{
	private enum CheckSDCardState 
	{
		eNone,
		eMD5Error,
		eNoSDCard,
		eNoWriteable,
		eNoEnoughSpace,
	}
	
	private static final int PlayerCoreTotalSize = 10 * 1024 * 1024 * 8;
	private static final int FireEventMsg = 1;
	private static final int ShowDialogMsg = 2;
	private static final int CopySDCard = 3;
	private static final int CheckSDCard = 4;
	private static final int CheckStoreIn = 5;
	private static final int CheckNetFaild = 6;

	private static final int OK = 1;
	private static final int Cancel = 2;

	private Logger logger = new Logger("PlayerCoreTask");
	private Looper looper = null;
	
	private Context mContext = null;

	/** 宿主回调 */
	private PlayerCoreUpgrade.Callback mCallback = null;

	/** 检查文件是否匹配 */
	private FileHandler mFileHandler = null;
	private float mDownloadTotalRate = 0;

	/** 标准包 */
	private List<Package> mFiles = new ArrayList<Package>();

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// flag
	/** 操作是否失败 */
	private boolean isFail = false;

	/** 是否用户选择了取消 */
	private boolean isUserCancel = false;

	/** 是否需要重命名 */
	private boolean needRename = true;

	/** 是否所有文件均下载 */
	private boolean isAllFileIsDownloaded = false;
	// /////////////////////////////////////////////////////////////////////////////////////////////
	// handler
	private Handler mWorkHandler = null;

	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if (msg.what == FireEventMsg)
			{
				EventCenter center = (EventCenter) mCallback.getServiceFactory().getServiceProvider(EventCenter.class);
				center.fireEvent(EventId.ePlayCoreCheckComplete, new UpgradeEventArgs(msg.arg1 == 1, msg.arg2 == 1));
				logger.d("ePlayCoreCheckComplete : " + (msg.arg1 == 1) + (msg.arg2 == 1));
			}
			else if (msg.what == ShowDialogMsg)
			{
				if (!mCallback.isForce())
				{
					SharedPreferences pre = mCallback.getContext().getSharedPreferences(Key.FileName, Context.MODE_WORLD_WRITEABLE);
					String ignoreVersion = pre.getString(Key.PlayerCoreIgnoreVerstion, "");
					if (StringUtil.isEmpty(ignoreVersion) || getAllMD5(mFiles).compareToIgnoreCase(ignoreVersion) != 0)
					{
						showDialog();
					}
				}
				else 
				{
					showDialog();
				}
			} 
			else if (msg.what == CopySDCard && msg.arg1 == 0)
			{
				Toast toast = Toast.makeText(mContext, R.string.dialog_upgrade_copy_playercore_faild, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
			else if (msg.what == CheckNetFaild)
			{
				Toast toast = Toast.makeText(mContext, R.string.dialog_upgrade_check_error, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
			else if (msg.what == CheckStoreIn)
			{
				Toast toast = Toast.makeText(mContext, R.string.dialog_playercore_upgrade_checkstorein_noenoughspace_faild, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
			else if (msg.what == CheckSDCard)
			{
				int messageID = R.string.dialog_upgrade_rename_playercore_other_faild;
				if (msg.obj == CheckSDCardState.eMD5Error)
				{
					messageID = R.string.dialog_upgrade_playercore_checkmd5_faild;
				} 
				else if (msg.obj == CheckSDCardState.eNoEnoughSpace)
				{
					messageID = R.string.dialog_playercore_upgrade_checksdcard_noenoughspace_faild;
				}
				else if (msg.obj == CheckSDCardState.eNoSDCard)
				{
					messageID = R.string.dialog_playercore_upgrade_checknosdcard_faild;
				}
				else if (msg.obj == CheckSDCardState.eNoWriteable)
				{
					messageID = R.string.dialog_playercore_upgrade_checksdcard_nowriteable_faild;
				}
				Toast toast = Toast.makeText(mContext, messageID, Toast.LENGTH_SHORT);;
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		}
	};

	public PlayerCoreTask(PlayerCoreUpgrade.Callback callback, Context context)
	{
		mCallback = callback;
		mContext = context;
		mFileHandler = new FileHandler(mCallback.getContext());
	}

	public boolean isFail()
	{
		return isFail;
	}

	public boolean isUserCancel()
	{
		return isUserCancel;
	}
	
	public boolean isAllFileIsDownloaded() {
		return isAllFileIsDownloaded;
	}

	public boolean needRename()
	{
		return needRename;
	}

	public int rename()
	{
		logger.d("rename library");
		return mFileHandler.rename();
	}

	@Override
	public void run()
	{
		if (null != Looper.myLooper())
		{
			mCallback.onCheckComplete();
			fireEvent(false, true);
			return;
		}
		Looper.prepare(); 
		looper = Looper.myLooper();
		mWorkHandler = new Handler(looper)
		{
			@Override
			public void handleMessage(Message msg)
			{
				if (msg.what == OK)
				{
					onDownload();
				}
				if (msg.what == Cancel)
				{
					complete();
				}
			}
		};
		check();
		Looper.loop();
		return;
	}
	
	private void check()
	{
		String cpuinfo = SystemUtil.getCPUInfo();
		if (StringUtil.isEmpty(cpuinfo))
		{
			logger.e("empty cpuinfo");
			fireEvent(true, false);
			complete();
			return;
		}
		String url = this.getConf().getPlayerCoreUpgradeUrl();
		Map<String, String> param = new HashMap<String, String>();
		param.put("cpuinfo", cpuinfo);
		logger.d("check()");
		new HttpComm(false).post(url, param, true, new HttpResultCallback()
		{ 
			@Override
			public void onResponse(HttpDownloaderResult result, String url, String message)
			{
				logger.d("check() : onResponse");
				if(mContext == null || !BaiduHD.cast(mContext).getServiceContainer().isCreated()) 
				{
					return;
				}
				onCheck(result, message);
				mCallback.onCheckComplete();
			}
			
			@Override
			public void onProgress(String url, float rate)
			{
			}
		});
	}

	private void onCheck(HttpDownloaderResult result, String message)
	{
		if (mCallback.isCancel())
		{
			logger.d("cancelled task");
			needRename = false;
			complete();
			return;
		}
		
//		mCallback.onCheckComplete();
		
		if (HttpDownloaderResult.eSuccessful != result)
		{
			logger.e("check net fail");
			if (!mCallback.isSilence())
			{
				mHandler.sendMessage(mHandler.obtainMessage(CheckNetFaild));
			}
			fireEvent(false, false);
			needRename = false;
			complete();
			return;
		}
		mFiles = parseCheckResult(message);
		// 不需要更新
		if (mFiles.isEmpty())
		{
			logger.d("need not upgrade");
			fireEvent(true, false);
			needRename = false;
			complete();
			return;
		}
		mFileHandler.set(convertToMap(mFiles));
		// 比较lib
		if (mFileHandler.checkLib())
		{
			logger.d("check lib true");
			fireEvent(true, false);
			needRename = false;
			mFileHandler.deleteFile();
			mFileHandler.deleteFileTmp();
			complete();
			return;
		}
		// 比较file
		if (mFileHandler.checkFile())
		{
			logger.d("check file true");
			fireEvent(true, false);
			needRename = false;
			mFileHandler.deleteFileTmp();
			complete();
			return;
		}
		// 比较file tmp
		if (mFileHandler.checkFileTmp())
		{
			logger.d("check file tmp true");
			fireEvent(true, false);
			complete();
			return;
		}
		// 比较sdcard
		if (mFileHandler.checkSDCard())
		{
			logger.d("check sdcard true");
			fireEvent(false, false);
			if (PlayerTools.getAvailaleSize( PlayerTools.getStoreInPath()) == -1) {
				logger.e("sdcard error");
				needRename = false;
				mFileHandler.deleteFileTmp();
				isFail = true;
				mHandler.sendMessage(mHandler.obtainMessage(CheckSDCard, CheckSDCardState.eNoSDCard));
			}
			else if (PlayerCoreTotalSize <= PlayerTools.getAvailaleSize( PlayerTools.getStoreInPath()))
			{
				if (!mFileHandler.copySDCard())
				{
					logger.e("copy from sdcard fail");
					needRename = false;
					mFileHandler.deleteFileTmp();
					isFail = true;
					mHandler.sendMessage(mHandler.obtainMessage(CopySDCard, 0, 0));
				}
			}
			else 
			{
				mHandler.sendMessage(mHandler.obtainMessage(CheckStoreIn));
			}
			complete();
			return;
		}
		//检查SD卡是否可用，空间是否足够
		if (PlayerTools.checkSDPath())
		{
			if (PlayerTools.getAvailaleSize( PlayerTools.getSDPath()) == -1) {
				fireEvent(false, false);
				mHandler.sendMessage(mHandler.obtainMessage(CheckSDCard, CheckSDCardState.eNoSDCard));
				needRename = false;
				complete();
				return;
			}
		    else if (PlayerCoreTotalSize <= PlayerTools.getAvailaleSize( PlayerTools.getSDPath()))
			{
				fireEvent(true, true);
				mHandler.sendEmptyMessage(ShowDialogMsg);
			}
			else 
			{
				fireEvent(false, false);
				mHandler.sendMessage(mHandler.obtainMessage(CheckSDCard, CheckSDCardState.eNoEnoughSpace));
				needRename = false;
				complete();
				return;
			}
		}
		else 
		{
			fireEvent(false, false);
			mHandler.sendMessage(mHandler.obtainMessage(CheckSDCard, CheckSDCardState.eNoSDCard));
			needRename = false;
			complete();
			return;
		}
	}

	private void showDialog()
	{
		BaiduHD playerApp = BaiduHD.cast(mCallback.getContext());
		if (playerApp.getCurrentActivity() == null)
		{
			logger.e("have not parent activity");
			isFail = true;
			complete();
			return;
		}

		//wifi下直接下载，不弹对话框，弹toast提示
		Detect detect = (Detect)mCallback.getServiceFactory().getServiceProvider(Detect.class);
		if (null != detect && detect.isNetAvailabeWithWifi())
		{
			mWorkHandler.sendEmptyMessage(OK);
			if (!mCallback.isSilence())
			{
				Toast toast = Toast.makeText(mContext, R.string.dialog_player_core_upgrade_downloadding, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
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
						logger.d("user ok");
						mWorkHandler.sendEmptyMessage(OK);
					}
					else if (type == PopupDialog.ReturnType.Cancel)
					{
						logger.d("user cancel");
						mWorkHandler.sendEmptyMessage(Cancel);
						isUserCancel = true;
					}
					if (checked)
					{
						SharedPreferences pre = mCallback.getContext().getSharedPreferences(Key.FileName, Context.MODE_WORLD_WRITEABLE);
						pre.edit().putString(Key.PlayerCoreIgnoreVerstion, getAllMD5(mFiles)).commit();
					}
				}
			});
			dialog.setTitle(dialog.createText(R.string.dialog_player_core_upgrade_title)).setMessage(dialog.createText(R.string.dialog_player_core_upgrade_message))
			.setPositiveButton(dialog.createText(R.string.dialog_upgrade_ok)).setNegativeButton(dialog.createText(R.string.dialog_upgrade_cancel));
			if (!mCallback.isForce())
			{
				dialog.setCheckBox(dialog.createText(R.string.dialog_upgrade_cancel_current), false);
			}
			dialog.show();
		}
	}

	private void onDownload()
	{
		mFileHandler.createSDCardDir();
		mFileHandler.deleteSDCard();
		final float mFilesNum = mFiles.size();
		mDownloadTotalRate = 0;
		mCallback.onStartRemote(!mCallback.isSilence());
		for (int i = 0; i < this.mFiles.size(); i++)
		{
			Package pack = this.mFiles.get(i);
			new HttpDownloader(false).download(pack.url, Const.Path.PlayerCoreUpgradeFilePath + pack.name, new HttpResultCallback()
			{
				float mark = 0;
				
				@Override
				public void onResponse(HttpDownloaderResult result, String url, String message)
				{
					onDownloadComplete(result, url);
					if (HttpDownloaderResult.eSuccessful == result)
					{
						mDownloadTotalRate += 1.0f;
						mark = 0;
					}
				}
				
				@Override
				public void onProgress(String url, float rate)
				{
					if(rate > mark + 0.1f  || rate == 1.0f)
					{
						mark = rate;
						if (!mCallback.isSilence())
						{
							mCallback.onProgressRemote((mDownloadTotalRate + rate) / mFilesNum);
						}
					}
				}
			});
		}
		
//		mCallback.onStopRemote(!mCallback.isSilence());
		mCallback.onDownloadComplete(mFilesNum == mDownloadTotalRate);
		isAllFileIsDownloaded = (mFilesNum == mDownloadTotalRate);
		
		logger.d("onDownload : " + mFilesNum + " == " + mDownloadTotalRate);
	}

	private void onDownloadComplete(HttpDownloaderResult result, String url)
	{
		logger.d("onDownloadComplete url:" + url + " success:" + result);
		// 设置标志位
		for (Package pack : mFiles)
		{
			if (pack.url.equalsIgnoreCase(url))
			{
				pack.downloaded = true;
				if (HttpDownloaderResult.eSuccessful != result)
				{
					pack.error = true;
				}
				break;
			}
		}
		// 判断完全下载完成
		boolean isError = false;
		for (Package pack : mFiles)
		{
			if (!pack.downloaded)
			{
				return;
			}
			if (pack.error)
			{
				isError = true;
			}
		}
		if (isError)
		{
			logger.d("all download complete, but have error");
			mFileHandler.deleteSDCard();
			isFail = true;
			//判断失败的原因是网络问题还是磁盘空间不足
			if (PlayerTools.checkSDPath())
			{
				if (HttpDownloaderResult.eReadError == result)
				{
					if (!mCallback.isSilence())
					{
						mHandler.sendMessage(mHandler.obtainMessage(CheckNetFaild));
					}
				}
				else if (HttpDownloaderResult.eWriteError == result)
				{
					mHandler.sendMessage(mHandler.obtainMessage(CheckSDCard, CheckSDCardState.eNoEnoughSpace));
				}
			}
			else 
			{
				mHandler.sendMessage(mHandler.obtainMessage(CheckSDCard, CheckSDCardState.eNoSDCard));
			}
		}
		else
		{
			logger.d("all download complete, all success");
			// 校验sdcard 的md5
			if (mFileHandler.checkSDCard())
			{
				logger.d("check sdcard md5 true");
				if (PlayerTools.getAvailaleSize(PlayerTools.getStoreInPath()) == -1) {
					logger.e(" sdcard failed");
					needRename = false;
					mFileHandler.deleteFileTmp();
					isFail = true;
					mHandler.sendMessage(mHandler.obtainMessage(CheckSDCard, CheckSDCardState.eNoSDCard));
				}
				else if (PlayerCoreTotalSize <= PlayerTools.getAvailaleSize(PlayerTools.getStoreInPath()))
				{
					if (!mFileHandler.copySDCard())
					{
						logger.e("copy from sdcard failed");
						needRename = false;
						mFileHandler.deleteFileTmp();
						isFail = true;
						mHandler.sendMessage(mHandler.obtainMessage(CopySDCard, 0, 0));
					}
				}
				else
				{
					mHandler.sendMessage(mHandler.obtainMessage(CheckStoreIn));
				}
			}
			else 
			{
				logger.d("check sdcard md5 false");
				mFileHandler.deleteSDCard();
				isFail = true;
				if (!mCallback.isSilence())
				{
					mHandler.sendMessage(mHandler.obtainMessage(CheckSDCard, CheckSDCardState.eMD5Error));
				}
			}
		}
		complete();
	}

	private List<Package> parseCheckResult(String message)
	{
		List<Package> packs = new ArrayList<Package>();
		try
		{
			JSONObject root = new JSONObject(message);
			if (!root.optBoolean("need"))
			{
				this.logger.d("check result, need not");
				return packs;
			}

			JSONArray list = root.optJSONArray("list");
			for (int i = 0; i < list.length(); ++i)
			{
				Package pack = new Package();
				JSONObject file = list.optJSONObject(i);

				pack.name = file.optString("nm");
				pack.md5 = file.optString("md5");
				pack.url = file.optString("ul");
				packs.add(pack);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return packs;
	}

	// /////////////////////////////////////////////////////////////////////////
	// helper
	private void fireEvent(boolean success, boolean haveNew)
	{
		logger.d("FireEventMsg : " + success + haveNew);
		mHandler.sendMessage(mHandler.obtainMessage(FireEventMsg, success ? 1 : 0, haveNew ? 1 : 0));
	}

	private void complete()
	{
		mCallback.onComplete();
		if (null != looper)
		{
			looper.quit();
			looper = null;
		}
	}

	private Map<String, String> convertToMap(List<Package> info)
	{
		Map<String, String> result = new HashMap<String, String>();
		for (Package pack : info)
		{
			result.put(pack.name, pack.md5);
		}
		return result;
	}
	
	private String getAllMD5(List<Package> info)
	{
		String result = "";
		for (Package pack : info)
		{
			result += pack.md5;
		}
		return result;
	}

	private Configuration getConf()
	{
		return (Configuration) mCallback.getServiceFactory().getServiceProvider(Configuration.class);
	}

	private class Package
	{
		private String url = "";
		private String name = "";
		private String md5 = "";
		private boolean downloaded = false;
		private boolean error = false;
	}
}
