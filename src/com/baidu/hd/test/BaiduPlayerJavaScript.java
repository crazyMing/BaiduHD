package com.baidu.hd.test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;

import com.baidu.hd.util.StringUtil;

public class BaiduPlayerJavaScript {
	
	public static final int TestWebSniffer = 0;
	
	private String mCharset = "UTF-8";
	private Handler mHander = null;

	public BaiduPlayerJavaScript(Handler hander) {
		mHander = hander;
	}

	public static class ResultArgs {

		private List<String> mResult = new ArrayList<String>();

		public ResultArgs() {

		}

		public ResultArgs(List<String> result) {
			this.mResult = result;
		}

		public List<String> getResult() {
			return this.mResult;
		}
	}

	/** «Î«Ûœﬂ≥Ã */
	public static class JavaScriptThreadRunnable implements Runnable {
		private WebView mView = null;
		private String mRunJavaScript = "javascript:window.BAIDUPLAYERURL.getBDHD('<head>'+document.documentElement.innerHTML+'</head>');";

		public JavaScriptThreadRunnable(WebView view) {
			this.mView = view;
		}

		@Override
		public void run() {
			try {
				this.mView.loadUrl(this.mRunJavaScript);
			} catch (Exception e) {

			}
		}
	}

	public void getBDHD(String html) {
		getCharset(html);
		List<String> arrayListBDHD = htmlDecode2(html);
		Message msg = mHander.obtainMessage(BaiduPlayerJavaScript.TestWebSniffer,
				new ResultArgs(arrayListBDHD));
		mHander.sendMessage(msg);
	}

	private void getCharset(String paramString) {
		if (!StringUtil.isEmpty(paramString)) {
			Pattern p = Pattern
					.compile("<meta[^>]+charset=[\"]?([\\w\\-]+)[^>]*>");
			Matcher m = p.matcher(paramString);
			if (m.find() && m.groupCount() == 1) {
				this.mCharset = m.group(1);
			}
		}
	}

	private List<String> htmlDecode2(String paramString) {
		List<String> arrayListBDHD = new ArrayList<String>();
		if (!StringUtil.isEmpty(paramString)) {
			String patternStr = null;
			if (paramString.contains("bdhd%3A%2F%2F")) {
				patternStr = "(bdhd%3A%2F%2F\\d+%7C[%0-9A-Z]+.[^%\"$']*)";
			} else if (paramString.contains("bdhd://")) {
				patternStr = "(bdhd://\\d+\\|[0-9A-Z]+\\|[^+\"$']*)";
			} else {
				return arrayListBDHD;
			}

			Pattern p = Pattern.compile(patternStr);
			Matcher m = p.matcher(paramString);

			while (m.find() && m.groupCount() == 1) {
				arrayListBDHD.add(m.group(1));
			}

			if (arrayListBDHD.size() > 0) {
				int i = 0;
				for (String string : arrayListBDHD) {
					try {
						string = URLDecoder.decode(string, this.mCharset);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					arrayListBDHD.set(i++, string);
				}
			}
		}
		return arrayListBDHD;
	}
}