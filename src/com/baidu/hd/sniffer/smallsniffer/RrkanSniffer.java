package com.baidu.hd.sniffer.smallsniffer;


public class RrkanSniffer extends SimpleSniffer{

	@Override
	public boolean Snifferable(String pageUrl) throws Exception {
		return pageUrl!=null&&pageUrl.contains("51rrkan.com/");
	}

	
}
