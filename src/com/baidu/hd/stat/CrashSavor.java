package com.baidu.hd.stat;

import com.baidu.hd.util.FileUtil;

public class CrashSavor {

	public static void save(String crashMessage) {
		// �������1000������bufferLogger��ȡ
		FileUtil.write(StatConst.crashPath, "\r\n" + crashMessage, true);
	}
}
