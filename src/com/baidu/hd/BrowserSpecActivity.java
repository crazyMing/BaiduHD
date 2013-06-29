package com.baidu.hd;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.browser.db.HistoryConfig;
import com.baidu.hd.image.ImageManager;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.Image;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.player.PlayerLauncher;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId.BrowPlay;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;

/**
 * @ClassName: BrowserSpecActivity 
 * @Description: 浏览器模式特型页
 * @author LEIKANG 
 * @date 2012-12-25 下午3:04:26
 */
public class BrowserSpecActivity extends BaseActivity implements
		View.OnClickListener {
	
	private Logger logger = new Logger(BrowserSpecActivity.class.getSimpleName());

	private TextView mTextTitle = null;
	private TextView mTextBack = null;

	private String mAlbumName = "";
	private TextView mTextName = null;
	private TextView mTextPlayInfo = null;
	private TextView mTextResource = null;
	private ImageView mBrowSpecAlbumIamge;

	private Button mBtnDownload = null;
	private Button mBtnPlay = null;
	private Button mBtnStow = null;

	private Album mAlbum;
	private int mCurrentVideo;

	private LinearLayout mLinearLayout = null;
	
	private PlayListManager mPlayListManager;
	
	private Context context;
	
	private boolean startFromPersonal = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser_spec_layout);
		mPlayListManager = (PlayListManager)getServiceProvider(PlayListManager.class);
		context = this;
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	// 初始化
	private void init() {

		mLinearLayout = (LinearLayout) findViewById(R.id.brow_sepc_list);
		mTextTitle = (TextView) findViewById(R.id.brow_spec_title);
		mTextBack = (TextView) findViewById(R.id.float_GoBackBtn);
		mBtnDownload = (Button) findViewById(R.id.brow_spec_download);
		mBtnPlay = (Button) findViewById(R.id.brow_spec_play);
		mBtnStow = (Button) findViewById(R.id.brow_spec_stow);

		mTextName = (TextView) findViewById(R.id.brow_spec_name);
		mTextPlayInfo = (TextView) findViewById(R.id.brow_sepc_can_play_info);
		mTextResource = (TextView) findViewById(R.id.brow_spec_resource);
		
		mBrowSpecAlbumIamge = (ImageView) findViewById(R.id.brow_spec_album_iamge);


		mBtnDownload.setOnClickListener(this);
		mBtnPlay.setOnClickListener(this);
		mBtnStow.setOnClickListener(this);

		mTextTitle.setVisibility(View.VISIBLE);
		mTextTitle.setText(getResources().getString(R.string.brow_spec_title)
				.toString());
		mTextBack.setOnClickListener(this);
		
		String currentVideo = getIntent().getStringExtra(Const.IntentExtraKey.BrowSpecIndex);
		if (currentVideo == null || "".equals(currentVideo)) {
			startFromPersonal = false;
			currentVideo = "1";
		}
		
		long albumId = getIntent().getLongExtra(Const.IntentExtraKey.BrowSpecAlbumId,0);
		
		mAlbum = mPlayListManager.findAlbumById(albumId);
		
		
		mAlbumName = mAlbum.getListName();
		
		if (StringUtil.isEmpty(mAlbumName))
			mAlbumName = getIntent().getStringExtra(Const.IntentExtraKey.BrowSpecName);
		
		String lastPlay = mAlbum.getCurrent().getEpisode();
		if (lastPlay == null || "".equals(lastPlay)) {
			currentVideo = "1";
		}
		else {
			currentVideo = lastPlay;
		}
		
		mCurrentVideo = Integer.parseInt(currentVideo);
			
		Drawable drawable =  ((ImageManager)getServiceProvider(ImageManager.class)).request(mAlbum.getImage(), Image.Type.Album);
		mBrowSpecAlbumIamge.setImageDrawable(drawable);
		
		drawList();

	}

	/*
	 * 画可播放剧集
	 * @param url 嗅探结果
	 */
	private void drawList() {
		mLinearLayout.removeAllViews();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 70);
		lp.setMargins(8, 10, 8, 5);
		
		final List<NetVideo> videos = mPlayListManager.getNetVideos(mAlbum.getId(),null,null);
		logger.d(" getNetVideosByAlbumRefer = " + videos.toString());
		
			for (int i=0;i<videos.size();i++) {
				
				Button btn = new Button(this);
				btn.setTextColor(getResources().getColor(
						R.color.text_color_gray_black));
				btn.setText(videos.get(i).getName());
				btn.setTextSize(17);
				btn.setBackgroundResource(R.drawable.brow_spec_album_list);
				btn.setSingleLine(true);
				btn.setLayoutParams(lp);
				btn.setTag(i);
				
				mLinearLayout.addView(btn);
				
				final NetVideo video = videos.get(i);
				
				video.setEpisode(String.valueOf(i+1));
				video.setType(mAlbum.getType());
				
				btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						
						mCurrentVideo = (Integer)v.getTag() + 1;
						mAlbum.setCurrent(video);
						if (!startFromPersonal)
							mAlbum.setHomeShow(!HistoryConfig.isPrivateMode(BrowserSpecActivity.this));
						PlayerLauncher.startup(BrowserSpecActivity.this, mAlbum, video);
						
						
						getStat().incEventCount(BrowPlay.Name, BrowPlay.ListIn);
						getStat().incEventCount(BrowPlay.Name, BrowPlay.Plays);
					}
				});
		}
		mTextName.setText(mAlbumName);
		
		String resourceFormat = getResources().getString(
				R.string.brow_spec_resource_text);
		String countFormat = getResources().getString(
				R.string.brow_spec_can_play_info_text);
		
		String fullResource = String.format(resourceFormat,mAlbum.getSite());
		
		String fullCount = String.format(countFormat, videos.size());
		mTextPlayInfo.setText(fullCount);
		mTextResource.setText(fullResource);
		refreshUI();
	}
	
	// 更新UI
	private void refreshUI() {
		
		    Album album = mPlayListManager.findAlbumById(mAlbum.getId());
		    if (album != null) {
		    	mAlbum = album;
		    }
		    
			if (mAlbum != null && mAlbum.isFavorite()) {
				mBtnStow.setText(R.string.brow_spec_cancel_text);
				getStat().incEventCount(BrowPlay.Name, BrowPlay.StowCa);
			} else {
				mBtnStow.setText(R.string.brow_spec_stow_text);
				getStat().incEventCount(BrowPlay.Name, BrowPlay.Stow);
			}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.float_GoBackBtn:
			finish();
			break;
		case R.id.brow_spec_download:
			Intent intent = new Intent(BrowserSpecActivity.this,BrowSpecSelectActivity.class);
			
			intent.putExtra(Const.IntentExtraKey.BrowSpecAlbumId,mAlbum.getId());
			startActivity(intent);
 
			getStat().incEventCount(BrowPlay.Name, BrowPlay.Down);
			break;

		case R.id.brow_spec_play:
			List<NetVideo> lists = mPlayListManager.getNetVideos(mAlbum.getId(),null,null);
			NetVideo video = lists.get(mCurrentVideo-1);
			video.setEpisode(String.valueOf(mCurrentVideo));
			mAlbum.setCurrent(video);
			if (!startFromPersonal)
				mAlbum.setHomeShow(!HistoryConfig.isPrivateMode(BrowserSpecActivity.this));
			
			PlayerLauncher.startup(this, mAlbum, video);
			getStat().incEventCount(BrowPlay.Name, BrowPlay.Play);
			break;

		case R.id.brow_spec_stow:
			mPlayListManager.setFavorite(mAlbum);
			refreshUI();
			break;

		default:
			break;
		}
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	private Stat getStat() {
		return (Stat) getServiceProvider(Stat.class);
	}
}