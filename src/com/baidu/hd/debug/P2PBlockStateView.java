package com.baidu.hd.debug;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.baidu.hd.debug.P2PShowBlock.Section;
import com.baidu.hd.debug.P2PShowBlock.Section.Block;

/**
 * 二级进度控件
 */
public class P2PBlockStateView extends View {
	
	private Paint mPaint;
	
	private P2PShowBlock mShowBlock = null;
	
	private int mWidth = -1;
	
	// 当前显示的段下标
	private int mIndex = 0;
    
    public P2PBlockStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        showBlockState(canvas);
    }
    
    public void setBlock(P2PShowBlock block) {
    	this.mShowBlock = block;
    }
    
    public void setIndex(int index) {
    	this.mIndex = index;
    }
    
    private void showBlockState(Canvas canvas) {

    	mPaint.setColor(Color.GRAY);
		Rect rect = new Rect(0, 0, this.getWidth(), this.getHeight());
		canvas.drawRect(rect, mPaint);

    	if(this.mShowBlock == null || this.mShowBlock.getSectionCount() == 0)
    		return;
    	
    	if(this.mWidth == -1) {
    		Section first = this.mShowBlock.getSection(0);
    		this.mWidth = this.getWidth() / (int)first.getRange();
    	}
  
		Section section = this.mShowBlock.getSection(this.mIndex);
		
		mPaint.setColor(Color.WHITE);
		rect = new Rect(0, 0, (int)section.getRange() * this.mWidth, this.getHeight());
		canvas.drawRect(rect, mPaint);
		
		List<Block> blocks = section.getBlocks();
    	for(int i = 0; i < blocks.size(); i++) {
    		
    		Block block = blocks.get(i);
    		mPaint.setColor(Color.GREEN);
    		rect = new Rect(
    				(int)block.getBegin() * this.mWidth, 
    				0, 
    				(int)block.getEnd() * this.mWidth, 
    				this.getHeight());
    		canvas.drawRect(rect, mPaint);
    	}
    }
}



