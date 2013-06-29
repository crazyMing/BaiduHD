package com.baidu.browser.visitesite;


/**
 * @ClassName: VisitedSite 
 * @Description: վ�������ʷ��¼
 * @author LEIKANG 
 * @date 2012-12-17 ����3:20:23
 */
public class VisiteSite {

	private long id;
	//��վtitle
	private String siteTitle;
	//��վurl
	private String siteUrl;
	//����ʱ��
	private long createTime;
	//����ʱ��
	private long visitedTime;
	//�Ƿ���ǩ����ǩ
	private int isBookMark;
	//�Ƿ�Ϊ��ʷ Ĭ�Ͼ�Ϊ��ʷ��¼
	private int isHistory ;
	//ͼ��
	private byte[] icon;
	
	public String getSiteTitle() {
		return siteTitle;
	}
	public void setSiteTitle(String siteTitle) {
		this.siteTitle = siteTitle;
	}
	public String getSiteUrl() {
		return siteUrl;
	}
	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}

	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public long getVisitedTime() {
		return visitedTime;
	}
	public void setVisitedTime(long visitedTime) {
		this.visitedTime = visitedTime;
	}

	public int getIsBookMark() {
		return isBookMark;
	}
	public void setIsBookMark(int isBookMark) {
		this.isBookMark = isBookMark;
	}
	public int getIsHistory() {
		return isHistory;
	}
	public void setIsHistory(int isHistory) {
		this.isHistory = isHistory;
	}
 
	
	public byte[] getIcon() {
		return icon;
	}
	public void setIcon(byte[] icon) {
		this.icon = icon;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	@Override
	public String toString() {
		return "VisitedSite [siteTitle=" + siteTitle + ", siteUrl=" + siteUrl
				+ ", createTime=" + createTime + ", visitedTime=" + visitedTime
				+ ", isBookMark=" + isBookMark + ", isHistory=" + isHistory
				+ ", icon=" + icon + "]";
	}
	
}
