package com.baidu.hd.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.baidu.browser.ui.IconTextView;
import com.baidu.browser.visitesite.VisiteSite;
import com.baidu.hd.R;

/**
 * @ClassName: BrowserHistoryAdapter 
 * @Description: ä¯ÀÀÆ÷ÀúÊ·
 * @author LEIKANG 
 * @date 2012-12-25 ÏÂÎç7:11:59
 */
public class BrowserBookmarkAdapter extends BaseAdapter{
	
	private List<VisiteSite> mBookmarks = new ArrayList<VisiteSite>();
	
	private Context mContext;
	
	public BrowserBookmarkAdapter(Context context) {
		mContext = context;
	}
	
	
	public void setBookmarks(List<VisiteSite> bookmarks) {
		mBookmarks = bookmarks;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mBookmarks.size();
	}

	@Override
	public Object getItem(int position) {
		return mBookmarks.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new IconTextView(mContext);
        }
        
        bind((IconTextView) convertView, position);

        return convertView;
	}
	
	private void bind(IconTextView view, int position) {
		VisiteSite bookmark = mBookmarks.get(position);
		view.setTopText(bookmark.getSiteTitle());
		view.setBottomText(bookmark.getSiteUrl());
		view.getRightView().setVisibility(View.GONE);
		view.setLeftView(R.drawable.bookmark_favicon);
		view.setTag(bookmark);
	}

}
