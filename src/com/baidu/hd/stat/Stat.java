package com.baidu.hd.stat;

import com.baidu.hd.service.ServiceProvider;

public interface Stat extends ServiceProvider {
	
	public enum NetState {
		eWifi,
		eNoWifi,
		eDisable,
	}

	/** �ϱ���־	 */
	void postLog();
	
	/** ��̽ʧ�� */
	void addSnifferFail(String refer);

	/** ���ñ�����Ƶ���� */
	void setLocalVideoCount(int value);
	
	void setNetState(NetState state);
	void setHaveSDCard(boolean value);

	// logƽ̨ͳ��
	void incLogCount(int id);
	
	// �ƶ�ͳ��
	void incEventCount(String type, String subType);
	void incEventValue(String type, String subType, int value);
	void incEventAppValid();
	
	void incUdpCount(int id);
	void incUdpValue(int id, long value);
	void setUdpString(int id, String value);
	void sendUdp();
}
