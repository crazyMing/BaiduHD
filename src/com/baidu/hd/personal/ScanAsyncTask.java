package com.baidu.hd.personal;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.Display;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Toast;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.log.Logger;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.util.ToastUtil;
import com.baidu.hd.R;

/**
 * 通过日志分析onCancelled 与  doInBackgroud 函数异步进行。
 * 因此，弃用了cancel机制，全部在onPostExecute中退出。
 * 用quit进行退出设置，取代cancel
 * @author sunjianshun
 */
public class ScanAsyncTask extends AsyncTask<List<String>, Integer, Integer> implements SDCardUtil.ScanEvent
{
	private final static String TAG = "ScanAsyncTask";	
	private Logger logger = new Logger(TAG); 
	
	/** 结束时操作回调 */
	public interface Callback {
		void onComplete(int nResult);
		boolean isNeedRefresh();
	}	
	private Callback mCallback = null;
	
	/** 显示对话框时要用到 */
	private Context mDlgParent = null;
	private Context mContext = null;
	
	/** 管理视频列表 */
	private PlayListManager mPlayListManager = null;

	/** 扫描对话框 */
	private ProgressDialog mProgressDialog = null;

	/** 真正插入到Local中的文件数目 */
	private int mAddedCount = 0;
	
	private List<String> mNameList = new ArrayList<String>();
	
	/** 是否需要刷新，即冲掉先前记录 */
	private boolean mIsNeedRefresh = false;
	
	/** 退出 */
	private boolean mQuit = false;
	
	/** 构造函数 */
	public ScanAsyncTask(Context context, Context dlgParent, Callback callback) {
		mCallback = callback;
		mContext = context;
		mDlgParent = dlgParent;
		mIsNeedRefresh = mCallback.isNeedRefresh();
	}	

	@Override
	protected Integer doInBackground(List<String>... params) {
		
		if (params == null || params.length == 0 || params[0].size() == 0) {
			mNameList = SDCardUtil.getInstance().getAllFiles(this, null);
		}
		else {
			List<String> pathList = params[0];
			mNameList = SDCardUtil.getInstance().getAllFiles(this, pathList);
		}		
		
		logger.d("doInBackground nameList size=" + ((Integer)mNameList.size()).toString());
		
		return mNameList.size();
	}

	@Override
	protected void onPreExecute() {
		this.mPlayListManager = (PlayListManager)((BaseActivity)mContext).getServiceProvider(PlayListManager.class);		

		if (mContext instanceof Activity) {
			Activity act = (Activity)mContext;
			mProgressDialog = new ProgressDialog(mDlgParent);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(true);
			this.setDlgMessage();
			mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, mContext.getText(R.string.dialog_button_cancel),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					quit(true);
				}
			});
			mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					quit(true);
				}
			});
			
			mProgressDialog.show();
			mProgressDialog.getWindow().setGravity(Gravity.CENTER);
			WindowManager windowManager = (act.getWindowManager());
	        Display display = windowManager.getDefaultDisplay(); // 获取屏幕宽、高用
			WindowManager.LayoutParams layoutParams = mProgressDialog.getWindow().getAttributes();
			int width = display.getWidth();
			int height = display.getHeight();
			layoutParams.width = (int)((Math.min(width, height)) * 3 / 4.0);
			mProgressDialog.getWindow().setAttributes(layoutParams);
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		logger.d("onPostExecute nameList size=" + ((Integer)mNameList.size()).toString());
	
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}

		/**下面两段关键代码修改：
		 * updateList();
		 * getVideoInfo();
		 * 在LocalActivity中在onComplete中更新列表
		 * 在FileBrowserActivity中无需调用，因为执行完之后，界面跳转到LocalActivity界面，
		 * 此时，LocalActivity会调用onResume函数，在该函数中更新列表
		 */
		
		refresh();
		mCallback.onComplete(mAddedCount);
		toastResultMessage();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		this.setDlgMessage();
	}

	/**追加本地视频，添加到LocalManager并写入数据库
	 * 找到一个就添加一个
	 */
	@Override
	public void found(String path) {
		// 刷新，将会冲掉先前记录，因此无论是否真正添加都要计数
		if (mIsNeedRefresh) {
			this.mPlayListManager.addLocal(path); 
			publishProgress(++mAddedCount);
		}
		// 导入，追加到末尾，只有真正添加了才计数
		else if (this.mPlayListManager.addLocal(path)) {
				publishProgress(++mAddedCount);
		}
	}

	@Override
	public boolean isCancel() {
		return mQuit;
	}

	private void setDlgMessage() {
		if (mProgressDialog == null) return;
		
		if (mIsNeedRefresh) {
			String format = mContext.getString(R.string.found_number_video);
			mProgressDialog.setMessage(String.format(format, mAddedCount));
		}
		else {
			String format = mContext.getString(R.string.import_file_succeed);
			mProgressDialog.setMessage(String.format(format, mAddedCount));
		}
	}
	
	private void toastResultMessage() {
		String message = "";
		if (mAddedCount == 0) {
			message = mContext.getString(R.string.import_file_failed);
		}
		else {
			String format = mContext.getString(R.string.import_file_succeed);
			message = String.format(format, mAddedCount);
		}
		ToastUtil.showMessage(mContext, message, Toast.LENGTH_SHORT);
	}
	
	public int getFoundCount() {
		return mAddedCount;
	}
	
	private void refresh() {
		if (mIsNeedRefresh) {
			// 刷新本地视频列表，包括LocalManager与数据当中的，两者同时更新
			// 刷新操作，只保留在nameList中存在的值，会冲掉先前记录
			this.mPlayListManager.refreshLocal(mNameList);
		}
	}
	
	public void quit(boolean quit) {
		mQuit = quit;
	}
}