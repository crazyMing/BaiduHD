package com.baidu.hd.sniffer.smallsniffer;

import java.util.ArrayList;


public interface ISniffer {
	/**
	 * url�Ƿ����̽
	 * @param pageUrl ҳ��url
	 * @return �Ƿ����̽
	 * @throws Exception
	 */
	boolean Snifferable(String pageUrl) throws Exception;
	
	/**
	 * �õ�bdhd��ַ
	 * @param pageUrl ҳ��url ����
	 * @param html ҳ��html ����
	 * @return bdhd://�ļ���С|����|�ļ���
	 * @throws Exception
	 */
	String getBdhd(String pageUrl,String html) throws Exception;
	
	ArrayList<String> getBdhdList();
}
