package com.baidu.hd.task;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.baidu.hd.conf.Configuration;
import com.baidu.hd.module.P2PBlock;
import com.baidu.hd.module.Task;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.util.StringUtil;
import com.baidu.player.download.DownloadServiceAdapter;
import com.baidu.player.download.JNITaskBuffer;
import com.baidu.player.download.JNITaskInfo;

/**
 * 简单用途的JNI底层适配器
 * 包括查询和设置
 */
class QueryAdapter {

	private ServiceFactory mServiceFactory = null;
	
	private DownloadServiceAdapter mDownloadService = null;
	
	public void create(ServiceFactory serviceFactory) {
		mServiceFactory = serviceFactory;
		mDownloadService = (DownloadServiceAdapter)serviceFactory.getServiceProvider(DownloadServiceAdapter.class);
	}
	
	public void setDefaultInfo(Task task) {
		
		switch(task.getType()) {
		case Task.Type.Small:
		{
			if(StringUtil.isEmpty(task.getUrl())) {
				return;
			}
			
			JNITaskInfo jniTaskInfo = new JNITaskInfo();
			this.mDownloadService.parseUrl(task.getUrl(), jniTaskInfo);
			if("".equals(task.getFileName())) {
				task.setFileName(jniTaskInfo.getFileName());
			}
			if("".equals(task.getName())) {
				task.setName(jniTaskInfo.getFileName());
			}
			if(task.getTotalSize() == 0) {
				task.setTotalSize(jniTaskInfo.getTotalSize());
			}
		}
		default:
			return;
		}
	}

	public boolean isFileExist(Task task) {
		Configuration conf = (Configuration)this.mServiceFactory.getServiceProvider(Configuration.class);
		String savePath = conf.getTaskSavePath() + task.getFolderName();
		return this.mDownloadService.isFileExist(savePath, task.getFileName(), task.getTotalSize());
	}

	public void setPlaying(Task task) {
		if(task == null || task.getHandle() == 0) {
			return;
		}
		HandlerThread thread = new HandlerThread("setPlaying");
		thread.start();
		Handler handler = new Handler(thread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				Task task = (Task)msg.obj;
				mDownloadService.setPlaying(task.getHandle(), task.isPlaying());
			}
		};
		handler.sendMessage(handler.obtainMessage(0, task));
	}

	public void setMediaTime(Task task, int value) {
		if(task == null || task.getHandle() == 0) {
			return;
		}
		HandlerThread thread = new HandlerThread("setMediaTime");
		thread.start();
		Handler handler = new Handler(thread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				Task task = (Task)msg.obj;
				mDownloadService.setMediaTime(task.getHandle(), msg.arg1);
			}
		};
		handler.sendMessage(handler.obtainMessage(0, value, 0, task));
	}
	
	public String getRedirectUrl(Task task) {
		JNITaskInfo jniTaskInfo = new JNITaskInfo();
		if(this.mDownloadService.getRedirectUrl(task.getHandle(), jniTaskInfo) == 0) {
			return jniTaskInfo.getUrl();
		}
		return "";
	}

	public P2PBlock getBlock(Task task) {
		
		JNITaskBuffer buffer = new JNITaskBuffer();
		if(this.mDownloadService.getBlock(task.getHandle(), buffer) < 0) {
			return null;
		}
		
		BlockStream stream = new BlockStream(buffer.getBuffer());
		int unitByte = stream.getInt();
		short pairByte = stream.getShort();
		short pairNumber = stream.getShort();
		short blockByte = (short)(pairByte / 2);
		int sectionSize = buffer.getBlockSize();

		if(unitByte == 0 || pairByte == 0 || sectionSize == 0) {
			return null;
		}
		
		P2PBlock block = new P2PBlock();
		block.setUnitByte(unitByte);
		block.setSectionSize(sectionSize);
		for(int i = 0; i < pairNumber; ++i) {
			block.addPair(new P2PBlock.Pair(
				stream.getNumber(blockByte), stream.getNumber(blockByte)));
		}
		return block;
	}
	
	private class BlockStream {
		
		private byte[] data = null;
		private int currentIndex = 0;
		
		public BlockStream(byte[] data) {
			this.data = data;
		}

		public short getShort() {
			return (short)this.getNumber(2);
		}
		
		public int getInt() {
			return (int)this.getNumber(4);
		}

		public long getNumber(int digit) {
			
			long number = 0;
			for(int i = 0; i < digit; ++i) {
				number |= (this.data[this.currentIndex] << (8 * i));
				++this.currentIndex;
			}
			return number;
		}
	}
}
