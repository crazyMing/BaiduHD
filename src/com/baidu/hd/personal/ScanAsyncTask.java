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
 * ͨ����־����onCancelled ��  doInBackgroud �����첽���С�
 * ��ˣ�������cancel���ƣ�ȫ����onPostExecute���˳���
 * ��quit�����˳����ã�ȡ��cancel
 * @author sunjianshun
 */
public class ScanAsyncTask extends AsyncTask<List<String>, Integer, Integer> implements SDCardUtil.ScanEvent
{
	private final static String TAG = "ScanAsyncTask";	
	private Logger logger = new Logger(TAG); 
	
	/** ����ʱ�����ص� */
	public interface Callback {
		void onComplete(int nResult);
		boolean isNeedRefresh();
	}	
	private Callback mCallback = null;
	
	/** ��ʾ�Ի���ʱҪ�õ� */
	private Context mDlgParent = null;
	private Context mContext = null;
	
	/** ������Ƶ�б� */
	private PlayListManager mPlayListManager = null;

	/** ɨ��Ի��� */
	private ProgressDialog mProgressDialog = null;

	/** �������뵽Local�е��ļ���Ŀ */
	private int mAddedCount = 0;
	
	private List<String> mNameList = new ArrayList<String>();
	
	/** �Ƿ���Ҫˢ�£��������ǰ��¼ */
	private boolean mIsNeedRefresh = false;
	
	/** �˳� */
	private boolean mQuit = false;
	
	/** ���캯�� */
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
	        Display display = windowManager.getDefaultDisplay(); // ��ȡ��Ļ������
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

		/**�������ιؼ������޸ģ�
		 * updateList();
		 * getVideoInfo();
		 * ��LocalActivity����onComplete�и����б�
		 * ��FileBrowserActivity��������ã���Ϊִ����֮�󣬽�����ת��LocalActivity���棬
		 * ��ʱ��LocalActivity�����onResume�������ڸú����и����б�
		 */
		
		refresh();
		mCallback.onComplete(mAddedCount);
		toastResultMessage();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		this.setDlgMessage();
	}

	/**׷�ӱ�����Ƶ����ӵ�LocalManager��д�����ݿ�
	 * �ҵ�һ�������һ��
	 */
	@Override
	public void found(String path) {
		// ˢ�£���������ǰ��¼����������Ƿ�������Ӷ�Ҫ����
		if (mIsNeedRefresh) {
			this.mPlayListManager.addLocal(path); 
			publishProgress(++mAddedCount);
		}
		// ���룬׷�ӵ�ĩβ��ֻ����������˲ż���
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
			// ˢ�±�����Ƶ�б�����LocalManager�����ݵ��еģ�����ͬʱ����
			// ˢ�²�����ֻ������nameList�д��ڵ�ֵ��������ǰ��¼
			this.mPlayListManager.refreshLocal(mNameList);
		}
	}
	
	public void quit(boolean quit) {
		mQuit = quit;
	}
}