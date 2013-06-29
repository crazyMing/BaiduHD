package com.baidu.hd.player;


interface PlayerCore {

	interface Callback {
		
		/**
		 * 缓冲
		 * @param percent 100完成
		 */
		void onCache(int percent);
		
		/**
		 * 错误
		 */
		void onError(int errorCode);
		
		/**
		 * 准备完成
		 */
		void onPrepare(int totalPos);
		
		/**
		 * 完成
		 */
		void onComplete();
		
		/**
		 * 刷新
		 * @param currentPos 当前时间
		 * @param totalPos 总时间
		 */
		void onRefresh(int currentPos);
		
		/**
		 * 是否需要刷新
		 */
		boolean needRefresh();
	}
	
	/**
	 * 是否为CyberPlayer
	 * @return
	 */
	boolean isCyberPlayer();

	/**
	 * 创建
	 */
	void create();
	
	/**
	 * 销毁
	 */
	void destroy();

	/**
	 * 开始播放
	 * @param src 任务源
	 */
	void start(String src, int startPos);
	
	/**
	 * 停止播放
	 */
	void stop();

	/**
	 * 暂停
	 * @return 最终结果是否为播放
	 */
	boolean pause();
	
	/**
	 * 暂停或者恢复
	 * @return 最终结果是否为播放
	 */
	boolean pauseResume();
	
	/**
	 * 开始seek
	 */
	void beginSeek();
	
	/**
	 * seek中
	 */
	void seeking(int pos);
	
	/**
	 * seek结束
	 */
	void endSeek(int pos);
	
	/**
	 * Activity的Start和Stop
	 */
	void onActivityStart();
	void onActivityStop();

	/** 获得最后播放时间 */
	int getLastPos();
	
	/** 获得视频源 */
	String getDataSource();
	
	/** 获得时长 */
	int getDuration();
	
	/** 获得当前时长*/
	int getCurrentPos();
	
	/** 获取视频尺寸 */
	int getVideoWidth();
	int getVideoHeight();
	
	/**
	 * 设置视频尺寸
	 * @return 是否设置成功
	 */
	boolean setVideoSize(int width, int height);
}
