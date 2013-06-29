package com.baidu.hd.task;

import java.util.List;

import com.baidu.hd.module.Task;
import com.baidu.hd.service.ServiceFactory;

/**
 * ���������������
 * ���������ص����ݣ���Ҫ��taskhandlerʹ��
 */
interface TaskManagerAccessor {

	/**
	 * ���񹤳�
	 */
	ServiceFactory getServiceFactory();
	
	/**
	 * ��������������Իص�������������
	 */
	TaskHandler getTaskHandler(int type);

	/**
	 * �����������
	 */
	List<Task> getAllTask();
	
	/**
	 * ����������������Բ�������
	 */
	ExecAdapter getExeAdpater();
	
	/**
	 * ��ò�ѯ������
	 */
	QueryAdapter getQueryAdapter();

	/**
	 * ����֪ͨ
	 */
	void postEvent(int eventId, Task task);
}
