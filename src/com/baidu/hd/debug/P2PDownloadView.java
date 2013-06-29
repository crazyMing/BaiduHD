package com.baidu.hd.debug;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.hd.module.P2PBlock;
import com.baidu.hd.module.Task;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.task.TaskManager;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;

/**
 * 下载监控控件
 */
public class P2PDownloadView extends LinearLayout {

	private P2PSectionStateView mSectionView = null;
	private P2PBlockStateView mBlockView = null;
	private SeekBar mSeekBar = null;
	
	private TextView mTxtSpeed = null;
	
	private Context mContext = null;
	private TaskManager mTaskManager = null;
	
	private boolean mStop = false;
	
	private boolean isDestroy = false;
	
	private Task mTask = null;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			refreshUI((P2PShowBlock)msg.obj);
		}
	};
    
    //这里的构造一定是两个参数。
    public P2PDownloadView(final Context ctxt, AttributeSet attrs) {
        super(ctxt,attrs);
    }

	protected void onFinishInflate() {
		
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.debug_p2p_block_view, this);
        
        this.mSectionView = (P2PSectionStateView)this.findViewById(R.id.view_section);
        this.mBlockView = (P2PBlockStateView)this.findViewById(R.id.view_block);
        this.mTxtSpeed = (TextView)this.findViewById(R.id.txt_speed);
        
        Button btnStart = (Button)this.findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				start();
			}
		});
        
        Button btnStop = (Button)this.findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				stop();
			}
		});

        this.mSeekBar = (SeekBar)this.findViewById(R.id.seekbar);
        this.mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mBlockView.setIndex(seekBar.getProgress());
				mBlockView.invalidate();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}
		});        
        this.startDetect();
    }
	
	public void setContext(Context context) {
		this.mContext = context;
	}
    
    public void setServiceFactory(ServiceFactory factory) {
    	this.mTaskManager = (TaskManager)factory.getServiceProvider(TaskManager.class);
    }
    
    public void setTask(Task task) {
    	this.mTask = task;
    }
    
    public void onDestroy() {
    	isDestroy = true;
    	this.stop();
    }
    
    private void startDetect() {
    	
    	new Handler() {

			@Override
			public void handleMessage(Message msg) {
				if(isDestroy) {
					return;
				}
				if(mTask != null) {
					List<String> keys = new ArrayList<String>();
					keys.add(mTask.getKey());
					List<Task> tasks = mTaskManager.multiQuery(keys);
					if(!tasks.isEmpty()) {
						mTask = tasks.get(0);
						mHandler.sendEmptyMessage(0);
					}
				}
				this.sendEmptyMessageDelayed(0, 1000);
			}
    		
    	}.sendEmptyMessageDelayed(0, 1000);
    }

	private void start() {
		
		if(this.mTask == null) {
    		Toast.makeText(this.mContext, "null task", Toast.LENGTH_LONG).show();
    		return;
		}

		HandlerThread thread = new HandlerThread("DetectThread");
		thread.start();
		
		new Handler(thread.getLooper()) {

			@Override
			public void handleMessage(Message msg) {
				
				P2PBlock block = mTaskManager.getBlock(mTask);
				P2PShowBlock showBlock = new P2PShowBlock();
				showBlock.setBlock(mTask.getTotalSize(), block);
				mHandler.sendMessage(mHandler.obtainMessage(0, showBlock));
				
				if(mStop) {
					this.getLooper().quit();
					mStop = false;
				} else {
					this.sendEmptyMessageDelayed(0, 1000);
				}
			}
		}.sendEmptyMessageDelayed(0, 1000);
	}
	
	private void stop() {
		this.mStop = true;
	}

	private void refreshUI(P2PShowBlock block) {
		
		this.mTxtSpeed.setText(StringUtil.formatSpeed(this.mTask.getSpeed()));

		if(block != null) {
			this.mSeekBar.setMax(block.getSectionCount() - 1);
			
			this.mSectionView.setBlock(block);
			this.mSectionView.invalidate();
	
			this.mBlockView.setBlock(block);
			this.mBlockView.invalidate();
		}
	}
}
