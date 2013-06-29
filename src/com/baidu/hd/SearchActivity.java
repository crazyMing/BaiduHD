package com.baidu.hd;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.baidu.android.ext.widget.SwipeListView;
import com.baidu.browser.BPBrowser;
import com.baidu.browser.SearchManager;
import com.baidu.browser.db.HistoryConfig;
import com.baidu.browser.db.Suggestion;
import com.baidu.browser.visitesite.SearchKeyword;
import com.baidu.browser.visitesite.SearchKeywordManager;
import com.baidu.browser.visitesite.VisiteSite;
import com.baidu.browser.visitesite.VisiteSiteManager;
import com.baidu.browser.visitesite.WebSuggestListParser;
import com.baidu.hd.ctrl.PopupDialog;
import com.baidu.hd.ctrl.PopupDialog.ReturnType;
import com.baidu.hd.module.album.NetVideo.NetVideoType;
import com.baidu.hd.player.PlayerLauncher;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.ui.ClearHistoryClickListener;
import com.baidu.hd.ui.FloatPlayerSearchLayout;
import com.baidu.hd.ui.SuggestionAdapter;
import com.baidu.hd.ui.SuggestionClickListener;
import com.baidu.hd.ui.FloatPlayerSearchLayout.FloatSearchboxMode;
import com.baidu.hd.ui.FloatPlayerSearchLayout.SearchBoxCommand;
import com.baidu.hd.ui.FloatPlayerSearchLayout.SearchBoxCommandListener;
import com.baidu.hd.ui.SuggestionAdapter.ClearLayoutClickType;
import com.baidu.hd.ui.SuggestionAdapter.SuggestionType;
import com.baidu.hd.util.SnifferResultUtil;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.UrlUtil;
import com.baidu.hd.util.Utility;
import com.baidu.hd.util.SnifferResultUtil.SaveResultArgs;
import com.baidu.hd.R;

/**
 * @ClassName: SearchActivity 
 * @Description: 
 * @author LEIKANG 
 * @date 2012-12-11 ����9:52:10
 */
@SuppressLint("NewApi")
public class SearchActivity extends BaseActivity implements SuggestionClickListener, ClearHistoryClickListener{
	
	/** ��� �Ӻδ���������ҳ�� �����ַ�� �� ������ */
	public final static String TAG_IS_START_FROM_SEARCH = "TAG_IS_START_FROM_SEARCH";
	
	public final static String TAG_CURRENT_URL = "TAG_CURRENT_URL";
	
    /** ���뷢�������ļ��. ���Ա������Ƶ���ķ���������*/
    private static final long TYPING_SUGGESTIONS_UPDATE_DELAY_MILLIS = 200;
	
	private boolean isStartFromSearchBtn;
	
    /** ������searchBox,�����ṩ��������ѡ��. */
    public FloatPlayerSearchLayout mFloatPlayerSearchLayout = null;
    
    /** �����. */
    private EditText mQueryTextView;
    
    /** handler. */
    private Handler mHandler = new Handler();
    
    /** ��������adapter. */
    private SuggestionAdapter mAdapter;
    
    /** ��������/��ʷ listview. */
    private SwipeListView mSuggestionsListView;
    
    /** ��ǰ�ؼ���. */
    private String mUserQuery = "";
    
    /** �첽������ʷ��¼ */
    private UpdateHistoryWorker mUpdateHistoryWorker;
    
    /** �첽���ؽ����б�*/
    private UpdateSuggestionWorker mUpdateSuggestionWorker;
    
    /** վ����ʷ��¼manager*/
    private VisiteSiteManager mVisiteSiteManager;
    
    /** ������ʷ��¼manager*/
    private SearchKeywordManager mSearchKeywordManager;
    
    /** ��ʷ��¼ ������ַ��ʷ��¼ �� ������ʷ��¼*/
    private List<Suggestion> mHistorys;
    
    private List<Suggestion> mSuggestions = new ArrayList<Suggestion>();
    
    private Context mContext;
    
    private String currentUrl = "";
    
    private ImageView mBrowTopRefreshStop;
    
    private RelativeLayout search_panel;
    
    
    /** �첽ִ�з������� runnable. */
    private final Runnable mUpdateSuggestionsTask = new Runnable() {
        @Override
        public void run() {
            updateSuggestions(getUserQuery());
        }
    };
    
    /** �첽ִ�з�����ʷ����runnable. */
    final Runnable mUpdateHistoryTask = new Runnable() {
        @Override
        public void run() {
            updateHistory(getUserQuery());
        }
    };
    
	@Override
	protected void onCreate(Bundle arg0) {
		//overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		super.onCreate(arg0);
		setContentView(R.layout.search);
		
		mContext = this;
		
		mVisiteSiteManager = (VisiteSiteManager)getServiceProvider(VisiteSiteManager.class);
		mSearchKeywordManager = (SearchKeywordManager)getServiceProvider(SearchKeywordManager.class);
		
		isStartFromSearchBtn = getIntent().getBooleanExtra(TAG_IS_START_FROM_SEARCH, true);
		currentUrl = getIntent().getStringExtra(TAG_CURRENT_URL);
		
		initUI();
		
		mFloatPlayerSearchLayout = (FloatPlayerSearchLayout) findViewById(R.id.float_MainRoot);
		mFloatPlayerSearchLayout.setEnableStartSearch(true);
		mFloatPlayerSearchLayout.updateMode();
		mFloatPlayerSearchLayout.setIsStartFromSearchButton(isStartFromSearchBtn);
		mFloatPlayerSearchLayout.setSearchBoxCommandListener(mSearchBoxCommandConcrete);
		mFloatPlayerSearchLayout.setOnEditorActionListener(mOnEditorActionListener);
		
		search_panel = (RelativeLayout)findViewById(R.id.search_panel);
		search_panel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();	
			}
		});
		
		
        mQueryTextView = (EditText) mFloatPlayerSearchLayout.findViewById(R.id.SearchTextInput);
        mQueryTextView.addTextChangedListener(new SearchTextWatcher());
        mQueryTextView.requestFocus();
        if (currentUrl == null || currentUrl.equals(BPBrowser.HOME_PAGE) )
        	currentUrl = "";
        else {
        	mQueryTextView.setText(currentUrl);
        	mQueryTextView.setSelection(mQueryTextView.getText().length());
        	mQueryTextView.selectAll();
        }
        if (isStartFromSearchBtn)
        	mQueryTextView.setHint("");

        
        mSuggestionsListView = (SwipeListView) findViewById(R.id.search_suggestion_list);
        mAdapter = new SuggestionAdapter(this, getLayoutInflater(), isStartFromSearchBtn);
        mAdapter.setSuggestionClickListener(this);
        mAdapter.setClearHistoryClickListener(this);
        
        View emptyView = findViewById(R.id.empty_view);
        
        mSuggestionsListView.setEmptyView(emptyView);
        mSuggestionsListView.setAdapter(mAdapter);
        mSuggestionsListView.setItemsCanFocus(true);
        mSuggestionsListView.setDivider(null);
        mSuggestionsListView.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // �������������б�ʱ���������뷨
                Utility.hideInputMethod(SearchActivity.this, mQueryTextView);
            }
            public void onScroll(AbsListView view, int firstVisibleItem, 
                    int visibleItemCount, int totalItemCount) {
            }
        });
        

	} 
	
	
	
	@Override
	protected void onPause() {
		super.onPause();
		//overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}



	@Override
	protected void onResume() {
		super.onResume();
		setUserQuery(currentUrl);
		updateHistory(getUserQuery());
	}



	/**
	 * @ClassName: SearchTextWatcher 
	 * @Description: ��������
	 * @author LEIKANG 
	 * @date 2012-12-12 ����11:06:11
	 */
    private class SearchTextWatcher implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            String query = s.toString();
            setUserQuery(query);
            
            if (TextUtils.isEmpty(query)) {
            	mBrowTopRefreshStop.setVisibility(View.GONE);
                mAdapter.setSuggestionsData(null, SuggestionType.SUGGESTION,getUserQuery()); // ������������б�
                //mFloatPlayerSearchLayout.hideClearContentIcon();
                updateHistory(getUserQuery());
            } else {
            	mBrowTopRefreshStop.setImageResource(R.drawable.searchbox_clear_text);
            	mBrowTopRefreshStop.setVisibility(View.VISIBLE);
            	//mFloatPlayerSearchLayout.showClearContentIcon();
            		updateSuggestionsBuffered();
            	// ʹ����ʱ���²��ԣ��������Ƶ���Ĳ���
            		updateHistoryBuffered();
            }
            
            mFloatPlayerSearchLayout.updateMode();
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        	
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        	
        }
    }
    
    /**
     * ����ʱ���� ��������, ���Ա������Ƶ���ķ���������������ٵ����������֣���ʱ�����ڣ�
     * ����ֱ����һ��������
     */
    private void updateSuggestionsBuffered() {
        mHandler.removeCallbacks(mUpdateSuggestionsTask);
        long delay = TYPING_SUGGESTIONS_UPDATE_DELAY_MILLIS;
        mHandler.postDelayed(mUpdateSuggestionsTask, delay);
    }
    
    /**
     * ����ʱ���� ������ʷ����, ���Ա������Ƶ���ķ���������������ٵ����������֣���ʱ�����ڣ�
     * ����ֱ����һ��������
     */
    private void updateHistoryBuffered() {
        mHandler.removeCallbacks(mUpdateHistoryTask);
        long delay = TYPING_SUGGESTIONS_UPDATE_DELAY_MILLIS;
        mHandler.postDelayed(mUpdateHistoryTask, delay);
    }
    
    /**
     * @Title: setUserQuery 
     * @Description:���ò�ѯ�ؼ���
     * @param userQuery   
     */
    protected void setUserQuery(String userQuery) {
        if (userQuery == null) {
            userQuery = "";
        }
        mUserQuery = userQuery;
        if (mAdapter != null) {
            mAdapter.setQuery(userQuery);
        }
    }
    
    private String getUserQuery() {
        if (mUserQuery == null) {
            return  "";
        }
        return mUserQuery;
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//���뷨����ʱ�����menu���������뷨
    	if (keyCode == KeyEvent.KEYCODE_MENU && Utility.isInputMethodActive(this, mQueryTextView)) {
			Utility.hideInputMethod(this, mQueryTextView);
			return true;
    	} 
		return super.onKeyDown(keyCode, event);
	}
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // activity �������һ����ʾ���뷨�����
            // Launch the IME after a bit
            Runnable showInputMethodTask = new Runnable() {
                public void run() {
                    Utility.showInputMethod(SearchActivity.this, mQueryTextView);
                }
            };
            mHandler.postDelayed(showInputMethodTask, 0);
        }
    }
    
    /**
     * @Title: initUI 
     * @Description: �����ַ�����ʱ���� �����UI��ʾ����
     */
    private void initUI() {
		findViewById(R.id.brow_top_mark).setVisibility(View.GONE);
		findViewById(R.id.devise).setVisibility(View.GONE);
		mBrowTopRefreshStop = (ImageView)findViewById(R.id.brow_top_refresh_stop);
		mBrowTopRefreshStop.setVisibility(View.GONE);
		mBrowTopRefreshStop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mQueryTextView.setText("");
			}
		});
    }

    /**
     * ���÷���������ť�ĵ���¼���������Ϊ��ʱ���Ƿ��ز����������ݲ�Ϊ��ʱ������������
     */
    private final SearchBoxCommandListener mSearchBoxCommandConcrete = new SearchBoxCommandListener() {
        @Override
        public void executeSearchBoxCommand(SearchBoxCommand command) {
            if (command == null) {
                return;
            }
            
            FloatSearchboxMode mode = command.currentMode;
            switch (mode) {
            case SEARCH_CANCEL:
                finish();
                break;
            case SEARCH_GO:
                executeSearchCommand();
                finish();
                break;
            case SEARCH_VISIT:
                executeVisitCommand();
                finish();
                break;
            case SEARCH_BDHD:
            	executeBdhdCommand();
            	finish();
            	break;
            default:
                break;
            }
        }
    };
    
    /**
     * ��Ӽ��̶���������������������¼�������������
     */
    private final OnEditorActionListener mOnEditorActionListener = new OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        	if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL) { 
        		boolean empty = (mUserQuery!=null)&&(!mUserQuery.equals(""));
        		if (empty)
        			mFloatPlayerSearchLayout.startSearchToplayer(mUserQuery);
        		return empty;
            } 
        	return false;
        }
    };
    
    /**
     * @Title: executeSearchCommand 
     * @Description: ִ������������������� 
     * @param command   
     */
    private void executeSearchCommand() { 
        if (TextUtils.isEmpty(mUserQuery))  // ������Ϊ�� ֱ�ӷ���
            return; 
        String c=mUserQuery.replaceAll(" ","");
        if (c.length() == 0) {
            String url = Utility.fixUrl("m.baidu.com").trim();
            url = Utility.addSchemeIfNeed(url);
            SearchManager.launchURL(mContext, url);
            
        } else {
        	SearchManager.launchSearch(this, mUserQuery);
        	if (!HistoryConfig.isPrivateMode(mContext))
        		saveSearchKeyword();
        	
        }
        
        finish();
    }
    
    /**
     * @Title: saveSearchKeyword 
     * @Description: ����ؼ��� 
     * @param     �趨�ļ� 
     * @return void    �������� 
     * @throws
     */
    private void saveSearchKeyword(){
    	SearchKeyword searchKeyword = new SearchKeyword();
    	searchKeyword.keyword = mUserQuery;
    	searchKeyword.searchTime = System.currentTimeMillis();
    	mSearchKeywordManager.insertOrUpdateSearchKeyword(searchKeyword);
    }
    
    private void saveSearchKeyword(String suggestionString) {
    	SearchKeyword searchKeyword = new SearchKeyword();
    	searchKeyword.keyword = suggestionString;
    	searchKeyword.searchTime = System.currentTimeMillis();
    	mSearchKeywordManager.insertOrUpdateSearchKeyword(searchKeyword);
    }
    
    
    /**
     * @Title: executeVisitCommand 
     * @Description: ��query��ʶ��Ϊurl��ֱ�ӷ����������
     * @param command   
     */
    private void executeVisitCommand() {
        String url = mUserQuery;
        url = Utility.fixUrl(url).trim();
        url = Utility.addSchemeIfNeed(url);
        
        SearchManager.launchURL(this,url);
        
        finish();
    }
    
    /**
     * @Title: executeBdhdCommand 
     * @Description: query ��ʶ��Ϊbdhd��ַ��ֱ�ӵ��𲥷������в���
     * @param command   
     */
    private void executeBdhdCommand() {
    	saveBDHDHistory();
    	
		ServiceFactory factory = (ServiceFactory) ((BaseActivity) mContext)
				.getPlayerApp().getServiceFactory();
		// �޸�Ϊ
		SaveResultArgs args = SnifferResultUtil.getInstance()
				.saveToDatabase(factory, mUserQuery, null, null,	NetVideoType.P2P_STREAM);
		if (args != null) {
			PlayerLauncher.startup(this, args.album,
					args.currentVideo);
		}
    	
    	finish();
    }
    
    /**
     * @Title: saveBDHDHistory 
     * @Description: ����BDHD��ʷΪ�������ʷ
     * @param     �趨�ļ� 
     * @return void    �������� 
     * @throws
     */
    private void saveBDHDHistory() {
    	VisiteSite mVisitedSite = new VisiteSite();
	    mVisitedSite.setSiteUrl(mUserQuery);
	    mVisitedSite.setSiteTitle(StringUtil.getNameForUrl(mUserQuery));
	    mVisitedSite.setIcon(null);
	    mVisitedSite.setCreateTime(System.currentTimeMillis());
	    mVisitedSite.setVisitedTime(System.currentTimeMillis());
	    mVisiteSiteManager.insertVisitedSite(mVisitedSite);
    }
    
    /**
     * @Title: updateSuggestions 
     * @Description: ���½����б����������ַ����ַ����������� �ؼ��ֽ���
     * @param  query    �趨�ļ� 
     * @return void    �������� 
     * @throws
     */
    private void updateSuggestions(String query) {
        
        if (mUpdateSuggestionWorker != null) {
            // ������һ��δʵ����ϵ��߳�
        	mUpdateSuggestionWorker.interrupt();
        }
        
        UpdateSuggestionWorker worker = new UpdateSuggestionWorker(query);
        mUpdateSuggestionWorker = worker;
        worker.start();
    }
    
    /**
     * @Title: updateHistory 
     * @Description: ������ʷ
     * @param  query    �趨�ļ� 
     * @return void    �������� 
     * @throws
     */
    private void updateHistory(String query) {
        //if (HistoryConfig.isPrivateMode(this)) {
            //mAdapter.setSuggestionsData(null, SuggestionType.HISTORY, mUserQuery);
            //return; // ��˽ģʽ����ʾ��ʷ��¼
        //}
        
        if (mUpdateHistoryWorker != null) {
            // ������һ��δʵ����ϵ��߳�
            mUpdateHistoryWorker.interrupt();
        }
        
        UpdateHistoryWorker worker = new UpdateHistoryWorker(query);
        mUpdateHistoryWorker = worker;
        worker.start();
    }
    
    
    /**
     * ������ʷ�����߳�
     * ���ݿ��¼��Ļ�����һ���ĺ�ʱ����Ҫʹ�ö����߳̽��и��¡�
     */
    private class UpdateHistoryWorker extends Thread {
        /** �˴�������Ӧ�Ĺؼ���. */
        private final String mQuery;

        /**
         * ���캯��.
         * @param query ������
         * @param historySource ������ʷ��¼��Դ
         */
        private UpdateHistoryWorker(String query) {
            super();
            mQuery = query;
            setName("UpdateHistoryWorker");
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
 
            if (mVisiteSiteManager == null)
            	mVisiteSiteManager = (VisiteSiteManager)getServiceProvider(VisiteSiteManager.class);
            
            if (isStartFromSearchBtn)
            	mHistorys = mSearchKeywordManager.getHistorySearchKeywordLike(mQuery);
            else
            	mHistorys = mVisiteSiteManager.getHistoryVisiteSiteLike(mQuery);
            
            mUpdateHistoryWorker = null;
            
            String currentQuery = mUserQuery;
            if (mQuery.equals(currentQuery)) {
                mHandler.post(new Runnable() {
                    public void run() {
                        mAdapter.setSuggestionsData(mHistorys, SuggestionType.HISTORY,mUserQuery);
                    }
                });
            }
        }
    }
    
    /**
     * ������ʷ�����߳�
     * ����������Ҫ�����ϻ�ȡ����һ���ĺ�ʱ����Ҫʹ�ö����߳̽��и��¡�
     */
    private class UpdateSuggestionWorker extends Thread {
        /** �˴�������Ӧ�Ĺؼ���. */
        private final String mQuery;
        private UpdateSuggestionWorker(String query) {
            super();
            mQuery = query;
            setName("UpdateSuggestionWorker");
        }
		@Override
		public void run() {
			
	    	if (!isStartFromSearchBtn) {
	    		mSuggestions = getAdressSuggestions(mQuery);
		    	mHandler.post(new Runnable() {
		    		public void run() {
		    			mAdapter.setSuggestionsData(mSuggestions, SuggestionType.SUGGESTION,mUserQuery);
		    		}
		    	});
	    	}
	    	else {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
				HttpClient client = new DefaultHttpClient();
				
				String serverFormat = SearchManager.keywordUrl;
				String server = String.format(serverFormat, UrlUtil.encode(mQuery));
	            HttpGet method = new HttpGet(server);
	            
	            try {
					HttpResponse response = client.execute(method);
	                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	                    if (!isInterrupted()) { // �����߳�Interrupt�󲻻�ֹͣ�����������������������жϡ�
	                    	InputStream result = response.getEntity().getContent();
	                    	parseSuggestion(result);
	                    }
	                }  
				} catch (Exception e) {
					e.printStackTrace();
				} 
	            
	            
	    	}
			 
		}
    }
    
	/**
	* ����server���ص����������б�
	* @param inputStream ������.
	*/
    private void parseSuggestion(InputStream inputStream) {
    	
    	String returnString = Utility.getStringFromInput(inputStream);
    	mSuggestions.clear();
    	
    	mSuggestions = WebSuggestListParser.Parse(this,returnString);
    	
    	mHandler.post(new Runnable() {
    		public void run() {
    			mAdapter.setSuggestionsData(mSuggestions, SuggestionType.SUGGESTION,mUserQuery);
    		}
    	});

    }
    
    /**
     * @Title: getAdressSuggestions 
     * @Description: ������������Ϊ�û��ṩ���ٵ������
     * @param  query
     * @param     �趨�ļ� 
     * @return List<Suggestion>    �������� 
     * @throws
     */
    private List<Suggestion> getAdressSuggestions(String query) {
    	List<Suggestion> urlSuggestions = new ArrayList<Suggestion>();
    	query = query.replace(" ", "");
    	/** ��ַ���� */
    	if (query.matches("h|ht|htt|http|http:|http:/|http://|http://w|http://ww|http://www|http://www.")) {
    		Suggestion urlSuggestion = new Suggestion();
    		urlSuggestion.title = "http://www.";
    		urlSuggestion.url = "http://www.";
    		urlSuggestion.type = SuggestionType.SUGGESTION;
    		urlSuggestions.add(urlSuggestion);
    	} else if (query.matches("w|ww|www|www.")) {
    		Suggestion urlSuggestion = new Suggestion();
    		urlSuggestion.title = ("www.");
    		urlSuggestion.url = ("www.");
    		urlSuggestion.type = SuggestionType.SUGGESTION;
    		urlSuggestions.add(urlSuggestion);
    	} else if (query.matches("^[a-zA-Z0-9]*[.][a-zA-Z0-9]+|^[a-zA-Z0-9]*|www.[a-zA-Z0-9]*|http://www.[a-zA-Z0-9]*|www.[a-zA-Z0-9]*")){
    		if (!query.equals("")) {
    			Suggestion urlSuggestion = new Suggestion();
    			urlSuggestion.title = (query+".com");
    			urlSuggestion.url = (query+".com");
    			urlSuggestion.type = SuggestionType.SUGGESTION;
    			urlSuggestions.add(urlSuggestion);
    		}
    	} else if (query.matches("http://www.[a-zA-Z0-9]*[.]|www.[a-zA-Z0-9]*[.]|^[a-zA-Z0-9]*[.]|^[a-zA-Z0-9]*[.][a-zA-Z0-9]*[.]")) {
    		if (!query.equals("")) {
    			Suggestion urlSuggestion = new Suggestion();
    			urlSuggestion.title = (query+"com");
    			urlSuggestion.url = (query+"com");
    			urlSuggestion.type = SuggestionType.SUGGESTION;
    			urlSuggestions.add(urlSuggestion);
    		}
    	}
    	
    	/** BDHD��ַ����*/
    	if (query.matches("b|bd|bdh|bdhd|bdhd:|bdhd:/|bdhd://")){
    		Suggestion urlSuggestion = new Suggestion();
    		urlSuggestion.title = "bdhd://";
    		urlSuggestion.url = "bdhd://";
    		urlSuggestion.type = SuggestionType.SUGGESTION;
    		urlSuggestions.add(urlSuggestion);
    	}
    	
    	return urlSuggestions;
    	
    }

	@Override
	public void onSuggestionClicked(Suggestion suggestion) {
		if (isStartFromSearchBtn) {
			if (TextUtils.isEmpty(suggestion.title)) 
				return; 
			SearchManager.launchSearch(this, suggestion.title);
			
			if (!HistoryConfig.isPrivateMode(mContext))
				saveSearchKeyword(suggestion.title);
			finish();
		} else {
			mQueryTextView.setText(suggestion.url);
			mQueryTextView.setSelection(mQueryTextView.getText().length());
		}
	}

	@Override
	public void onSuggestionQueryRefineClicked(Suggestion suggestion) {
		
	}



	@Override
	public void onClearHistoryClicked(ClearLayoutClickType clearLayoutClickType) {
		
        Utility.hideInputMethod(SearchActivity.this, mQueryTextView);
		
		switch (clearLayoutClickType) {
		case GO_SEARCH:
	        if (TextUtils.isEmpty(mUserQuery)) 
	            return; 
	        SearchManager.launchSearch(this, mUserQuery);
	        saveSearchKeyword();
			break;
		case CLEAR_HISTORY:
			
			PopupDialog dialog = new PopupDialog((BaseActivity)mContext, new PopupDialog.Callback() {
				public void onReturn(ReturnType type, boolean checked) {
					if(type == PopupDialog.ReturnType.OK) {
						new Handler() {
							public void handleMessage(Message msg) {
								if (!isStartFromSearchBtn) {
									mVisiteSiteManager.deleteAllHistory();
									updateHistory("");	
								}
								else {
									mSearchKeywordManager.deleteAll();
									updateHistory("");
									updateSuggestions("");
								}
							}
						}.sendEmptyMessageDelayed(0, 0);
					}
				}
			});
			dialog.setTitle(dialog.createText(!isStartFromSearchBtn?R.string.web_clear_history:R.string.search_clear_history))
			.setMessage(dialog.createText(!isStartFromSearchBtn?R.string.web_clear_history_tips:R.string.search_clear_history_tips))
			.setPositiveButton(dialog.createText(R.string.delete_all))
			.setNegativeButton(dialog.createText(R.string.cancel))
			.show();
			
			
			break;
			
		case PLAY_BDHD:
			executeBdhdCommand();
			
		default:
			break;
		}
	}
	

}
