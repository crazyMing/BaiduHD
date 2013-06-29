package com.baidu.hd.sniffer.smallsniffer;

import java.util.ArrayList;

import com.baidu.hd.log.Logger;

import android.util.Log;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
/**
 * 本地通用嗅探
 * @author juqiang
 *
 */
public class NativeSniffer {
	
	private Logger logger = new Logger("NativeSniffer");
	
	public interface CallBack{
		/**
		 * 
		 * @param pageUrl
		 * @param bdhd bdhd结果
		 */
		void onResult(String pageUrl,String bdhd);
	}
	
	private final static String js = "javascript:var len=window.frames.length;for(i=1;i<len;i++){if((window.frames[i].BdPlayer)!=undefined){window.BAIDUPLAYERURL.getBDHD(window.frames[i].BdPlayer['url']+'#');break}else{window.BAIDUPLAYERURL.getBDHD(i)}};window.BAIDUPLAYERURL.getBDHD(document.documentElement.innerHTML);window.BAIDUPLAYERURL.getBDHD(BdPlayer['url']+'#');";
	
	private boolean stoped=false;
	private CallBack callback;
	private WebView mWebview;
	private String pageUrl;
	private synchronized void addBDHDJSPlugin(WebView v) {
		v.getSettings().setJavaScriptEnabled(true);
		v.getSettings().setPluginState(WebSettings.PluginState.OFF);
		v.addJavascriptInterface(this, "BAIDUPLAYERURL");
		v.getSettings().setBlockNetworkImage(false);
	}

	public NativeSniffer(CallBack callBack){
		this.callback=callBack;
	}
	
	public void sniffer(WebView aView,String pageUrl) {
		//Log.e("qq", "request"+pageUrl);
		this.stoped=false;
		this.mWebview=new WebView(aView.getContext());
		this.pageUrl=pageUrl;
		addBDHDJSPlugin(this.mWebview);
		this.mWebview.stopLoading();
		mWebview.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageFinished(WebView view, String url) {
				try{
				if(view!=null){
				view.loadUrl(js);}}catch (Exception e) {
					e.printStackTrace();
				}
				//Log.e("qq", "page finished:"+url);
				//super.onPageFinished(view, url);
			}
			
			@Override
			public void onLoadResource(WebView view, String url) {
				try{
				checkUrl(url);
				if(view!=null){
				view.loadUrl(js);}
				}catch (Exception e) {
					e.printStackTrace();
				}//super.onLoadResource(view, url);
			}
			
		});
		mWebview.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype, long contentLength) {
				
			}
		});
		mWebview.loadUrl(pageUrl);
		//Log.e("qq", "start:"+pageUrl);
	}

	/**
	 * 结束本次任务
	 */
	public void cancle(){
		stoped=true;
//		if(mWebview!=null){
//			mWebview.stopLoading();
//		}
		//Log.e("qq", "stop:"+pageUrl);

	}
	/**
	 * never use in java code
	 * just for javaScript callBack
	 * @param html
	 */
	@Deprecated
	public void getBDHD(String html) {
		//Log.e("qq", "get:"+html);
		html = EscapeUnescape.unescape(html);
		ArrayList<String> arr = BDHDGeter.searchBDHD(html);
		if (arr != null && arr.size()==1) {
			onResult(arr.get(0));
		}
	}

	/**
	 * use on loadResource
	 * @param url
	 */
	private void checkUrl(String url) {
		String res = EscapeUnescape.unescape(url);
		if (res.contains("bdhd")) {
			ArrayList<String> arr = BDHDGeter.searchBDHD(EscapeUnescape
					.unescape(url));
			if (arr != null && arr.size()==1) {
				onResult(arr.get(0));
			}
		}
		//Log.i("qq", "check:"+pageUrl);

	}

	private synchronized void onResult(String bdhd) {
		try {
			mWebview.destroy();
			//Log.e("qq", bdhd);
			//Log.e("qq",""+this.hashCode() );
			if(!stoped){
				stoped=true;
				logger.d("bdhd : " + bdhd);
				callback.onResult(pageUrl,bdhd);
			}
		} catch (Exception e) {
			e.printStackTrace();
			//Log.e("qq", e.toString());
		}
	}
	

}