package com.baidu.hd.task;

import java.util.List;

import com.baidu.hd.module.P2PBlock;
import com.baidu.hd.module.Task;
import com.baidu.hd.service.ServiceProvider;

/**
 * �������������ӿ�
 */
public interface TaskManager extends ServiceProvider {

	/////////////////////////////////////////////////////////
	// ��������
	/**
	 * ɾ������
	 * ��ǰ��������״̬������
	 */
	void remove(Task value);
	
	/**
	 * ��ʼ����
	 * ��ǰ��������״̬����ʼ��ֹͣ������
	 * ����������򴴽�
	 */
	void start(Task value);
	
	/**
	 * ֹͣ����
	 * ��ǰ��������״̬����ʼ��ֹͣ
	 */
	void stop(Task value);
	
	/** ������� */
	void error(Task value);
	
	/** �������� */
	Task find(String value);
	

	/////////////////////////////////////////////////////////
	// ��������
	/** ��ѯ��� */
	List<Task> multiQuery(List<String> keys);
	
	/** �Ƴ���� */
	void multiRemove(List<String> keys);
	
	/**
	 * ��ʼ���пɼ�������
	 * �ɼ���Ϊ��UI�Ͽ���չʾ��
	 */
	void startAllVisible();
	
	/** ֹͣ���пɼ������� */
	void stopAllVisible();

	/** ������пɼ����� */
	List<Task> getAllVisible();
	
	/** ����������� */
	List<Task> getAll();
	
	/////////////////////////////////////////////////////////
	// ����
	/** ����������ļ����� */
	boolean isFileExist(Task value);
	
	/** ����������Ƶʱ�� */
	void setMediaTime(Task task, int value);

	/** �������� */
	P2PBlock getBlock(Task value);
	
	/** ������� */
	public interface clearGarbageEvent
	{
		void clearSize(long size);
		boolean isCancel();
	}
	
	void clearGarbage(clearGarbageEvent event);
}
