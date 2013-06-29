package com.baidu.hd.sniffer.smallsniffer;

import java.util.ArrayList;

import com.baidu.hd.sniffer.smallsniffer.BDHD;
import com.baidu.hd.sniffer.smallsniffer.ISniffer;
/**
 * 单个url在html中
 * @author juqiang
 *
 */
public class SimpleSniffer implements ISniffer {

	@Override
	public boolean Snifferable(String pageUrl) throws Exception {
		return true;
	}

	@Override
	public String getBdhd(String pageUrl, String html) throws Exception {
		try {
			return BDHD.searchBDHD(html).get(0);
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	@Override
	public ArrayList<String> getBdhdList() {
		// TODO Auto-generated method stub
		return null;
	}

}
