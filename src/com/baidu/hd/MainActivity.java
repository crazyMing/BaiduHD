 
package com.baidu.hd;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.baidu.browser.BPBrowser;
import com.baidu.browser.SearchManager;
import com.baidu.hd.ctrl.PopupDialog;
import com.baidu.hd.ctrl.PopupDialog.ReturnType;
import com.baidu.hd.detect.Detect;
import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.IntentConstants;
import com.baidu.hd.upgrade.Upgrade;
import com.baidu.hd.upgrade.UpgradeEventArgs;
import com.baidu.hd.R;
 
/**
 * @ClassName: MainActivity 
 * @Description: 百度HD主activity
 * @author LEIKANG 
 * @date 2012-12-7 上午10:51:09
 */
public class MainActivity extends BaseActivity {
	
	/**从前端JS调起APP*/
	private static final int MSG_SWITCH_TO_BROWSER_FROM_OTHER = 1;
	
	private static final int MSG_SWITCH_TO_BROWSER_FROM_NATIVE_SEARCH = 2;
	
	private static final int MSG_SWITCH_TO_BROWSER_FROM_NATIVE_INPUT_BOX = 3;

	/** log tag. */
	public static final String TAG = "MainActivity";
	
	private Logger logger = new Logger(TAG);
	
	private Context mContext;
	
	/** 标志是否前端调起app，static为和退出同步 */
	public static boolean mBInvokeApp = false; 
	
	/**
	 * 主线程的Handler，便于异步执行。
	 */
	private Handler mHandler = new Handler() {
	    
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	        	case MSG_SWITCH_TO_BROWSER_FROM_OTHER:
	        		String url = getIntent().getStringExtra("url");
	        		logger.d(url);
	                SearchManager.launchURL(mContext,url);
	                break;
	                
	        	case MSG_SWITCH_TO_BROWSER_FROM_NATIVE_SEARCH:
	                break;
	                
	        	case MSG_SWITCH_TO_BROWSER_FROM_NATIVE_INPUT_BOX :
	        		break;
	                
	            default:
	                break; 
	        }
	        
	    }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		logger.d("onCreate");
		super.onCreate(savedInstanceState);
			mContext = this;
			
			if (!isServiceCreated()) {
				getPlayerApp().getServiceContainer().createDirect(this);
				mBInvokeApp = true;
			}
			
	        init(savedInstanceState);
	        
	        if (isServiceCreated() && !mBInvokeApp) {
				onServiceCreated();
			}
	}
 
	/**
	 * @Title: handleVoiceSearch 
	 * @Description: 处理语音搜索进入的情况 
	 * @param     设定文件 
	 * @return void    返回类型 
	 * @throws
	 */
	private void handleVoiceSearch() {
		int voiceFrom = getIntent().getIntExtra(VoiceSearchActivity.TAG_VOICE_START_FROM, VoiceSearchActivity.EXTRA_START_FROM_NONE);
		String[] sugs = getIntent().getStringArrayExtra(VoiceSearchActivity.TAG_VOICE_RESULT_);
		
		if(voiceFrom != -1) {
			FragmentManager manager = getSupportFragmentManager();
	        BPBrowser browser = (BPBrowser) manager.findFragmentByTag(BPBrowser.FRAGMENT_TAG);
			browser.handleVoiceSearch(voiceFrom, sugs);
		}
		
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		FragmentManager manager = getSupportFragmentManager();
        BPBrowser browser = (BPBrowser) manager.findFragmentByTag(BPBrowser.FRAGMENT_TAG);
        browser.onActivityResult(requestCode, resultCode, data);
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		
        String url = getIntent().getStringExtra(SearchManager.TAG_KEY_URL);
        if(url!=null&&url!="") {
		BPBrowser searchBrowser = (BPBrowser) fragment;
		searchBrowser.loadUrl(url);
        }
	}

	/**
	 * @Title: init 
	 * @Description: 初始化动作
	 * @param savedInstanceState   
	 */
	private void init(Bundle savedInstanceState) {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    setContentView(R.layout.main);
        
        FragmentManager manager = getSupportFragmentManager();
        
        BPBrowser browser = (BPBrowser) manager.findFragmentByTag(BPBrowser.FRAGMENT_TAG);
        if (browser == null) {
            browser = new BPBrowser();
            browser.setRetainInstance(true);
            FragmentTransaction fragmentTransaction = manager.beginTransaction();
            fragmentTransaction.add(R.id.layout_for_fragment,browser, BPBrowser.FRAGMENT_TAG);
            fragmentTransaction.commitAllowingStateLoss();
        }
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		String action = intent.getAction();
		String url = intent.getStringExtra(SearchManager.TAG_KEY_URL);
		if (IntentConstants.ACTION_BROWSER.equals(action)) {
			   switchToSearchBrowser(url);
		} else if (action != null && !action.equals("")) {
        	if (action.equals(IntentConstants.ACTION_HOME)) {
        		String exurl = getIntent().getStringExtra("url");
                SearchManager.launchURL(this,exurl);
        	}
        }
		
		handleVoiceSearch();
	}
	
    /**
     * 切换到浏览界面.
     */
    public void switchToSearchBrowser(String url) {
        FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = manager.beginTransaction();
		BPBrowser searchBrowser = (BPBrowser) manager.findFragmentByTag(BPBrowser.FRAGMENT_TAG);
		searchBrowser.loadUrl(url);
		fragmentTransaction.attach(searchBrowser);
		fragmentTransaction.commitAllowingStateLoss();
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		FragmentManager manager = getSupportFragmentManager();
        BPBrowser browser = (BPBrowser) manager.findFragmentByTag(BPBrowser.FRAGMENT_TAG);
        if (browser.onKeyDown(keyCode, event)) {
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_BACK)
        {
    		if (mBInvokeApp)
    		{
    			getPlayerApp().destroyService();
    			getPlayerApp().toForceExitApp();
    		}
    		else 
    		{
    			setBackExitFlag(true);
    		}
        }
		return super.onKeyDown(keyCode, event);
	}
	
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
		
		super.onBackPressed();
	}

	protected boolean canExit() {
		return true;
	}

	@Override
	protected void onServiceCreated() {
		logger.d("onServiceCreated");
		super.onServiceCreated();
		loadUrlFromOtherBrowser();
	}
	
	private void loadUrlFromOtherBrowser() {
        String action = getIntent().getAction();
        if (action != null && !action.equals("")) {
        	if (action.equals(IntentConstants.ACTION_HOME)) {
                Message msg = mHandler.obtainMessage(MSG_SWITCH_TO_BROWSER_FROM_OTHER);
                mHandler.sendMessageDelayed(msg, 2000);
        	}
        }
	}
	
}
