package com.baidu.hd.sniffer.smallsniffer;


public class ShuangtvSniffer extends SimpleSniffer{

	@Override
	public boolean Snifferable(String pageUrl) throws Exception {
		return pageUrl!=null&&pageUrl.contains("shuangtv.net/");
	}


}
