package com.baidu.browser.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.browser.SearchManager;
import com.baidu.hd.MainActivity;
import com.baidu.hd.SearchActivity;
import com.baidu.hd.VoiceSearchActivity;
import com.baidu.hd.settings.SettingsActivity;
import com.baidu.hd.R;

public class NativeSearchPanel extends FrameLayout implements OnClickListener, AnimationListener{
	
	private static final String kBaiduProductNewsUrl ="http://news.baidu.com";
	private static final String kBaiduProductTiebaUrl =  "http://tieba.baidu.com";
	private static final String kBaiduProductZhidaoUrl =  "http://zhidao.baidu.com";
	private static final String kBaiduProductMusicUrl =  "http://music.baidu.com";
	private static final String kBaiduProductPhotoUrl =  "http://m.baidu.com/img";
	private static final String kBaiduProductVideoUrl =  "http://m.baidu.com/video?static=ipad_index.html";
	private static final String kBaiduProductMapUrl =  "http://map.baidu.com/mobile/?third_party=baidu_search_ipad";
	private static final String kBaiduProductBaikeUrl =  "http://baike.baidu.com";
	private static final String kBaiduProductWenkuUrl =  "http://wenku.baidu.com";
	private static final String kBaiduProductHao123Url =  "http://ipad.hao123.com";
	
	private FrameLayout mNativeBaiduSearchPanel;
	private LinearLayout mNativeMoreAndVoicePanel;
	private ImageView mNativeSettingBtn;
	private TextView mNativeInputBox;
	private TextView mNativeMoreBtn;
	private TextView mNativeVoiceBtn;
	private Animation showAction ;
	private Animation hideAction ;
	
	private TextView xinwen;
	private TextView tieba;
	private TextView zhidao;
	private TextView yinyue;
	private TextView tupian;
	private TextView shipin;
	private TextView ditu;
	private TextView baike;
	private TextView wenku;
	private TextView hao123;
	
	private boolean isMoreShow = false;
	
	private Context mContext;
	
	public NativeSearchPanel(Context context) {
		super(context);
        LayoutInflater factory = LayoutInflater.from(context);
        factory.inflate(R.layout.native_baidu_search, this);
        mContext = context;
        
		mNativeBaiduSearchPanel = (FrameLayout)findViewById(R.id.native_baidu_search_panel);
		mNativeMoreAndVoicePanel = (LinearLayout)findViewById(R.id.native_more_and_voice_panel);
		
		mNativeSettingBtn = (ImageView)findViewById(R.id.native_setting_btn);
		mNativeMoreBtn =(TextView)findViewById(R.id.native_more_btn);
		mNativeInputBox = (TextView)findViewById(R.id.native_input_box);
		mNativeVoiceBtn = (TextView)findViewById(R.id.native_voice_btn);
		
		xinwen = (TextView)findViewById(R.id.xinwen);
		tieba = (TextView)findViewById(R.id.tieba);
		zhidao = (TextView)findViewById(R.id.zhidao);
		yinyue = (TextView)findViewById(R.id.yinyue);
		tupian = (TextView)findViewById(R.id.tupian);
		shipin = (TextView)findViewById(R.id.shipin);
		ditu = (TextView)findViewById(R.id.ditu);
		baike = (TextView)findViewById(R.id.baike);
		wenku = (TextView)findViewById(R.id.wenku);
		hao123 = (TextView)findViewById(R.id.hao123);
		
		xinwen.setOnClickListener(this);
		tieba.setOnClickListener(this);
		zhidao.setOnClickListener(this);
		yinyue.setOnClickListener(this);
		tupian.setOnClickListener(this);
		shipin.setOnClickListener(this);
		ditu.setOnClickListener(this);
		baike.setOnClickListener(this);
		wenku.setOnClickListener(this);
		hao123.setOnClickListener(this);
		
		mNativeBaiduSearchPanel.setOnClickListener(this);
		mNativeSettingBtn.setOnClickListener(this);
		mNativeMoreBtn.setOnClickListener(this);
		mNativeInputBox.setOnClickListener(this);
		mNativeVoiceBtn.setOnClickListener(this);
		showAction = AnimationUtils.loadAnimation(mContext, R.anim.popshow_anim);
		hideAction = AnimationUtils.loadAnimation(mContext, R.anim.pophidden_anim);
		showAction.setAnimationListener(this);
		hideAction.setAnimationListener(this);
	}
	
	@Override
	public void onClick(View v) {
		
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mNativeMoreAndVoicePanel.getLayoutParams();
		
		switch (v.getId()) {
		case R.id.native_voice_btn:
			
			Intent voiceIntent = new Intent(mContext, VoiceSearchActivity.class);
			voiceIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			voiceIntent.putExtra(VoiceSearchActivity.TAG_VOICE_START_FROM, VoiceSearchActivity.EXTRA_START_FROM_NATIVE_PANEL);
			mContext.startActivity(voiceIntent);
			break;
		case R.id.native_setting_btn:
			Intent intentSettings = new Intent(mContext, SettingsActivity.class);
			mContext.startActivity(intentSettings);
			break;
		case R.id.native_input_box:
			
			this.setVisibility(View.GONE);
	        Intent intent = new Intent(mContext, SearchActivity.class);
	        intent.putExtra(SearchActivity.TAG_IS_START_FROM_SEARCH, true);
	        mContext.startActivity(intent);
			break;
			
		case R.id.native_more_btn:
			if(isMoreShow) {
				mNativeMoreAndVoicePanel.startAnimation(hideAction);	
				params.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.native_baidu_search_margins_bottom));
				mNativeMoreAndVoicePanel.setLayoutParams(params);
			}
			else {
				mNativeMoreAndVoicePanel.startAnimation(showAction);
				params.setMargins(0, 0, 0, 0);
				mNativeMoreAndVoicePanel.setLayoutParams(params);

			}
			isMoreShow = !isMoreShow;
			break;
		case R.id.native_baidu_search_panel:
			if(isMoreShow) {
				mNativeMoreAndVoicePanel.startAnimation(hideAction);	
				params.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.native_baidu_search_margins_bottom));
				mNativeMoreAndVoicePanel.setLayoutParams(params);
				isMoreShow = false;
			}
			break;
		case R.id.xinwen:
			
			this.setVisibility(View.GONE);
			SearchManager.launchURL(mContext,kBaiduProductNewsUrl);
			break;
		case R.id.tieba:
			
			this.setVisibility(View.GONE);
			SearchManager.launchURL(mContext,kBaiduProductTiebaUrl);
			break;
		case R.id.zhidao:
			
			this.setVisibility(View.GONE);
			SearchManager.launchURL(mContext,kBaiduProductZhidaoUrl);
			break;
		case R.id.yinyue:
			
			this.setVisibility(View.GONE);
			SearchManager.launchURL(mContext,kBaiduProductMusicUrl);
			break;
		case R.id.tupian:
			
			this.setVisibility(View.GONE);
			SearchManager.launchURL(mContext,kBaiduProductPhotoUrl);
			break;
		case R.id.shipin:
			
			this.setVisibility(View.GONE);
			SearchManager.launchURL(mContext,kBaiduProductVideoUrl);
			break;
		case R.id.ditu:
			
			this.setVisibility(View.GONE);
			SearchManager.launchURL(mContext,kBaiduProductMapUrl);
			break;
		case R.id.baike:
			
			this.setVisibility(View.GONE);
			SearchManager.launchURL(mContext,kBaiduProductBaikeUrl);
			break;
		case R.id.wenku:
			
			this.setVisibility(View.GONE);
			SearchManager.launchURL(mContext,kBaiduProductWenkuUrl);
			break;
		case R.id.hao123:
			
			this.setVisibility(View.GONE);
			SearchManager.launchURL(mContext,kBaiduProductHao123Url);
			break;

		default:
			break;
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
	}

}
