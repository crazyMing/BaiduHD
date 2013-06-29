package com.baidu.hd.module;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * ר����Ϣ
 * 
 * @author juqiang
 * 
 */
public class AlbumInfo implements Serializable {

	private static final long serialVersionUID = -4989559489693830244L;

	private int errno; // 0 �ɹ� -1 ������ -2 ����������
	private int currentNumber; // ��ǰ��ѯplayUrl�ǵڶ��ټ����������ϸ��Ϣ��detailInfo��ȡ
	private ArrayList<DetailInfo> detailInfo=new ArrayList<AlbumInfo.DetailInfo>();
	private String title; // ����
	private String poster; // ר������
	private int totalNum; // ר���ڵ�����
	private int catagoryId; // ר��id���������ϼ�ʱ�ı�ʶ
	private int finish; // 1�����Ѿ���ᣬ0����δ���
	private String siteName;// ��վ���֣�û����������ֱ��������
	private String albumId; // ר��id���������ϼ�ʱ�ı�ʶ
	private String desc; // ר��������� ����������
	private String introduction; // ר����� ����������

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
		public String imgSrc;// ÿһ���ľ���
		public int playId; // �缯Ϊ����������Ϊ���ڣ��ַ�����
		public String linkTitle; //
		public String orginUrl; // ԭʼ����ҳ��ַ
		public String playUrl; // swf��bdhd��ԭʼҳ�棬 urlType��ʶtype
		public int urlType; // ͬ������Ϣ���urlType
		public String desc; // �������һ��������� ����������
		public String introduction; // �������һ����� ����������
		
		@Override
		public String toString() {
			return "DetailInfo [imgSrc=" + imgSrc + ", playId=" + playId
					+ ", linkTitle=" + linkTitle + ", orginUrl=" + orginUrl
					+ ", playUrl=" + playUrl + ", urlType=" + urlType
					+ ", desc=" + desc + ", introduction=" + introduction + "]";
		}
	}
}
