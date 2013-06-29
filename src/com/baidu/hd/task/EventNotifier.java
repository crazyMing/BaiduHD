package com.baidu.hd.task;

import android.os.Handler;
import android.os.Message;

import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.module.Task;

/**
 * 事件通知器
 * 向EventCenter通知事件
 */
class EventNotifier {
	
	/** 事件中心 */
	private EventCenter mEventCenter = null;
	
	public void create(EventCenter eventCenter) {
		this.mEventCenter = eventCenter;
	}

	/** 任务消息包 */
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
	
	/** 转线程消息处理 */
	private Handler mHanlder = new Handler() {
	
		@Override
		public void handleMessage(Message msg) {
			
			TaskMessagePackage pack = (TaskMessagePackage)msg.obj;
			mEventCenter.fireEvent(pack.getEventId(), pack.convertToEventArgs());
		}
	};

	/** 通知 */
	public void postEvent(EventId eventId, Task task) {
		this.mHanlder.sendMessage(
				this.mHanlder.obtainMessage(
						0, new EventNotifier.TaskMessagePackage(eventId, task)));
	}
}
