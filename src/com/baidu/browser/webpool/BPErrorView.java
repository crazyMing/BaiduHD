package com.baidu.browser.webpool;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.baidu.hd.R;

/**
 * @ClassName: BdErrorView 
 * @Description: 错误页
 * @author LEIKANG 
 * @date 2012-12-6 下午12:46:47
 */
public class BPErrorView extends ScrollView  {

	public static final int ERROR_CODE_400 = 400;
	public static final int ERROR_CODE_401 = 401;
	public static final int ERROR_CODE_403 = 403;
	public static final int ERROR_CODE_404 = 404;
	public static final int ERROR_CODE_405 = 405;
	public static final int ERROR_CODE_406 = 406;
	public static final int ERROR_CODE_407 = 407;
	public static final int ERROR_CODE_408 = 408;
	public static final int ERROR_CODE_409 = 409;
	public static final int ERROR_CODE_410 = 410;
	public static final int ERROR_CODE_411 = 411;
	public static final int ERROR_CODE_412 = 412;
	public static final int ERROR_CODE_413 = 413;
	public static final int ERROR_CODE_414 = 414;
	public static final int ERROR_CODE_415 = 415;
	public static final int ERROR_CODE_416 = 416;
	public static final int ERROR_CODE_417 = 417;

	public static final int ERROR_CODE_500 = 500;
	public static final int ERROR_CODE_501 = 501;
	public static final int ERROR_CODE_502 = 502;
	public static final int ERROR_CODE_503 = 503;
	public static final int ERROR_CODE_504 = 504;
	public static final int ERROR_CODE_505 = 505;

	public static final int ERROR_CODE_900 = 900;

	public static final int HTTP_NOT_FOUND = 1;
	public static final int HTTP_TIMEOUT = 2;
	public static final int HTTP_MOVE_ERROR = 3;
	public static final int HTTP_UNKONW_ERROR = 4;
	public static final int HTTP_USER_STOP = 5;
	public static final int HTTP_FORMAT_ERROR = 6;
	public static final int HTTP_CONNECT_NO_FOUND = 7;
	public static final int HTTP_ILLEGAL_ARGUMENT = 8;
	public static final int HTTP_IO_ERROR = 9;
	public static final int HTTP_ZERO_BYTES = 10;
	public static final int HTTP_TRANS_DOWNLOAD = 11;
	public static final int HTTP_INTERRUPT_ERROR = 12;
	public static final int HTTP_NOT_MODIFIED = 13;
	public static final int OUT_OF_MEMORY = 14;
	public static final int NOT_PERMISSIONS = 15;
	public static final int BAD_NETWORK = 16;
	public static final int FILE_EXIST = 17;
	public static final int NO_SDCARD = 18;
	public static final int FILE_NAME_ERROR = 19;
	public static final int FILE_SIZE_ERROR = 20;
	public static final int PARSE_DATA_ERROR = 21;

	/** 关联的webview实例 */
	private BPWebPoolView mAttachedFixedWebView;

	/** 文案 */
	private TextView mDescriptionTextView;
	/** 图 */
	private ImageView mImageView;
	/** 按钮 */
	private Button mButton;
	/** BdErrorView事件监听器 */
	private BPErrorViewListener mListener;
	
	/** 设置网络按钮 */
    private Button mRefurbishButton;
	

	/**
	 * 构造。
	 * @param context
	 */
	public BPErrorView(Context context) {
		super(context);
		init();
	}

	/**
	 * 构造 for xml.
	 * @param context
	 * @param attrs
	 */
	public BPErrorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * 初始化。
	 */
	private void init() {

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mDescriptionTextView = (TextView) findViewById(R.id.webview_error_description_textview);
		mImageView = (ImageView) findViewById(R.id.webview_error_imageview);
		mButton = (Button) findViewById(R.id.webview_error_button);
		mRefurbishButton = (Button)findViewById(R.id.refurbish_button);
	}

	/**
	 * 设置联结的WebView。
	 * @param mWebView
	 */
	public void setAttachedFixedWebView(BPWebPoolView mWebView) {
		mAttachedFixedWebView = mWebView;
	}

	/**
	 * 获取联结的FixedWebView。
	 * @return 联结该ErrorView的FixedWebView。
	 */
	public BPWebPoolView getAttachedFixedWebView() {
		return mAttachedFixedWebView;
	}

	/**
	 * 设置错误码，改变UI。
	 * @param errorCode
	 *            错误码。
	 */
	public void setErrorCode(int errorCode) {
		int desTxtId = R.string.webview_error_fail_connection;
		int imgResId = R.drawable.borwser_error_page;
		boolean retry = true;

		switch (errorCode) {
			case WebViewClient.ERROR_FILE:
			case WebViewClient.ERROR_FILE_NOT_FOUND:
				desTxtId = R.string.webview_error_file_not_found;
				imgResId = R.drawable.borwser_errorpage_error_not_found;
				retry = false;
				break;
			default:
				// do nothing
		}

		mDescriptionTextView.setText(desTxtId);
		mImageView.setImageResource(imgResId);
		mButton.setTag(retry);
		if (retry) {
			mButton.setText(R.string.webview_error_retry_button);
		} else {
			mButton.setText(R.string.webview_error_back_button);
		}

		mButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mAttachedFixedWebView != null) {
					boolean shouldRetry = (Boolean) mButton.getTag();
					if (mListener != null) {
						if (shouldRetry) {
							mListener.onErrorPageRefresh();
						} else {
							mListener.onErrorPageGoBack();
						}
					}
				}
			}
		});
		
		mRefurbishButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                getContext().startActivity(intent);
                
            }
        });
	}

	/**
	 * 设置BdExploreView的监听器
	 * @param aListener
	 */
	public void setEventListener(BPErrorViewListener aListener) {
		mListener = aListener;
	}

	/**
	 * BPErrorView监听类
	 */
	public interface BPErrorViewListener {

		/**
		 * 后退操作
		 */
		void onErrorPageGoBack();

		/**
		 * 刷新操作
		 */
		void onErrorPageRefresh();

	}
}
