package com.baidu.hd.settings;

import java.io.File;
import java.text.DecimalFormat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.browser.db.HistoryConfig;
import com.baidu.hd.BaseActivity;
import com.baidu.hd.detect.Detect;
import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.log.Logger;
import com.baidu.hd.personal.SDCardUtil;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId.Settings;
import com.baidu.hd.task.TaskManager;
import com.baidu.hd.upgrade.RemoteUpgrade;
import com.baidu.hd.upgrade.Upgrade;
import com.baidu.hd.upgrade.UpgradeEventArgs;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.ProgressDialogAsyncTask;
import com.baidu.hd.util.ToastUtil;
import com.baidu.hd.util.ProgressDialogAsyncTask.ProgressDialogTask;
import com.baidu.hd.R;

public class SettingsActivity extends BaseActivity implements OnClickListener {

	private Logger logger = new Logger("SettingsActivity");
	
	/** 发送崩溃日志 */
	private CheckBox settings_crash_report = null;
	/** 显示搜索历史 */
	private CheckBox settings_privacy = null;
	/** 重启后打开上次未关闭页面*/
	private CheckBox settings_reopen_last_pages = null;
	
	/** 缓存路径 */
	//private TextView mBufferTextView = null;
//	
//	private static boolean isOnResumeAnimation = false;
//	private static boolean isOnPauseAnimation = false;
//	public static void setOnResumeAnimation (boolean isAnimation) {
//    	SettingsActivity.isOnResumeAnimation = isAnimation;
//    }
//    public static void setOnPauseAnimation (boolean isAnimation) {
//    	SettingsActivity.isOnPauseAnimation = isAnimation;
//    }
	
	private class UpgradeEventListener implements EventListener
	{
		private ProgressDialog progressDialog = null;

		public UpgradeEventListener(ProgressDialog progressDialog)
		{
			this.progressDialog = progressDialog;
		}

		@Override
		public void onEvent(EventId id, EventArgs args)
		{
			/** Apk ****************************************************************************************/
			if (id == EventId.eUpgradeCheckComplete) {
				
				UpgradeEventArgs upgradeArgs = (UpgradeEventArgs) args;
				int textId = 0;
				if (upgradeArgs.isSuccess())
				{
					if (!upgradeArgs.isHaveNew())
					{
						textId = R.string.dialog_upgrade_check_no_need;
					}
				}
				else
				{
					if (upgradeArgs.isHaveNew())
					{
						textId = R.string.dialog_upgrade_check_working;
						RemoteUpgrade remoteUpgrade = (RemoteUpgrade)SettingsActivity.this.getServiceProvider(RemoteUpgrade.class);
						remoteUpgrade.startRemoteUpgrade(RemoteUpgrade.Type.AppUpgrade, SettingsActivity.this.getText(R.string.settings_other_update_name).toString());
					}
					else
					{
					}
				}
				if (textId != 0)
				{
					Toast toast = Toast.makeText(SettingsActivity.this, textId, Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				if (this.progressDialog != null)
				{
					EventCenter eventCenter = (EventCenter) SettingsActivity.this.getServiceProvider(EventCenter.class);
					eventCenter.removeListener(this);
					progressDialog.dismiss();
				}
			}
			/** PlayerCore ****************************************************************************************/
			else if (id == EventId.ePlayCoreCheckComplete) {
				
				UpgradeEventArgs upgradeArgs = (UpgradeEventArgs) args;
				int textId = 0;
				if (upgradeArgs.isSuccess())
				{
					if (!upgradeArgs.isHaveNew())
					{
						textId = R.string.dialog_upgrade_check_no_need;
					}
				}
				else
				{
					if (upgradeArgs.isHaveNew())
					{
						textId = R.string.dialog_upgrade_check_working;
						RemoteUpgrade remoteUpgrade = (RemoteUpgrade)SettingsActivity.this.getServiceProvider(RemoteUpgrade.class);
						remoteUpgrade.startRemoteUpgrade(RemoteUpgrade.Type.PlayerCoreUpgrade, SettingsActivity.this.getText(R.string.settings_other_update_playercore_name).toString());
					}
					else
					{
					}
				}
				if (textId != 0)
				{
					Toast toast = Toast.makeText(SettingsActivity.this, textId, Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				if (this.progressDialog != null)
				{
					EventCenter eventCenter = (EventCenter) SettingsActivity.this.getServiceProvider(EventCenter.class);
					eventCenter.removeListener(this);
					progressDialog.dismiss();
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		update3GSuggestCheckBox();
		updatePrivacy();
		updateBufferPath();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bd_settings);
		settings_crash_report = (CheckBox)findViewById(R.id.settings_crash_report);
		settings_privacy = (CheckBox)findViewById(R.id.settings_privacy);
		settings_reopen_last_pages = (CheckBox)findViewById(R.id.settings_reopen_last_pages);
		//mBufferTextView = (TextView)findViewById(R.id.setting_buffer_path);
		
		((Button)findViewById(R.id.btn_titlebar_back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		ViewGroup holder = (ViewGroup)findViewById(R.id.bdsetttings_holder);
		for (int i=0; i<holder.getChildCount(); i++) {
			holder.getChildAt(i).setOnClickListener(this);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		/** 清除cookies */
		case R.id.bdsettings_clear_buffer:
			onClickClearBuffer();
			break;
		
		/** 清除缓存 */
		case R.id.bdsettings_clear_cache:
			onClickClearBuffer();
			//onClickWifi();
			//getStat().incEventCount(Settings.Name, Settings.Wifi);
			break;

		/** 显示搜索历史记录 */
		case R.id.bdsettings_privacy:
			onClickPrivacy();
			break;
			
		/** 发送崩溃报告 */
		case R.id.bdsettings_crash_report:
			onClickCrashReport();
			break;

		/** 启动时恢复未关闭标签设置 */
		case R.id.bdsettings_reopen_last_pages:
			onClickReopenLastPages();
			break;

//		/** 检查更新 */
//		case R.id.bdsettings_update:
//			onClickUpdate();
//			break;
//
//		/** 播放内核更新 */
//		case R.id.bdsettings_update_codecs:
//			onClickUpdateCodes();
//			break;

		/** 意见反馈 */
		case R.id.bdsettings_feedback:
			onClickFeedback();
			break;

		/** 关于 */
		case R.id.bdsettings_about:
			//onClickAbout();
			onClickUpdate();
			break;
		}
	}
	
	/** 清除软件缓存 */
	private void onClickClearBuffer() {
		new ProgressDialogAsyncTask<String, String, String>(SettingsActivity.this, new ProgressDialogTask<String, String, String>()
		{
			long deletedFiles = 0;
			ProgressDialogAsyncTask<String, String, String> asysncTask = null;
			
			TaskManager.clearGarbageEvent clearGarbageEvent = new TaskManager.clearGarbageEvent()
			{
				@Override
				public boolean isCancel()
				{
					// TODO Auto-generated method stub
					return false;
				}
				
				@Override
				public void clearSize(long size)
				{
					// TODO Auto-generated method stub
					deletedFiles += size;
					DecimalFormat df = new DecimalFormat("##0.00 ");
					float size_ = deletedFiles / 1024;
					size_ /= 1024;
					if (null != asysncTask)
					{
						asysncTask.upDate(df.format(size_));
					}
					SystemClock.sleep(0);
				}
			};
			
			@Override
			public void onPreExecute(ProgressDialogAsyncTask<String, String, String> asysncTask, ProgressDialog dialog)
			{
				this.asysncTask = asysncTask;
				dialog.setMessage(SettingsActivity.this.getText(R.string.settings_set_clearcache_title));
				dialog.setIndeterminate(true);
				dialog.setCanceledOnTouchOutside(false);
				dialog.setCancelable(true);
				dialog.show();
				dialog.getWindow().setGravity(Gravity.CENTER);
			}

			@Override
			public String doInBackground(String... param)
			{
				File file = null;
				Upgrade upgrade = (Upgrade)getServiceProvider(Upgrade.class);
				if (null != upgrade)
				{
					if (upgrade.upgradeAppStatus() == 0)
					{
						file = new File(Const.Path.AppUpgradeFilePath);
						deletedFiles = clearCacheFolder(file, deletedFiles, asysncTask);
					}
					if (upgrade.upgradePlayerCoreStatus() == 0)
					{
						file = new File(Const.Path.PlayerCoreUpgradeFilePath);
						deletedFiles = clearCacheFolder(file, deletedFiles, asysncTask);
					}
				}
				
				TaskManager taskManager = (TaskManager)getServiceProvider(TaskManager.class);
				if (null != taskManager)
				{
					taskManager.clearGarbage(clearGarbageEvent);
				}
				
				file = getCacheDir();
				deletedFiles = clearCacheFolder(file, deletedFiles, asysncTask);
				return null;
			}

			@Override
			public void onProgressUpdate(ProgressDialog dialog, String... values)
			{
				dialog.setMessage(SettingsActivity.this.getText(R.string.settings_set_clearcache_cleared) + values[0] + "MB");
			}

			@Override
			public void onPostExecute(String result)
			{
				Toast toast = Toast.makeText(SettingsActivity.this, SettingsActivity.this.getText(R.string.settings_set_clearcache_complete), Toast.LENGTH_SHORT);
				String clearResult = "";
				if (deletedFiles > 0)
				{
					DecimalFormat df = new DecimalFormat("##0.00 ");
					float size_ = deletedFiles / 1024;
					size_ /= 1024;
					clearResult = String.format(SettingsActivity.this.getText(R.string.settings_set_clearcache_complete_size).toString(), df.format(size_));
					toast = Toast.makeText(SettingsActivity.this, clearResult, Toast.LENGTH_SHORT);
				}
				RemoteUpgrade remoteUpgrade = (RemoteUpgrade)SettingsActivity.this.getServiceProvider(RemoteUpgrade.class);
				remoteUpgrade.stopRemoteUpgrade(RemoteUpgrade.Type.AppUpgrade);
				remoteUpgrade.stopRemoteUpgrade(RemoteUpgrade.Type.PlayerCoreUpgrade);
				remoteUpgrade.stopRemoteUpgrade(RemoteUpgrade.Type.DowndLoadTaskUpgrade);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			};
		}).execute();

//		deleteDatabase("webview.db");
//		deleteDatabase("webviewCache.db");
	}
	
	private long clearCacheFolder(File dir, long deletedFilesSize, ProgressDialogAsyncTask<String, String, String> progressDialog)
	{
		long deletedFiles = deletedFilesSize;
		if (dir != null && dir.isDirectory())
		{
			try
			{
				for (File child : dir.listFiles())
				{
					if (child.isDirectory())
					{
						deletedFiles = clearCacheFolder(child, deletedFiles, progressDialog);
					}
					else
					{
						deletedFiles += child.length();
						if (child.delete())
						{
							DecimalFormat df = new DecimalFormat("##0.00 ");
							if (null != progressDialog)
							{
								float size_ = deletedFiles / 1024;
								size_ /= 1024;
								progressDialog.upDate(df.format(size_));
								SystemClock.sleep(0);
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return deletedFiles;
	}
	
	/** 非wifi下播放/下载提示 */
	private void onClickWifi() {
//		m3GSuggestCheckBox.setChecked(!m3GSuggestCheckBox.isChecked());
//		Detect detect = (Detect) this.getServiceProvider(Detect.class);
//		detect.setNetPrompt(m3GSuggestCheckBox.isChecked());
	}
	
	private void update3GSuggestCheckBox() {
//		if (null != m3GSuggestCheckBox) {
//			Detect detect = (Detect) this.getServiceProvider(Detect.class);
//			m3GSuggestCheckBox.setChecked(detect.isNetPrompt());
//		}
	}
	
	private void updatePrivacy() {
		if (null != settings_privacy) {
			settings_privacy.setChecked(HistoryConfig.isPrivateMode(this));
		}
	}
	
	private void updateBufferPath() {
//		SharedPreferences sharedPreferences = 	getSharedPreferences(Const.SharedPreferences.NAME_NetVideoBufferPath, Context.MODE_PRIVATE);
//		String path = sharedPreferences.getString(Const.SharedPreferences.KEY_NetVideoBufferPath, Const.Path.NetVideoBufferFilePath);
//		mBufferTextView.setText(path);
	}
	
	/** 隐私设置 */
	private void onClickPrivacy() {
		settings_privacy.setChecked(!settings_privacy.isChecked());
		HistoryConfig.setPrivateMode(this, settings_privacy.isChecked());
	}
	
	/** 崩溃报告 */
	private void onClickCrashReport() {
		settings_crash_report.setChecked(!settings_crash_report.isChecked());
		
	}
	
	private void onClickReopenLastPages(){
		settings_reopen_last_pages.setChecked(!settings_reopen_last_pages.isChecked());
	}

	/** 缓存文件目录 */
	private void onClickBufferDirectory() {
		if (!SDCardUtil.getInstance().isMediaMounted()) {
			ToastUtil.showMessage(this, getText(R.string.dialog_sdcard_message).toString(), Toast.LENGTH_LONG);
			return;
		}
		
		Intent intent = new Intent(this, SettingsBufferPathActivity.class);
		startActivity(intent);
	}

	/** 检查更新 */
	private void onClickUpdate() {
		// 1.先检查网络
		Detect detect = (Detect)getServiceProvider(Detect.class);
		if(!detect.isNetAvailabe()) {
			ToastUtil.showMessage(this, getResources().getString(R.string.network_not_available), Toast.LENGTH_LONG);
			return;
		}
		
		// 2.开始检查
		final Upgrade upgrade = (Upgrade) this.getServiceProvider(Upgrade.class);
		final EventCenter eventCenter = (EventCenter) this.getServiceProvider(EventCenter.class);
		ProgressDialog dialog = new ProgressDialog(this);
		final EventListener listener = new UpgradeEventListener(dialog);
		eventCenter.addListener(EventId.eUpgradeCheckComplete, listener);

		dialog.setMessage(SettingsActivity.this.getText(R.string.settings_other_update_title));
		dialog.setIndeterminate(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(true);
		dialog.getWindow().setGravity(Gravity.CENTER);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				upgrade.cancelAppCheck();
				eventCenter.removeListener(listener);
			}
		});
		dialog.show();
		dialog.getWindow().setGravity(Gravity.CENTER);
		upgrade.manualAppCheck(true, false);
	}

	/** 播放内核更新 */
	private void onClickUpdateCodes() {
		// 1.先检查网络
		Detect detect = (Detect)getServiceProvider(Detect.class);
		if(!detect.isNetAvailabe()) {
			ToastUtil.showMessage(this, getResources().getString(R.string.network_not_available), Toast.LENGTH_LONG);
			return;
		}
		
		// 2.检查更新
		
		final Upgrade upgrade = (Upgrade) this.getServiceProvider(Upgrade.class);
		final EventCenter eventCenter = (EventCenter) this.getServiceProvider(EventCenter.class);
		ProgressDialog dialog = new ProgressDialog(this);
		final EventListener listener = new UpgradeEventListener(dialog);
		eventCenter.addListener(EventId.ePlayCoreCheckComplete, listener);

		dialog.setMessage(SettingsActivity.this.getText(R.string.settings_other_update_title));
		dialog.setIndeterminate(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(true);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				logger.d("onUpdateCodecsClickListener : onCancel");
				upgrade.cancelPlayerCoreCheck();
				eventCenter.removeListener(listener);
			}
		});
		dialog.show();
		dialog.getWindow().setGravity(Gravity.CENTER);
		upgrade.manualPlayerCoreCheck(true, false);
	}

	/** 意见反馈 */
	private void onClickFeedback() {
		Intent intent = new Intent(this, SettingsFeedbackActivity.class);
		intent.putExtra("FromHome", false);
		startActivity(intent);
	}

	/** 关于 */
	private void onClickAbout() {
		Intent intent = new Intent(this, SettingsAboutActivity.class);
		startActivity(intent);
	}

	private Stat getStat() {
		return (Stat)getServiceProvider(Stat.class);
	}
}
