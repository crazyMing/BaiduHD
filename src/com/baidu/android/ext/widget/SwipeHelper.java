
package com.baidu.android.ext.widget;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import com.baidu.android.ext.widget.VersionedHelper.VersionedAnimationListener;

/**
 * @ClassName: SwipeHelper 
 * @Description: 
 * @author LEIKANG 
 */
public class SwipeHelper {
    static final String TAG = SwipeHelper.class.getSimpleName();
    static final boolean DEBUG = false;
    private static final boolean SLOW_ANIMATIONS = false;  
    private static final boolean CONSTRAIN_SWIPE = true;
    private static final boolean FADE_OUT_DURING_SWIPE = true;
    private static final boolean DISMISS_IF_SWIPED_FAR_ENOUGH = true;

    public static final int X = 0;
    public static final int Y = 1;

    private float SWIPE_ESCAPE_VELOCITY = 100f;  
    private int DEFAULT_ESCAPE_ANIMATION_DURATION = 200;  
    private int MAX_ESCAPE_ANIMATION_DURATION = 400;  
    private int MAX_DISMISS_VELOCITY = 2000;  
    private static final int SNAP_ANIM_LEN = SLOW_ANIMATIONS ? 1000 : 150; 

    public static float ALPHA_FADE_START = 0f;  
                                       
    static final float ALPHA_FADE_END = 0.5f;  
                                              

    private float mPagingTouchSlop;
    private SwipeCallback mSwipeCallback;
    private int mSwipeDirection;
    private VelocityTracker mVelocityTracker;

    private float mInitialTouchPos;
    private boolean mDragging;
    private View mCurrView;
    private float mCurrViewOriginalPos;
    private View mCurrAnimView;
    private float mDensityScale;
    
    private VersionedHelper mVersionedHelper;

    public SwipeHelper(int swipeDirection, SwipeCallback swipeCallback, float densityScale,
            float pagingTouchSlop) {
        mSwipeCallback = swipeCallback;
        mSwipeDirection = swipeDirection;
        mVelocityTracker = VelocityTracker.obtain();
        mDensityScale = densityScale;
        mPagingTouchSlop = pagingTouchSlop;
        mVersionedHelper = VersionedHelper.getInstance();
    }

    public void setDensityScale(float densityScale) {
        mDensityScale = densityScale;
    }

    public void setPagingTouchSlop(float pagingTouchSlop) {
        mPagingTouchSlop = pagingTouchSlop;
    }

    private float getPos(MotionEvent ev) {
        return mSwipeDirection == X ? ev.getX() : ev.getY();
    }

    private float getTranslation(View v) {
        return mSwipeDirection == X ? mVersionedHelper.getTranslationX(v) : mVersionedHelper.getTranslationY(v);
    }

    private float getVelocity(VelocityTracker vt) {
        return mSwipeDirection == X ? vt.getXVelocity() :
                vt.getYVelocity();
    }

    private float getPerpendicularVelocity(VelocityTracker vt) {
        return mSwipeDirection == X ? vt.getYVelocity() :
                vt.getXVelocity();
    }

    private void setTranslation(View v, float translate) {
        if (mSwipeDirection == X) {
            mVersionedHelper.setTranslationX(v, translate);
        } else {
            mVersionedHelper.setTranslationY(v, translate);
        }
    }

    private float getSize(View v) {
        return mSwipeDirection == X ? v.getMeasuredWidth() :
                v.getMeasuredHeight();
    }

    private float getAlphaForOffset(View view) {
        float viewSize = getSize(view);
        final float fadeSize = ALPHA_FADE_END * viewSize;
        float result = 1.0f;
        float pos = getTranslation(view);
        if (pos >= viewSize * ALPHA_FADE_START) {
            result = 1.0f - (pos - viewSize * ALPHA_FADE_START) / fadeSize;
        } else if (pos < viewSize * (1.0f - ALPHA_FADE_START)) {
            result = 1.0f + (viewSize * ALPHA_FADE_START + pos) / fadeSize;
        }
        // Make .03 alpha the minimum so you always see the item a bit-- slightly below
        // .03, the item disappears entirely (as if alpha = 0) and that discontinuity looks
        // a bit jarring
        return Math.max(0.03f, result);
    }

    // invalidate the view's own bounds all the way up the view hierarchy
    public void invalidateGlobalRegion(View view) {
        mVersionedHelper.invalidateGlobalRegion(
            view,
            new RectF(view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDragging = false;
                mCurrView = mSwipeCallback.getSwipeChildAtPosition((int) ev.getX(), (int) ev.getY());
                mVelocityTracker.clear();
                if (mCurrView != null) {
                    mCurrViewOriginalPos = getTranslation(mCurrView);
                    mCurrAnimView = mCurrView;
                    mVelocityTracker.addMovement(ev);
                    mInitialTouchPos = getPos(ev);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCurrView != null) {
                    mVelocityTracker.addMovement(ev);
                    float pos = getPos(ev);
                    float delta = pos - mInitialTouchPos;
                    if (Math.abs(delta) > mPagingTouchSlop) {
                        mSwipeCallback.onBeginDrag(mCurrView);
                        mDragging = true;
                        mInitialTouchPos = getPos(ev) - getTranslation(mCurrAnimView);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mDragging = false;
                mCurrView = null;
                mCurrAnimView = null;
                break;
        }
        return mDragging;
    }

    /**
     * @param view The view to be dismissed
     * @param velocity The desired pixels/second speed at which the view should move
     */
    public void dismissChild(final View view, float velocity) {
        final View animView = view;
        final int position = mSwipeCallback.getSwipeChildIndex(view);
        if (position < 0) {
            return;
        }
        final int animViewSwipAction = mSwipeCallback.getAdapter().getSwipeAction(position);
        float newPos;

        if (velocity < 0
                || (velocity == 0 && getTranslation(animView) < 0)
                // if we use the Menu to dismiss an item in landscape, animate up
                || (velocity == 0 && getTranslation(animView) == 0 && mSwipeDirection == Y)) {
            newPos = -getSize(animView);
        } else {
            newPos = getSize(animView);
        }
        int duration = MAX_ESCAPE_ANIMATION_DURATION;
        if (velocity != 0) {
            duration = Math.min(duration,
                                (int) (Math.abs(newPos - getTranslation(animView)) * 1000f / Math
                                        .abs(velocity)));
        } else {
            duration = DEFAULT_ESCAPE_ANIMATION_DURATION;
        }

        String propertyName = mSwipeDirection == X ? "translationX" : "translationY";
        mVersionedHelper.startAnimation(animView, duration, propertyName, newPos, 
                new VersionedAnimationListener() {

            @Override
            public void onAnimationStart() {
                
            }

            @Override
            public void onAnimationUpdate() {
                if (FADE_OUT_DURING_SWIPE 
                        && (animViewSwipAction & SwipeAdapter.SWIPE_REMOVE) == SwipeAdapter.SWIPE_REMOVE) {
                    mVersionedHelper.setAlpha(animView, getAlphaForOffset(animView));
                }
                invalidateGlobalRegion(animView);
            }

            @Override
            public void onAnimationRepeat() {
                
            }

            @Override
            public void onAnimationEnd() {
                mSwipeCallback.onChildDismissed(view);
            }
            
        });
    }

    public void snapChild(final View view, float velocity, float orginalPos) {
        final View animView = view;
        final int position = mSwipeCallback.getSwipeChildIndex(view);
        if (position < 0) {
            return;
        }
        final int animViewSwipAction = mSwipeCallback.getAdapter().getSwipeAction(position);
        int duration = SNAP_ANIM_LEN;
        String propertyName = mSwipeDirection == X ? "translationX" : "translationY";
        
        mVersionedHelper.startAnimation(animView, duration, propertyName, /*0f*/orginalPos, 
                new VersionedAnimationListener() {
            
            @Override
            public void onAnimationUpdate() {
                if (FADE_OUT_DURING_SWIPE 
                        && (animViewSwipAction & SwipeAdapter.SWIPE_REMOVE) == SwipeAdapter.SWIPE_REMOVE) {
                    mVersionedHelper.setAlpha(animView, getAlphaForOffset(animView));
                }
                invalidateGlobalRegion(animView);
            }
            
            @Override
            public void onAnimationStart() {
                
            }
            
            @Override
            public void onAnimationRepeat() {
                
            }
            
            @Override
            public void onAnimationEnd() {
                
            }
        });
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!mDragging) {
            return false;
        }

        mVelocityTracker.addMovement(ev);
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_MOVE:
                if (mCurrView != null) {
                    final int position = mSwipeCallback.getSwipeChildIndex(mCurrView);
                    if (position < 0) {
                        return false;
                    }
                    final int swipAction = mSwipeCallback.getAdapter().getSwipeAction(position);
                    
                    float delta = getPos(ev) - mInitialTouchPos;
                    
                    if ((swipAction & SwipeAdapter.SWIPE_DRAG) == SwipeAdapter.SWIPE_DRAG) {
                     // don't let items that can't be dismissed be dragged more than
                        // maxScrollDistance
                        if (CONSTRAIN_SWIPE 
                                && (swipAction & SwipeAdapter.SWIPE_REMOVE) != SwipeAdapter.SWIPE_REMOVE) {
                            float size = getSize(mCurrAnimView);
                            float maxScrollDistance = 0.15f * size;
                            if (Math.abs(delta) >= size) {
                                delta = delta > 0 ? maxScrollDistance : -maxScrollDistance;
                            } else {
                                delta = maxScrollDistance * (float) Math.sin((delta/size)*(Math.PI/2));
                            }
                        }
                        setTranslation(mCurrAnimView, delta);
                        if (FADE_OUT_DURING_SWIPE 
                                && (swipAction & SwipeAdapter.SWIPE_REMOVE) == SwipeAdapter.SWIPE_REMOVE) {
                            mVersionedHelper.setAlpha(mCurrAnimView, getAlphaForOffset(mCurrAnimView));
                        }
                        invalidateGlobalRegion(mCurrView);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mCurrView != null) {
                    float maxVelocity = MAX_DISMISS_VELOCITY * mDensityScale;
                    mVelocityTracker.computeCurrentVelocity(1000 /* px/sec */, maxVelocity);
                    float escapeVelocity = SWIPE_ESCAPE_VELOCITY * mDensityScale;
                    float velocity = getVelocity(mVelocityTracker);
                    float perpendicularVelocity = getPerpendicularVelocity(mVelocityTracker);

                    // Decide whether to dismiss the current view
                    boolean childSwipedFarEnough = DISMISS_IF_SWIPED_FAR_ENOUGH &&
                            Math.abs(getTranslation(mCurrAnimView)) > 0.4 * getSize(mCurrAnimView);
                    boolean childSwipedFastEnough = (Math.abs(velocity) > escapeVelocity) &&
                            (Math.abs(velocity) > Math.abs(perpendicularVelocity)) &&
                            (velocity > 0) == (getTranslation(mCurrAnimView) > 0);

                    final int position = mSwipeCallback.getSwipeChildIndex(mCurrView);
                    if (position < 0) {
                        return false;
                    }
                    final int swipAction = mSwipeCallback.getAdapter().getSwipeAction(position);
                    
                    boolean dismissChild = ((swipAction & SwipeAdapter.SWIPE_REMOVE) == SwipeAdapter.SWIPE_REMOVE) &&
                            (childSwipedFastEnough || childSwipedFarEnough);

                    if (dismissChild) {
                        dismissChild(mCurrView, childSwipedFastEnough ? velocity : 0f);
                    } else {
                        mSwipeCallback.onDragCancelled(mCurrView);
                        snapChild(mCurrView, velocity, mCurrViewOriginalPos);
                    }
                }
                break;
        }
        return true;
    }
    
    public boolean isDragging() {
        return mDragging;
    }
}
