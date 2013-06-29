package com.baidu.hd;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.hd.ctrl.PopupDialog;
import com.baidu.hd.ctrl.PopupDialog.ReturnType;
import com.baidu.hd.detect.FilterCallback.DetectPromptReturn;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.TaskFactory;
import com.baidu.hd.module.Task.Type;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.personal.SDCardUtil;
import com.baidu.hd.player.PlayerLauncher;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId;
import com.baidu.hd.stat.StatId.BrowPlay;
import com.baidu.hd.task.TaskManager;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.PlayerTools;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.ToastUtil;
import com.baidu.hd.R;

/**
 * @ClassName: BrowSpecSelectActivity 
 * @Description: 浏览器模式下载选择页
 * @author LEIKANG 
 * @date 2012-12-25 下午3:04:53
 */
public class BrowSpecSelectActivity extends BaseActivity implements OnClickListener{
	
	private TextView mTextTitle = null;
	private TextView mTextBack = null;
	private RelativeLayout mRelativeLayout = null;  // 视频模式Title
	
	private ProgressBar mSDCardRatioPb = null;
	private TextView mTextSDCardLeft = null;
	private TextView mTextSDCardUse = null;
	private Button mBtnDownload = null;
	
	private LinearLayout mListLayout = null;
	
	private List<Integer> mDownList = null;	
	
	private long mAlbumId = 0;
	
	private List<NetVideo> videos;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser_spec_selected_layout);
		init();
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		mDownList.clear();
		
		mAlbumId = getIntent().getLongExtra(Const.IntentExtraKey.BrowSpecAlbumId,0);
		videos = ((PlayListManager)getServiceProvider(PlayListManager.class)).getNetVideos(mAlbumId,null,null);
		drawList();
		refreshUI();
	}

	private void init() {
		
		mDownList = new ArrayList<Integer>();
		
		mTextTitle = (TextView)findViewById(R.id.brow_spec_title);
		mTextBack = (TextView)findViewById(R.id.float_GoBackBtn);
		mRelativeLayout = (RelativeLayout)findViewById(R.id.title2);
		
		mSDCardRatioPb = (ProgressBar)findViewById(R.id.brow_spec_select_sdcardpb);
		mTextSDCardUse = (TextView)findViewById(R.id.brow_spec_select_sdcard_use);
		mTextSDCardLeft = (TextView)findViewById(R.id.brow_spec_select_sdcard_left);
		mBtnDownload = (Button)findViewById(R.id.brow_spec_select_go_down);
		
		mListLayout = (LinearLayout)findViewById(R.id.brow_spec_select_list_layout);
		
		mRelativeLayout.setVisibility(View.GONE);
		mTextTitle.setVisibility(View.VISIBLE);
		mTextTitle.setText(R.string.brow_spec_select_title);
		mTextBack.setOnClickListener(this);
		
		mBtnDownload.setOnClickListener(this);
		
		setSDCardInfo();
	}
	
	private void drawList() {
		mListLayout.removeAllViews();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 70);
		lp.setMargins(8, 10, 8, 5);
			for (int i=0;i<videos.size();i++) {
				final int tempIndex = i;
				Button btn = new Button(this);
				btn.setText(videos.get(i).getName());
				btn.setBackgroundResource(R.drawable.brow_spec_download_list);
				btn.setSingleLine(true);
				btn.setLayoutParams(lp);
				btn.setSelected(false);
				btn.setTextColor(getResources().getColor(R.color.text_color_gray_black));
				btn.setTextSize(17);
				
				TaskManager taskManager = (TaskManager)getServiceProvider(TaskManager.class);
				Task task = createTask(videos.get(i));
				
				if (taskManager.find(task.getKey()) != null) {
					btn.setSelected(true);
				}
				else {
					btn.setSelected(false);
				}
				
				mListLayout.addView(btn);
				
				final NetVideo  video = videos.get(i);
				btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (((Button)v).isSelected()) {
							TaskManager taskManager = (TaskManager)getServiceProvider(TaskManager.class);
							Task task = createTask(video);
							if (taskManager.find(task.getKey()) != null) {
								ToastUtil.showMessage(BrowSpecSelectActivity.this, getResources().getString(R.string.brow_spec_select_task_exist), Toast.LENGTH_SHORT);
							}
							else {
								((Button)v).setSelected(false);
								mDownList.remove((Integer)tempIndex);
							}
							refreshUI();
						}
						else {
							((Button)v).setSelected(true);
							mDownList.add((Integer)tempIndex);
							refreshUI();
						}
					}
				});
		}
	}
	
	private Task createTask(NetVideo video) {
		
		final String url = video.getUrl();
		final String refer = video.getRefer();
		String name = video.getName();
		
		PlayListManager playListManager = ((PlayListManager)getServiceProvider(PlayListManager.class));
		
		Task task = TaskFactory.create(StringUtil.isSmallUrl(url)?Task.Type.Small:Type.Big);
		Album album = playListManager.findAlbumById(mAlbumId);
		
		if (album.asBigServerAlbum()!=null) {
			name = album.getListName() + "-" + name;
		}
		task.setName(name);
		playListManager.setDownload(album, true);
		task.setAlbumId(album.getId());
		task.setUrl(url);
		task.setRefer(refer);
		task.setVideoType(StringUtil.isSmallUrl(url)?NetVideo.NetVideoType.P2P_STREAM:NetVideo.NetVideoType.BIGSITE);
		return task;
	}
	
	// 设置SD卡相关信息
	private void setSDCardInfo() {
		
		double availableSize = SDCardUtil.getInstance().getAvailableSize();
		double totoalSize = SDCardUtil.getInstance().getAllSize();
		
		if (availableSize != -1 && totoalSize != -1) {
			mSDCardRatioPb.setProgress((int)((totoalSize-availableSize)/totoalSize*100));
			String text1 = String.format(getResources().getString(R.string.brow_spec_select_sdcard_use), totoalSize - availableSize);
			String text2 = String.format(getResources().getString(R.string.brow_spec_select_sdcard_left), availableSize);
			mTextSDCardUse.setText(text1);
			mTextSDCardLeft.setText(text2);
		}
		else {
			ToastUtil.showMessage(this, getResources().getString(R.string.brow_spec_select_sdcard_no_tip), Toast.LENGTH_SHORT);
			mSDCardRatioPb.setProgress(0);
			mTextSDCardLeft.setText(R.string.brow_spec_select_sdcard_no);
			mTextSDCardUse.setText(R.string.brow_spec_select_sdcard_no);
		}
	}
	
	
	private void startDownload() {
		for (Integer elem : mDownList) {
			Task task = createTask(videos.get(elem));
			TaskManager taskManager = (TaskManager)getServiceProvider(TaskManager.class);
			if (taskManager.find(task.getKey()) == null) {
				taskManager.start(task);
				ToastUtil.showMessage(this, getResources().getString(R.string.download_tip), Toast.LENGTH_SHORT);
				getStat().incEventCount(BrowPlay.Name, BrowPlay.Downs);
				// 添加下载次数统计
				getStat().incEventCount(StatId.Download.Name, StatId.Download.Count);
					getStat().incEventCount(StatId.Download.Name, StatId.Download.Count + task.getFormatVideoType());
				
				// 添加有效使用
				getStat().incEventAppValid();
			}
		}
	}
	
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			startDownload();
		}
	};
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.float_GoBackBtn:
			finish();
			break;
			
		case R.id.brow_spec_select_go_down:
			
			if (mDownList.size() == 0) {
				ToastUtil.showMessage(this, getResources().getString(R.string.brow_spec_select_no_item_download), Toast.LENGTH_SHORT);
			} else if (PlayerTools.isNetworkConnected(this,PlayerTools.NetworkType.NETWORK_TYPE_GPRS) || PlayerTools.isNetworkConnected(this,PlayerTools.NetworkType.NETWORK_TYPE_3G)) { 
				PopupDialog dialog = new PopupDialog(this, new PopupDialog.Callback() {
					public void onReturn(ReturnType type, boolean checked) {
						if (type == PopupDialog.ReturnType.OK)
							handler.sendEmptyMessage(0);
					}
				});
				dialog.setTitle(dialog.createText(R.string.exit_dialog_title))
				.setMessage(dialog.createText(R.string.dialog_3g_message))
				.setPositiveButton(dialog.createText(R.string.ok))
				.setNegativeButton(dialog.createText(R.string.cancel))
				.show();
			} else
				handler.sendEmptyMessage(0);

			break;

		default:
			break;
		}
	}
	
	private void refreshUI() {
		if (mDownList.size() != 0) {
			mBtnDownload.setEnabled(true);
			mBtnDownload.setTextColor(getResources().getColor(R.color.text_color_gray_black));
		}
		else {
			mBtnDownload.setEnabled(false);
			mBtnDownload.setTextColor(getResources().getColor(R.color.text_color_gray_black));
		}
	}
	
	private Stat getStat() {
		return (Stat)getServiceProvider(Stat.class);
	}
}
