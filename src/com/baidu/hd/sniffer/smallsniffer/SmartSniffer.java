package com.baidu.hd.sniffer.smallsniffer;

import java.util.ArrayList;

public class SmartSniffer implements ISniffer {

	@Override
	public boolean Snifferable(String pageUrl) throws Exception {
		return true;
	}

	@SuppressWarnings("null")
	@Override
	public String getBdhd(String pageUrl, String html) throws Exception {
		// search direct
		ArrayList<String> arr = BDHD.searchBDHD(EscapeUnescape.unescape(html));
		if (arr != null && arr.size() > 0) {
			outArr(arr);
			return arr.size()==1?arr.get(0):"";
		}
//		// secarch special js
//		Elements elst = Jsoup.parse(html).select("script[src*=/playdata/]");
//		if (elst != null && elst.size() > 0) {
//			String jsSite = getBaseUrl(pageUrl) + elst.get(0).attr("src");
//			arr = BDHD.searchBDHD(EscapeUnescape.unescape(Geter.get(jsSite)));
//			if (arr != null && arr.size() > 0) {
//				outArr(arr);
//				return arr.size()==1?arr.get(0):"";
//			}
//		}
//		// search all js
//		elst = Jsoup.parse(html).select("script");
//		if (elst != null || elst.size() != 0) {
//			for (int i = 0; i < elst.size(); i++) {
//				try{
//				String jsSite = getBaseUrl(pageUrl) + elst.get(i).attr("src");
//				arr = BDHD
//						.searchBDHD(EscapeUnescape.unescape(Geter.get(jsSite)));
//				if (arr != null && arr.size() > 0) {
//					outArr(arr);
//					return arr.size()==1?arr.get(0):"";
//				}}catch (Exception e) {
//					continue;
//				}
//			}
//		}

		return "";
	}

	private ArrayList<String> mBList;

	public ArrayList<String> getBdhdList(String pageUrl, String html)
			throws Exception {
		if (mBList == null) {
			getBdhd(pageUrl, html);
		}
		return mBList;
	}

	public ArrayList<String> getBdhdList() {
		return mBList;
	}

	private void outArr(ArrayList<String> arr) {
		mBList = arr;
		//TODO to be delete
		for (String bdhd : arr) {
			System.out.println(bdhd);
		}
	}

	public String getBaseUrl(String pageUrl) {
		int end = pageUrl.indexOf("/", pageUrl.indexOf("://") + 4);
		return pageUrl.substring(0, end);
	}
}
