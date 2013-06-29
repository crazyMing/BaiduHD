package com.baidu.hd.sniffer.smallsniffer;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BDHD {
	// test code
	// bdhd://83149489|005893f65dfb0cc2441a3bcc66ae6eea|屌丝男士第5集.rmvb
	// static String str =
	// "var VideoInfoList=unescape('百度HD$$第01集$bdhd://215196032|9AA3552D3F7DB0CFE24EBEEBBD767844|[dage.cc]麻辣女兵01.HDTV.mp4$bdhd#第14集$bdhd://328924462|813882DDE7125A7FAB85F4953CFD1BB4|[dage.cc]麻辣女兵14.HDTV.rmvb$bdhd')";
	// public static void main(String[] args) {
	// searchBDHD("BdPlayer['url'] = 'bdhd://131967416|5532B150F637A1811DAF7F499771161B|[www.d8.cm]因为是你所以喜欢055.rmvb';//当前播放任务播放地址");
	// }!@#$%\\^&\\+'\"
	final static String p = "bdhd[:][/][/][0-9]+[|][0-9a-zA-Z]+[|].+?[\\{\\}<>!@#$%\\^&\\+'\"\\s]";

	public static ArrayList<String> searchBDHD(String js) {
		ArrayList<String> arr = new ArrayList<String>();
		Pattern pattern = Pattern.compile(p, Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher(js);
		while (m.find()) {
			String res = m.group().trim();
			char last = res.charAt(res.length() - 1);
			if (isSpecialChar(last)) {
				res = res.substring(0, res.length() - 1);
			}
			arr.add(res);
			// System.out.println(res);
		}
		return arr;
	}

	// 48-57;65-90;97-122
	private static boolean isSpecialChar(char c) {
		return (c < 48 || (c > 57 && c < 65) || (c > 90 && c < 97) || c > 122);
	}

}
