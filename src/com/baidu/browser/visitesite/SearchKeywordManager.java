package com.baidu.browser.visitesite;

import java.util.List;

import com.baidu.browser.db.Suggestion;
import com.baidu.hd.service.ServiceProvider;

public interface SearchKeywordManager extends ServiceProvider {

	void insertOrUpdateSearchKeyword(SearchKeyword mSearchKeyword);
	
	List<Suggestion> getHistorySearchKeywordLike(String query);

	void deleteAll();

}
