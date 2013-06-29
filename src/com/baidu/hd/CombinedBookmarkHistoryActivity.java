package com.baidu.hd;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.browser.SearchManager;
import com.baidu.browser.visitesite.VisiteSite;
import com.baidu.browser.visitesite.VisiteSiteManager;
import com.baidu.hd.adapter.BrowserBookmarkAdapter;
import com.baidu.hd.adapter.BrowserHistoryAdapter;
import com.baidu.hd.ctrl.PopupDialog;
import com.baidu.hd.ctrl.PopupDialog.ReturnType;
import com.baidu.hd.player.PlayerLauncher;
import com.baidu.hd.util.Utility;
import com.baidu.hd.R;

/**
 * @ClassName: CombinedBookmarkHistoryActivity 
 * @Description: 历史书签 
 * @author LEIKANG 
 * @date 2012-12-25 下午7:30:08
 */
public class CombinedBookmarkHistoryActivity extends BaseActivity implements OnClickListener, OnItemClickListener{
	
	public static int BOOKMARK = 0;
	
	public static int HISTORY = 1;
	
	private Button btn_bookmarktabs_back;
	
	private Button btn_bookmarktabs_delete;
	
    private LinearLayout bdsettingsTitleBar;
	
	private TextView mark_button;
	
	private TextView history_button;
	
	private TextView empty_text;
	
	private LinearLayout mHistoryListPanel;
	
	private ExpandableListView historylist;
	
	private ListView bookmarklist;
	
    private VisiteSiteManager mVisiteSiteManager;
    
    private List<VisiteSite> mBookmarks = new ArrayList<VisiteSite>();
    
    private List<VisiteSite> mHistorys = new ArrayList<VisiteSite>();
    
    private BrowserBookmarkAdapter mBookmarkAdapter;
    
    private BrowserHistoryAdapter mHistoryAdapter;
    
    private Context mContext;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		mVisiteSiteManager = (VisiteSiteManager)getServiceProvider(VisiteSiteManager.class);
		mContext = this;
        setContentView(R.layout.bookmark_tabs);
        btn_bookmarktabs_back = (Button) findViewById(R.id.btn_bookmarktabs_back);
        btn_bookmarktabs_delete = (Button) findViewById(R.id.btn_bookmarktabs_delete);
        mark_button = (TextView) findViewById(R.id.mark_button);
        history_button = (TextView) findViewById(R.id.history_button);
        empty_text = (TextView) findViewById(R.id.empty_text);
        bdsettingsTitleBar = (LinearLayout)findViewById(R.id.markandhistory_layout);
        
        btn_bookmarktabs_back.setOnClickListener(this);
        btn_bookmarktabs_delete.setOnClickListener(this);
        mark_button.setOnClickListener(this);
        history_button.setOnClickListener(this);
        
        mHistorys = mVisiteSiteManager.getAllHistory();
        mBookmarks = mVisiteSiteManager.getAllBookmarks();
        
        mBookmarkAdapter = new BrowserBookmarkAdapter(this);
        mBookmarkAdapter.setBookmarks(mBookmarks);
        
        mHistoryAdapter = new BrowserHistoryAdapter(this);
        mHistoryAdapter.setHistorys(mHistorys);
        
        mHistoryListPanel = (LinearLayout) findViewById(R.id.historylist_panel);
        
        historylist = (ExpandableListView) findViewById(R.id.historylist);
        bookmarklist = (ListView) findViewById(R.id.bookmarklist);
        
        historylist.setOnItemClickListener(this);
        bookmarklist.setOnItemClickListener(this);
        
        bookmarklist.setAdapter(mBookmarkAdapter);
        historylist.setAdapter(mHistoryAdapter);
        historylist.setGroupIndicator(null);
        historylist.expandGroup(0);
        
        if (BaiduHD.mBookmarksAndHistoryPageIndex == BOOKMARK) {
        	showBookmarks();
        } else {
        	showHistorys();
        }
        
    }
    
    @Override
	protected void onResume() {
    	//overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		super.onResume();
	}

	@Override
	protected void onPause() {
		//overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		super.onPause();
	}

	private void showHistorys() {
       
		bdsettingsTitleBar.setBackgroundResource(R.drawable.tabs_button_history);
		mark_button.setTextColor(Color.BLACK);
		history_button.setTextColor(Color.WHITE);
    	
        if (mHistorys.size() == 0) {
        	empty_text.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.history_empty, 0, 0);
        	empty_text.setText(R.string.history_empty_text);
        	empty_text.setVisibility(View.VISIBLE);
        	
        	mHistoryListPanel.setVisibility(View.GONE);
        	btn_bookmarktabs_delete.setVisibility(View.GONE);
        } else {
        	empty_text.setVisibility(View.GONE);
        	mHistoryListPanel.setVisibility(View.VISIBLE);
        	btn_bookmarktabs_delete.setVisibility(View.VISIBLE);
        }
        bookmarklist.setVisibility(View.GONE);
    }
    
    private void showBookmarks() {
      
		bdsettingsTitleBar.setBackgroundResource(R.drawable.tabs_button_marks);
		mark_button.setTextColor(Color.WHITE);
		history_button.setTextColor(Color.BLACK);
		
    	
        if (mBookmarks.size() == 0) {
        	empty_text.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.bookmark_empty, 0, 0);
        	empty_text.setText(R.string.bookmark_empty_text);
        	empty_text.setVisibility(View.VISIBLE);
        	bookmarklist.setVisibility(View.GONE);
        	btn_bookmarktabs_delete.setVisibility(View.GONE);
        } else {
        	empty_text.setVisibility(View.GONE);
        	bookmarklist.setVisibility(View.VISIBLE);
        	btn_bookmarktabs_delete.setVisibility(View.VISIBLE);
        }
        mHistoryListPanel.setVisibility(View.GONE);
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_bookmarktabs_back:
			finish();
			break;
		case R.id.btn_bookmarktabs_delete:
			
			final boolean isBookmark = BaiduHD.mBookmarksAndHistoryPageIndex == BOOKMARK;
			
			PopupDialog dialog = new PopupDialog(this, new PopupDialog.Callback() {
				public void onReturn(ReturnType type, boolean checked) {
					if(type == PopupDialog.ReturnType.OK) {
						new Handler() {
							public void handleMessage(Message msg) {
								if (isBookmark) {
									mVisiteSiteManager.deleteAllBookmark();
									mBookmarks = new ArrayList<VisiteSite>();
									showBookmarks();
								} else {
									mVisiteSiteManager.deleteAllHistory();
									mHistorys = new ArrayList<VisiteSite>();
									showHistorys();
								}
							}
						}.sendEmptyMessageDelayed(0, 0);
					}
				}
			});
			dialog.setTitle(dialog.createText(isBookmark?R.string.delbookmark:R.string.title_delete_all_history))
			.setMessage(dialog.createText(isBookmark?R.string.delete_all_bookmarks_warning:R.string.delete_history_warning_all))
			.setPositiveButton(dialog.createText(R.string.delete_all))
			.setNegativeButton(dialog.createText(R.string.cancel))
			.show();
			
			break;
		case R.id.mark_button:
			
			showBookmarks();
			BaiduHD.mBookmarksAndHistoryPageIndex = BOOKMARK;
			break;
		case R.id.history_button:
			
			showHistorys();
			BaiduHD.mBookmarksAndHistoryPageIndex = HISTORY;
			break;
		default:
			break;
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String url = "";
		if (BaiduHD.mBookmarksAndHistoryPageIndex == BOOKMARK) {
			url = mBookmarks.get(arg2).getSiteUrl();
		} else {
			url = mHistorys.get(arg2).getSiteUrl();
		}
		if (Utility.isUrl(url))
			SearchManager.launchURL(this,url);
		else if (Utility.isBDHD(url))
			PlayerLauncher.startup(this, url);
	}

}
