package com.baidu.hd.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.Video;

public class StringUtil {
	public static String bytes2String(byte[] value) {
		return (value == null) ? "" : new String(value);
	}

	public static boolean isEmpty(String paramString) {
		if ((paramString == null) || ("".equals(paramString))) {
			return true;
		}
		return false;
	}

	public static boolean isEmptyArray(Object[] obj) {
		return isEmptyArray(obj, 1);
	}

	public static boolean isEmptyArray(Object[] array, int paramInt) {
		if ((array == null) || (array.length < paramInt)) {
			return true;
		}
		return false;
	}

	public static boolean isNumeric(String str) {
		final String number = "0123456789";
		for (int i = 0; i < str.length(); i++) {
			if (number.indexOf(str.charAt(i)) == -1) {
				return false;
			}
		}
		return true;
	}

	public static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0XFF);
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
		}
		return hs.toUpperCase();
	}

	public static String reverse(String value) {
		StringBuilder sb = new StringBuilder();
		for (int i = value.length() - 1; i >= 0; --i) {
			sb.append(value.charAt(i));
		}
		return sb.toString();
	}

	public static String createUUID() {
		return UUID.randomUUID().toString();
	}

	public static String formatSpeed(int value) {
		return formatSize(value) + "/s";
	}

	public static String formatSize(long value) {

		double k = (double) value / 1024;
		if (k == 0) {
			return String.format("%dB", value);
		}

		double m = k / 1024;
		if (m < 1) {
			return String.format("%.1fK", k);
		}

		double g = m / 1024;
		if (g < 1) {
			return String.format("%.1fM", m);
		}

		return String.format("%.1fG", g);
	}

	public static String formatTime(int second) {

		int hh = second / 3600;
		int mm = second % 3600 / 60;
		int ss = second % 60;

		if (0 != hh) {
			return String.format("%02d:%02d:%02d", hh, mm, ss);
		} else {
			return String.format("%02d:%02d", mm, ss);
		}
	}

	public static String encode(String url) {
		return URLEncoder.encode(url);
	}

	public static String decode(String url) {
		return URLDecoder.decode(url);
	}

	public static String getVideoName(Video v, Album album) {
		if (v.isLocal() || album == null) {
			return v.getName();
		} else {
			if (album.asBigServerAlbum() != null) {
				return album.asBigServerAlbum().getListName() + v.getName();
			} else {
				return v.getName();
			}
		}
	}

	public static void resetVideoName(Album album, Video video) {
		if (album != null && album.asBigServerAlbum() != null) {
			List<NetVideo> lst = album.asBigServerAlbum().getVideos();
			for (NetVideo v : lst) {
				v.setName(StringUtil.getVideoName(v, album));
			}
			if (video != null) {
				video.setName(StringUtil.getVideoName(video, album));
			}
		}
	}

	public static boolean isSmallUrl(String url) {
		try {
			url = url.toLowerCase();
			if (url.startsWith("bdhd") || url.startsWith("ed2k")) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static String getNameForUrl(String url){
		try{
		if(!isSmallUrl(url)){
			return url;
		}else{
			return url.split("[|]")[2];
		}}catch (Exception e) {
			return url;
		}
	}
}
