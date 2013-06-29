package com.baidu.hd.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.android.ext.widget.SwipeAdapter;
import com.baidu.browser.db.HistoryConfig;
import com.baidu.browser.db.Suggestion;
import com.baidu.hd.util.Utility;
import com.baidu.hd.R;

/**
 * @ClassName: SuggestionAdapter 
 * @Description: 搜索建议adapter
 * @author LEIKANG 
 * @date 2012-12-12 下午12:20:12
 */
public class SuggestionAdapter extends SwipeAdapter{
	
	/** 搜索建议view type */
	private static final int VIEW_TYPE_SUG = 0;
	
	/** 隐私模式开关view type */
	private static final int VIEW_TYPE_SWITCH_PRIVATE_MODE = 1;
	
	/** 隐私模式提示view type */
	private static final int VIEW_TYPE_PRIVATE_MODE_TIP = 2;
	
	/** view type count */
	private static final int VIEW_TYPE_COUNT = 3;
	
	/** VIEW_TAG_KEY */
	private static final int VIEW_TAG_KEY = ((0x2f << 24) | android.R.id.summary);
	
	private Context mAdapterContext;

	private LayoutInflater mInflater;
	
	private List<Suggestion> mHistorys;
	
	private List<Suggestion> mSuggestions;
	
	private List<Suggestion> mDisplaySuggestions;
	
	private boolean mIsStartFromSearchBox;
	
    /** 是否需要显示清除历史 */
    private boolean mNeedShowSwitchPrivateModeItem;
    
    /** 当前搜索词。 */
    private String mQuery;
    
    /** 清空历史记录 item onClickListener. */
    private ClearHistoryClickListener mClearHistoryClickListener;
    
    /** 打开隐私模式的onClickListener */
    private OnClickListener mSwitchPrivateModeClickListener;
    
    /** 建议列表点击. */
    private SuggestionClickListener mSuggestionClickListener;
    
    /** item text对应的 click listener. */
    private final ItemClickListener mItemClickListener = new ItemClickListener();
    
    private final ClearClickListener mClearClickListener = new ClearClickListener();
    
    public static enum SuggestionType { SUGGESTION, HISTORY };
    
    public static enum ClearLayoutClickType {GO_SEARCH, CLEAR_HISTORY, PLAY_BDHD};
	
    public SuggestionAdapter(Context mContext, LayoutInflater inflater, boolean isStartFromSearchBox) {
        mAdapterContext = mContext;
        mInflater = inflater;
        mHistorys = new ArrayList<Suggestion>();
        mSuggestions = new ArrayList<Suggestion>();
        mDisplaySuggestions = new ArrayList<Suggestion>();
        
        mIsStartFromSearchBox = isStartFromSearchBox;
        Utility.setScreenDensity(mContext);
    }

	@Override
	public int getCount() {
        int count = mDisplaySuggestions.size();
        /**
        if (TextUtils.isEmpty(mQuery) && HistoryConfig.isPrivateMode(mAdapterContext)) 
            count = 1;
        */
        setNeedShowSwitchPrivateModeItem();
        
        if (mNeedShowSwitchPrivateModeItem) 
            count += 1;
        
        return count;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
    @Override
    public int getItemViewType(int position) {
    	/**
        if (TextUtils.isEmpty(mQuery) && HistoryConfig.isPrivateMode(mAdapterContext)) {
            if(position == 0) 
                return VIEW_TYPE_PRIVATE_MODE_TIP;
             else 
                return VIEW_TYPE_SWITCH_PRIVATE_MODE;
        }
        */
        if (position >= mDisplaySuggestions.size()) 
            return VIEW_TYPE_SWITCH_PRIVATE_MODE;
        return VIEW_TYPE_SUG;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int viewType = getItemViewType(position);
		
        if (convertView != null) {
            if (((Integer) convertView.getTag(VIEW_TAG_KEY)).intValue() != viewType) {
                convertView = null;
            }
        }
        
        if (convertView == null) {
            int resource = R.layout.suggestion_item;
            if (viewType == VIEW_TYPE_SWITCH_PRIVATE_MODE) {
                resource = R.layout.suggestion_clear_history_item;
            } else if(viewType == VIEW_TYPE_PRIVATE_MODE_TIP) {
                resource = R.layout.suggestion_private_mode_tip_item;
            } 

            convertView = mInflater.inflate(resource, parent, false);
            // 标记此view的类型
            convertView.setTag(VIEW_TAG_KEY, Integer.valueOf(viewType));
        }
        
        if (viewType == VIEW_TYPE_SWITCH_PRIVATE_MODE) {
            buildSwitchPrivateModeView(convertView);
        } else if(viewType == VIEW_TYPE_PRIVATE_MODE_TIP) {
            // 隐藏模式下的提示条
        } else {
            buildSugView(position, convertView, parent);
        }
        
		return convertView;
	}
	
	/**
	 * @Title: buildSwitchPrivateModeView 
	 * @Description: 构造建议提示 包括 清空历史，添加百度HD关键字， 搜索关键字‘xxx’，隐私模式等等
	 * @param convertView   
	 */
    private void buildSwitchPrivateModeView(View convertView) {
        View itemClearHistoryLayout = convertView.findViewById(R.id.suggestion_clear_history_layout);     
        TextView itemClearHistory = (TextView) convertView.findViewById(R.id.suggestion_clear_history);
        TextView itemOpenPrivateMode = (TextView) convertView.findViewById(R.id.suggestion_open_private_mode);
        itemClearHistory.setOnClickListener(mClearClickListener);
        itemOpenPrivateMode.setOnClickListener(mSwitchPrivateModeClickListener);
        
        if (TextUtils.isEmpty(mQuery) && mSuggestions.size() == 0 && mHistorys.size() == 0) {
            itemClearHistoryLayout.setVisibility(View.VISIBLE);
            itemClearHistory.setEnabled(false);
            itemClearHistory.setText(mIsStartFromSearchBox ? R.string.no_search_history : R.string.no_web_history);
        } 
        
        if (mHistorys.size() > 0){
            itemClearHistoryLayout.setVisibility(View.VISIBLE);
            itemClearHistory.setEnabled(true);
           	itemClearHistory.setText(mIsStartFromSearchBox ? R.string.search_clear_history : R.string.web_clear_history);
           	itemClearHistory.setTag(ClearLayoutClickType.CLEAR_HISTORY);
        }
        
        if (Utility.isBDHD(mQuery)) {
        	itemClearHistory.setText(R.string.brow_play_bdhd);
        	itemClearHistory.setTag(ClearLayoutClickType.PLAY_BDHD);
        } else if (!TextUtils.isEmpty(mQuery) && !Utility.isUrl(mQuery) && mHistorys.size() == 0) {
            itemClearHistoryLayout.setVisibility(View.VISIBLE);
            itemClearHistory.setEnabled(true);
            if (mQuery.length()>10)
            	mQuery = mQuery.substring(0, 6) + "...";
        	String format = mAdapterContext.getResources().getString(R.string.brow_address_go_search_text);
        	String text = String.format(format, mQuery);
        	itemClearHistory.setText(text);
        	itemClearHistory.setTag(ClearLayoutClickType.GO_SEARCH);
        	
        	if(mSuggestions.size() == 0 && mIsStartFromSearchBox) {
        		itemClearHistory.setEnabled(false);
        		itemClearHistory.setText(R.string.brow_search_no_suggestion);	
        	}
        }
        
        if (HistoryConfig.isPrivateMode(mAdapterContext)) {
            itemOpenPrivateMode.setText(R.string.search_close_private_mode);
        } else {
            itemOpenPrivateMode.setText(R.string.suggestion_open_private_mode);
        }
    }
    
    /**
     * @Title: buildSugView 
     * @Description: 构造搜索建议 
     * @param position
     * @param convertView
     * @param parent   
     */
    private void buildSugView(int position, View convertView, ViewGroup parent) { 
        TextView title = (TextView) convertView.findViewById(R.id.suggestion_item_title);
        TextView description = (TextView) convertView.findViewById(R.id.suggestion_item_description);
        ImageView icon = (ImageView) convertView.findViewById(R.id.suggestion_item_icon);
        
        Suggestion suggestion = mDisplaySuggestions.get(position);
        
        switch (suggestion.type) {
		case HISTORY:
			
			if (mIsStartFromSearchBox)
				icon.setImageResource(R.drawable.history_favicon_normal);
			else
				icon.setImageResource(R.drawable.history_favicon_normal);
			
			if (mIsStartFromSearchBox)
				description.setVisibility(View.GONE);
			else
				description.setVisibility(View.VISIBLE);
			
			title.setText(suggestion.title);
			description.setText(suggestion.url);
			break;
		case SUGGESTION:
			
			if (mIsStartFromSearchBox)
				icon.setImageResource(R.drawable.search_sug_keywords_normal);
			else
				icon.setImageResource(R.drawable.navigator_edit_default_icon_available);
			
			title.setText(suggestion.title);
			description.setVisibility(View.GONE);
			break;
		default:
			break;
		}
        
        
        View item = convertView.findViewById(R.id.suggestion_item_layout);
        item.setTag(suggestion);
        item.setOnClickListener(mItemClickListener);
        // 最后一个要变背景，统一设背景
        int resId = R.drawable.suggestion_list_selector_bg;
        if (position == getCount() - 1) {
            resId = R.drawable.suggestion_list_selector_simple_bg;
        }
        
        item.setBackgroundResource(resId);
        
    }

	@Override
	protected int removeAndInsert(int removePos) {
		return 0;
	}

	@Override
	public int getSwipeAction(int position) {
		return 0;
	}
	
    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
    
	
    /**
     * 设置当前搜索词。
     * @param query 搜索词
     */
    public void setQuery(String query) {
        mQuery = query;
        // 由于清除历史记录显示状态有变化会改变getCount(清除历史按钮在“隐私模式”item上)，需要notify
        boolean oldNeedShowSwitchPrivateModeItem = mNeedShowSwitchPrivateModeItem;
        setNeedShowSwitchPrivateModeItem();
        if (oldNeedShowSwitchPrivateModeItem != mNeedShowSwitchPrivateModeItem) {
            notifyDataSetChanged();
        }
    }
    
    /**
     * 设置匹配当前搜索词的历史记录。
     * @param history  匹配当前搜索词的历史记录
     */
    private  void setHistory(List<Suggestion> history) {
        mHistorys.clear();
        // 不能直接引用源数据，源数据会被非UI线程修改
        if (history != null) {
            for (Suggestion item : history) {
                mHistorys.add(item);
            }
        }
    }
    
    /**
     * @Title: setAdvises 
     * @Description: 设置当前匹配网址建议 
     * @param  advises    设定文件 
     * @return void    返回类型 
     */
    private void setSuggestion(List<Suggestion> suggestions) {
    	mSuggestions.clear();
        if (suggestions != null) {
            for (Suggestion item : suggestions) {
            	mSuggestions.add(item);
            }
        }
    }
    
    /**
     * @Title: rebuildDisplayList 
     * @Description: 重组显示列表
     * @param     设定文件 
     * @return void    返回类型 
     * @throws
     */
    private void rebuildDisplayList() {
    	mDisplaySuggestions.clear();
    	
    	if (mIsStartFromSearchBox) {
    		
    		int maxHistorySize = 3;
    		if (mSuggestions.size()<5)
    			maxHistorySize = 8-mSuggestions.size();
    		
    		int count = 1;
    		for (Suggestion item : mHistorys) {
    			if (count <= maxHistorySize) {
    				mDisplaySuggestions.add(item);
    				count++;
    			}
    		}
			for (Suggestion item : mSuggestions) {
				if (count <= 8) {
					mDisplaySuggestions.add(item);
					count++;
				}
			}
    	} else {
    		int count = 1;
    		for (Suggestion item : mSuggestions) {
    			if (count <= 3) {
    				mDisplaySuggestions.add(item);
    				count++;
    			}
    		}
    		for (Suggestion item : mHistorys) {
    			if (count <= 8) {
    				mDisplaySuggestions.add(item);
    				count++;
    			}
    		}
    	}
    }
    
    
    public synchronized void setSuggestionsData(List<Suggestion> suggestions, SuggestionType type,
            String query) {
        
        if(query == null) {
            query = "";
        }
        
        if(!mQuery.equals(query)) {
            mQuery = query;
        }
        
    	if (type == SuggestionType.HISTORY) {
    		setHistory(suggestions);
    	} else if (type == SuggestionType.SUGGESTION) {
    		setSuggestion(suggestions);
    	}  
    	
    	rebuildDisplayList();
    	
    	notifyDataSetChanged();
    }
    
    /**
     * 设置是否需要显示清除历史
     */
    private void setNeedShowSwitchPrivateModeItem() {
        boolean result = false;
        if (TextUtils.isEmpty(mQuery)) {
            result = true;
        } else if (!Utility.isUrl(mQuery)) {
            result = true;
        }
        mNeedShowSwitchPrivateModeItem = result;
    }
    
    /**
     * set suggestion/history item click listener.
     * @param listener  the listener
     */
    public void setSuggestionClickListener(SuggestionClickListener listener) {
        mSuggestionClickListener = listener;
    }
    
    /**
     * 设置清空历史记录 click listener.
     * @param listener listener
     */
    public void setClearHistoryClickListener(ClearHistoryClickListener listener) {
        mClearHistoryClickListener = listener;
    }
    
    /**
     * 打开隐私模式的 click listener.
     * @param listener listener
     */
    public void setSwitchPrivateModeClickListener(OnClickListener listener) {
        mSwitchPrivateModeClickListener = listener;
    }
    
    private class ItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Suggestion suggestion = (Suggestion) v.getTag();
            if (suggestion != null && mSuggestionClickListener != null) {
                mSuggestionClickListener.onSuggestionClicked(suggestion);
            }
        }
    }
    
    private class ClearClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			ClearLayoutClickType cct = (ClearLayoutClickType) v.getTag();
            if (cct != null && mClearHistoryClickListener != null) {
            	mClearHistoryClickListener.onClearHistoryClicked(cct);
            }
		}
    	
    }

}
