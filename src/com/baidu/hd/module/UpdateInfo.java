package com.baidu.hd.module;

import java.io.Serializable;

public class UpdateInfo implements Serializable {

	private static final long serialVersionUID = -4989559489693830245L;

	public static class Response {
		public int hasUpdate; // 1有更新，0是无更新
		public int finish; // 1完结，0未完结
		public int latest; // 最新集数，综艺：最新日期
		
		@Override
		public String toString() {
			return "Response [hasUpdate=" + hasUpdate + ", finish=" + finish
					+ ", latest=" + latest + "]";
		}
	}

	public static class Request {
		public String albumId; // 专辑id
		public String playId; // 最新的集数或期数
	}
}
