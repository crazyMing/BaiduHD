package com.baidu.hd.net;

import com.baidu.hd.event.EventArgs;

public class UploaderEventArgs extends EventArgs 
{
	private boolean  isSuccess = false;
	private String message = null;

	public UploaderEventArgs(boolean isSuccess, String message) 
	{
		this.isSuccess = isSuccess;
		this.message = message;
	}
	
	public boolean isSuccess() {
		return isSuccess;
	}
	
	public String getMessage() {
		return message;
	}
};
