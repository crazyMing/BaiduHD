package com.baidu.hd.module.album;

import com.baidu.hd.log.Logger;

public class BigServerAlbum extends Album {
	private Logger logger = new Logger(this.getClass().getSimpleName());

	@Override
	public BigServerAlbum asBigServerAlbum() {
		return this;
	}

	public BigServerAlbum() {
		super();
		this.setFromType(0);
	}
}
