package com.baidu.hd.sniffer;

import java.util.ArrayList;
import java.util.List;

import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.Turple;

/**
 * Hi，龙哥
 
嗅探选集需求如下：
 

用播放列表页/播放页进行查询，希望得到如下结果：

1、  剧集title

2、  剧集海报图片

3、  剧集摘要信息

4、  剧集播放页url list

5、  更新信息：更新的播放页url 
 *@author from email
 */

/**
 * 包括剧集信息与视频的播放地址
 */
public class BigSiteSnifferResult {
	
	/**
	 * 视频信息
	 */
	public static class BigSiteVideoResult {
		// 第几集
		public String mPlayId = "";
		// 视频名称
		public String mName = "";
		// 播放地址
		public String mPlayUrl = "";
		// 播放页地址
		public String mRefer = "";
		
		public BigSiteVideoResult(){};
	}
	/**
	 * 剧集信息，来自服务器
	 */
	public static class BigSiteAlbumResult {
		
		// 剧集ID
		public String mAlbumId = "";
		// 剧集标题
		public String mTitel = "";
		// 剧集海报
		public String mThumbnail = "";
		// 剧集摘要
		public String mDescription = "";
		// 播放页链接列表playId playUrl,name
		public List<BigSiteVideoResult> mVideoList = new ArrayList<BigSiteVideoResult>();
		// 是否更新
		public boolean mIsHaveNew = false;
		// 是否完结
		public boolean mIsFinished = false;
		// 最新集数
		public int mLastlatest; // 最新集数，综艺：最新日期

		public BigSiteAlbumResult() {}
	}
	
	// 当前播放页的地址
	private String mCurrentPlayRefer = "";
	// 当前页的播放地址（M3U8/MP4/3GP等）
	private String mCurrentPlayUrl = "";
	// 剧集信息
	private BigSiteAlbumResult mAlbumResult = null;
	
	public String getAlbumId() {
		if (mAlbumResult == null) return null;		
		return mAlbumResult.mAlbumId;
	}
	
	public void setAlbumId(String albumId) {
		if (mAlbumResult == null) return;
		mAlbumResult.mAlbumId = albumId;
	}
	
	public BigSiteAlbumResult getBigSiteAlbumResult() {
		return mAlbumResult;
	}
	
	public  void setBigSiteAlbumResult(BigSiteAlbumResult result) {
		mAlbumResult = result;
	}
	
	public String getCurrentPlayRefer() {
		return mCurrentPlayRefer;
	}
	
	public void setCurrentPlayRefer(String refer) {
		this.mCurrentPlayRefer = refer;
	}
	
	public String getCurrentPlayUrl() {
		return mCurrentPlayUrl;
	}
	
	public void setCurrentPlayUrl(String url) {
		this.mCurrentPlayUrl = url;
	}

	public String getTitel() {
		if (mAlbumResult == null) return null;
		return mAlbumResult.mTitel;
	}
	
	public void setTitel(String titel) {
		if (mAlbumResult == null) return;
		mAlbumResult.mTitel = titel;
	}
	
	public String getThumbnail() {
		if (mAlbumResult == null) return null;
		return mAlbumResult.mThumbnail;
	}
	
	public void setThumbnail(String thumbnail) {
		if (mAlbumResult == null) return;
		mAlbumResult.mThumbnail = thumbnail;
	}
	
	public String getDescription() {
		if (mAlbumResult == null) return null;
		return mAlbumResult.mDescription;
	}
	
	public void setDescription(String description) {
		if (mAlbumResult == null) return;
		mAlbumResult.mDescription = description;
	}
	
	public List<BigSiteVideoResult> getVideoList() {
		if (mAlbumResult == null) return null;
		return mAlbumResult.mVideoList;
	}
	
	public void setVideoList(List<BigSiteVideoResult> videoReferList) {
		if (mAlbumResult == null) return;
		mAlbumResult.mVideoList = videoReferList;
	}
	
	public boolean isHaveNew() {
		if (mAlbumResult == null) return false;
		return mAlbumResult.mIsHaveNew;
	}
	
	public void setHaveNew(boolean isHaveNew) {
		if (mAlbumResult == null) return;
		mAlbumResult.mIsHaveNew = isHaveNew;
	}
	
	public boolean isFinished() {
		if (mAlbumResult == null) return false;
		return mAlbumResult.mIsFinished;
	}
	
	public void setFinished(boolean iFinished) {
		mAlbumResult.mIsFinished = iFinished;
	}
	
	public int getLastlatest() {
		if (mAlbumResult == null) return -1;
		return mAlbumResult.mLastlatest;
	}
	
	public void setLastlatest(int lastlatest) {
		mAlbumResult.mLastlatest = lastlatest;
	}

	@Override
	public String toString() {
		return "BigSiteSnifferResult [mRefer=" + mCurrentPlayRefer + ", mPlayUrl="
				+ mCurrentPlayUrl + ", mAlbumResult=" + mAlbumResult!=null?mAlbumResult.toString():"null" + "]";
	}
}
