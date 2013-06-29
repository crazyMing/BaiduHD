package com.baidu.hd.module;

import java.io.Serializable;

public class UpdateInfo implements Serializable {

	private static final long serialVersionUID = -4989559489693830245L;

	public static class Response {
		public int hasUpdate; // 1�и��£�0���޸���
		public int finish; // 1��ᣬ0δ���
		public int latest; // ���¼��������գ���������
		
		@Override
		public String toString() {
			return "Response [hasUpdate=" + hasUpdate + ", finish=" + finish
					+ ", latest=" + latest + "]";
		}
	}

	public static class Request {
		public String albumId; // ר��id
		public String playId; // ���µļ���������
	}
}
