package com.baidu.hd.task;

import com.baidu.hd.log.Logger;
import com.baidu.hd.module.Task;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.sniffer.Sniffer;
import com.baidu.hd.sniffer.BigSiteSnifferResult.BigSiteAlbumResult;

class TaskSniffer {
	private Logger logger = new Logger(this.getClass().getSimpleName());
	
	public interface Callback {
		void onCallback(boolean success, String url);
	}

	private ServiceFactory mServiceFactory = null;
	
	private Task mTask = null;
	private Callback mCallback = null;

	public TaskSniffer(Task task, ServiceFactory serviceFactory) {
		this.mTask = task;
		this.mServiceFactory = serviceFactory;
	}
	
	public void run(Callback callback) {
		logger.d("run mTask.getRefer() = " + mTask.getRefer());
		
		final long millisecond = System.currentTimeMillis();
		mCallback = callback;
		
		Sniffer sniffer = (Sniffer)this.mServiceFactory.getServiceProvider(Sniffer.class);
		sniffer.createM3U8Entity(new Sniffer.BigSiteCallback() {
			
			@Override
			public void onCancel(String refer) {
			}

			@Override
			public void onComplete(String refer, String url, 	BigSiteAlbumResult result) {
				logger.d("refer=" + refer + " url=" + url);
				logger.d("sniffer duration =" + (System.currentTimeMillis() - millisecond));
				mCallback.onCallback(!"".equals(url), url);
			}
		}, null).request(mTask.getRefer());
		
	}
}






