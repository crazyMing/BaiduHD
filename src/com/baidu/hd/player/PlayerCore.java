package com.baidu.hd.player;


interface PlayerCore {

	interface Callback {
		
		/**
		 * ����
		 * @param percent 100���
		 */
		void onCache(int percent);
		
		/**
		 * ����
		 */
		void onError(int errorCode);
		
		/**
		 * ׼�����
		 */
		void onPrepare(int totalPos);
		
		/**
		 * ���
		 */
		void onComplete();
		
		/**
		 * ˢ��
		 * @param currentPos ��ǰʱ��
		 * @param totalPos ��ʱ��
		 */
		void onRefresh(int currentPos);
		
		/**
		 * �Ƿ���Ҫˢ��
		 */
		boolean needRefresh();
	}
	
	/**
	 * �Ƿ�ΪCyberPlayer
	 * @return
	 */
	boolean isCyberPlayer();

	/**
	 * ����
	 */
	void create();
	
	/**
	 * ����
	 */
	void destroy();

	/**
	 * ��ʼ����
	 * @param src ����Դ
	 */
	void start(String src, int startPos);
	
	/**
	 * ֹͣ����
	 */
	void stop();

	/**
	 * ��ͣ
	 * @return ���ս���Ƿ�Ϊ����
	 */
	boolean pause();
	
	/**
	 * ��ͣ���߻ָ�
	 * @return ���ս���Ƿ�Ϊ����
	 */
	boolean pauseResume();
	
	/**
	 * ��ʼseek
	 */
	void beginSeek();
	
	/**
	 * seek��
	 */
	void seeking(int pos);
	
	/**
	 * seek����
	 */
	void endSeek(int pos);
	
	/**
	 * Activity��Start��Stop
	 */
	void onActivityStart();
	void onActivityStop();

	/** �����󲥷�ʱ�� */
	int getLastPos();
	
	/** �����ƵԴ */
	String getDataSource();
	
	/** ���ʱ�� */
	int getDuration();
	
	/** ��õ�ǰʱ��*/
	int getCurrentPos();
	
	/** ��ȡ��Ƶ�ߴ� */
	int getVideoWidth();
	int getVideoHeight();
	
	/**
	 * ������Ƶ�ߴ�
	 * @return �Ƿ����óɹ�
	 */
	boolean setVideoSize(int width, int height);
}
