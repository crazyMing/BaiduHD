package com.baidu.hd.module.album;

/**
 * Сվ��������̽�缯��
 * @author sunjianshun
 *
 */
public class SmallServerAlbum extends Album {

	public SmallServerAlbum() {
		super();
		this.setFromType(2);
	}

	@Override
	public SmallServerAlbum asSmallServerAlbum() {
		return this;
	}

}
