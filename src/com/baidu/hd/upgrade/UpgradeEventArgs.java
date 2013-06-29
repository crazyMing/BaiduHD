package com.baidu.hd.upgrade;

import com.baidu.hd.event.EventArgs;

public class UpgradeEventArgs extends EventArgs {
	
	private boolean isSuccess = false;
	private boolean haveNew = false;
	
	public boolean isSuccess() {
		return isSuccess;
	}
	public boolean isHaveNew() {
		return haveNew;
	}

	public UpgradeEventArgs(boolean isSuccess, boolean haveNew) {
		this.isSuccess = isSuccess;
		this.haveNew = haveNew;
	}
}
