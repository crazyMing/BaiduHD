package com.baidu.hd.sniffer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.log.Logger;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.sniffer.BigSiteSnifferResult.BigSiteAlbumResult;
import com.baidu.hd.sniffer.Sniffer.BigSiteCallback;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.StringUtil;

public class M3U8SnifferEntityImpl implements SnifferEntity {

	private static final int TimoutMsg = 1;
	private static final int WaitNetMsg = 2;
	private static final int WaitAlbumMsg = 3;
	
	private Logger logger = new Logger(this.getClass().getSimpleName());
	
	/** ��̽���� */
	private List<String> mRefers = new ArrayList<String>();
	private String mRefer = "";
	private String mUrl = "";
	private BigSiteCallback mCallback = null;
	private BigSiteSnifferResult result = new BigSiteSnifferResult();

	/** ֧�ִ����� */
	private WebViewSpecHandler mSpecHandler = new WebViewSpecHandler();
	private WebViewCommonHandler mCommonHandler = new WebViewCommonHandler();
	private BigSiteNetHandler mNetHandler = new BigSiteNetHandler();
	
	/** ������������֧���� */
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
			}  else {
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
			
			// 1�Ʒ�������ѯ
			if(reporter == mNetHandler) {
				// 1.1�޽��
				if(StringUtil.isEmpty(url)) {
					// 1.1.1û������
					if(StringUtil.isEmpty(mUrl)) {
						// 1.1.1.1ȫ��������������ս��
						if(isAllComplete()) {
							complete(false);
						} 
						// 1.1.1.2δȫ�����������أ����ԣ�
						else {
							return;
						}
					}
					// 1.1.2�Ѿ��������ˣ���������ս��
					else {
						return;
					}
				}
				// 1.2 �з��ؽ���������
				else {
					mUrl = url;
					return;
				}
			} 
			// 2 ������̽
			else {
				// 2.1 �޽��
				if(StringUtil.isEmpty(url)) {
					// 2.1.1 ȫ�������������
					if(isAllComplete()) {
						complete(false);
					} 
					// 2.1.2 δȫ���������򷵻أ����ԣ�
					else {
						return;
					}
				} 
				// 2.2 �н��
				else {
					mUrl = url;
					// 2.2.1 �Ʒ��������ѽ����������
					if(mNetHandler.isComplete()) {
						complete(false);
					} 
					// 2.2.2 �Ʒ�����δ��������ȴ�
					else {
						if(!mHandler.hasMessages(WaitNetMsg)) {
							mHandler.sendEmptyMessageDelayed(WaitNetMsg, Const.Timeout.SnifferWaitNet);
						}
					}
				}
			}
		}
	};
	
	public M3U8SnifferEntityImpl(Context context, WebView webView, BigSiteCallback callback) {
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
	}

	@Override
	public void request(String refer) {
		logger.d("refer = " + refer);
		
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
		
		mHandler.removeMessages(TimoutMsg);
		mHandler.removeMessages(WaitNetMsg);
		mHandler.removeMessages(WaitAlbumMsg);

		if(mCallback != null) {
			if(cancel) {
				mCallback.onCancel(mRefer);
			} else {
				mCallback.onComplete(mRefer, mUrl, null);
			}
		}
		mRefer = "";
		mUrl = "";
		fetchOneStart();
	}
	
	private boolean isAllComplete() {
		return mNetHandler.isComplete() && mSpecHandler.isComplete() && mCommonHandler.isComplete();
	}

}
