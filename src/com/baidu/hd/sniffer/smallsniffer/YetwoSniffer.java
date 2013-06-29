package com.baidu.hd.sniffer.smallsniffer;

import java.util.ArrayList;

import com.baidu.hd.sniffer.smallsniffer.BDHD;
import com.baidu.hd.sniffer.smallsniffer.ISniffer;

public class YetwoSniffer implements ISniffer{

	@Override
	public boolean Snifferable(String pageUrl) throws Exception {
		return pageUrl!=null&&pageUrl.contains("yetwo.com/");
	}

	@Override
	public String getBdhd(String pageUrl, String html) throws Exception {
		int index=Integer.parseInt(pageUrl.substring(pageUrl.lastIndexOf("-")+1, pageUrl.lastIndexOf(".")))-1;
		return BDHD.searchBDHD(html).get(index);
	}

	@Override
	public ArrayList<String> getBdhdList() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
