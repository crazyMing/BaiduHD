package com.baidu.hd.player;


import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.task.TaskManager;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.UrlUtil;
import com.baidu.hd.R;
import com.baidu.video.CyberPlayer;


public class CacheErrorView {
	
	public interface CallBack {
		void onRetry();
		void onBack();
	}
	
	private PlayerAccessor mAccessor;
	private TextView mInfo;
	private TextView mName;
	private TextView mOrignal;
	private LinearLayout layoutCache;
	private LinearLayout layoutError;
	private LinearLayout layoutMain;
	private RelativeLayout layoutParent;
	private TextView txtPromt;
	private boolean isOnCache = true;
	
	private TaskManager mTaskManager = null;
	private int mPercent;
	private int mSpeed;
	private int mErrorCode=-1;
	private CallBack callBack = null;
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			
			if(mAccessor.getTask() == null) {
				return;
			}
			List<String> keys = new ArrayList<String>();
			keys.add(mAccessor.getTask().getKey());
			List<Task> tasks = mTaskManager.multiQuery(keys);
			if(tasks.size() == 0) {
				this.sendEmptyMessageDelayed(0, Const.Elapse.TaskRefresh);
				return;
			}
			mSpeed = tasks.get(0).getSpeed();
			setCacheText();
			this.sendEmptyMessageDelayed(0, Const.Elapse.TaskRefresh);
		}
	};
	
	public void setCallBack(CallBack callBack) {
		this.callBack = callBack;
	}
	
	public CacheErrorView(PlayerAccessor accessor) {
		this.mAccessor = accessor;
		BaiduHD app = (BaiduHD)this.mAccessor.getHost().getApplication();
		this.mTaskManager = (TaskManager)app.getServiceFactory().getServiceProvider(TaskManager.class);
		
		mInfo = (TextView)this.mAccessor.getHost().findViewById(R.id.chcheview_text_info);
		mName = (TextView)this.mAccessor.getHost().findViewById(R.id.chcheview_text_name);
		mOrignal = (TextView)this.mAccessor.getHost().findViewById(R.id.chcheview_text_orignal);
		layoutCache = (LinearLayout)this.mAccessor.getHost().findViewById(R.id.player_cache);
		layoutError = (LinearLayout)this.mAccessor.getHost().findViewById(R.id.player_error);
		layoutMain = (LinearLayout)this.mAccessor.getHost().findViewById(R.id.palyer_cache_error);
		layoutParent = (RelativeLayout)this.mAccessor.getHost().findViewById(R.id.player_main);
		txtPromt = (TextView)this.mAccessor.getHost().findViewById(R.id.error_text_info);
		mPercent = 0;
		mSpeed = 0;

		// 返回事件响应
		ImageView btnBack = (ImageView)this.mAccessor.getHost().findViewById(R.id.chcheview_back_btn);
		btnBack.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				callBack.onBack();
				hide();
			}
		});
		
		// 重试事件响应
		Button btnRetry = (Button)this.mAccessor.getHost().findViewById(R.id.error_btn_retry);
		btnRetry.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				callBack.onRetry();
			}
		});
	}
	
	public void hide() {
		handler.removeMessages(0);
		layoutParent.setVisibility(View.VISIBLE);
		layoutMain.setVisibility(View.GONE);
		mPercent = 0;
		mSpeed = 0;
		isOnCache = false;
	}
	
	/** 进入 */
	public void showPrepare() {
		setmErrorCode(-1);
		layoutParent.setVisibility(View.GONE);
		layoutMain.setVisibility(View.VISIBLE);
		layoutCache.setVisibility(View.VISIBLE);
		layoutError.setVisibility(View.GONE);

		if (mAccessor.getVideo().isLocal()) {
			setLocalView();
		} else {
			this.mName.setText(this.mAccessor.getVideo().getName());
			NetVideo netVideo = (NetVideo) this.mAccessor.getVideo();
			if (null != netVideo) {
				this.mOrignal.setText(UrlUtil.getHost(netVideo
						.getRefer()));
			}
		}
	}
	
	/** 大站视频的嗅探 */
	public void showParse() {
		setmErrorCode(-1);
		layoutParent.setVisibility(View.GONE);
		layoutMain.setVisibility(View.VISIBLE);
		layoutCache.setVisibility(View.VISIBLE);
		layoutError.setVisibility(View.GONE);
		this.mInfo.setText(R.string.player_parse);
	}
	
	public void showParse(NetVideo netVideo) {
		setmErrorCode(-1);
		layoutParent.setVisibility(View.GONE);
		layoutMain.setVisibility(View.VISIBLE);
		layoutCache.setVisibility(View.VISIBLE);
		layoutError.setVisibility(View.GONE);
		this.mInfo.setText(R.string.player_parse);
		this.mName.setText(netVideo.getName());
	}
	/** 网络视频的缓冲 */
	public void showCache() {
		this.mPercent = 0;
		this.mSpeed = 0;
		isOnCache = true;
		this.handler.sendEmptyMessage(0);
		setCacheText();
		setmErrorCode(-1);
	}
	
	/** 错误 */
	public void showError(int errorCode) {
		layoutParent.setVisibility(View.GONE);
		layoutMain.setVisibility(View.VISIBLE);
		layoutError.setVisibility(View.VISIBLE);
		layoutCache.setVisibility(View.GONE);
		setmErrorCode(errorCode);
		switch (errorCode) {
		case ErrorCode.PlayerCoreBase + CyberPlayer.ERROR_NO_SUPPORTED_CODEC:
			txtPromt.setText(R.string.player_error_no_supported_codec);
			break;
		
		case ErrorCode.InvalidPath:
			txtPromt.setText(R.string.player_error_invalid_path);
			break;
		
		case ErrorCode.SDCardNotUseable:
			txtPromt.setText(R.string.player_error_sdcard);
			break;
		
		case ErrorCode.SnifferFail:
			txtPromt.setText(R.string.player_error_sniffer_fail);
			break;
		
		case ErrorCode.NetNotUseable:
			txtPromt.setText(R.string.player_error_net);
		break;
		
		default:
			txtPromt.setText(R.string.player_error_other);
			break;
		}
	}
	
	public void updateName() {
		this.mName.setText(this.mAccessor.getVideo().getName());
	}

	public void onCache(int percent) {
		if (isOnCache) {
			setmErrorCode(-1);
			this.mPercent = percent;
			setCacheText();
		}
	}

	private void setCacheText() {
		String format = this.mAccessor.getHost().getString(R.string.player_caching);
		String text = String.format(format, this.mPercent);
		
		if(this.mAccessor.getTask() != null) {
			format = this.mAccessor.getHost().getString(R.string.player_speed);
			String speed = String.format(format, StringUtil.formatSpeed(this.mSpeed));
			text += " " + speed;
		}
		this.mInfo.setText(text);
	}
	
	private void setLocalView() {
		this.mInfo.setVisibility(View.INVISIBLE);
		this.mName.setVisibility(View.INVISIBLE);
		this.mOrignal.setVisibility(View.INVISIBLE);
	}

	public int getmErrorCode() {
		return mErrorCode;
	}

	public void setmErrorCode(int mErrorCode) {
		this.mErrorCode = mErrorCode;
	}
}
