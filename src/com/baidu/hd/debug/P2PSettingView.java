package com.baidu.hd.debug;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.R;
import com.baidu.player.download.DownloadServiceAdapter;

public class P2PSettingView extends LinearLayout {
	
	private ServiceFactory mServiceFactory = null;

    public P2PSettingView(final Context ctxt, AttributeSet attrs) {
        super(ctxt,attrs);
    }
    
    public void setServiceFactory(ServiceFactory serviceFactory) {
    	mServiceFactory = serviceFactory;
    }

	protected void onFinishInflate() {
		
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.debug_p2p_setting, this);

		Button btnSetLogLevel = (Button)findViewById(R.id.btn_task_set_log_level);
		btnSetLogLevel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String info = ((EditText)findViewById(R.id.txt_task_info)).getText().toString();
				DownloadServiceAdapter adapter = (DownloadServiceAdapter)mServiceFactory.getServiceProvider(DownloadServiceAdapter.class);
				try {
					adapter.setLogLevel(Integer.parseInt(info));
				} catch(NumberFormatException e) {
					e.printStackTrace();
				}
			}
		});
		
		Button btnLimitSpeed = (Button)findViewById(R.id.btn_task_limit_speed);
		btnLimitSpeed.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String info = ((EditText)findViewById(R.id.txt_task_info)).getText().toString();
				DownloadServiceAdapter adapter = (DownloadServiceAdapter)mServiceFactory.getServiceProvider(DownloadServiceAdapter.class);
				try {
					adapter.setSpeedLimit(Integer.parseInt(info));
				} catch(NumberFormatException e) {
					e.printStackTrace();
				}
			}
		});
		
    }
	
}
