package com.baidu.hd;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.baidu.hd.ServiceContainer.Callback;
import com.baidu.hd.ctrl.PopupDialog;
import com.baidu.hd.ctrl.PopupDialog.ReturnType;
import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.log.Logger;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.stat.MarketChannelHelper;
import com.baidu.mobstat.StatService;
import com.baidu.hd.R;

public class BaiduHD extends Application {

	private Logger logger = new Logger("BaiduPlayer");
	
	public static int mBookmarksAndHistoryPageIndex = CombinedBookmarkHistoryActivity.BOOKMARK;

	/** 当前显示的BaseActivity */
	private BaseActivity mCurrentActivity = null;
	
	/** 服务容器 */
	private ServiceContainer mServiceContainer = new ServiceContainer();

	/** 致命错误事件监听器，用于提示销毁程序 */
	private EventListener mFatalListener = new EventListener() {
		
		@Override
		public void onEvent(EventId id, EventArgs args) {
			onFatal();
		}
	};
	
	public static BaiduHD cast(Context context) {
		if(context instanceof BaiduHD) 
		{
			return (BaiduHD)context;
		}
		else if(context instanceof Activity)
		{
			return (BaiduHD)((Activity)context).getApplication();
		}
		return null;
	}
	
	public ServiceContainer getServiceContainer() {
		return mServiceContainer;
	}

	public ServiceFactory getServiceFactory() {
		return mServiceContainer.getFactory();
	}

	public BaseActivity getCurrentActivity() {
		return mCurrentActivity;
	}

	public void setCurrentActivity(BaseActivity currentActivity) {
		mCurrentActivity = currentActivity;
	}

	@Override
	public void onCreate() {

		super.onCreate();

		// 产品初始化
		Product.init(this);
		
		// 崩溃日志初始化
		Logger.init();
		
		// 设置渠道号
		StatService.setAppChannel(MarketChannelHelper.getInstance(this).getChannelID());

		logger.i("onCreate");
		
		// 崩溃统计上报
		if(Product.getProcessType() == Product.ProcessType.Main) {
			Thread.setDefaultUncaughtExceptionHandler(new MyCrashHandler(this));
		}
		
		// 创建服务
		mServiceContainer.construct();
		
		logger.i("onCreated");
		
        mLowMemoryListeners = new ArrayList<WeakReference<OnLowMemoryListener>>();
	}
	

	public void createService(BaseActivity host) {
		mServiceContainer.create(host);
		mServiceContainer.addCreatedActivity(host);
		getEventCenter().addListener(EventId.eFatal, mFatalListener);
	}
	
	public void setCreateServiceCallback(Callback callBack) {
		mServiceContainer.addCallback(callBack);
	}

	public void destroyService() {
		getEventCenter().removeListener(mFatalListener);
		mServiceContainer.destroy();
	}
	
	/** 提示退出 */
	public void promptExit()
	{
		PopupDialog dialog = new PopupDialog(getCurrentActivity(), new PopupDialog.Callback() {
			
			@Override
			public void onReturn(ReturnType type, boolean checked) {
				if(type == PopupDialog.ReturnType.OK) {
					new Handler() {
						@Override
						public void handleMessage(Message msg) {
							onDestroy();
						}
					}.sendEmptyMessageDelayed(0, 0);
				}
			}
		});
		dialog.setTitle(dialog.createText(R.string.exit_dialog_title))
		.setMessage(dialog.createText(R.string.exit_dialog_message))
		.setPositiveButton(dialog.createText(R.string.ok))
		.setNegativeButton(dialog.createText(R.string.cancel))
		.show();
	}

	/** 致命错误 */
	private void onFatal() {
		PopupDialog dialog = new PopupDialog(getCurrentActivity(), new PopupDialog.Callback() {
			
			@Override
			public void onReturn(ReturnType type, boolean checked) {
				toSingleTop();
			}
		});
		dialog.setTitle(dialog.createText(R.string.force_exit_dialog_title))
		.setMessage(dialog.createText(R.string.force_exit_dialog_message))
		.setPositiveButton(dialog.createText(R.string.ok))
		.setCancelable(false)
		.show();
	}

	/**
	 * 只有在Root活动才能生效
	 */
	public void onDestroy() {
		destroyService();
		toSingleTop();
	}
	
	public void toSingleTop() {
		logger.d("toSingleTop()");
		//Intent i = new Intent(getCurrentActivity(), NativeBaiduSearchActivity.class);
		//i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//mCurrentActivity.startActivity(i);
	}
	
	public void toForceExitApp()
	{
		Logger.flush();
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	private EventCenter getEventCenter() {
		return (EventCenter)mServiceContainer.getFactory().getServiceProvider(EventCenter.class);
	}
	
	
 /******************************** add by leikang ****************************************/
    
    
    private ExecutorService mExecutorService;
    
    private ArrayList<WeakReference<OnLowMemoryListener>> mLowMemoryListeners;
    
    private static final int CORE_POOL_SIZE = 5;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "GreenDroid thread #" + mCount.getAndIncrement());
        }
    };
    
    public ExecutorService getExecutor() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newFixedThreadPool(CORE_POOL_SIZE, sThreadFactory);
        }
        return mExecutorService;
    }

    public static interface OnLowMemoryListener {
        
        /**
         * Callback to be invoked when the system needs memory.
         */
        public void onLowMemoryReceived();
    }
    
    
    /**
     * Add a new listener to registered {@link OnLowMemoryListener}.
     * 
     * @param listener The listener to unregister
     * @see OnLowMemoryListener
     */
    public void registerOnLowMemoryListener(OnLowMemoryListener listener) {
        if (listener != null) {
            mLowMemoryListeners.add(new WeakReference<OnLowMemoryListener>(listener));
        }
    }

    /**
     * Remove a previously registered listener
     * 
     * @param listener The listener to unregister
     * @see OnLowMemoryListener
     */
    public void unregisterOnLowMemoryListener(OnLowMemoryListener listener) {
        if (listener != null) {
            int i = 0;
            while (i < mLowMemoryListeners.size()) {
                final OnLowMemoryListener l = mLowMemoryListeners.get(i).get();
                if (l == null || l == listener) {
                    mLowMemoryListeners.remove(i);
                } else {
                    i++;
                }
            }
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        int i = 0;
        while (i < mLowMemoryListeners.size()) {
            final OnLowMemoryListener listener = mLowMemoryListeners.get(i).get();
            if (listener == null) {
                mLowMemoryListeners.remove(i);
            } else {
                listener.onLowMemoryReceived();
                i++;
            }
        }
        
		// 这个回调后进程不一定会结束，所以只保存数据，不卸载服务
		mServiceContainer.save();
    }
	

}
