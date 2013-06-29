package com.baidu.hd.image;

import android.graphics.drawable.Drawable;

import com.baidu.hd.service.ServiceProvider;

public interface ImageManager extends ServiceProvider {

	Drawable request(String url, int type);
}
