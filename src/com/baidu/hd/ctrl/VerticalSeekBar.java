package com.baidu.hd.ctrl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsSeekBar;

import com.baidu.hd.log.Logger;
import com.baidu.hd.R;


public class VerticalSeekBar extends AbsSeekBar
{
	private Logger logger = new Logger("VerticalSeekBar");
	private boolean bStartAdjustVoice = false;
	private boolean isStartTouch = false;
	private int mHeight;
	private OnSeekBarChangeListener mOnSeekBarChangeListener;
	private float mScale = 0.0F;
	private int mStartCount = 0;
	private Drawable mThumb;
	private Paint mPaint=new Paint();
	private Bitmap mCache;
	private int mWidth;
	private int mleft,mtop;

	public static abstract interface OnSeekBarChangeListener
	{
		public abstract void onProgressChanged(VerticalSeekBar paramVerticalSeekBar, int paramInt, boolean paramBoolean);

		public abstract void onStartTrackingTouch(VerticalSeekBar paramVerticalSeekBar);

		public abstract void onStopTrackingTouch(VerticalSeekBar paramVerticalSeekBar);
	}
	
	public VerticalSeekBar(Context paramContext)
	{
		this(paramContext, null);
	}

	public VerticalSeekBar(Context paramContext, AttributeSet paramAttributeSet)
	{
		this(paramContext, paramAttributeSet, android.R.attr.seekBarStyle);
	}

	public VerticalSeekBar(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
	{
		super(paramContext, paramAttributeSet, paramInt);
	}

	private void attemptClaimDrag()
	{
		if (getParent() != null)
			getParent().requestDisallowInterceptTouchEvent(true);
	}

	private void progressChanged(boolean paramBoolean)
	{
		logger.d("progressChanged");
		if (this.mThumb != null)
		{
			setThumbPos(getHeight(), this.mThumb, this.mScale, Integer.MIN_VALUE);
			invalidate();
		}
		if (((this.mStartCount < 2) || (!this.bStartAdjustVoice)) && (this.mOnSeekBarChangeListener != null))
			this.mOnSeekBarChangeListener.onProgressChanged(this, getProgress(), paramBoolean);
	}

	private void setThumbPos(int paramInt1, Drawable paramDrawable, float paramFloat, int paramInt2)
	{
		int available = paramInt1 - getPaddingLeft() - getPaddingRight();
		int thumbWidth = paramDrawable.getIntrinsicWidth();
		int thumbHeight = paramDrawable.getIntrinsicHeight();
		available = (int) (paramFloat * (available - thumbWidth + 2 * getThumbOffset()));
		int topBound;
		if (paramInt2 != Integer.MIN_VALUE)
		{
			topBound = paramInt2;
			thumbHeight = paramInt2 + thumbHeight;
		}
		else
		{
			Rect localRect = paramDrawable.getBounds();
			topBound = localRect.top;
			thumbHeight = localRect.bottom;
		}
		logger.d("setThumbPos : " + available + "  " + topBound + "  " + (available + thumbWidth) + "  " + thumbHeight);
		paramDrawable.setBounds(available, topBound, available + thumbWidth, thumbHeight);
		mleft=paramDrawable.getBounds().left;
		mtop=paramDrawable.getBounds().top;
	}
	

	private void trackTouchEvent(MotionEvent event)
	{
		final int Height = getHeight();
		final int available = Height - getPaddingBottom() - getPaddingTop();
		int Y = (int) event.getY();
		float scale;
		float progress = 0;
		if (Y > Height - getPaddingBottom())
		{
			scale = 0.0f;
		}
		else if (Y < getPaddingTop())
		{
			scale = 1.0f;
		}
		else
		{
			scale = (float) (Height - getPaddingBottom() - Y) / (float) available;
		}

		final int max = getMax();
		progress = scale * max;

		setIndeterminate(false);
		setProgress((int) progress);
		progressChanged(false);
	}

	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getAction() == KeyEvent.ACTION_DOWN)
		{
			KeyEvent newEvent = null;
			switch (event.getKeyCode())
			{
				case KeyEvent.KEYCODE_DPAD_UP:
					newEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT);
					break;
				case KeyEvent.KEYCODE_DPAD_DOWN:
					newEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT);
					break;
				case KeyEvent.KEYCODE_DPAD_LEFT:
					newEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN);
					break;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					newEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP);
					break;
				default:
					newEvent = new KeyEvent(KeyEvent.ACTION_DOWN, event.getKeyCode());
					break;
			}
			return newEvent.dispatch(this);
		}
		return false;
	}

	protected void onDraw(Canvas paramCanvas)
	{
		paramCanvas.rotate(-90.0F);
		paramCanvas.translate(-this.mHeight, 0.0F);
		mleft=mThumb.getBounds().left;
		mtop=mThumb.getBounds().top;
		super.onDraw(paramCanvas);
		if(mCache==null){
			mCache=BitmapFactory.decodeResource(getResources(),R.drawable.common_progressbar_sound_normal);
		}
		paramCanvas.drawBitmap(mCache, mleft, mtop, mPaint);
		
	}

	protected synchronized void onMeasure(int paramInt1, int paramInt2)
    {
	      this.mHeight = View.MeasureSpec.getSize(paramInt2);
	      this.mWidth = View.MeasureSpec.getSize(paramInt1);
	      setMeasuredDimension(this.mWidth, this.mHeight);
	      return;
    }

	public void onProgressRefresh(float paramFloat, boolean paramBoolean)
	{
		logger.d("onProgressRefresh : " + paramFloat + " " + paramBoolean);
		this.mScale = paramFloat;
		if (this.mThumb != null)
		{
			setThumbPos(getHeight(), this.mThumb, paramFloat, Integer.MIN_VALUE);
			invalidate();
		}
		if (((this.mStartCount < 2) || (!this.bStartAdjustVoice)) && (this.mOnSeekBarChangeListener != null) && (!this.isStartTouch))
			this.mOnSeekBarChangeListener.onProgressChanged(this, getProgress(), paramBoolean);
	}

	protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
	{
		super.onSizeChanged(paramInt2, paramInt1, paramInt3, paramInt4);
	}

	void onStartTrackingTouch()
	{
		this.isStartTouch = true;
		this.mStartCount = (1 + this.mStartCount);
		if (this.mOnSeekBarChangeListener != null)
		{
			this.bStartAdjustVoice = true;
			this.mOnSeekBarChangeListener.onStartTrackingTouch(this);
		}
	}

	void onStopTrackingTouch()
	{
		this.isStartTouch = false;
		if (this.mOnSeekBarChangeListener != null)
		{
			this.bStartAdjustVoice = false;
			this.mOnSeekBarChangeListener.onStopTrackingTouch(this);
		}
		this.mStartCount -= 1;
	}

	public boolean onTouchEvent(MotionEvent event)
	{
		if (!isEnabled())
		{
			return false;
		}
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				setPressed(true);
				onStartTrackingTouch();
				trackTouchEvent(event);
				break;

			case MotionEvent.ACTION_MOVE:
				trackTouchEvent(event);
				attemptClaimDrag();
				break;

			case MotionEvent.ACTION_UP:
				trackTouchEvent(event);
				onStopTrackingTouch();
				setPressed(false);
				break;

			case MotionEvent.ACTION_CANCEL:
				onStopTrackingTouch();
				setPressed(false);
				break;
		}
		return true;
	}

	public void setOnSeekBarChangeListener(OnSeekBarChangeListener paramOnSeekBarChangeListener)
	{
		this.mOnSeekBarChangeListener = paramOnSeekBarChangeListener;
	}

	public void setThumb(Drawable paramDrawable)
	{
		this.mThumb = paramDrawable;
		paramDrawable.setAlpha(0);
		super.setThumb(paramDrawable);
	}
	
	@Override
	public synchronized void setProgress(int progress) {
		super.setProgress(progress);
	}
	
	public void init(){
		mleft=mThumb.getBounds().left;
		mtop=mThumb.getBounds().top;
		postInvalidate();
	}
}
