package com.baidu.hd.sniffer.smallsniffer;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class BaiySniffer implements ISniffer {
	final String baseUrl = "http://www.baiy.net";

	@Override
	public boolean Snifferable(String pageUrl) throws Exception {
		return pageUrl != null && pageUrl.contains("baiy.net/");
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
		ArrayList<String> arr = BDHD.searchBDHD(EscapeUnescape.unescape(Geter
				.get(jsSite)));
		return arr.get(index);
	}

	@Override
	public ArrayList<String> getBdhdList() {
		// TODO Auto-generated method stub
		return null;
	}

}
