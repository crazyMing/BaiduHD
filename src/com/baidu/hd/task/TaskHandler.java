package com.baidu.hd.task;

import com.baidu.hd.module.P2PBlock;
import com.baidu.hd.module.Task;

/**
 * ��ͬ�������͵Ĵ������ӿ�
 * 
 * ������������ࣺ
 * BigSiteTaskHandler
 * SmallSiteTaskHandler
 * 
 * ���������ķ��棺
 * ֪ͨ����Adapterȥִ��ĳ�������ȡĳ����
 * �ö���TaskManagerForServiceʹ��
 * �ö���ʹ������Adapter
 * 
 * �������ܵķ��棺
 * �������ط���Ļص�
 * �ö���ExeAdapterʹ��
 * 
 * �������������ڶ���
 * 
 * ��Ϊ����Ϊ�ڲ��࣬����������Ч�ԣ����������ͱ�����
 * 
 * Api����ʧ�ܲ��ص��������ļ��ʱȽϵͣ�����Ҳ��֪�����չʾ
 * ֻ��״̬��ɴ���Żص�OnError
 */
interface TaskHandler {
	
	/** ���������� */
	void create(TaskManagerAccessor accessor);
	
	/** ���ٴ����� */
	void destroy();
	
	/** ��������� */
	void clearHandle(Task task);
	
	/** ǰ�ÿ������� */
	void forceStart(Task task);
	
	/** �������� */
	void create(Task task);
	
	/**
	 * ɾ������
	 * ��ǰ��������״̬������
	 */
	void remove(Task task);
	
	/**
	 * ��ʼ����
	 * ��ǰ��������״̬����ʼ��ֹͣ������
	 */
	void start(Task task);
	
	/**
	 * ֹͣ����
	 * ��ǰ��������״̬����ʼ��ֹͣ
	 */
	void stop(Task task);

	/**
	 * ��ѯ����
	 * ��ǰ��������״̬������
	 */
	void query(Task task);
	
	/** �����Ŷ�  */
	void queue(Task task);
	
	/** �������  */
	void error(Task task);
	
	/** ��ʼ��������  */
	void startPlay(Task task);
	
	/** ������������ */
	void endPlay(Task task);
	
	/** �Ƿ���Ҫ�����Ŀ�ʼ���� */
	boolean needRealStart(Task task);
	
	/** ����������ļ����� */
	boolean isFileExist(Task task);
	
	/** ���p2p block  */
	P2PBlock getBlock(Task task);
	
	/** ����ʱ�Ĳ��� */
	void startup(Task task);
	
	/** ����ʱ�Ĳ��� */
	void shutdown(Task task);
	
	/** �ɹ��ص� */
	void onSuccess(int eventId, Task task);
	
	/** ʧ�ܻص� */
	void onError(int eventId, Task task, int errorCode);
	
	/**
	 * �Ѳ��������һЩ���Ը�ֵ�������ڶ�����start,errorʱʹ��
	 * Ŀǰֻ��BigSiteTaskHandlerʹ��
	 * @param inTask �����ڶ���
	 * @param paramTask ��������
	 */
	void setProperty(Task inTask, Task paramTask);
	
	/** �����Ƿ�������ⷢ���¼� */
	void setPostEventEnable(boolean value);
}
