package com.baidu.hd.detect;

import com.baidu.hd.event.EventArgs;

public class NetUsableChangedEventArgs extends EventArgs {

	private NetUsable mOld = NetUsable.eDisable;
	private NetUsable mNew = NetUsable.eDisable;
	
	NetUsableChangedEventArgs(NetUsable oldValue, NetUsable newValue) {
		this.mOld = oldValue;
		this.mNew = newValue;
	}
	public NetUsable getOld() {
		return mOld;
	}
	public NetUsable getNew() {
		return mNew;
	}
}
