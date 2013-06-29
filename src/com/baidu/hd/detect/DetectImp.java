package com.baidu.hd.detect;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.baidu.hd.service.ServiceConsumer;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId;

public class DetectImp implements Detect, ServiceConsumer {
	
	private static final int NetFilter = 1;
	private static final int SDCardFilter = 2;
	
	private Context mContext = null;
	private ServiceFactory mServiceFactory = null;
	
	private NetworkHandler mNetworkHandler = new NetworkHandler();
	private SDCardHandler mSDCardHandler = new SDCardHandler();

	/**
	 * 防止在onCreate中调用时，currentActivity还未设置，以前一个Activity为父窗口的情况
	 */
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			
			if(msg.what == NetFilter) {
				Package pack = (Package)msg.obj;
				mNetworkHandler.filter(pack.callback);
			}
			if(msg.what == SDCardFilter) {
				mSDCardHandler.prompt();
			}
		}
	};

	@Override
	public void setContext(Context context) {
		this.mContext = context;
	}

	@Override
	public void onCreate() {
		this.mNetworkHandler.create(this.mContext);
		this.mSDCardHandler.create(this.mContext);
		
		Stat stat = (Stat) this.mServiceFactory.getServiceProvider(Stat.class);
		stat.setHaveSDCard(this.mSDCardHandler.canUse());
		stat.incEventValue(StatId.Startup.Name, StatId.Startup.HaveSDCard, mSDCardHandler.canUse() ? 1 : 0);
		stat.incEventValue(StatId.Startup.Name, StatId.Startup.HaveNoSDCard, mSDCardHandler.canUse() ? 0 : 1);
		stat.incEventValue(StatId.Startup.Name, StatId.Startup.IsWife, mNetworkHandler.isAvailableWithWifi() ? 1 : 0);
		stat.incEventValue(StatId.Startup.Name, StatId.Startup.IsNotWife, mNetworkHandler.isAvailableWithWifi() ? 0 : 1);
	}

	@Override
	public void onDestory() {
		this.mNetworkHandler.destroy();
		this.mSDCardHandler.destroy();
	}

	@Override
	public void onSave() {
	}

	@Override
	public void setServiceFactory(ServiceFactory factory) {
		this.mServiceFactory = factory;
	}

	@Override
	public NetUsable getNetUsable() {
		return mNetworkHandler.getUsable();
	}

	@Override
	public boolean isNetAvailabe() {
		return this.mNetworkHandler.isAvailable();
	}
	
	@Override
	public boolean isNetAvailabeWithWifi() {
		return this.mNetworkHandler.isAvailableWithWifi();
	}
	
	@Override
	public boolean isSDCardAvailable() {
		return this.mSDCardHandler.canUse();
	}
	
	@Override
	public void filterByNet(FilterCallback callback) {
		mHandler.sendMessage(mHandler.obtainMessage(NetFilter, new Package(callback)));
	}

	@Override
	public void SDCardPrompt() {
		mHandler.sendMessage(mHandler.obtainMessage(SDCardFilter));
	}

	@Override
	public boolean isNetPrompt() 
	{
		return mNetworkHandler.isPrompt();
	}
	
	@Override
	public void setNetPrompt(boolean value) 
	{
		mNetworkHandler.setPrompt(value);
	}

	private class Package {
		public FilterCallback callback = null;
		public Package(FilterCallback callback) {
			this.callback = callback;
		}
	}
}
