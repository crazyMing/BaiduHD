package com.baidu.hd;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.baidu.hd.log.Logger;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.service.ServiceFactoryImpl;
import com.baidu.hd.R;

public class ServiceContainer {
	
	public interface Callback
	{
		void onServiceCreated();
	}
	
	private Logger logger = new Logger("ServiceContainer");
	
	private BaseActivity mHost = null;
	private BaiduHD mApp = null;

	/** 服务工厂 */
	private ServiceFactory mServiceFactory = null;
	
	/** 服务创建 */
	private boolean isCreated = false;
	private boolean isCreating = false;
	
	/** 
	 * 在服务创建过程中创建的activity
	 * 因为TabActivity会自动创建子BaseActivity
	 */
	private List<BaseActivity> mCreatedActivityWhenCreatingService = new ArrayList<BaseActivity>();
	private List<Callback> mCallbackWhenCreatingService = new ArrayList<Callback>();
	
	public void construct() {
		mServiceFactory = new ServiceFactoryImpl();
	}
	
	public void create(BaseActivity host) {
		if(isCreated) {
			logger.i("service created, return"); 
		} else {
			if(isCreating) {
				// 创建过程中切换了页面导致
				logger.i("service creating, return");
			} else {
				logger.i("null service, will create");
				mHost = host;
				mApp = mHost.getPlayerApp();
				new ServiceCreatorAsyncTask().execute();
			}
		}
	}
	
	public void createDirect(BaseActivity host) {
		if(isCreated) {
			logger.i("service created, return");
			return;
		}
		logger.i("create service direct");
		isCreating = true;
		mServiceFactory.setContext(host.getPlayerApp());
		mServiceFactory.create();
		isCreating = false;
		isCreated = true;
		notifyCreatedActivity();
	}
	
	public void destroy() {
		if(isCreated) {
			isCreated = false;
			mServiceFactory.destory();
			mServiceFactory.setContext(null);
			logger.i("will destroy service");
		} else {
			logger.i("service not created, return");
		}
	}
	
	public void save() {
		mServiceFactory.save();
	}
	
	public ServiceFactory getFactory() {
		return mServiceFactory;
	}

	public boolean isCreated() {
		return isCreated;
	}
	
	public boolean isCreating() {
		return isCreating;
	}

	public void addCreatedActivity(BaseActivity value) {
		mCreatedActivityWhenCreatingService.add(value);
	}
	
	public void addCallback(Callback value) {
		mCallbackWhenCreatingService.add(value);
	}
	
	private void notifyCreatedActivity() {
		for(BaseActivity activity : mCreatedActivityWhenCreatingService) {
			activity.onServiceCreated();
		}
		for(Callback callBack : mCallbackWhenCreatingService) {
			callBack.onServiceCreated();
		}
		mCreatedActivityWhenCreatingService.clear();
		mCallbackWhenCreatingService.clear();
	}

	private class ServiceCreatorAsyncTask extends AsyncTask<Void, Void, Void> {
		
		ProgressDialog mDialog = null;

		@Override
		protected Void doInBackground(Void... params) {
			mServiceFactory.setContext(mApp);
			mServiceFactory.create();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mDialog != null) {
				mDialog.dismiss();
				mDialog = null;
			}
			isCreating = false;
			isCreated = true;
			logger.i("service created in async task");
			notifyCreatedActivity();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			logger.i("create service in async task");
			isCreating = true;
			if(mHost.showProgressDialogWhenCreatingService()) {
				mDialog = new ProgressDialog(mHost);
				mDialog.setMessage(mHost.getText(R.string.service_creating));
				mDialog.setIndeterminate(true);
				mDialog.setCanceledOnTouchOutside(false);
				mDialog.setCancelable(false);
				mDialog.show();
			}
		}
		
	}
}
