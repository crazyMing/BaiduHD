package com.baidu.hd.sniffer.smallsniffer;

import java.util.ArrayList;


public interface ISniffer {
	/**
	 * url是否可嗅探
	 * @param pageUrl 页面url
	 * @return 是否可嗅探
	 * @throws Exception
	 */
	boolean Snifferable(String pageUrl) throws Exception;
	
	/**
	 * 得到bdhd地址
	 * @param pageUrl 页面url 必须
	 * @param html 页面html 必须
	 * @return bdhd://文件大小|特征|文件名
	 * @throws Exception
	 */
	String getBdhd(String pageUrl,String html) throws Exception;
	
	ArrayList<String> getBdhdList();
}
