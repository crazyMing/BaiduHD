package com.baidu.hd.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.baidu.hd.util.Utility;
import com.baidu.hd.R;
/**
 * @ClassName: FloatPlayerSearchLayout 
 * @Description: 
 * 加载所有于浮动的搜索框相关的view,并处理动画及各种点击事件. 使用方法:
 * 
 *         LayoutParams mfloatBoxLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
 *         LayoutParams.FILL_PARENT);
 * 		   LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 * 		   FloatSearchBoxLayout mfloatSearchBoxLayout = (FloatSearchBoxLayout) inflater.inflate(
 *         R.layout.searchbox, null);
 * 		   YourActivity.this.addContentView(mfloatSearchBoxLayout, mfloatBoxLayoutParams);
 * 
 * @author LEIKANG 
 * @date 2012-12-10 下午8:00:06
 */
public class FloatPlayerSearchLayout extends RelativeLayout{
	
	
    public enum FloatSearchboxMode {
        /**发起搜索就绪模式。*/
        SEARCH_GO,
        /**取消搜索就绪模式。*/
        SEARCH_CANCEL,
        /**访问按钮模式. */
        SEARCH_VISIT,  
        /** about settings模式.*/
        SEARCH_BDHD;
    }
	
	
	private Context mContext;
	
    /** 搜索框内容. */
    private EditText mSearchTextInput = null;
    
    /** 发起搜索或取消按钮。 */
    private TextView mSearchOrCancelView = null;
    
    /** 当前搜索框模式.*/
    private FloatSearchboxMode currentMode = FloatSearchboxMode.SEARCH_CANCEL;
    
    /** 搜索框命令监听者.*/
    private SearchBoxCommandListener mSearchBoxCommandListener;
    
    private boolean isStartFromSearchButton = false;
	
	/**
	 * @param context
	 */
	public FloatPlayerSearchLayout(Context context) {
		super(context);
		this.mContext = context;
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
    public FloatPlayerSearchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }
    
    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public FloatPlayerSearchLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init(mContext);
    }
    
    /**
     * @Title: init 
     * @Description: 初始化
     * @param context   
     */
    private void init(Context context) {
    	mSearchTextInput = (EditText) findViewById(R.id.SearchTextInput);
    	mSearchOrCancelView = (TextView) findViewById(R.id.float_search_or_cancel);
        mSearchOrCancelView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startSearchToplayer(mSearchTextInput.getText().toString());
            }
        });
    }

	public void setEnableStartSearch(boolean enableStartSearch) {
			mSearchTextInput.setFocusable(enableStartSearch);
        	mSearchTextInput.setFocusableInTouchMode(enableStartSearch);
	}

	public void setStopLoadingOnClickListener(OnClickListener onClickListener) {
		// TODO Auto-generated method stub
		
	}

	public void showStopLoadingIcon() {
		// TODO Auto-generated method stub
		
	}

	public void hideStopLoadingIcon() {
		// TODO Auto-generated method stub
		
	}
	
    public void setIsStartFromSearchButton(boolean is) {
    	isStartFromSearchButton = is;
        mSearchTextInput.setImeOptions(is?EditorInfo.IME_ACTION_SEARCH:EditorInfo.IME_ACTION_DONE);
    }
	
    public void setSearchBoxCommandListener(SearchBoxCommandListener listener) {
        mSearchBoxCommandListener = listener;
    }
	
	
	
    public void startSearchToplayer(String query) {
        if (mSearchBoxCommandListener != null) {
            SearchBoxCommand command = new SearchBoxCommand();
            command.currentMode = currentMode;
            mSearchBoxCommandListener.executeSearchBoxCommand(command);
        }
    }

	/**
	 * @Title: updateMode 
	 * @Description: 刷新搜索模式   
	 */
	public void updateMode() {
		FloatSearchboxMode mode = FloatSearchboxMode.SEARCH_CANCEL;	
		String query = mSearchTextInput.getText().toString();
        if (TextUtils.isEmpty(query)) {
            mode = FloatSearchboxMode.SEARCH_CANCEL;
        } else {
            String tmp = Utility.fixUrl(query).trim();
            
            if (Utility.isUrl(tmp)) {
                mode = FloatSearchboxMode.SEARCH_VISIT;
            } else if (Utility.isBDHD(tmp)) {
            	mode = FloatSearchboxMode.SEARCH_BDHD;
            } else {
                mode = FloatSearchboxMode.SEARCH_GO;
            }
        }
        setMode(mode);
	}
	
	/**
	 * @Title: setMode 
	 * @Description: 更新搜索模式UI
	 * @param mode   
	 */
    private void setMode(FloatSearchboxMode mode) {
    	currentMode = mode;
        switch (mode) {
        case SEARCH_GO:
            mSearchOrCancelView.setVisibility(VISIBLE);
            mSearchOrCancelView.setText(R.string.search_go);
            break;
        case SEARCH_CANCEL:
            mSearchOrCancelView.setVisibility(VISIBLE);
            mSearchOrCancelView.setText(R.string.search_cancel);
            break;
        case SEARCH_VISIT:
            mSearchOrCancelView.setVisibility(VISIBLE);
            mSearchOrCancelView.setText(R.string.search_visit);
            break;
        case SEARCH_BDHD:
        	mSearchOrCancelView.setVisibility(VISIBLE);
        	mSearchOrCancelView.setText(R.string.search_play);
        	break;
        default:
            break;
        }
    }

    /**
     * @ClassName: SearchBoxCommand 
     * @Description: 命令。封装数据域指令.
     * @author LEIKANG 
     * @date 2012-12-12 下午4:38:53
     */
    public static class SearchBoxCommand {
    	/** 当前搜索框模式.*/
    	public FloatSearchboxMode currentMode;
    	/** 搜索框中query.*/
    	public String query;
    }
    
    /**
     * @ClassName: SearchBoxCommandListener 
     * @Description: 搜索框发起命令，监听者执行  只需搜索框发起的命令
     * @author LEIKANG 
     * @date 2012-12-12 下午4:47:18
     */
    public interface SearchBoxCommandListener {
        void executeSearchBoxCommand(SearchBoxCommand command);
    }
    

    /**
     * @Title: setOnEditorActionListener 
     * @Description: 设置键盘动作监听器。 
     * @param mOnEditorActionListener   
     */
	public void setOnEditorActionListener(OnEditorActionListener mOnEditorActionListener) {
		mSearchTextInput.setOnEditorActionListener(mOnEditorActionListener);
	}
}
