package com.baidu.hd.task;

import com.baidu.hd.module.Task;

/**
 * �����б�״̬�����棬��ͣ������
 * ����״̬�����õ�����ͣȫ��
 * ����״̬������ͣ����
 * 
 * �������������ڶ���
 */
interface ListState {
	
	public enum BatchOperate {
		eNone,
		eStart,
		eStop,
		eRemove,
	}
	
	void create(TaskManagerAccessor accessor, ListStateChanger changer);
	void reset();
	
	//////////////////////////////////////////
	// ������� 
	void start(Task value);
	void stop(Task value);
	void remove(Task value);
	void error(Task value);
	void onPrePostEvent(int eventId, Task value);
	void setBatching(BatchOperate op);
}
