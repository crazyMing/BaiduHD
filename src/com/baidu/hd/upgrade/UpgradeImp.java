package com.baidu.hd.upgrade;

import android.content.Context;

import com.baidu.hd.log.Logger;
import com.baidu.hd.service.ServiceConsumer;
import com.baidu.hd.service.ServiceFactory;

public class UpgradeImp implements Upgrade, ServiceConsumer {
	private Logger logger = new Logger("UpgradeImp");

	private Context mContext = null;
	private ServiceFactory mServiceFactory = null;
	
	private AppUpgrade mAppUpgrade = new AppUpgrade();
	private PlayerCoreUpgrade mPlayerCoreUpgrade = new PlayerCoreUpgrade();
	
	@Override
	public void setContext(Context ctx) {
		logger.d("setContext");
		
		this.mContext = ctx;
	}

	@Override
	public void onCreate() {
		logger.d("onCreate");
		
		this.mAppUpgrade.create(this.mContext, this.mServiceFactory);
		this.mPlayerCoreUpgrade.create(this.mContext, this.mServiceFactory);
	}

	@Override
	public void onDestory() {
		logger.d("onDestroy");
		
		this.mAppUpgrade.destroy();
		this.mPlayerCoreUpgrade.destroy();
	}

	@Override
	public void onSave() {
	}

	@Override
	public void setServiceFactory(ServiceFactory factory) {
		this.mServiceFactory = factory;
	}

	@Override
	public void manualAppCheck(boolean isForce, boolean isSilence) {
		this.mAppUpgrade.check(isForce, isSilence);
	}

	@Override
	public int upgradeAppStatus() {
		return this.mAppUpgrade.getStatus();
	}
	
	@Override
	public void cancelAppCheck() {
		// TODO Auto-generated method stub
		this.mAppUpgrade.cancel();
	}

	@Override
	public void manualPlayerCoreCheck(boolean isForce, boolean isSilence) {
		this.mPlayerCoreUpgrade.check(isForce, isSilence);
	}

	@Override
	public int upgradePlayerCoreStatus() {
		return this.mPlayerCoreUpgrade.getStatus();
	}

	@Override
	public void cancelPlayerCoreCheck() {
		// TODO Auto-generated method stub
		this.mPlayerCoreUpgrade.cancel();
	}
}
