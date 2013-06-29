package com.baidu.hd.module;

import java.io.Serializable;
/**
 * 播放信息
 * @author juqiang
 *
 */
public class PlayInfo implements Serializable {

	private static final long serialVersionUID = -4989559489693830243L;

	private int errno; // 0 成功 -1 无数据 -2 服务器错误
	private String orginUrl; // 原始url
	private String url; // type=1时，url为swf链接，扔给flash播放器
						// type=2时，url=bdhd或p2s链接， 扔给xbdyy
						// type=3时，url为原始播放页面，需要注入； 在选集里切换剧集时，这种type需要刷页面
						// type=4时，url为合作站播放页面，不需要注入； 需要嵌入iframe
	private int urlType; // 1:swf, 2:bdhd或普通p2s链接， 3:需要注入的原始页面链接,
	private String title; // 名称
	private String thumbnail;// 剧照
	private Integer playTime; // 播放时长
	private Integer catagoryId; // 分类id，电影，电视剧，动漫，综艺等
	private String siteName;// 网站名字，没有中文名字直接用域名
	private String albumId; // 专辑id，用来做合集时的标识
	private Integer tvSeq; // 序号，第多少集
	private String desc; // 简短描述 （短描述）
	private String introduction; // 简介 （长描述）
	private String totalNum;
	
	public int getErrno() {
		return errno;
	}
	public void setErrno(int errno) {
		this.errno = errno;
	}
	public String getOrginUrl() {
		return orginUrl;
	}
	public void setOrginUrl(String orginUrl) {
		this.orginUrl = orginUrl;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getUrlType() {
		return urlType;
	}
	public void setUrlType(int urlType) {
		this.urlType = urlType;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	public Integer getPlayTime() {
		return playTime;
	}
	public void setPlayTime(Integer playTime) {
		this.playTime = playTime;
	}
	public Integer getCatagoryId() {
		return catagoryId;
	}
	public void setCatagoryId(Integer catagoryId) {
		this.catagoryId = catagoryId;
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
	public Integer getTvSeq() {
		return tvSeq;
	}
	public void setTvSeq(Integer tvSeq) {
		this.tvSeq = tvSeq;
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
	
	public String getTotalNum() {
		return totalNum;
	}
	public void setTotalNum(String totalNum) {
		this.totalNum = totalNum;
	}
	@Override
	public String toString() {
		return "PlayInfo [errno=" + errno + ", orginUrl=" + orginUrl + ", url="
				+ url + ", urlType=" + urlType + ", title=" + title
				+ ", thumbnail=" + thumbnail + ", playTime=" + playTime
				+ ", catagoryId=" + catagoryId + ", siteName=" + siteName
				+ ", albumId=" + albumId + ", tvSeq=" + tvSeq + ", desc="
				+ desc + ", introduction=" + introduction + ", totalNum="
				+ totalNum + "]";
	}
}
