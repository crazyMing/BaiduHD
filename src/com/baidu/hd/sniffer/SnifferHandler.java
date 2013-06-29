package com.baidu.hd.sniffer;

import android.content.Context;
import android.webkit.WebView;

import com.baidu.hd.log.DebugLogger;
import com.baidu.hd.log.Logger;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.sniffer.BigSiteSnifferResult.BigSiteAlbumResult;
import com.baidu.hd.util.StringUtil;

public class SnifferHandler {
	

	/**
	 * TODO 不进行嗅探的网址，后期进行优化，可以从服务器下发
	 */
	private static class FilterSnifferUrl {
		
		private static String[] REFERLIST = new String[] {
			"http://www.letv.com/",
			"http://v.qq.com/",
			"http://m.v.qq.com/" 
		};
		
		public static boolean isFilter(String refer) {
			
			for(int i=0; i<REFERLIST.length; i++) {
				if (REFERLIST[i].equalsIgnoreCase(refer)) {
					return true;
				}
			}
			
			return false;
		}
	}
	

	// 嗅探类型
	public enum SnifferType {
		UNKNOWN,
		BIG,
		SMALL;
	}
	
	public interface CallBack {
		void onComplete(SnifferResult result);
		void onCancel(SnifferResult result);
	}
	
	private Logger logger = new Logger(this.getClass().getSimpleName());
	
	/** Context */
	private Context mContext = null;
	/** 服务工厂*/
	private ServiceFactory mServiceFactory = null;	
	/** 回调对象*/
	private CallBack mCallBack = null;	
	/** 嗅探对象*/
	private SnifferEntity mSnifferEntity = null;
	/** 嗅探网址*/
	private String mRefer  = null;
	/** 大站WebView */
	private WebView mBigWebView = null;
	/** 小站WebView */
	private WebView mSmallWebView = null;
	/** 大站过滤 */
	private BigHost mBigHost = new BigHost();
	
	public SnifferHandler(Context context, ServiceFactory factory, CallBack callBack, WebView forBig, WebView forSmall) {
		this.mContext = context;
		this.mServiceFactory = factory;
		this.mCallBack = callBack;
		mBigWebView = forBig;
		mSmallWebView = forSmall;
		mBigHost.create(mContext);
	}
	
	public void destroy() {
		if (mSnifferEntity !=null) {
			mSnifferEntity.cancel();
		}
	}
	
	// 嗅探M3U8
	public void requestM3U8(String refer) {
		logger.d("requestM3U8 refer = " + refer);
		
		mRefer = refer;
		Sniffer sniffer = (Sniffer)mServiceFactory.getServiceProvider(Sniffer.class);
		// 大站嗅探
		if (filterRefer() == SnifferType.BIG) {
			mSnifferEntity = sniffer.createM3U8Entity(new Sniffer.BigSiteCallback() {
				
				@Override
				public void onComplete(String refer, String url, 	BigSiteAlbumResult albumResult) {
					logger.d("requestM3U8 onComplete refer=" + refer + " url=" + url );
					
					if (mCallBack == null || !refer.equals(mRefer) ) return;
					
					// 嗅探结果
					SnifferResult result = new SnifferResult();
					result.setType(SnifferType.BIG);
					result.setRefer(refer);
					result.setSmallSiteUrl(null);
					
					if (url==null || StringUtil.isEmpty(url)) {
						result.setSucceed(false);
						result.setBigSiteResult(null);
						mCallBack.onComplete(result);
					}
					else {
						BigSiteSnifferResult bigResult = new BigSiteSnifferResult();
						bigResult.setCurrentPlayUrl(url);
						bigResult.setCurrentPlayRefer(refer);
						
						result.setSucceed(true);
						result.setBigSiteResult(bigResult);
						mCallBack.onComplete(result);
					}
				}
				
				@Override
				public void onCancel(String refer) {
					if (mCallBack == null || !refer.equals(mRefer) ) return;
					
					SnifferResult result = new SnifferResult();
					result.setType(SnifferType.BIG);
					result.setRefer(refer);
					mCallBack.onCancel(result);
				}
			}, null);
		}
		
		mSnifferEntity.request(refer);
	}
	
	/** 查询 */
	private void request(String refer) {
		logger.d("want sniffer " + refer);
		
		mRefer = refer;
		Sniffer sniffer = (Sniffer)mServiceFactory.getServiceProvider(Sniffer.class);
		// 大站嗅探
		if (filterRefer() == SnifferType.BIG) {
			mSnifferEntity = sniffer.createBig(new Sniffer.BigSiteCallback() {
				
				@Override
				public void onComplete(String refer, String url, 	BigSiteAlbumResult albumResult) {
					
					if (mCallBack == null || !refer.equals(mRefer) ) return;
					
					// 嗅探结果
					SnifferResult result = new SnifferResult();
					result.setType(SnifferType.BIG);
					result.setRefer(refer);
					result.setSmallSiteUrl(null);
					
					if (url==null || StringUtil.isEmpty(url)) {
						result.setSucceed(false);
						result.setBigSiteResult(null);
						mCallBack.onComplete(result);
					}
					else {
						// 大站嗅探结果
						BigSiteSnifferResult bigResult = new BigSiteSnifferResult();
						bigResult.setBigSiteAlbumResult(albumResult);
						bigResult.setCurrentPlayUrl(url);
						bigResult.setCurrentPlayRefer(refer);
						
						result.setSucceed(true);
						result.setBigSiteResult(bigResult);
						mCallBack.onComplete(result);
					}
				}
				
				@Override
				public void onCancel(String refer) {
					if (mCallBack == null || !refer.equals(mRefer) ) return;
					
					SnifferResult result = new SnifferResult();
					result.setType(SnifferType.BIG);
					result.setRefer(refer);
					mCallBack.onCancel(result);
				}
			}, mBigWebView);
		}
		// 小站嗅探
		else {
			mSnifferEntity = sniffer.createSmall(new Sniffer.SmallSiteCallback() {
				
				@Override
				public void onComplete(String refer, SmallSiteUrl url) {
					if (mCallBack == null || !refer.equals(mRefer)) return;
					
					SnifferResult result = new SnifferResult();
					result.setType(SnifferType.SMALL);
					result.setRefer(refer);
					result.setSucceed(url.isSuccess());
					result.setSmallSiteUrl(url);
					result.setBigSiteResult(null);
					mCallBack.onComplete(result);
				}
				
				@Override
				public void onCancel(String refer) {
					if (mCallBack == null || !refer.equals(mRefer) ) return;
					
					SnifferResult result = new SnifferResult();
					result.setType(SnifferType.SMALL);
					result.setRefer(refer);
					mCallBack.onCancel(result);
				}
			}, mSmallWebView);
		}
		
		mSnifferEntity.request(refer);
	}
	
	public void cancel() {
		logger.d("cancel");
		
		if (mSnifferEntity != null) {
			mSnifferEntity.cancel();
		}
	}
	
	public void request(WebView webview, String refer) {
		mBigWebView = webview;
		mSmallWebView = webview;
		
		if (FilterSnifferUrl.isFilter(refer)) return;
		
		request(refer);
	}
	
	//* 筛选站点 */
	private SnifferType filterRefer() {
		
		if (mBigHost.isBigHost(mRefer)) {
			return SnifferType.BIG;
		}
//		else if () {
//			return Type.SMALL;
//		}
		
		return SnifferType.SMALL;
	}
}
