package com.baidu.hd;
//package com.baidu.hd;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.animation.Animation;
//import android.view.animation.Animation.AnimationListener;
//import android.view.animation.AnimationUtils;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.baidu.browser.SearchManager;
//import com.baidu.hd.log.Logger;
//
//public class NativeBaiduSearchActivity extends BaseActivity implements OnClickListener, AnimationListener {
//
//	private Logger logger = new Logger("NativeBaiduSearchActivity");
//	
//	public final static String TAG_IS_START_FROM_NATIVE_INPUT_BOX = "TAG_IS_START_FROM_NATIVE_INPUT_BOX";
//	
//	private static final String kBaiduProductNewsUrl ="http://news.baidu.com";
//	private static final String kBaiduProductTiebaUrl =  "http://tieba.baidu.com";
//	private static final String kBaiduProductZhidaoUrl =  "http://zhidao.baidu.com";
//	private static final String kBaiduProductMusicUrl =  "http://music.baidu.com";
//	private static final String kBaiduProductPhotoUrl =  "http://m.baidu.com/img";
//	private static final String kBaiduProductVideoUrl =  "http://m.baidu.com/video?static=ipad_index.html";
//	private static final String kBaiduProductMapUrl =  "http://map.baidu.com/mobile/?third_party=baidu_search_ipad";
//	private static final String kBaiduProductBaikeUrl =  "http://baike.baidu.com";
//	private static final String kBaiduProductWenkuUrl =  "http://wenku.baidu.com";
//	private static final String kBaiduProductHao123Url =  "http://ipad.hao123.com";
//	
//	private FrameLayout mNativeBaiduSearchPanel;
//	private LinearLayout mNativeMoreAndVoicePanel;
//	private ImageView mNativeSettingBtn;
//	private TextView mNativeInputBox;
//	private TextView mNativeMoreBtn;
//	private TextView mNativeVoiceBtn;
//	private Animation showAction ;
//	private Animation hideAction ;
//	
//	private TextView xinwen;
//	private TextView tieba;
//	private TextView zhidao;
//	private TextView yinyue;
//	private TextView tupian;
//	private TextView shipin;
//	private TextView ditu;
//	private TextView baike;
//	private TextView wenku;
//	private TextView hao123;
//	
//	private boolean isMoreShow = false;
//	
//	
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		logger.d("onCreate");
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.native_baidu_search);
//		mNativeBaiduSearchPanel = (FrameLayout)findViewById(R.id.native_baidu_search_panel);
//		mNativeMoreAndVoicePanel = (LinearLayout)findViewById(R.id.native_more_and_voice_panel);
//		
//		mNativeSettingBtn = (ImageView)findViewById(R.id.native_setting_btn);
//		mNativeMoreBtn =(TextView)findViewById(R.id.native_more_btn);
//		mNativeInputBox = (TextView)findViewById(R.id.native_input_box);
//		mNativeVoiceBtn = (TextView)findViewById(R.id.native_voice_btn);
//		
//		xinwen = (TextView)findViewById(R.id.xinwen);
//		tieba = (TextView)findViewById(R.id.tieba);
//		zhidao = (TextView)findViewById(R.id.zhidao);
//		yinyue = (TextView)findViewById(R.id.yinyue);
//		tupian = (TextView)findViewById(R.id.tupian);
//		shipin = (TextView)findViewById(R.id.shipin);
//		ditu = (TextView)findViewById(R.id.ditu);
//		baike = (TextView)findViewById(R.id.baike);
//		wenku = (TextView)findViewById(R.id.wenku);
//		hao123 = (TextView)findViewById(R.id.hao123);
//		
//		xinwen.setOnClickListener(this);
//		tieba.setOnClickListener(this);
//		zhidao.setOnClickListener(this);
//		yinyue.setOnClickListener(this);
//		tupian.setOnClickListener(this);
//		shipin.setOnClickListener(this);
//		ditu.setOnClickListener(this);
//		baike.setOnClickListener(this);
//		wenku.setOnClickListener(this);
//		hao123.setOnClickListener(this);
//		
//		mNativeBaiduSearchPanel.setOnClickListener(this);
//		mNativeSettingBtn.setOnClickListener(this);
//		mNativeMoreBtn.setOnClickListener(this);
//		mNativeInputBox.setOnClickListener(this);
//		mNativeVoiceBtn.setOnClickListener(this);
//		showAction = AnimationUtils.loadAnimation(this, R.anim.popshow_anim);
//		hideAction = AnimationUtils.loadAnimation(this, R.anim.pophidden_anim);
//		showAction.setAnimationListener(this);
//		hideAction.setAnimationListener(this);
//	}
//
//	@Override
//	protected void onResume() {
//		super.onResume();
//	}
//
//	@Override
//	protected void onDestroy() {
//		logger.d("onDestroy");
//		super.onDestroy();
//	}
//
//
//	@Override
//	protected boolean showProgressDialogWhenCreatingService() {
//		return false;
//	}
//	
//	@Override
//	protected void onStop() {
//		super.onStop();
//	}
//
//	@Override
//	public void finish() {
//		super.finish();
//	}
//
//	@Override
//	public void onClick(View v) {
//		
//		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mNativeMoreAndVoicePanel.getLayoutParams();
//		
//		switch (v.getId()) {
//		case R.id.native_voice_btn:
//			Intent voiceIntent = new Intent(this, VoiceSearchActivity.class);
//			voiceIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//			startActivity(voiceIntent);
//			break;
//		case R.id.native_setting_btn:
//			
//			break;
//		case R.id.native_input_box:
//	        Intent intent = new Intent(this, MainActivity.class);
//	        intent.putExtra(TAG_IS_START_FROM_NATIVE_INPUT_BOX, true);
//	        startActivity(intent);
//			break;
//			
//		case R.id.native_more_btn:
//			if(isMoreShow) {
//				mNativeMoreAndVoicePanel.startAnimation(hideAction);	
//				params.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.native_baidu_search_margins_bottom));
//				mNativeMoreAndVoicePanel.setLayoutParams(params);
//			}
//			else {
//				mNativeMoreAndVoicePanel.startAnimation(showAction);
//				params.setMargins(0, 0, 0, 0);
//				mNativeMoreAndVoicePanel.setLayoutParams(params);
//
//			}
//			isMoreShow = !isMoreShow;
//			break;
//		case R.id.native_baidu_search_panel:
//			if(isMoreShow) {
//				mNativeMoreAndVoicePanel.startAnimation(hideAction);	
//				params.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.native_baidu_search_margins_bottom));
//				mNativeMoreAndVoicePanel.setLayoutParams(params);
//				isMoreShow = false;
//			}
//			break;
//		case R.id.xinwen:
//			SearchManager.launchURL(this,kBaiduProductNewsUrl);
//			break;
//		case R.id.tieba:
//			SearchManager.launchURL(this,kBaiduProductTiebaUrl);
//			break;
//		case R.id.zhidao:
//			SearchManager.launchURL(this,kBaiduProductZhidaoUrl);
//			break;
//		case R.id.yinyue:
//			SearchManager.launchURL(this,kBaiduProductMusicUrl);
//			break;
//		case R.id.tupian:
//			SearchManager.launchURL(this,kBaiduProductPhotoUrl);
//			break;
//		case R.id.shipin:
//			SearchManager.launchURL(this,kBaiduProductVideoUrl);
//			break;
//		case R.id.ditu:
//			SearchManager.launchURL(this,kBaiduProductMapUrl);
//			break;
//		case R.id.baike:
//			SearchManager.launchURL(this,kBaiduProductBaikeUrl);
//			break;
//		case R.id.wenku:
//			SearchManager.launchURL(this,kBaiduProductWenkuUrl);
//			break;
//		case R.id.hao123:
//			SearchManager.launchURL(this,kBaiduProductHao123Url);
//			break;
//
//		default:
//			break;
//		}
//	}
//
//	@Override
//	public void onAnimationEnd(Animation animation) {
//		
//	}
//
//	@Override
//	public void onAnimationRepeat(Animation animation) {
//	}
//
//	@Override
//	public void onAnimationStart(Animation animation) {
//		
//	}
//}