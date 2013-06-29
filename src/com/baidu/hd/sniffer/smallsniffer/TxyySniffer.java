package com.baidu.hd.sniffer.smallsniffer;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import com.baidu.hd.sniffer.smallsniffer.BDHD;
import com.baidu.hd.sniffer.smallsniffer.EscapeUnescape;
import com.baidu.hd.sniffer.smallsniffer.Geter;
import com.baidu.hd.sniffer.smallsniffer.ISniffer;

public class TxyySniffer implements ISniffer{
	final String baseUrl="http://www.txyy.tv";
	@Override
	public boolean Snifferable(String pageUrl) throws Exception {
		return pageUrl!=null&&pageUrl.contains("txyy.tv/");
	}

	@Override
	public String getBdhd(String pageUrl, String html) throws Exception {
		Elements elst=Jsoup.parse(html).select("script[src*=/playdata/]");
		if(elst==null||elst.size()==0){
			return "";
		} 
		String jsSite=baseUrl+elst.get(0).attr("src");
		String js=Geter.get(jsSite);
		ArrayList<String> arr=BDHD.searchBDHD(EscapeUnescape.unescape(js));
		int index=Integer.parseInt(pageUrl.substring(pageUrl.lastIndexOf("-")+1, pageUrl.lastIndexOf(".")));
		return arr.get(index);
	}

	@Override
	public ArrayList<String> getBdhdList() {
		// TODO Auto-generated method stub
		return null;
	}

}
