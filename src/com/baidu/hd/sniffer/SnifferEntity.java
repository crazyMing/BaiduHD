package com.baidu.hd.sniffer;

/**
 * ��̽ʵ�壬������������̽����
 * ʵ�ַ�Ϊ��վʵ���Сվʵ��
 * 
 * ��վ��̽��m3u8��Ƶ����
 * Сվ��̽�������б�͵�ǰҳ����Ƶ����
 */
public interface SnifferEntity {

	/**
	 * ��ʼ��̽
	 * @param refer ��Ƶ����ҳ
	 */
	void request(String refer);
	
	/**
	 * ֹͣ��̽���������յ����
	 */
	void cancel();
}
