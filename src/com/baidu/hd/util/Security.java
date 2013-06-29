package com.baidu.hd.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

public class Security {

	public static String getFileMD5(String strfilePath) {
		MessageDigest md = null;
		BufferedInputStream in = null;
		try {
			md = MessageDigest.getInstance("MD5");
			in = new BufferedInputStream(new FileInputStream(strfilePath));
			byte[] bytes = new byte[8192];
			int byteCount;
			while ((byteCount = in.read(bytes)) > 0) {
				md.update(bytes, 0, byteCount);
			}
			bytes = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return StringUtil.byte2hex(md.digest());
	}

}
