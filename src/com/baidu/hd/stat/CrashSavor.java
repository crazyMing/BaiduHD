package com.baidu.hd.stat;

import com.baidu.hd.util.FileUtil;

public class CrashSavor {

	public static void save(String crashMessage) {
		// 保存最后1000条，从bufferLogger中取
		FileUtil.write(StatConst.crashPath, "\r\n" + crashMessage, true);
	}
}
