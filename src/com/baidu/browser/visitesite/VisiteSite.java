package com.baidu.browser.visitesite;


/**
 * @ClassName: VisitedSite 
 * @Description: 站点访问历史记录
 * @author LEIKANG 
 * @date 2012-12-17 下午3:20:23
 */
public class VisiteSite {

	private long id;
	//网站title
	private String siteTitle;
	//网站url
	private String siteUrl;
	//创建时间
	private long createTime;
	//访问时间
	private long visitedTime;
	//是否书签或书签
	private int isBookMark;
	//是否为历史 默认均为历史记录
	private int isHistory ;
	//图标
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
