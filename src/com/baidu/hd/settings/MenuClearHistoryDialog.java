package com.baidu.hd.settings;

import java.io.File;
import java.text.DecimalFormat;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.browser.visitesite.SearchKeywordManager;
import com.baidu.browser.visitesite.VisiteSiteManager;
import com.baidu.hd.BaseActivity;
import com.baidu.hd.log.Logger;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.task.TaskManager;
import com.baidu.hd.upgrade.RemoteUpgrade;
import com.baidu.hd.upgrade.Upgrade;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.ProgressDialogAsyncTask;
import com.baidu.hd.util.ProgressDialogAsyncTask.ProgressDialogTask;
import com.baidu.hd.R;

public class MenuClearHistoryDialog extends BaseActivity implements OnClickListener {
	
	private Logger logger = new Logger(this.getClass().getSimpleName());
	private final int PORTRAIT_HEIGHT = 380;
	private final int LANDSCAPE_HEIGHT = 230;
	
	/** 浏览历史 */
	private TextView mTextViewBrowserHistory = null;
	/** 搜索历史 */
	private TextView mTextViewSearchHistory = null;
	/** 访问cookies */
	private TextView mTextViewCookies = null;
	/** 软件缓存 */
	private LinearLayout mLayoutBuffer = null;
	/** 最近播放记录 */
	private TextView mTextViewPlay = null;
	/** 清除按钮 */
	private Button mButtonClear = null;
	/** 取消按钮 */
	private Button mButtonCancel = null;
    /** 站点历史记录manager*/
    private VisiteSiteManager mVisiteSiteManager;
    /** 搜索历史记录manager*/
    private SearchKeywordManager mSearchKeywordManager;
    /** 历史播放记录 */
	private PlayListManager playListManager;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_clear_history);
		
		mVisiteSiteManager = (VisiteSiteManager)getServiceProvider(VisiteSiteManager.class);
		mSearchKeywordManager = (SearchKeywordManager)getServiceProvider(SearchKeywordManager.class);
		playListManager = (PlayListManager) getServiceProvider(PlayListManager.class);
		
		mTextViewBrowserHistory = (TextView)findViewById(R.id.menu_clear_history_browser_history);
		mTextViewSearchHistory = (TextView)findViewById(R.id.menu_clear_history_search_history);
		mTextViewCookies = (TextView)findViewById(R.id.menu_clear_history_cookies);
		mLayoutBuffer = (LinearLayout)findViewById(R.id.menu_clear_history_buffer);
		mTextViewPlay = (TextView)findViewById(R.id.menu_clear_history_present_playlist);
		mButtonClear = (Button)findViewById(R.id.menu_clear_his_btn_clear);
		mButtonCancel = (Button)findViewById(R.id.menu_clear_his_btn_cancel);
		
		mTextViewBrowserHistory.setOnClickListener(this);
		mTextViewSearchHistory.setOnClickListener(this);
		mTextViewCookies.setOnClickListener(this);
		mLayoutBuffer.setOnClickListener(this);
		mTextViewPlay.setOnClickListener(this);
		mButtonClear.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);
		
		mTextViewBrowserHistory.setSelected(true);
		mTextViewSearchHistory.setSelected(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		onConfigurationChanged(	getResources().getConfiguration());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		onBackPressed();
		return super.onTouchEvent(event);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		ScrollView scrollView = (ScrollView)findViewById(R.id.scrollview);
		LayoutParams params =   scrollView.getLayoutParams();
		
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			logger.d("portrait");
			params.height = PORTRAIT_HEIGHT;
		}
		else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			logger.d("landscape");
			params.height = LANDSCAPE_HEIGHT;
			scrollView.setVisibility(View.VISIBLE);
		}
		
		scrollView.setLayoutParams(params);
	}

	@Override
	public boolean isServiceCreated() {
		return super.isServiceCreated();
	}

	@Override
	public void onClick(View v) {
		v.setSelected(!v.isSelected());
		switch (v.getId()) {
		/** 清除按钮 */
		case R.id.menu_clear_his_btn_clear:
			clear();
			onBackPressed();
			break;
		/** 取消按钮 */
		case R.id.menu_clear_his_btn_cancel:
			onBackPressed();
			break;
		}
	}
	
	private void clear() {
		
		boolean showtoast = false;
		
		if (mTextViewBrowserHistory.isSelected()) {
			logger.d("clear browser history");
			mVisiteSiteManager.deleteAllHistory();
			showtoast = true;
		}
		if (mTextViewSearchHistory.isSelected()) {
			logger.d("clear search history");
			mSearchKeywordManager.deleteAll();
			showtoast = true;
		}
		if (mTextViewCookies.isSelected()) {
			logger.d("clear cookies");
			CookieManager.getInstance().removeAllCookie();
			showtoast = true;
		}
		if (mLayoutBuffer.isSelected()) {
			logger.d("clear buffer");
			clearBuffer();
			showtoast = true;
		}
		if (mTextViewPlay.isSelected()) {
			logger.d("clear present play list");
			playListManager.removeAllHomeShowAlbum();
			showtoast = true;
		}
		
		if (showtoast) {
			Toast toast = Toast.makeText(MenuClearHistoryDialog.this,
					getText(R.string.settings_set_clearcache_complete),
					Toast.LENGTH_SHORT);
			RemoteUpgrade remoteUpgrade = (RemoteUpgrade) getServiceProvider(RemoteUpgrade.class);
			remoteUpgrade.stopRemoteUpgrade(RemoteUpgrade.Type.AppUpgrade);
			remoteUpgrade
					.stopRemoteUpgrade(RemoteUpgrade.Type.PlayerCoreUpgrade);
			remoteUpgrade
					.stopRemoteUpgrade(RemoteUpgrade.Type.DowndLoadTaskUpgrade);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
		
	}
	
	/** 清除软件缓存 */
	private void clearBuffer() {
		new ProgressDialogAsyncTask<String, String, String>(this, new ProgressDialogTask<String, String, String>()
		{
			long deletedFiles = 0;
			ProgressDialogAsyncTask<String, String, String> asysncTask = null;
			
			TaskManager.clearGarbageEvent clearGarbageEvent = new TaskManager.clearGarbageEvent() {
				@Override
				public boolean isCancel() {
					return false;
				}
				
				@Override
				public void clearSize(long size) {
					deletedFiles += size;
					DecimalFormat df = new DecimalFormat("##0.00 ");
					float size_ = deletedFiles / 1024;
					size_ /= 1024;
					if (null != asysncTask) {
						asysncTask.upDate(df.format(size_));
					}
					SystemClock.sleep(0);
				}
			};
			
			@Override
			public void onPreExecute(ProgressDialogAsyncTask<String, String, String> asysncTask, ProgressDialog dialog)
			{
				this.asysncTask = asysncTask;
				dialog.setMessage(MenuClearHistoryDialog.this.getText(R.string.settings_set_clearcache_title));
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
				dialog.setMessage(getText(R.string.settings_set_clearcache_cleared) + values[0] + "MB");
			}

			@Override
			public void onPostExecute(String result)
			{
			};
		}).execute();
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
}
