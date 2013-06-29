package com.baidu.hd.sniffer.smallsniffer;


public class D8Sniffer extends SimpleSniffer{

	@Override
	public boolean Snifferable(String pageUrl) throws Exception {
		return pageUrl!=null&&pageUrl.contains("d8.cm/playlist/");
	}


}
