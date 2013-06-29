package com.baidu.hd.sniffer.smallsniffer;

import java.util.ArrayList;

import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.BigServerAlbum;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.VideoFactory;
import com.baidu.hd.player.PlayerLauncher;
/**
 * BDHD sniffer plugin
 * @author juqiang
 *
 */
public class BDHDPlugin {
	final static String js = "javascript:var len=window.frames.length;for(i=1;i<len;i++){if((window.frames[i].BdPlayer)!=undefined){window.BAIDUPLAYERURL.getBDHD(window.frames[i].BdPlayer['url']+'#');break}};window.BAIDUPLAYERURL.getBDHD(document.documentElement.innerHTML);window.BAIDUPLAYERURL.getBDHD(BdPlayer['url']+'#');";
	private static WebView currentWebView;

	@SuppressWarnings("deprecation")
	public static void addBDHDJSPlugin(WebView v) {
		v.getSettings().setJavaScriptEnabled(true);
		v.getSettings().setPluginsEnabled(false);
		v.getSettings().setPluginState(WebSettings.PluginState.OFF);
		v.addJavascriptInterface(new BDHDPlugin(), "BAIDUPLAYERURL");
	}

	/**
	 * use at onPageFinish
	 * @param aView
	 */
	public static void sniffer(WebView aView) {
		currentWebView = aView;
		aView.loadUrl(js);
	}

	public void getBDHD(String html) {
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
	public static void checkUrl(String url) {
		//lLog.e("qq", "check "+url);
		String res = EscapeUnescape.unescape(url);
		if (res.contains("bdhd")) {
			ArrayList<String> arr = BDHDGeter.searchBDHD(EscapeUnescape
					.unescape(url));
			if (arr != null && arr.size()==1) {
				onResult(arr.get(0));
			}
		}
	}

	private static synchronized void onResult(String bdhd) {
		try {
			//Log.e("qq", "bdhd from plugin: "+bdhd);
			NetVideo video = VideoFactory.create(false).toNet();
			video.setType(NetVideo.NetVideoType.P2P_STREAM);
			video.setName(bdhd);
			video.setUrl(bdhd);
			Album album=new BigServerAlbum();
			album.setCurrent(video);
			ArrayList<NetVideo> vlst=new ArrayList<NetVideo>();
			vlst.add(video);
			vlst.add(video);
			album.setVideos(vlst);
			PlayerLauncher.startup(currentWebView.getContext(), album);
		} catch (Exception e) {
			//Log.e("qq", e.toString());
		}
	}

}