package com.baidu.browser.visitesite;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.baidu.browser.db.Suggestion;
import com.baidu.hd.db.DBReader;
import com.baidu.hd.db.DBWriter;
import com.baidu.hd.service.ServiceConsumer;
import com.baidu.hd.service.ServiceFactory;

public class SearchKeywordManagerImpl  implements SearchKeywordManager, ServiceConsumer{

	private ServiceFactory mServiceFactory = null;
	
	private DBReader mDBReader;
	
	private DBWriter mDbWriter;
	
	private List<SearchKeyword> mSearchKeywords = new ArrayList<SearchKeyword>();
	
	@Override
	public void setContext(Context ctx) {
		
	}

	@Override
	public void onCreate() {
		mDBReader = (DBReader) this.mServiceFactory.getServiceProvider(DBReader.class);
		mDbWriter = (DBWriter) this.mServiceFactory.getServiceProvider(DBWriter.class);
		mSearchKeywords = mDBReader.getAllSearchKeyword();		
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
	public void insertOrUpdateSearchKeyword(SearchKeyword mSearchKeyword) {
		SearchKeyword exitKw = findSearchKeywordByUrl(mSearchKeyword.keyword);
		if (exitKw != null) {
			mSearchKeyword.id = exitKw.id;
			mDbWriter.modifySearchKeyword(mSearchKeyword, DBWriter.Action.Update);
		} else {
			mDbWriter.modifySearchKeyword(mSearchKeyword, DBWriter.Action.Add);
		}
		refresh();
	}

	private SearchKeyword findSearchKeywordByUrl(String query) {
		
		for(SearchKeyword a: mSearchKeywords) {
			final String keyword = a.keyword;
			if(keyword.equals(query)) {
				return a;
			}
		}
		return null;
	}
	
	private void refresh() {
		mSearchKeywords = mDBReader.getAllSearchKeyword();
	}
	
	@Override
	public List<Suggestion> getHistorySearchKeywordLike(String query) {
		return mDBReader.getHistorySearchKeywordLike(query);
	}

	@Override
	public void deleteAll() {
		for(SearchKeyword a: mSearchKeywords) {
			mDbWriter.modifySearchKeyword(a, DBWriter.Action.Delete);
		}
		refresh();
	}

}
