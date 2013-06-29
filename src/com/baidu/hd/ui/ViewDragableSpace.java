
package com.baidu.hd.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Scroller;

import com.baidu.hd.R;

public class ViewDragableSpace extends ViewGroup {
    private Scroller mScroller;

    private VelocityTracker mVelocityTracker;

    private int mScrollX = 0;

    private int mCurrentScreen = 0;
    
    private int mScreenIndex = 1;

    private float mLastMotionX;
    private float mLastMotionY;

    private static final String LOG_TAG = "ViewDragableSpace";

    private static final int SNAP_VELOCITY = 500;

    private final static int TOUCH_STATE_REST = 0;

    private final static int TOUCH_STATE_SCROLLING = 1;

    private int mTouchState = TOUCH_STATE_REST;

    /**获得可以被认定为滚动的像素距离*/
    private int mTouchSlop = 0;

    public boolean isHuaDong = false;
    
    //定义属性
    //滑动至最后一屏时是否循环
    private boolean isRepeat;
    //是否允许滑动
    private boolean isDragable;
    
	private OnViewChangedListener mListener;

    /* ===================== 监听currentScreen end======================== */

    public ViewDragableSpace(Context context) {
        super(context);
        init();
    }

    public ViewDragableSpace(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewDragableSpace); 
        isRepeat= a.getBoolean(R.styleable.ViewDragableSpace_isrepeat, false);
        isDragable= a.getBoolean(R.styleable.ViewDragableSpace_isdragable, true);
        a.recycle(); 
        init();
    }

    private void init() {
        Context localContext = getContext();
        AccelerateDecelerateInterpolator localAccelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
        Scroller localScroller = new Scroller(localContext, localAccelerateDecelerateInterpolator);
        mScroller = localScroller;
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /**
     * 拦截所有触摸屏移动事件
     */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	
    	if (!isDragable)
    		return false;

        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }
        final float x = ev.getX();
        final float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);
                boolean xMoved = xDiff > mTouchSlop;
                
    			boolean yMoved = yDiff > mTouchSlop;
                
    			if (xMoved || yMoved) {

    				if (xMoved) {

    					if (Math.abs(xDiff) > Math.abs(yDiff)) {
    						// Scroll if the user moved far enough along the X axis
    						mTouchState = TOUCH_STATE_SCROLLING;
    						
    						requestDisallowInterceptTouchEvent(true);
    					}
    					// enableChildrenCache();
    				}
                
    			}
                
                break;
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        return mTouchState != TOUCH_STATE_REST;
    }

    private void checkRecircle() {
        final int count = getChildCount();
        
        if(!isRepeat)
        	return;
        
        if (mCurrentScreen == count - 1) {
            final View child = getChildAt(0);
            removeViewInLayout(child);
            mCurrentScreen--;
            addViewInLayout(child, -1, child.getLayoutParams());
            layoutChildren();
        } else if (mCurrentScreen == 0) {
            final View child = getChildAt(count - 1);
            removeViewInLayout(child);
            mCurrentScreen++;
            addViewInLayout(child, 0, child.getLayoutParams());
            layoutChildren();
        }
    }

    private void layoutChildren() {
        final int count = getChildCount();

        final int width = getWidth();
        final int height = getHeight();

        int startX = 0;
        for (int i = 0; i < count; i++) {
            getChildAt(i).layout(startX, 0, startX + width, height);
            startX += width;
        }
        setScreen(mCurrentScreen);
    }

    
    public void setScreen(int whichScreen, int duration) {
        if (duration == 0) {
        	if (mScroller != null) {
                 mCurrentScreen = whichScreen;
                 if (mListener!=null)
                 mListener.onViewChanged(mCurrentScreen);
                 final int newX = whichScreen * getWidth();
                 mScroller.setFinalX(newX);
            	 mScroller.abortAnimation();
        	}
        }
        else {
        	 int dis = Math.abs(mCurrentScreen - whichScreen);
             mCurrentScreen = whichScreen;
             if (mListener!=null)
             mListener.onViewChanged(mCurrentScreen);
             final int newX = whichScreen * getWidth();
             final int delta = newX - mScrollX;
             mScroller.startScroll(mScrollX, 0, delta, 0, duration * dis);
        }
    }
    
    public boolean isScrollerFinished() {
    	if (mScroller == null) {
    		return true;
    	}
    		
    	return mScroller.isFinished();
    }
    
    public void setScreen(int screen) {
        if (screen >= 0 && screen < getChildCount()) {
            mCurrentScreen = screen;
            scrollTo(mCurrentScreen * getWidth(), 0);
            invalidate();
        }
    }
    

    public void setScreenId(int screenId) {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).getId() == screenId)
                setScreen(i);
        }
    }

    private double ads;// touch初始位置

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if (!isDragable)
    		return false;
    	if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();
        double flag = 0;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastMotionX = x;
                mLastMotionY = y;
                ads = x;
                isHuaDong = true;
                break;
            case MotionEvent.ACTION_MOVE:
                 if(Math.abs(ads-x)>30)
                	 flag = 1.2;
                 else
                	 flag = 0.0;
                 flag = 1.2;
                final int deltaX = (int) (flag * (mLastMotionX - x));
                mLastMotionX = x;
                mLastMotionY = y;

                if (deltaX < 0) {
                    if (mScrollX > 0) {
                        scrollBy(Math.max(-mScrollX, deltaX), 0);
                    } else {
                        checkRecircle();
                    }
                } else if (deltaX > 0) {
                    final int availableToScroll = getChildAt(getChildCount() - 1).getRight()
                            - mScrollX - getWidth();
                    if (availableToScroll > 0) {
                        scrollBy((int)(Math.min(availableToScroll, deltaX)*1.2), 0);
                    } else {
                        checkRecircle();
                    }
                }
                isHuaDong = true;
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000);
                int velocityX = (int) velocityTracker.getXVelocity();
                
                if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
                	mScreenIndex = mScreenIndex - 1;
                	if (mScreenIndex < 0)
                		mScreenIndex = getChildCount() - 1;
                    snapToScreen(mCurrentScreen - 1);
                } else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1) {
                    snapToScreen(mCurrentScreen + 1);
                    mScreenIndex = mScreenIndex + 1;
                    if (mScreenIndex > (getChildCount() - 1))
                    	mScreenIndex = 0;
                } else {
                	snapToDestination();
                }

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mTouchState = TOUCH_STATE_REST;
                isHuaDong = false;
                mScrollX = this.getScrollX();
                break;
            case MotionEvent.ACTION_CANCEL:
                isHuaDong = false;
                mTouchState = TOUCH_STATE_REST;
        }
        mScrollX = this.getScrollX();

        return true;
    }

    private void snapToDestination() {
        final int screenWidth = getWidth();
        int whichScreen = (mScrollX + (screenWidth / 2)) / screenWidth;
        int screenCount = getChildCount();
        whichScreen = Math.min(whichScreen, screenCount-1);
        whichScreen = Math.max(whichScreen, 0);
        
        snapToScreen(whichScreen);
    }

    public void snapToScreen(int whichScreen) {
        
        mCurrentScreen = whichScreen;
        if (mListener!=null)
        mListener.onViewChanged(mCurrentScreen);
        final int newX = whichScreen * getWidth();
        final int delta = newX - mScrollX;
        mScroller.startScroll(mScrollX, 0, delta, 0, (int)(Math.abs(delta)/1.3));
        invalidate();
    }

    public void setToScreen(int whichScreen) {
        mCurrentScreen = whichScreen;
        if (mListener!=null)
        	mListener.onViewChanged(mCurrentScreen);
        final int newX = whichScreen * getWidth();
        mScroller.startScroll(newX, 0, 0, 0, 100);
        invalidate();
    }
    
    public void scroolToNextScreen() {
    	
    	final int count = getChildCount();
    	
        if(!isRepeat)
        	return;
        
        mScreenIndex = mScreenIndex + 1;
        if (mScreenIndex > (count - 1))
        	mScreenIndex = 0;
        
        setScreen(mScreenIndex);
    	
        if (mListener!=null)
        	mListener.onViewChanged(mScreenIndex);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = MeasureSpec.getSize(widthMeasureSpec);

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
        scrollTo(mCurrentScreen * width, 0);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mScrollX = mScroller.getCurrX();
            scrollTo(mScrollX, 0);
            postInvalidate();
        }
    }

    protected void enableChildrenCache() {
        boolean bool = true;
        int i = getChildCount();
        for (int j = 0;; ++j) {
            if (j >= i)
                return;
            View localView = getChildAt(j);
            localView.setDrawingCacheEnabled(bool);
            if (!(localView instanceof ViewGroup))
                continue;
            ((ViewGroup) localView).setAlwaysDrawnWithCacheEnabled(bool);
        }
    }
    
    
    public interface OnViewChangedListener {
        /**
         * When view changed.
         * @param viewIndex index.
         */
    	void onViewChanged(int viewIndex);
    }
    
	/**
	 * setOnViewChangedListener.
	 * 
	 * @param listener
	 *            Listener
	 */
	public void setOnViewChangedListener(OnViewChangedListener listener) {
		this.mListener = listener;
	}
}
