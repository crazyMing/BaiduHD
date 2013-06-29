package com.baidu.hd.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class UrlUtil 
{
	public static boolean isUrl(String value) 
	{
		if (!StringUtil.isEmpty(value))
		{
			return value.matches("(((http|ftp|https|file)://)?([\\w\\-]+\\.)+[\\w\\-]+(/[\\w\\u4e00-\\u9fa5\\-\\./?\\@\\%\\!\\&=\\+\\~\\:\\#\\;\\,]*)?)");
//			((http[s]{0,1}|ftp)://)?[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?
//			return value.matches("^(http://)?[a-zA-Z0-9-]+(\\.[a-zA-z0-9-]+)+/?$");
//			try 
//			{
//				new URL(value);
//				return true;
//			} 
//			catch (MalformedURLException e) 
//			{
//				return false;
//			}
		}
		else 
			return false;
		
	}
	
	public static String encode(String url) {
		return URLEncoder.encode(url);
	}
	
	public static String decode(String url) {
		return URLDecoder.decode(url);
	}
	
	public static String getUrl(String host, String method) {
		
		try {
			URL u = new URL(host);
			return new URL(u.getProtocol(), u.getHost(), method).toString();
		} catch (MalformedURLException e) {
			return "";
		} catch(Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static String getHost(String url) {
		
		try {
			return new URL(url).getHost();
		} catch(MalformedURLException e) {
			return "";
		} catch(Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static String getDomain(String url) {
		
		String host = getHost(url);
		int index = host.lastIndexOf('.');
		if(index == -1) {
			return "";
		}
		String str = host.substring(0, index);
		index = str.lastIndexOf('.');
		if(index == -1) {
			return "";
		}
		str = host.substring(index + 1);
		if(str.endsWith("/")) {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}
	
	public static Map<String, String> getQuery(String url) {
		
		String query = "";
		try {
			query = new URL(url).getQuery();
		} catch(MalformedURLException e) {
			query = "";
		}
		
		Map<String, String> queries = new HashMap<String, String>();
		if(query == null) {
			return queries;
		}
		
		for(String entry: query.split("&")) {
			String[] keyvalue = entry.split("=");
			if(keyvalue.length != 2) {
				continue;
			}
			queries.put(keyvalue[0], keyvalue[1]);
		}
		return queries;
	}
	
	public static String filterSearchUrl(String value) {
		
		if(value.startsWith("http://m.baidu.com")) {
			Map<String, String> queries = UrlUtil.getQuery(value);
			if(!queries.containsKey("src")) {
				return value;
			}
			return UrlUtil.decode(queries.get("src"));
		}
		return value;
	}
}
