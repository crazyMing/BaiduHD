package com.baidu.hd.task;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.baidu.hd.conf.Configuration;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.BigSiteTask;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.TaskFactory;
import com.baidu.player.download.DownloadServiceAdapter;
import com.baidu.player.download.JNIP2P;
import com.baidu.player.download.JNITaskCreateParam;
import com.baidu.player.download.JNITaskInfo;

/**
 * ִ����;��JNI�ײ�������
 * ���в�����������ʵ�����񣬲�����������
 * �ص������Ĳ��������´������������
 */
class ExecAdapter {
	
	private class AdapterEventId {
		public static final int Start = 3;
		public static final int Stop = 4;
		public static final int Remove = 5;
		public static final int Query = 6;
	}
	
	private Logger logger = new Logger("ExecAdapter");

	/**
	 * ���������������
	 */
	private TaskManagerAccessor mAccessor = null;
	
	private DownloadServiceAdapter mDownloadService = null;
	
	/**
	 * ֪ͨ�����̵߳����ݰ�
	 */
	private class Package {
		
		Task mTask = null;
		int mEventId = 0;
		int mErrorCode = 0;
		
		Package(Task task, int eventId, int errorCode) {
			this.mTask = task;
			this.mEventId = eventId;
			this.mErrorCode = errorCode;
		}
	}
	
	/**
	 * ת�����߳�handler
	 */
	private Handler mNotifyHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			Package pack = (Package)msg.obj;			
			TaskHandler handler = mAccessor.getTaskHandler(pack.mTask.getType());
			if(pack.mErrorCode == 0) {
				handler.onSuccess(pack.mEventId, pack.mTask);
			} else {
				handler.onError(pack.mEventId, pack.mTask, pack.mErrorCode);
			}
		}
	};
	
	/**
	 * ת�����̵߳�handler
	 */
	private Handler mWorkHandler = null;
	
	public void create(TaskManagerAccessor accessor) {

		this.mAccessor = accessor;
		
		this.mDownloadService = (DownloadServiceAdapter)this.mAccessor.getServiceFactory().getServiceProvider(DownloadServiceAdapter.class);
		
		HandlerThread thread = new HandlerThread("Download");
		thread.start();
		
		this.mWorkHandler = new Handler(thread.getLooper()) {

			@Override
			public void handleMessage(Message msg) {

				switch(msg.what) {
				case AdapterEventId.Start:
					_start((Task)msg.obj);
					break;
				case AdapterEventId.Stop:
					_stop((Task)msg.obj);
					break;
				case AdapterEventId.Remove:
					_remove((Task)msg.obj);
					break;
				case AdapterEventId.Query:
					_query((Task)msg.obj);
					break;
				}
			}
			
		};		
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// interface
	public void start(Task value) {
		this.mWorkHandler.sendMessage(this.mWorkHandler.obtainMessage(AdapterEventId.Start, value));
	}
	
	public void stop(Task value) {
		this.mWorkHandler.sendMessage(this.mWorkHandler.obtainMessage(AdapterEventId.Stop, value));
	}
	
	public void remove(Task value) {
		this.mWorkHandler.sendMessage(this.mWorkHandler.obtainMessage(AdapterEventId.Remove, value));
	}
	
	public void query(Task value) {
		this.mWorkHandler.sendMessage(this.mWorkHandler.obtainMessage(AdapterEventId.Query, value));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// thread
	void _start(Task aTask) {
		
		this.logger.d("start " + aTask.getInfo());
		
		// ��ÿ���
		Task task = this.cloneTask(aTask);
		
		// �����ڣ�����
		if(!this.mDownloadService.exist(task.getHandle())) {

			this.logger.d("start not exist. create " + task.getInfo());
			
			// ���״̬����
			task.clearState();
			
			// ���ô�������
			Configuration conf = (Configuration)this.mAccessor.getServiceFactory().getServiceProvider(Configuration.class);
			String savePath = conf.getTaskSavePath() + task.getFolderName();
			
			long ret = 0;
			JNITaskCreateParam param = new JNITaskCreateParam();
			param.setSavePath(savePath);
			param.setUrl(task.getUrl());
			param.setRefer(task.getRefer());
			param.setFileName(task.getFileName());
			
			int flag = 0;
			
			switch(task.getType()) {
			case Task.Type.Small:
				{
					// ��ʽ����
					if(task.toSmall().isStreamMode()) {
						//flag = JNITaskCreateParam.FlagType.StreamMemory;
						flag = JNITaskCreateParam.FlagType.StreamFile;
					} else {
						flag = JNITaskCreateParam.FlagType.NormalSplit;
					}
				}
				break;
			case Task.Type.Big:
				{
					// һ������ֿ飬�������񲿷ֿ�
					if(task.toBig().getSubType() == BigSiteTask.SubType.First) {
						flag = JNITaskCreateParam.FlagType.NormalSplit;
					} else {
						flag = JNITaskCreateParam.FlagType.NormalNoSplit;
					}
				}
				break;
			// TODO ��ʱ�������б�
			default:
				break;
			}

			param.setFlag(flag);

			// ����
			ret = this.mDownloadService.create(param);
			if(ret >= 0) {
				task.setHandle(param.getHandle());
				this.postSuccess(CallbackEventId.OnCreate, task);
			} else {
				this.postError(CallbackEventId.OnCreate, task, (int)ret);
				this.postError(CallbackEventId.OnStart, task, (int)ret);
				return;
			}
		}

		// ��ʼ
		int ret = this.mDownloadService.start(task.getHandle());
		if(ret >= 0) {
			task.setState(Task.State.Start);
			this.postSuccess(CallbackEventId.OnStart, task);
		} else {
			this.postError(CallbackEventId.OnStart, task, ret);
		}
	}

	void _stop(Task aTask) {

		this.logger.d("stop " + aTask.getInfo());
		
		// ��ÿ���
		Task task = this.cloneTask(aTask);
		
		// �����ڣ��ɹ�
		if(!this.mDownloadService.exist(task.getHandle())) {
			
			this.logger.d("stop not exist. return");
			task.setState(Task.State.Stop);
			this.postSuccess(CallbackEventId.OnStop, task);
			return;
		}

		// ֹͣ
		int ret = this.mDownloadService.stop(task.getHandle());
		if(ret >= 0) {
			task.setState(Task.State.Stop);
			this.postSuccess(CallbackEventId.OnStop, task);
		} else {
			this.postError(CallbackEventId.OnStop, task, ret);
		}
	}

	void _remove(Task aTask) {
		
		this.logger.d("remove " + aTask.getInfo());
		
		// ��ÿ���
		Task task = this.cloneTask(aTask);
		
		// ���ڣ��Ƴ�
		if(this.mDownloadService.exist(task.getHandle())) {
			
			int ret = this.mDownloadService.delete(task.getHandle());
			if(ret >= 0) {
				this.postSuccess(CallbackEventId.OnRemove, task);
			} else {
				this.postError(CallbackEventId.OnRemove, task, ret);
			}
			aTask.setHandle(0);
		}
		
		// �����ڣ��ɹ�
		else {

			this.logger.d("remove not exist. return");
			task.clearState();
			this.postSuccess(CallbackEventId.OnRemove, task);
		}
	}

	void _query(Task aTask) {

		this.logger.v("query " + aTask.getInfo());
		
		// ��ÿ���
		Task task = this.cloneTask(aTask);
		
		// �����ڣ�ʧ��
		if (!this.mDownloadService.exist(task.getHandle())) {
			
			this.logger.d("query not exist. return");
			this.postError(CallbackEventId.OnQuery, task, JNIP2P.APIErrorCode.API_ERROR_NOT_FOUND);
			return;
		}
		
		//  ��ѯ
		JNITaskInfo jniTaskInfo = new JNITaskInfo();
		int ret = this.mDownloadService.query(task.getHandle(), jniTaskInfo);
		if (ret >= 0) {
			this.JNITaskInfo2TaskInfo(jniTaskInfo, task);
			this.postSuccess(CallbackEventId.OnQuery, task);
		} else {
			this.postError(CallbackEventId.OnQuery, task, ret);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	// helper
	private void postSuccess(int eventId, Task task) {
		this.mNotifyHandler.sendMessage(
				this.mNotifyHandler.obtainMessage(
						0, new Package(task, eventId, 0)));
	}

	private void postError(int eventId, Task task, int errorCode) {
		this.mNotifyHandler.sendMessage(
				this.mNotifyHandler.obtainMessage(
						0, new Package(task, eventId, errorCode)));
	}
	
	private Task cloneTask(Task task) {
		
		Task result = TaskFactory.create(task.getType());
		result.copyFrom(task);
		return result;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	// utility
	private void JNITaskInfo2TaskInfo(JNITaskInfo jniTaskInfo, Task task) {
		task.setFileName(jniTaskInfo.getFileName());
		task.setState(this.convertState(jniTaskInfo.getState()));
		task.setErrorCode(jniTaskInfo.getErrorCode());
		task.setTotalSize(jniTaskInfo.getTotalSize());
		task.setDownloadSize(jniTaskInfo.getDownloadedSize());
		task.setSpeed(jniTaskInfo.getSpeed());
		task.setDiskFile(jniTaskInfo.getDiskFiles());
	}

	private int convertState(int value) {
		switch(value) {
		case JNIP2P.TASK_STATUS_CODE.TSC_NOITEM:
		case JNIP2P.TASK_STATUS_CODE.TSC_PAUSE:
			return Task.State.Stop;
		case JNIP2P.TASK_STATUS_CODE.TSC_CONNECT:
		case JNIP2P.TASK_STATUS_CODE.TSC_DOWNLOAD:
		case JNIP2P.TASK_STATUS_CODE.TSC_QUEUE:
			return Task.State.Start;
		case JNIP2P.TASK_STATUS_CODE.TSC_COMPLETE:
			return Task.State.Complete;
		case JNIP2P.TASK_STATUS_CODE.TSC_ERROR:
			return Task.State.Error;
		default:
			return Task.getDefaultState();
		}
	}
}
