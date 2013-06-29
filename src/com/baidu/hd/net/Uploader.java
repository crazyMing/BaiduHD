package com.baidu.hd.net;

import com.baidu.hd.service.ServiceProvider;

public interface Uploader extends ServiceProvider {
	
	public void feedBack(String msg, String contact);
}
