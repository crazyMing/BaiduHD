package com.baidu.hd.upgrade;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class LocalInfo {
	
	private String mVersion = "";

	public LocalInfo(Context context) {
		
		PackageManager manager = context.getPackageManager();
		try {
			PackageInfo pInfo = manager.getPackageInfo(
					context.getPackageName(),
					PackageManager.GET_CONFIGURATIONS);
			this.mVersion = pInfo.versionName;

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public String getAppVerstion()
	{
		return this.mVersion;
	}
	
	public int compare(String value) {
		
		if(this.mVersion.equalsIgnoreCase(value)) {
			return 0;
		}
		
		String[] self = this.mVersion.split("\\.");
		String[] other = value.split("\\.");
		
		if(self.length != other.length) {
			return 0;
		}
		
		for(int i = 0; i < self.length; ++i) {
			
			int selfDigit = Integer.parseInt(self[i].trim());
			int otherDigit = Integer.parseInt(other[i].trim()); 
			if(selfDigit < otherDigit) {
				return -1;
			}
			if(selfDigit > otherDigit) {
				return 1;
			}
		}
		return 0;
	}
}
