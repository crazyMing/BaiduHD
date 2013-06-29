package com.baidu.hd.player;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.baidu.hd.adapter.PlayerDownLoadListAdapter;
import com.baidu.hd.adapter.PlayerPlayListAdapter;
import com.baidu.hd.stat.StatId.PlCtr;
import com.baidu.hd.util.Turple;
import com.baidu.hd.R;

class PlayListView {

	private PlayerAccessor mAccessor = null;
	private LinearLayout linearLayout = null;
	private CallBack callBack = null;
	private boolean downloadMode = false;

	interface CallBack {
		void onShowCtrl();
	}

	public void setCallBack(CallBack callBack) {
		this.callBack = callBack;
	}

	/**
	 * 弹出窗口
	 */
	private RelativeLayout mWindow = null;

	private PlayerPlayListAdapter mAdapter = null;

	PlayListView(PlayerAccessor accessor) {
		this.mAccessor = accessor;
		this.mAdapter = new PlayerPlayListAdapter(this.mAccessor.getHost());
	}

	void destroy() {
		mAccessor.stopHideRightBar();
		if (this.mWindow != null) {
			this.mWindow.setVisibility(View.GONE);
			this.mWindow = null;
		}
	}

	boolean isShow() {
		return this.mWindow != null;
	}

	void show(boolean isDownLoad) {
		//mAccessor.stopHideRightBar();
		if(isShow()){
			destroy();
		}
		this.downloadMode = isDownLoad;

		LayoutInflater mInflater = LayoutInflater
				.from(this.mAccessor.getHost());
		View view = mInflater.inflate(R.layout.player_playlist, null);
		linearLayout = (LinearLayout) view
				.findViewById(R.id.palyer_playlist_layout);
		TextView errorBtn=(TextView)view.findViewById(R.id.error);
		errorBtn.setVisibility(downloadMode?View.GONE:View.GONE);//always gone~
		
		Turple<Integer, Integer> screenSize = mAccessor.getScreenSize();

		this.mWindow = ((RelativeLayout) this.mAccessor.getHost().findViewById(
				R.id.rightpanel));
		LayoutParams lp = (LayoutParams) mWindow.getLayoutParams();
		lp.width = (int) (186 * mAccessor.getHost().getResources()
				.getDisplayMetrics().density);
		lp.height = (int) screenSize.getY();
		mWindow.setLayoutParams(lp);

		linearLayout.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (event.getAction() == KeyEvent.ACTION_UP
						&& keyCode == KeyEvent.KEYCODE_BACK) {
					destroy();
				}
				return false;
			}
		});

		ListView listview = (ListView) view.findViewById(R.id.playlist);
		
		//非下载模式
		if (!downloadMode) {
			if (this.mAccessor.getAlbum() != null) {
				this.mAdapter.fillList(this.mAccessor.getAlbum());
			}

			if (mAccessor != null) {
				this.mAdapter.getCurrentVideo(this.mAccessor.getVideo());
			}
			
			TextView tv = new TextView(this.mAccessor.getHost());
			tv.setHeight((int) (56 * this.mAccessor.getHost().getResources()
					.getDisplayMetrics().density));
			//listview.addFooterView(tv);
			listview.setAdapter(this.mAdapter);
			// 指定位置显示ListItem
			int position = 0;
			for (int i = 0; i < mAccessor.getAlbum().getVideos().size(); ++i) {
				if (mAccessor.getAlbum().getVideos().get(i).getRefer()
						.equals(this.mAccessor.getVideo().toNet().getRefer())) {
					break;
				} else {
					position++;
				}
			}
			listview.setSelection(position);
			final int currentPos=position;
			listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					if(arg2==currentPos){
						return;
					}
					mAccessor.startHideControl();
					if (mAccessor.getAlbum() != null) {
						mAccessor.playMedia(mAccessor.getAlbum().getVideos()
								.get(arg2));
						((PlayerActivity)mAccessor.getHost()).getStat().incEventCount(PlCtr.Name, PlCtr.Btn_list);

					}
				}
			});
		}
		//下载模式
		else{
			PlayerDownLoadListAdapter adapter=new PlayerDownLoadListAdapter(mAccessor.getHost(),mAccessor.getAlbum().getRefer(),mAccessor);
			if(mAccessor.getAlbum()!=null){
				adapter.fillList(mAccessor.getAlbum());
			}
			TextView tv = new TextView(this.mAccessor.getHost());
			tv.setHeight((int) (56 * this.mAccessor.getHost().getResources()
					.getDisplayMetrics().density));
			//listview.addFooterView(tv);
			listview.setAdapter(adapter);
			int position = 0;
			for (int i = 0; i < mAccessor.getAlbum().getVideos().size(); ++i) {
				if (mAccessor.getAlbum().getVideos().get(i).getRefer()
						.equals(this.mAccessor.getVideo().toNet().getRefer())) {
					break;
				} else {
					position++;
				}
			}
			listview.setSelection(position);
		}
		mWindow.removeAllViews();
		mWindow.addView(view);
		mWindow.setVisibility(View.VISIBLE);
		mAccessor.startHideRightBar();
	}

	public boolean isDownloadMode() {
		return downloadMode;
	}

	public void setDownloadMode(boolean downloadMode) {
		this.downloadMode = downloadMode;
	}
}
