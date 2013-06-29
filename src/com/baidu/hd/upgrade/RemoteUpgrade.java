package com.baidu.hd.upgrade;

import com.baidu.hd.service.ServiceProvider;

public interface RemoteUpgrade extends ServiceProvider
{
	public class Type
	{
		public static final int Unknown = 0;
		public static final int AppUpgrade = 1;
		public static final int PlayerCoreUpgrade = 2;
		public static final int DowndLoadTaskUpgrade = 3;
	}
	
	void startRemoteUpgrade(int type, String title);
	void stopRemoteUpgrade(int type);
	void updateRemoteUpgrade(int type, String update);
}
