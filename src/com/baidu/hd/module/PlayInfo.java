package com.baidu.hd.module;

import java.io.Serializable;
/**
 * ������Ϣ
 * @author juqiang
 *
 */
public class PlayInfo implements Serializable {

	private static final long serialVersionUID = -4989559489693830243L;

	private int errno; // 0 �ɹ� -1 ������ -2 ����������
	private String orginUrl; // ԭʼurl
	private String url; // type=1ʱ��urlΪswf���ӣ��Ӹ�flash������
						// type=2ʱ��url=bdhd��p2s���ӣ� �Ӹ�xbdyy
						// type=3ʱ��urlΪԭʼ����ҳ�棬��Ҫע�룻 ��ѡ�����л��缯ʱ������type��Ҫˢҳ��
						// type=4ʱ��urlΪ����վ����ҳ�棬����Ҫע�룻 ��ҪǶ��iframe
	private int urlType; // 1:swf, 2:bdhd����ͨp2s���ӣ� 3:��Ҫע���ԭʼҳ������,
	private String title; // ����
	private String thumbnail;// ����
	private Integer playTime; // ����ʱ��
	private Integer catagoryId; // ����id����Ӱ�����Ӿ磬���������յ�
	private String siteName;// ��վ���֣�û����������ֱ��������
	private String albumId; // ר��id���������ϼ�ʱ�ı�ʶ
	private Integer tvSeq; // ��ţ��ڶ��ټ�
	private String desc; // ������� ����������
	private String introduction; // ��� ����������
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
