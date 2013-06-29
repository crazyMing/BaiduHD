package com.baidu.hd.sniffer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.sniffer.BigSiteSnifferResult.BigSiteAlbumResult;
import com.baidu.hd.sniffer.Sniffer.BigSiteCallback;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.StringUtil;

/**
 * 大站嗅探实体
 *
 * 实现细节：
 * 1 排队策略
 * 2 三条腿走路
 *     1 特殊支持：针对某些站点根据其规则做了特殊的支持，一般用js实现
 *     2 常规支持：使用webview渲染网页，不停的查找video标签，直到找到该tag
 *     3 网络支持：取自我们自己的云服务网站
 * 3 网络支持优先：云服务信息若有效，直接使用该结果。
 * 					特殊常规支持若有效，还要等云服务5s，若收不到有效结果，使用特殊常规支持的结果
 * 4 20s超时，返回失败
 */
class BigSnifferEntityImp implements SnifferEntity {

	private static final int TimoutMsg = 1;
	private static final int WaitNetMsg = 2;
	private static final int WaitAlbumMsg = 3;
	
	/** 嗅探数据 */
	private List<String> mRefers = new ArrayList<String>();
	private String mRefer = "";
	private String mUrl = "";
	private BigSiteCallback mCallback = null;
	private BigSiteSnifferResult result = new BigSiteSnifferResult();

	/** 支持处理器 */
	private WebViewSpecHandler mSpecHandler = new WebViewSpecHandler();
	private WebViewCommonHandler mCommonHandler = new WebViewCommonHandler();
	private BigSiteNetHandler mNetHandler = new BigSiteNetHandler();
	private BigSiteAlbumHandler mAlbumHandler = new BigSiteAlbumHandler();
	
	/** 过滤器和特殊支持器 */
	private Filter mFilter = new Filter();
	private Spec mSpec = new Spec();
	private BigHost mHost = new BigHost();
	
	private Context mContext = null;
	private ServiceFactory mServiceFactory = null;
	private WebView mWebView = null;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == TimoutMsg) {
				complete(false);
			} else if (msg.what == WaitAlbumMsg){
				if (!mAlbumHandler.isComplete()) {
					mHandler.sendEmptyMessageDelayed(WaitAlbumMsg, Const.Timeout.SnifferWaitAlbum);
				}
			} else {
				complete(false);
			}
		}
	};
	
	private OnCompleteListener mOnCompleteListener = new OnCompleteListener() {
		
		@Override
		public void onComplete(String refer, String url, BigSiteAlbumResult result, BaseHandler reporter) {
			if(!refer.equals(mRefer)) {
				return;
			}
			
			// 1云服务器查询
			if(reporter == mNetHandler) {
				// 1.1无结果
				if(StringUtil.isEmpty(url)) {
					// 1.1.1没有数据
					if(StringUtil.isEmpty(mUrl)) {
						// 1.1.1.1全部结束，则给最终结果
						if(isAllComplete()) {
							complete(false);
						} 
						// 1.1.1.2未全部结束，返回（忽略）
						else {
							return;
						}
					}
					// 1.1.2已经有数据了，则给出最终结果
					else {
						if (mAlbumHandler.isComplete()) {
							complete(false);
						}
						else {
							return;
						}
					}
				}
				// 1.2 有返回结果，则结束
				else {
					mUrl = url;
					if (mAlbumHandler.isComplete()) {
						complete(false);
					}
					else {
						return;
					}
				}
			} 
			// 3 剧集查询
			else if (reporter == mAlbumHandler) {
				if (StringUtil.isEmpty(mUrl)) {
					if (isAllComplete()) {
						complete(false);
					}
					else {
						return;
					}
				}
				else {
					complete(false);
				}
			}
			// 2 其他嗅探
			else {
				// 2.1 无结果
				if(StringUtil.isEmpty(url)) {
					// 2.1.1 全部结束，则结束
					if(isAllComplete()) {
						complete(false);
					} 
					// 2.1.2 未全部结束，则返回（忽略）
					else {
						return;
					}
				} 
				// 2.2 有结果
				else {
					mUrl = url;
					if (mAlbumHandler.isComplete()) {
						// 2.2.1 云服务器若已结束，则结束
						if(mNetHandler.isComplete()) {
							complete(false);
						} 
						// 2.2.2 云服务器未结束，则等待
						else {
							if(!mHandler.hasMessages(WaitNetMsg)) {
								mHandler.sendEmptyMessageDelayed(WaitNetMsg, Const.Timeout.SnifferWaitNet);
							}
						}
					}
					else {
						if(!mHandler.hasMessages(WaitAlbumMsg)) {
							mHandler.sendEmptyMessageDelayed(WaitAlbumMsg, Const.Timeout.SnifferWaitAlbum);
						}
					}
				}
			}
		}
	};
	
	public BigSnifferEntityImp(Context context, WebView webView, BigSiteCallback callback) {
		mContext = context;
		mCallback = callback;
		mWebView = webView;
		if (mWebView == null) {
			mWebView = new WebView(mContext);
		}
		mServiceFactory = BaiduHD.cast(context).getServiceFactory();
		
		mFilter.create(mContext);
		mSpec.create(mContext);
		mHost.create(mContext);
		
		mNetHandler.onCreate(mOnCompleteListener, mServiceFactory, mContext);
		mSpecHandler.onCreate(mOnCompleteListener, mContext, mWebView, mSpec);
		mCommonHandler.onCreate(mOnCompleteListener, mContext, mWebView);
		mAlbumHandler.onCreate(mOnCompleteListener, mServiceFactory, mContext);
	}

	@Override
	public void request(String refer) {
		mRefers.add(refer);
		fetchOneStart();
	}
	
	@Override
	public void cancel() {
		complete(true);
	}
	
	private void fetchOneStart() {
		if(!"".equals(mRefer)) {
			return;
		}
		if(mRefers.isEmpty()) {
			return;
		}
		mRefer = mRefers.remove(0);
		
		if(!mHost.isBigHost(mRefer)) {
			return;
		}
		
		if(mSpec.isSpec(mRefer)) {
			mSpecHandler.fetch(mRefer);
		}
		if(!mFilter.isFilter(mRefer)) {
			mCommonHandler.fetch(mRefer);
		}
		mNetHandler.fetch(mRefer);
		mAlbumHandler.fetch(mRefer);
		mHandler.sendEmptyMessageDelayed(TimoutMsg, Const.Timeout.SnifferComplete);
	}
	
	private void complete(boolean cancel) {
		if("".equals(mUrl)) {
			Stat stat = (Stat)mServiceFactory.getServiceProvider(Stat.class);
			stat.addSnifferFail(mRefer);
		}
		
		mSpecHandler.cancel();
		mCommonHandler.cancel();
		mNetHandler.cancel();
		mAlbumHandler.cancel();
		
		mHandler.removeMessages(TimoutMsg);
		mHandler.removeMessages(WaitNetMsg);
		mHandler.removeMessages(WaitAlbumMsg);

		if(mCallback != null) {
			if(cancel) {
				mCallback.onCancel(mRefer);
			} else {
				mCallback.onComplete(mRefer, mUrl, mAlbumHandler.getAlbumResult());
			}
		}
		mRefer = "";
		mUrl = "";
		fetchOneStart();
	}
	
	private boolean isAllComplete() {
		return mNetHandler.isComplete() && mSpecHandler.isComplete() && mCommonHandler.isComplete() && mAlbumHandler.isComplete();
	}
}
