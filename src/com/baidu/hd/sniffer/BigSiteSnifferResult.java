package com.baidu.hd.sniffer;

import java.util.ArrayList;
import java.util.List;

import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.Turple;

/**
 * Hi������
 
��̽ѡ���������£�
 

�ò����б�ҳ/����ҳ���в�ѯ��ϣ���õ����½����

1��  �缯title

2��  �缯����ͼƬ

3��  �缯ժҪ��Ϣ

4��  �缯����ҳurl list

5��  ������Ϣ�����µĲ���ҳurl 
 *@author from email
 */

/**
 * �����缯��Ϣ����Ƶ�Ĳ��ŵ�ַ
 */
public class BigSiteSnifferResult {
	
	/**
	 * ��Ƶ��Ϣ
	 */
	public static class BigSiteVideoResult {
		// �ڼ���
		public String mPlayId = "";
		// ��Ƶ����
		public String mName = "";
		// ���ŵ�ַ
		public String mPlayUrl = "";
		// ����ҳ��ַ
		public String mRefer = "";
		
		public BigSiteVideoResult(){};
	}
	/**
	 * �缯��Ϣ�����Է�����
	 */
	public static class BigSiteAlbumResult {
		
		// �缯ID
		public String mAlbumId = "";
		// �缯����
		public String mTitel = "";
		// �缯����
		public String mThumbnail = "";
		// �缯ժҪ
		public String mDescription = "";
		// ����ҳ�����б�playId playUrl,name
		public List<BigSiteVideoResult> mVideoList = new ArrayList<BigSiteVideoResult>();
		// �Ƿ����
		public boolean mIsHaveNew = false;
		// �Ƿ����
		public boolean mIsFinished = false;
		// ���¼���
		public int mLastlatest; // ���¼��������գ���������

		public BigSiteAlbumResult() {}
	}
	
	// ��ǰ����ҳ�ĵ�ַ
	private String mCurrentPlayRefer = "";
	// ��ǰҳ�Ĳ��ŵ�ַ��M3U8/MP4/3GP�ȣ�
	private String mCurrentPlayUrl = "";
	// �缯��Ϣ
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
