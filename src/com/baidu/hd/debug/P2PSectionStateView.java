package com.baidu.hd.debug;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.baidu.hd.debug.P2PShowBlock.Section;

/**
 * 一级进度控件
 */
public class P2PSectionStateView extends View {

	private Paint mPaint;
	
	private P2PShowBlock mShowBlock = null;
	
	private int mWidth = -1;
	
    public P2PSectionStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();       
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        showSectionState(canvas);
    }

    public void setBlock(P2PShowBlock block) {
    	this.mShowBlock = block;
    }
    
    private void showSectionState(Canvas canvas) {
    	
    	mPaint.setColor(Color.GRAY);
		Rect rect = new Rect(0, 0, this.getWidth(), this.getHeight());
		canvas.drawRect(rect, mPaint);

    	if(this.mShowBlock == null)
    		return;
  
    	int count = this.mShowBlock.getSectionCount();
    	if(count == 0) {
    		return;
    	}
    	
    	if(this.mWidth == -1) {
    		this.mWidth = this.getWidth() / count;
    	}
    	
    	for(int i = 0; i < count; i++) {
    		
    		Section section = this.mShowBlock.getSection(i);
    		
    		switch(section.getState())
    		{
			case P2PShowBlock.None:
				mPaint.setColor(Color.WHITE);
				break;
			case P2PShowBlock.Downloading:
				mPaint.setColor(Color.RED);
				break;
			case P2PShowBlock.Complete:
				mPaint.setColor(Color.GREEN);
				break;
    		}
    		rect = new Rect(i * this.mWidth, 0, (i + 1) * this.mWidth, this.getHeight());
    		canvas.drawRect(rect, mPaint);
    	}
    }
}
