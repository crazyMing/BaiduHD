package com.baidu.hd.sniffer.smallsniffer;

import java.util.ArrayList;

import com.baidu.hd.sniffer.smallsniffer.BDHD;
import com.baidu.hd.sniffer.smallsniffer.EscapeUnescape;
import com.baidu.hd.sniffer.smallsniffer.Geter;
import com.baidu.hd.sniffer.smallsniffer.ISniffer;

public class YiyiSniffer implements ISniffer{

	@Override
	public boolean Snifferable(String pageUrl) throws Exception {
		return pageUrl!=null&&pageUrl.contains("yiyi.cc/film");
	}

	@Override
	public String getBdhd(String pageUrl, String html) throws Exception {
		int index=Integer.parseInt(pageUrl.substring(pageUrl.lastIndexOf("_")+1, pageUrl.lastIndexOf(".")))-1;
		String jsUrl=pageUrl.substring(0,pageUrl.indexOf("_"))+".js";
		String js=Geter.get(jsUrl);
		ArrayList<String> arr=BDHD.searchBDHD(EscapeUnescape.unescape(js));
		return arr.get(index);
	}

	@Override
	public ArrayList<String> getBdhdList() {
		// TODO Auto-generated method stub
		return null;
	}

}
