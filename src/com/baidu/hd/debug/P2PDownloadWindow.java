package com.baidu.hd.debug;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.baidu.hd.module.Task;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.R;

public class P2PDownloadWindow {

	private PopupWindow mWindow = null;
	private View mContentView = null;
	
	private Context mContext = null;
	private View mParent = null;
	
	private ServiceFactory mFactory = null;

	public P2PDownloadWindow(Context context, View parent, ServiceFactory factory) {
		this.mContext = context;
		this.mParent = parent;
		this.mFactory = factory;
	}
	
	public void show(Task task) {
		
		if(task == null) {
			Toast.makeText(this.mContext, "null task", Toast.LENGTH_LONG).show();
			return;
		}
		this.createUI(task);
	}
	
	public void dismiss() {
		if(this.mWindow != null) {
			this.mWindow.dismiss();
			this.mWindow = null;
		}
	}	

	private void createUI(Task task) {
		
		LayoutInflater inflater = LayoutInflater.from(this.mContext);
		this.mContentView = inflater.inflate(R.layout.debug_p2p_block, null);
		
		P2PDownloadView downloadView = (P2PDownloadView)this.mContentView.findViewById(R.id.view_download);
		downloadView.setServiceFactory(this.mFactory);
		downloadView.setTask(task);
		
		this.mWindow = new PopupWindow(this.mContentView, 400, 500);
		this.mWindow.setTouchable(true);
		this.mWindow.setFocusable(true);
		this.mWindow.showAtLocation(this.mParent, Gravity.CENTER, 0, 0);
		
		Button btnClose = (Button)this.mContentView.findViewById(R.id.btn_close);
		btnClose.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
}
