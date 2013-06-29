package com.baidu.hd.sniffer.smallsniffer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import com.baidu.hd.sniffer.smallsniffer.EscapeUnescape;
import com.baidu.hd.sniffer.smallsniffer.ISniffer;

public class DageSniffer implements ISniffer{
	final String baseUrl="http://www.dage.cc";
	@Override
	public boolean Snifferable(String pageUrl) throws Exception {
		return pageUrl!=null&&pageUrl.contains("dage.cc/play/");
	}

	@Override
	public String getBdhd(String pageUrl, String html) throws Exception {
		Elements elst=Jsoup.parse(html).select("script[src*=/playdata/]");
		if(elst==null||elst.size()==0){
			return "";
		} 
		String jsSite=baseUrl+elst.get(0).attr("src");
		
		HttpURLConnection urlConnection=(HttpURLConnection) new URL(jsSite).openConnection();
		InputStream in = urlConnection.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(in));
		StringBuffer temp = new StringBuffer();
		String line = bufferedReader.readLine();
		while (line != null) {
			temp.append(line).append("\r\n");
			line = bufferedReader.readLine();
		}
		bufferedReader.close();
		String jsRes=temp.substring(temp.indexOf("('")+2, temp.lastIndexOf("'")).trim();
		jsRes=EscapeUnescape.unescape(jsRes);
		String[] arr=jsRes.split("[$][$]")[1].split("[$]bdhd[#]");
		int index=Integer.parseInt(pageUrl.substring(pageUrl.lastIndexOf("-")+1, pageUrl.lastIndexOf(".")));
		String bdhd=arr[index].contains("$")?arr[index].split("[$]")[1]:arr[index];
		return bdhd;
	}

	@Override
	public ArrayList<String> getBdhdList() {
		// TODO Auto-generated method stub
		return null;
	}

}
