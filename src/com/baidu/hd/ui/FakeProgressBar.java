package com.baidu.hd.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

import com.baidu.hd.R;

/**
 * 
 * 显示虚假进度的进度条 。
 * 整个加载过程分为三段：第一阶段快速填充进度到某一个值；第二阶段慢速填充，直到达到设定的虚假进度最大值；若期间加载完毕，则快速将进度条填充满，然后进度条消失。
 * 本进度条不可以设置进度值范围，为了动画流畅度，进度最大值被固定为10000
 * @author LEIKANG
 * @since 2012-11-28
 */
public class FakeProgressBar extends ProgressBar {
    /** 默认第一阶段加载速度 */
    public static final int FAST_VELOCITY = 40;
    /** 默认第二阶段加载速度 */
    public static final int SLOW_VELOCITY = 3;
    /** 默认第三阶段动画持续时间 */
    public static final int HIDE_DURATION = 120;
    /** 默认第一阶段最大进度值 */ 
    public static final int FIRST_SECTION_MAX = 10;
    /** 默认第二阶段最大进度值 */
    public static final int FAKE_PROGRESS_MAX = 90;
    /** 最大进度值 */
    private static final int MAX_PROGRESS = 10000;
    /** 刷新间隔 */
    private static final int REFRESH_DELAY = 30;
    /** 用于设置{@link #mLastUpdateTime},标识未启动加载 */
    private static final long UNINITIALED = -1;
    /** 设置的真实进度 */
    private int mRealProgress;
    /** 用于显示的进度值，虚假的进度*/
    private int mFakeProgress;
    /** 第三阶段动画持续时间 */
    private int mHideDuration = HIDE_DURATION;
    /** 第二阶段最大进度值 */
    private int mFakeProgressMax = resolveProgress(FAKE_PROGRESS_MAX);
    /** 第一阶段最大进度值 */
    private int mFirstSectionMax = resolveProgress(FIRST_SECTION_MAX);
    /** 第二阶段加载速度 */
    private float mSlowVelocity = resolveVelocity(SLOW_VELOCITY);
    /** 第一阶段加载速度 */
    private float mFastVelocity = resolveVelocity(FAST_VELOCITY);
    /** 上一次刷新时间 */
    private long mLastUpdateTime = UNINITIALED;
    /** 用于计算第三阶段的进度值 */
    private Transformation mTrans = new Transformation();
    /** 第三阶段动画Interpolator */
    private Interpolator mInterpolator = new AccelerateInterpolator(2.0f);
    /** 最三阶段动画 */
    private AnimationSet mAnimation;
    
    /**
     * 进度条头部图标
     */
    private Drawable mThumb;
    /** thumb offset */
    private int mThumbOffset;
    
    /**
     * 进度值范围固定在0-10000,初始值为0.
     * @param context context
     */
    public FakeProgressBar(Context context) {
        super(context);
        init();
    }
    
    /**
     * 进度值范围固定在0-10000,初始值为0.
     * @param context context
     * @param attrs attrs
     */
    public FakeProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    /**
     * 进度值范围固定在0-10000,初始值为0.
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public FakeProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    /**
     * 进度值范围，固定在0-10000
     */
    private void init() {
        super.setMax(MAX_PROGRESS);
        mThumb = getResources().getDrawable(R.drawable.progress_thumb);
        mThumbOffset = getResources().getDimensionPixelSize(R.dimen.browser_progressbar_offset);
    }
    
    /**
     * 
     * 此方法不可用
     * 不可以设置进度条进度值范围，为了动画流畅度，进度最大值被固定为10000
     * @param max max
     */
    @Override
    public synchronized void setMax(int max) {
        //do nothing
    }
    
    /**
     * 加载完毕，显示进度填充动画后使进度条消失。
     */
    public void hide() {
        if (getVisibility() == View.VISIBLE) {
            float start = getProgress() / (float) getMax();
            // mAnimation = new AlphaAnimation(start, 1.0f);
            // mAnimation.setDuration(mHideDuration);
            // mAnimation.setInterpolator(mInterpolator);
            mAnimation = new AnimationSet(false);
            AlphaAnimation a = new AlphaAnimation(start, 1.0f);
            a.setDuration(mHideDuration);
            a.setInterpolator(mInterpolator);
            AlphaAnimation b = new AlphaAnimation(1.0f, 1.0f);
            b.setDuration(mHideDuration);
            b.setStartOffset(mHideDuration);
            mAnimation.addAnimation(a);
            mAnimation.addAnimation(b);

            mTrans.clear();
            mAnimation.start();
            invalidate();
        }
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mThumb != null) {
            canvas.save();
            int progress = getProgress();
            float percent = progress / (float) getMax();
            int x = ((int) ((getWidth() - getPaddingLeft() - getPaddingRight()) * percent))
                    + mThumbOffset;
            canvas.translate(-mThumb.getIntrinsicWidth(), 0);
            mThumb.setBounds(x, 0, x + mThumb.getIntrinsicWidth(),
                    mThumb.getIntrinsicHeight());
            mThumb.draw(canvas);
            canvas.restore();
        }
        if (mAnimation != null) {
            if (mAnimation.getTransformation(System.currentTimeMillis(), mTrans)) {
                setProgressInAnimation((int) (mTrans.getAlpha() * getMax()));
                invalidate();
            } else {
                mAnimation = null;
                reset();
            }
            return;
        }

        if (mLastUpdateTime != UNINITIALED && mFakeProgress < mFakeProgressMax) {
            long current = System.currentTimeMillis();
            int increasment = 0;
            int resovledProgress = mRealProgress > mFirstSectionMax ? mRealProgress : mFirstSectionMax;
            if (resovledProgress > mFakeProgress) {
                increasment = (int) (mFastVelocity * 2 * (current - mLastUpdateTime));
                mFakeProgress += increasment;
            } else {
                increasment = (int) (mSlowVelocity * (current - mLastUpdateTime));
                mFakeProgress += increasment;
            }

            if (increasment != 0) {
                mLastUpdateTime = current;
                setProgressInAnimation(mFakeProgress);
            }
            postInvalidateDelayed(REFRESH_DELAY);
        }
    }
    
    /**
     * 设置进度条显示的值，用于绘制进度条的进度。
     * @param progress 进度值，0-10000
     */
    private void setProgressInAnimation(int progress) {
        super.setProgress(progress);
    }

    @Override
    public synchronized void setProgress(int progress) {
        int resolved = resolveProgress(progress);
        mRealProgress = resolved;
        if (mFakeProgress < resolved) {
            mFakeProgress = resolved;
        }
        postInvalidate();
    }
    
    /**
     * 获得真实进度
     * @return 真实进度
     */
    public int getRealProgress() {
        return mRealProgress / 100;  
    }
    
    /**
     * 开始加载进度条，显示假进度。
     */
    public void start() {
        mFakeProgress = 0;
        mRealProgress = 0;
        mLastUpdateTime = System.currentTimeMillis();
        mAnimation = null;
        setProgressInAnimation(mFakeProgress);
        setVisibility(View.VISIBLE);
        invalidate();
    }
    
    /**
     * 重置进度条状态
     */
    public void reset() {
        mFakeProgress = 0;
        mRealProgress = 0;
        mLastUpdateTime = System.currentTimeMillis();
        mAnimation = null;
        setProgressInAnimation(0);
        setVisibility(View.INVISIBLE);
        invalidate();
    }
    
    /**
     * 设置假进度的最大值。
     * 
     * 若实际进度一直未完成，则进度条在显示假进度到最大值后，便会停止增长。
     * @param progress 进度值，应在0-100之间
     */
    public void setFakeProgressMax(int progress) {
        mFakeProgressMax = resolveProgress(progress);
    }
    
    /**
     * 设置第一阶段的最大进度值。
     * 在此最大进度值以前，进度条会以{@link FakeProgressBar#FAST_VELOCITY}的速度快速加载。
     * 增加到此进度后，加载进入第二阶段
     * @param progress 进度值，应在0-100之间
     */
    public void setFirstSectionMax(int progress) {
        mFirstSectionMax = resolveProgress(progress);
    }
    
    /**
     * 设置第二阶段的进度条加载速度。
     * @param velocity 速度，单位为 进度/秒
     */
    public void setSlowVelocity(int velocity) {
        mSlowVelocity = resolveVelocity(velocity);
    }
    
    /**
     * 设置第一阶段的进度条加载速度。
     * @param velocity 速度，单位为 进度/秒
     */
    public void setFastVelocity(int velocity) {
        mFastVelocity = resolveVelocity(velocity);
    }
    
    /**
     * 设置第三阶段的动画持续时间
     * @param duration 动画持续时间
     */
    public void setHideAnimationDuration(int duration) {
        mHideDuration = duration;
    }
   
    /**
     * 对设置的速度度值进行转换
     * @param velocityInProgress 速度，单位为 进度/秒，其中进度最大值为100
     * @return 转换后的速度，单位为 进度/毫秒，进度最大值为10000
     */
    private static float resolveVelocity(int velocityInProgress) {
        return velocityInProgress * 100 / (float) 1000;  
    }

    /**
     * 对设置的进度值进行转换
     * @param progress 进度，最大值为100
     * @return 转换后的进度，最大值为10000
     */
    private static int resolveProgress(int progress) {
        return progress * 100;  
    }
}
