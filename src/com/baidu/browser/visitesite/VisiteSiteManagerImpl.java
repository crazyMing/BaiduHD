package com.baidu.browser.visitesite;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.Context;

import com.baidu.browser.db.HistoryConfig;
import com.baidu.browser.db.Suggestion;
import com.baidu.hd.db.DBReader;
import com.baidu.hd.db.DBVisiteSite;
import com.baidu.hd.db.DBWriter;
import com.baidu.hd.service.ServiceConsumer;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.Utility;

/**
 * @ClassName: VisiteSiteManagerImpl 
 * @Description: 历史记录/书签 manager 实现
 * @author LEIKANG 
 * @date 2012-12-26 下午12:58:14
 */
public class VisiteSiteManagerImpl implements VisiteSiteManager, ServiceConsumer{

	private ServiceFactory mServiceFactory = null;
	
	private List<VisiteSite> visiteSites = new ArrayList<VisiteSite>();
	
	private DBReader mDBReader;
	
	private DBWriter mDbWriter;
	
	private Context context;
	
	@Override
	public void setContext(Context ctx) {
		context = ctx;
	}

	@Override
	public void onCreate() {
		mDBReader = (DBReader) this.mServiceFactory.getServiceProvider(DBReader.class);
		mDbWriter = (DBWriter) this.mServiceFactory.getServiceProvider(DBWriter.class);
		visiteSites = mDBReader.getAllVisitedSite();
	}

	@Override
	public void onDestory() {
		
	}

	@Override
	public void onSave() {
		
	}

	@Override
	public void setServiceFactory(ServiceFactory factory) {
		mServiceFactory = factory;	
	}

	@Override
	public void insertVisitedSite(VisiteSite mVisitedSite) {
		
		String siteUrl = mVisitedSite.getSiteUrl();
		if (!Utility.isUrl(siteUrl)&&!Utility.isBDHD(siteUrl))
			return;
		VisiteSite exitVs = findVisitedByUrl(siteUrl);
		if (exitVs != null) {
			mVisitedSite.setId(exitVs.getId());
			mVisitedSite.setIsBookMark(exitVs.getIsBookMark());
			updateVisitedSite(mVisitedSite);
		} else if (!StringUtil.isEmpty(mVisitedSite.getSiteTitle())){
			if (!HistoryConfig.isPrivateMode(context))
				mVisitedSite.setIsHistory(DBVisiteSite.HISTORY);
			else 
				mVisitedSite.setIsHistory(DBVisiteSite.NORMAL);
			
			mVisitedSite.setIsBookMark(DBVisiteSite.NORMAL);
			mDbWriter.modifyVisiteSite(mVisitedSite, DBWriter.Action.Add);
			refresh();
		}
	}

	public void updateVisitedSite(VisiteSite mVisitedSite) {
		if (!StringUtil.isEmpty(mVisitedSite.getSiteTitle())){
			if (!HistoryConfig.isPrivateMode(context))
				mVisitedSite.setIsHistory(DBVisiteSite.HISTORY);
			else 
				mVisitedSite.setIsHistory(DBVisiteSite.NORMAL);
			
			mVisitedSite.setVisitedTime(System.currentTimeMillis());
			mDbWriter.modifyVisiteSite(mVisitedSite, DBWriter.Action.Update);
			refresh();
		}
	}

	@Override
	public void updateVisitedTitle(VisiteSite mVisitedSite) {
		String siteUrl = mVisitedSite.getSiteUrl();
		VisiteSite exitVs = findVisitedByUrl(siteUrl);
		if (exitVs != null) {
			String siteTitle = mVisitedSite.getSiteTitle();
			exitVs.setSiteTitle(siteTitle);
			updateVisitedSite(exitVs);
		} else if (!StringUtil.isEmpty(mVisitedSite.getSiteTitle())){
			if (!HistoryConfig.isPrivateMode(context))
				mVisitedSite.setIsHistory(DBVisiteSite.HISTORY);
			else 
				mVisitedSite.setIsHistory(DBVisiteSite.NORMAL);
			
			mVisitedSite.setIsBookMark(DBVisiteSite.NORMAL);
			mDbWriter.modifyVisiteSite(mVisitedSite, DBWriter.Action.Add);
			refresh();
		}
	}

	@Override
	public void updateVisitedIcon(VisiteSite mVisitedSite) {
		String siteUrl = mVisitedSite.getSiteUrl();
		VisiteSite exitVs = findVisitedByUrl(siteUrl);
		if (exitVs != null) {
			byte[] icon = mVisitedSite.getIcon();
			exitVs.setIcon(icon);
			updateVisitedSite(exitVs);
		} else if (!StringUtil.isEmpty(mVisitedSite.getSiteTitle())){
			if (!HistoryConfig.isPrivateMode(context))
				mVisitedSite.setIsHistory(DBVisiteSite.HISTORY);
			else 
				mVisitedSite.setIsHistory(DBVisiteSite.NORMAL);
			
			mVisitedSite.setIsBookMark(DBVisiteSite.NORMAL);
			mDbWriter.modifyVisiteSite(mVisitedSite, DBWriter.Action.Add);
			refresh();
		}
		
	}
	
	private VisiteSite findVisitedByUrl(String url) {
		
		for(VisiteSite a: visiteSites) {
			final String siteUrl = a.getSiteUrl();
			if(siteUrl.equals(url)) {
				return a;
			}
		}
		return null;
	}
	
	private void refresh() {
		visiteSites = mDBReader.getAllVisitedSite();
	}

	@Override
	public List<Suggestion> getHistoryVisiteSiteLike(String query) {
		
		if (mDBReader == null)
			mDBReader = (DBReader) this.mServiceFactory.getServiceProvider(DBReader.class);
		
		return mDBReader.getHistoryVisiteSiteLike(query);
	}

	@Override
	public boolean insertOrDelmark(String currentUrl) {
		VisiteSite vs = findVisitedByUrl(currentUrl);
		boolean isBookmark = false;
		if (vs != null) {
			vs.setIsBookMark(1-vs.getIsBookMark());
			isBookmark = DBVisiteSite.BOOKMARK == vs.getIsBookMark();
			insertVisitedSite(vs);
			return isBookmark;
		}
		return false;
	}

	@Override
	public boolean isBookmark(String currentUrl) {
		VisiteSite vs = findVisitedByUrl(currentUrl);
		boolean isBookmark = false;
		if (vs != null) {
			isBookmark = DBVisiteSite.BOOKMARK == vs.getIsBookMark();
			return isBookmark;
		}
		return false;
	}

	@Override
	public void deleteAllHistory() {
		mDbWriter.deleteAllHistorys();		
		refresh();
	}

	@Override
	public List<VisiteSite> getAllHistory() {
		
		 Calendar calendar=new GregorianCalendar(); 
		 calendar.add(Calendar.DATE, -30);
		
		List<VisiteSite> historys = new ArrayList<VisiteSite>();
		for(VisiteSite a: visiteSites) {
			if (a.getIsHistory() == DBVisiteSite.HISTORY && a.getVisitedTime() > calendar.getTimeInMillis())
				historys.add(a);
		}	
		return historys;
	}

	@Override
	public List<VisiteSite> getAllBookmarks() {
		List<VisiteSite> bookmarks = new ArrayList<VisiteSite>();
		for(VisiteSite a: visiteSites) {
			if (a.getIsBookMark() == DBVisiteSite.BOOKMARK)
				bookmarks.add(a);
		}	
		return bookmarks;
	}

	@Override
	public void deleteAllBookmark() {
		mDbWriter.deleteAllBookmarks();	
		refresh();
	}

}
