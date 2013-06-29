package com.baidu.hd.sniffer.smallsniffer;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import com.baidu.hd.sniffer.smallsniffer.BDHD;
import com.baidu.hd.sniffer.smallsniffer.EscapeUnescape;
import com.baidu.hd.sniffer.smallsniffer.Geter;
import com.baidu.hd.sniffer.smallsniffer.ISniffer;

public class HlgzsSinffer implements ISniffer {
	String baseUrl = "http://www.hlgzs.com";

	@Override
	public boolean Snifferable(String pageUrl) throws Exception {
		return pageUrl != null && pageUrl.contains("hlgzs.com/tv/");
	}

	@Override
	public String getBdhd(String pageUrl, String html) throws Exception {
		int index = Integer.parseInt(pageUrl.substring(
				pageUrl.lastIndexOf("-") + 1, pageUrl.lastIndexOf(".")));
		Elements elst = Jsoup.parse(html).select("script[src*=/playdata/]");
		if (elst == null || elst.size() == 0) {
			return "";
		}
		String jsSite = baseUrl + elst.get(0).attr("src");
		return BDHD.searchBDHD(EscapeUnescape.unescape(Geter.get(jsSite))).get(
				index);
	}

	@Override
	public ArrayList<String> getBdhdList() {
		// TODO Auto-generated method stub
		return null;
	}

}
