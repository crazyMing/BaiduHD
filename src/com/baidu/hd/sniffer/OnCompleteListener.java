package com.baidu.hd.sniffer;

import com.baidu.hd.sniffer.BigSiteSnifferResult.BigSiteAlbumResult;

interface OnCompleteListener {
	void onComplete(String refer, String url, BigSiteAlbumResult result, BaseHandler reporter);
}
