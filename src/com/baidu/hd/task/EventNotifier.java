package com.baidu.hd.task;

import android.os.Handler;
import android.os.Message;

import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.module.Task;

/**
 * �¼�֪ͨ��
 * ��EventCenter֪ͨ�¼�
 */
class EventNotifier {
	
	/** �¼����� */
	private EventCenter mEventCenter = null;
	
	public void create(EventCenter eventCenter) {
		this.mEventCenter = eventCenter;
	}

	/** ������Ϣ�� */
	private static class TaskMessagePackage {
		
		private EventId eventId = EventId.eNone;
		private Task task = null;
		
		public TaskMessagePackage(EventId eventId, Task task) {
			this.eventId = eventId;
			this.task = task;
		}

		public EventId getEventId() {
			return eventId;
		}

		public TaskEventArgs convertToEventArgs() {
			return new TaskEventArgs(this.task);
		}
	}
	
	/** ת�߳���Ϣ���� */
	private Handler mHanlder = new Handler() {
	
		@Override
		public void handleMessage(Message msg) {
			
			TaskMessagePackage pack = (TaskMessagePackage)msg.obj;
			mEventCenter.fireEvent(pack.getEventId(), pack.convertToEventArgs());
		}
	};

	/** ֪ͨ */
	public void postEvent(EventId eventId, Task task) {
		this.mHanlder.sendMessage(
				this.mHanlder.obtainMessage(
						0, new EventNotifier.TaskMessagePackage(eventId, task)));
	}
}
