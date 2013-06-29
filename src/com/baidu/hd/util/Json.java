package com.baidu.hd.util;

public class Json {

	public static String filterJsonp(String message) {
		int startIndex = message.indexOf("(");
		if(startIndex == -1) {
			return message;
		}
		int endIndex = message.lastIndexOf(")");
		if(endIndex == -1) {
			return message;
		}
		return message.substring(startIndex + 1, endIndex);
	}
}
