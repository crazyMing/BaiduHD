package com.baidu.browser.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.hd.R;

/**
 * @ClassName: IconTextView 
 * @Description:一行，左icon，中文本，右icon. 用于书签列表，历史列表.
 * @author LEIKANG 
 * @date 2012-12-25 下午7:41:45
 */
public class IconTextView extends LinearLayout {

	/** top view.*/
    private TextView    mTopView;
    /** below view.*/
    private TextView    mBottomView;
    
    /** imageview.*/
    private ImageView   mLeftView;
    
    /** right image.*/
    private ImageView     mRightView;

    /**
     * create a IconTextView.
     * @param context Context
     */
    public IconTextView(Context context) {
        super(context);

        LayoutInflater factory = LayoutInflater.from(context);
        factory.inflate(R.layout.history_item, this);
        mTopView = (TextView) findViewById(R.id.top);
        mBottomView = (TextView) findViewById(R.id.bottom);
        mLeftView = (ImageView) findViewById(R.id.favicon);
        mRightView = (ImageView) findViewById(R.id.indicator);
    }

    /**
     * get method.
     * @return textview
     */
    public String getTopText() {
        return mTopView.getText().toString();
    }

    /**
     * get method.
     * @return textview
     */
    public String getBottomText() {
        return mBottomView.getText().toString();
    }
    
    /**
     * set textview's text.
     * @param topText String
     */
    public void setTopText(String topText) {
        mTopView.setText(topText);
    }

    /**
     * set textview's text.
     * @param name String
     */
    public void setBottomText(String bottomText) {
    	mBottomView.setText(bottomText);
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
    
    /**
     * get bottom view.
     * @return imageview
     */
    public TextView getBottomView() {
    	return mBottomView;
    }
    
    public TextView getTopView() {
    	return mTopView;
    }
}
