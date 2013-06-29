package com.baidu.hd.sniffer;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.webkit.WebView;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.log.Logger;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.sniffer.Sniffer.SmallSiteCallback;
import com.baidu.hd.stat.Stat;

/**
 * Сվ��̽ʵ��
 * ���ַ�����ֱ��ȡ�����з���������ò����б�͵�ǰҳ��Ƶ����
 *          ������̽
 *          ˭����˭
 *          ���ڿ��ǣ�����ʹ�÷�������̽�����㱾����̽���˽����Ҳ�����ŵȴ�������̽�Ľ��  to do �����Ƿ��б�Ҫ
 */
class SmallSnifferEntityImp implements SnifferEntity 
{
	
	private Logger logger = new Logger("SmallSnifferEntityImp");
	
	private final int DELAYED_TIME = 500;  // ��ʱ�ȴ�������̽  
	private final int CMD_WAITSERVICE = 1;  // �ȴ�������̽��Ϣ 
	
	private Context mContext = null;
	private WebView mWebView = null;
	private SmallSiteCallback mCallback = null;
	private String mRefer = "";
	private SmallSiteUrl mSmallSiteUrl = new SmallSiteUrl();
	private SmallSiteUrl mSmallSiteUrlTemp;
	private ServiceFactory mServiceFactory;
	
	private SmallSiteHandler mSmallSiteHandler = new SmallSiteHandler();
	private SmallNativeHandler mSmallNativeHandler = new SmallNativeHandler();
	
	private OnSmallCompleteListener mOnSmallCompleteListener = new OnSmallCompleteListener()
	{
		@Override
		public void onComplete(String refer, SmallSiteUrl smallSiteUrl,
				SmallBaseHandler reporter)
		{	

			if (reporter == mSmallSiteHandler)
			{
				if (smallSiteUrl != null)
				{
					if (!mSmallSiteHandler.isCancel())
					{
						if (smallSiteUrl.getPlayUrls().size() != 0)
						{
							mSmallNativeHandler.cancel();
							mSmallSiteUrl = smallSiteUrl;
							complete(false);
						}
						else
						{
							mSmallSiteHandler.cancel();
							if (isAllComplete())
							{
								mSmallSiteUrl = new SmallSiteUrl();
								complete(false);
							}
						}
					}
				}
			}
			else 
			{
				if (smallSiteUrl != null)
				{
					if (!mSmallNativeHandler.isCancel())
					{
						if (!smallSiteUrl.getBdhd().equals(""))
						{
							mSmallSiteUrlTemp = smallSiteUrl;
							mSmallSiteUrl = smallSiteUrl;
							mHandler.sendEmptyMessageDelayed(CMD_WAITSERVICE, DELAYED_TIME);
							mSmallNativeHandler.isComplete = false;
						}
						else 
						{
							mSmallNativeHandler.cancel();
							if (isAllComplete())
							{
								mSmallSiteUrl = new SmallSiteUrl();
								complete(false);
							}
						}
					}
				}
			}
		}
	};
	
	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if (!mSmallSiteHandler.isCancel())
			{
				mSmallSiteHandler.cancel();
				complete(false);
			}
			super.handleMessage(msg);
		}
	};
	
	public SmallSnifferEntityImp(Context context, SmallSiteCallback callback, WebView webView) 
	{
		mContext = context;
		mCallback = callback;
		this.mWebView = webView;
		
		mServiceFactory = BaiduHD.cast(mContext).getServiceFactory();
		
		mSmallSiteHandler.onCreate(mOnSmallCompleteListener, mContext);
		mSmallNativeHandler.onCreate(mOnSmallCompleteListener, mContext, mWebView);
		HandlerThread handlerThread = new HandlerThread("SmallSniffer");
		handlerThread.start();
		mHandler = new Handler(handlerThread.getLooper())
		{
			@Override
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
				case CMD_WAITSERVICE:
					if (!mSmallNativeHandler.isCancel())
					{
						complete(false);
						mSmallSiteHandler.cancel();   // ����������̽
					}
					break;

				default:
					break;
				}
				super.handleMessage(msg);
			}
		};
	}

	@Override
	public void request(String refer) 
	{
		mRefer = refer;
		fetch();
	}

	@Override
	public void cancel() {
		mSmallNativeHandler.cancel();
		mSmallSiteHandler.cancel();
		complete(true);
	}
	
	private void fetch()
	{
		if ("".equals(mRefer))
		{
			return ;
		}

		// Ϊ��ʱΪ׷�����ã����ñ�����̽
		if (mWebView == null)
		{
			logger.d("sniffer in fetch new album");
			mSmallSiteHandler.fetch(mRefer);
		}
		else 
		{
			logger.d("normal sniffer" + mRefer);
			mSmallSiteHandler.fetch(mRefer);
			mSmallNativeHandler.fetch(mRefer);
		}
	}
	
	private void complete(boolean cancel)
	{
		if (mSmallSiteUrl != null)
		{
			
			if (!mSmallSiteUrl.isSuccess())
			{
				Stat stat = (Stat)mServiceFactory.getServiceProvider(Stat.class);
				stat.addSnifferFail(mRefer);
				
			}
		}
		
		if (mCallback != null)
		{
			if (cancel)
			{
				mCallback.onCancel(mRefer);
			}
			else 
			{
				if (mSmallSiteUrl != null)
				{
					boolean type = mSmallSiteUrl.getSnifferType();
					logger.d("sniffer type : " + type);
					if (type)
					{
						logger.d("sniffer data : " + mSmallSiteUrl.getBdhd());
					}
					else 
					{
						logger.d("sniffer data : " + mSmallSiteUrl.getPlayUrls().size());
						logger.d("sniffer result : " + mSmallSiteUrl.toString());
					}
					
					mCallback.onComplete(mRefer, mSmallSiteUrl);
				}
			}
		}
		
		mRefer = "";
		mSmallSiteUrl = null;
	}
	
	private boolean isAllComplete()
	{
		return mSmallNativeHandler.isComplete() && mSmallSiteHandler.isComplete();
	}
}

	