package com.baidu.hd.player;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.module.Task;
import com.baidu.hd.task.TaskManager;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;

class CacheView {

	private PlayerAccessor mAccessor = null;
	
	private ProgressBar mPrepareStatus = null;
	private TextView mProgressHint = null;

	private boolean isCaching = false;
	
	private int mPercent = 0;
	private int mSpeed = 0;
	
	private TaskManager mTaskManager = null;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			
			if(mAccessor.getTask() == null) {
				return;
			}
			List<String> keys = new ArrayList<String>();
			keys.add(mAccessor.getTask().getKey());
			List<Task> tasks = mTaskManager.multiQuery(keys);
			if(tasks.size() == 0) {
				this.sendEmptyMessageDelayed(0, 1000);
				return;
			}
			mSpeed = tasks.get(0).getSpeed();
			setHint();
			this.sendEmptyMessageDelayed(0, 1000);
		}
	};
	
	public CacheView(PlayerAccessor accessor) {
		this.mAccessor = accessor;
		
		this.mPrepareStatus = (ProgressBar) this.mAccessor.getHost().findViewById(R.id.showprepare);
		this.mProgressHint = (TextView) this.mAccessor.getHost().findViewById(R.id.cachehint);
		
		BaiduHD app = (BaiduHD)this.mAccessor.getHost().getApplication();
		this.mTaskManager = (TaskManager)app.getServiceFactory().getServiceProvider(TaskManager.class);
	}
	
	public void onPrepare() {
		if (!this.isCaching) {
			this.goneAll();
		}
	}
	
	public void onCache(int percent) {
		if (percent == 100) {
			this.isCaching = false;
			this.goneAll();
		} else {
			this.isCaching = true;
			this.mPercent = percent;
			this.showAll();
		}
	}
	
	public void create() {
		this.mPercent = 0;
		this.mSpeed = 0;
		this.showAll();
	}
	
	public void destroy() {
		this.goneAll();
		this.isCaching = false;
		this.mPercent = 0;
		this.mSpeed = 0;
	}
	
	private void showAll() {
		this.mPrepareStatus.setVisibility(View.VISIBLE);
		
		if(this.mAccessor.getVideo().isLocal()) {
			return;
		}
		if(this.mAccessor.getTask() != null) {
			this.mHandler.sendEmptyMessage(0);
		}
		this.mProgressHint.setVisibility(View.VISIBLE);
		this.setHint();
	}
	
	private void goneAll() {
		this.mHandler.removeMessages(0);
		this.mPrepareStatus.setVisibility(View.GONE);
		this.mProgressHint.setVisibility(View.GONE);
	}
	
	private void setHint() {
		String format = this.mAccessor.getHost().getString(R.string.player_caching);
		String text = String.format(format, this.mPercent);
		
		if(this.mAccessor.getTask() != null) {
			format = this.mAccessor.getHost().getString(R.string.player_speed);
			String speed = String.format(format, StringUtil.formatSpeed(this.mSpeed));
			text += " " + speed;
		}
		mProgressHint.setText(text);
	}
}
