package com.baidu.browser.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.browser.framework.BPFrameView;
import com.baidu.browser.framework.BPWindow;
import com.baidu.hd.R;

/**
 * @ClassName: IconTextView 
 * @Description:一行，左icon，右关闭按钮，用于.
 * @author LEIKANG 
 * @date 2012-12-25 下午7:41:45
 */
public class TabView extends RelativeLayout implements OnClickListener {

    private TextView    mMiddleView;
    
    private ImageView   mLeftView;
    
    private ImageView     mRightView;
    
    private BPFrameView mBPFrameView;
    
    private BPWindow mWindow;

    /**
     * create a IconTextView.
     * @param context Context
     */
    public TabView(Context context) {
        super(context);

        LayoutInflater factory = LayoutInflater.from(context);
        factory.inflate(R.layout.tab_view, this);
        mMiddleView = (TextView) findViewById(R.id.top);
        mLeftView = (ImageView) findViewById(R.id.favicon);
        mRightView = (ImageView) findViewById(R.id.indicator);
        mRightView.setOnClickListener(this);
    }

    /**
     * get method.
     * @return textview
     */
    public String getMiddleText() {
        return mMiddleView.getText().toString();
    }

    /**
     * set textview's text.
     * @param MiddleText String
     */
    public void setMiddleText(String topText) {
    	mMiddleView.setText(topText);
    }

    /**
     * set image res.
     * @param res res's id
     */
    public void setLeftView(int res) {
    	mLeftView.setImageResource(res);
    }
    
    /**
     * get left view.
     * @return imageview
     */
    public ImageView getLeftView() {
    	return mLeftView;
    }
    
    /**
     * get right view.
     * @return imageview
     */
    public ImageView getRightView() {
        return mRightView;
    }
    
    
    public TextView getMiddleView() {
    	return mMiddleView;
    }
    
    public void setBPFrameViewWithWindow(BPFrameView frame,BPWindow pos) {
    	mBPFrameView = frame;
    	mWindow = pos;
    }

	@Override
	public void onClick(View v) {
		mBPFrameView.closeWindow(mWindow);
	}
}
