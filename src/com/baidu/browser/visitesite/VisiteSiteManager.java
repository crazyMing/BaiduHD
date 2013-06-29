package com.baidu.browser.visitesite;

import java.util.List;

import com.baidu.browser.db.Suggestion;
import com.baidu.hd.service.ServiceProvider;

public interface VisiteSiteManager  extends ServiceProvider {

	void insertVisitedSite(VisiteSite mVisitedSite);

	void updateVisitedTitle(VisiteSite mVisitedSite);
	
	void updateVisitedIcon(VisiteSite mVisitedSite);
	
	List<Suggestion> getHistoryVisiteSiteLike(String query);
	
	List<VisiteSite> getAllHistory();
	
	List<VisiteSite> getAllBookmarks();

	boolean insertOrDelmark(String currentUrl);

	boolean isBookmark(String currentUrl);

	void deleteAllHistory();
	
	void deleteAllBookmark();
	
}
