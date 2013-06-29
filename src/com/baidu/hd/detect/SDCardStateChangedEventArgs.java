package com.baidu.hd.detect;

import com.baidu.hd.event.EventArgs;

public class SDCardStateChangedEventArgs extends EventArgs {

	private boolean canUse = false;
	public SDCardStateChangedEventArgs(boolean canUse) {
		this.canUse = canUse;
	}
	public boolean isCanUse() {
		return canUse;
	}
}
