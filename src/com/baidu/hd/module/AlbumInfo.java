package com.baidu.hd.module;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 专辑信息
 * 
 * @author juqiang
 * 
 */
public class AlbumInfo implements Serializable {

	private static final long serialVersionUID = -4989559489693830244L;

	private int errno; // 0 成功 -1 无数据 -2 服务器错误
	private int currentNumber; // 当前查询playUrl是第多少集，具体的详细信息从detailInfo获取
	private ArrayList<DetailInfo> detailInfo=new ArrayList<AlbumInfo.DetailInfo>();
	private String title; // 名称
	private String poster; // 专辑海报
	private int totalNum; // 专辑内的条数
	private int catagoryId; // 专辑id，用来做合集时的标识
	private int finish; // 1代表已经完结，0代表未完结
	private String siteName;// 网站名字，没有中文名字直接用域名
	private String albumId; // 专辑id，用来做合集时的标识
	private String desc; // 专辑简短描述 （短描述）
	private String introduction; // 专辑简介 （长描述）

	public int getErrno() {
		return errno;
	}

	public void setErrno(int errno) {
		this.errno = errno;
	}

	public int getCurrentNumber() {
		return currentNumber;
	}

	public void setCurrentNumber(int currentNumber) {
		this.currentNumber = currentNumber;
	}

	public ArrayList<DetailInfo> getDetailInfo() {
		return detailInfo;
	}

	public void setDetailInfo(ArrayList<DetailInfo> detailInfo) {
		this.detailInfo = detailInfo;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPoster() {
		return poster;
	}

	public void setPoster(String poster) {
		this.poster = poster;
	}

	public int getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(int totalNum) {
		this.totalNum = totalNum;
	}

	public int getCatagoryId() {
		return catagoryId;
	}

	public void setCatagoryId(int catagoryId) {
		this.catagoryId = catagoryId;
	}

	public int getFinish() {
		return finish;
	}

	public void setFinish(int finish) {
		this.finish = finish;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getIntroduction() {
		return introduction;
	}

	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}

	@Override
	public String toString() {
		return "AlbumInfo [errno=" + errno + ", currentNumber=" + currentNumber
				+ ", detailInfo=" + detailInfo + ", title=" + title
				+ ", poster=" + poster + ", totalNum=" + totalNum
				+ ", catagoryId=" + catagoryId + ", finish=" + finish
				+ ", siteName=" + siteName + ", albumId=" + albumId + ", desc="
				+ desc + ", introduction=" + introduction + "]";
	}

	public static class DetailInfo {
		public String imgSrc;// 每一集的剧照
		public int playId; // 剧集为集数，综艺为日期（字符串）
		public String linkTitle; //
		public String orginUrl; // 原始播放页地址
		public String playUrl; // swf，bdhd，原始页面， urlType标识type
		public int urlType; // 同播放信息里的urlType
		public String desc; // 具体的这一集简短描述 （短描述）
		public String introduction; // 具体的这一集简介 （长描述）
		
		@Override
		public String toString() {
			return "DetailInfo [imgSrc=" + imgSrc + ", playId=" + playId
					+ ", linkTitle=" + linkTitle + ", orginUrl=" + orginUrl
					+ ", playUrl=" + playUrl + ", urlType=" + urlType
					+ ", desc=" + desc + ", introduction=" + introduction + "]";
		}
	}
}
