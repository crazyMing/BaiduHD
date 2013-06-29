package com.baidu.android.ext.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.RectF;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;

/**
 * 用来管理各个SDK版本下的函数兼容问题。
 */
public abstract class VersionedHelper {

    /**TAG.*/
    static final String TAG = VersionedHelper.class.getSimpleName();
    /**DEBUG.*/
    private static final boolean DEBUG = false;
    
    /**单例。*/
    private static VersionedHelper sHelper;
    
    /**
     * 根据SDK版本获取单例。
     * @return 单例对象。
     */
    public static VersionedHelper getInstance() {
        if (sHelper == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                sHelper = new Versioned11Helper();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                sHelper = new Versioned8Helper();
            } else {
                sHelper = new VersionedHelper() {
                };
            }
        }
        
        return sHelper;
    }
    
    /**
     * 隐藏构造。
     */
    private VersionedHelper() {
        
    }
    
    /**
     * view.getTranslationX()方法，在SDK11以下用getLeft()替代。
     * @param v View
     * @return tranlate X值或left值。
     */
    public float getTranslationX(View v) {
        return v.getLeft();
    }
    
    /**
     * view.getTranslationY()方法，在SDK11以下用getTop()替代。
     * @param v View
     * @return tranlate Y值或top值。
     */
    public float getTranslationY(View v) {
        return v.getTop();
    }
    
    /**
     * view.getAlpha()方法，在SDK11以下直接返回1.0，即完全不透明（因为没法设置alpha值）。
     * @param v View
     * @return alpha值或1.0。
     */
    public float getAlpha(View v) {
        return 1.0f;
    }
    
    /**
     * ViewConfiguration.getScaledPagingTouchSlop方法，在SDK7以下直接返回24.0。
     * @param context Context
     * @return alpha值或1.0。
     */
    public float getScaledPagingTouchSlop(Context context) {
        final float slop = 24.0f;
        return slop;
    }
    
    /**
     * 是否开启硬件加速，在SDK11以下直接返回false，因为没法开启硬件加速。
     * @param v View
     * @return true or false.
     */
    public boolean isHardwareAccelerated(View v) {
        return false;
    }
    
    /**
     * layouttransition.isRunning()方法。
     * @param v ViewGroup
     * @return true or false.
     */
    public boolean isTransitionRunning(ViewGroup v) {
        return false;
    }
    
    /**
     * view.setTranslationX()方法，在SDK11以下用layout替代。
     * @param v View
     * @param translate 相对移动值
     */
    public void setTranslationX(View v, float translate) {
        v.layout(/*v.getLeft() + */(int) translate, v.getTop(), 
                /*v.getRight() + */(int) translate + v.getWidth(), v.getBottom());
    }
    
    /**
     * view.setTranslationY()方法，在SDK11以下用layout替代。
     * @param v View
     * @param translate 相对移动值
     */
    public void setTranslationY(View v, float translate) {
        v.layout(v.getLeft(), /*v.getTop() + */(int) translate, 
                v.getRight(), /*v.getBottom() + */(int) translate + v.getHeight());
    }
    
    /**
     * view.setAlpha()方法，在SDK11以下不作任何处理。
     * @param v View
     * @param alpha alpha值0.0-1.0.
     */
    public void setAlpha(View v, float alpha) {
        
    }
    
    /**
     * view.setActivated()方法，在SDK11以下不作任何处理。
     * @param v View
     * @param activated true or false.
     */
    public void setActivated(View v, boolean activated) {
        
    }
    
    /**
     * 设置LayoutTranstition的回调，在SDK11以下不作任何处理。
     * @param layoutTransition LayoutTransition对象. 为了兼容，此处只能使用Object类型作参数。
     * @param listener 监听。
     */
    public void setLayoutTransitionCallback(Object layoutTransition, final VersionedTransitionListener listener) {
        
    }
    
    /**
     * 根据translation值更新View的位置，在SDK11以下不作任何处理。
     * @param view View
     * @param childBounds 位置区域
     */
    public void invalidateGlobalRegion(View view, RectF childBounds) {
        
    }
    
    /**
     * 展示动画。在SDK11以下使用TranslateAnimation，而SDK11及以上则使用translation值配合ObjectAnimator来完成。
     * @param animView 展示动画的View
     * @param duration 持续时间
     * @param propertyName ObjectAnimator的属性名，在SDK11以下可忽略
     * @param value 在SDK11以下表示目的地的相对移动值，而SDK11及以上表示ObjectAnimator propertyName对应的属性值
     * @param listener 监听。
     */
    public void startAnimation(final View animView, long duration,
            String propertyName, Object value,
            final VersionedAnimationListener listener) {
        float floatValue = (Float) value;
        float fromXDelta = animView.getLeft();
        animView.layout((int) floatValue, animView.getTop(), 
                (int) floatValue + animView.getWidth(), animView.getBottom());
        //<add by qumiao 2012.8.21 BEGIN
        //如果没有移动，则不执行动画
        final float del = 0.1f;
        if (Math.abs(fromXDelta - floatValue) < del) {
            return;
        }
        //add by qumiao 2012.8.21 END>
        TranslateAnimation animation = new TranslateAnimation(fromXDelta, floatValue, 0, 0);
        animation.setDuration(duration);
        animation.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
                if (listener != null) {
                    listener.onAnimationStart();
                }
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
                if (listener != null) {
                    listener.onAnimationRepeat();
                }
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                if (listener != null) {
                    listener.onAnimationEnd();
                }
            }
        });
        animView.startAnimation(animation);
    }
   
    /**
     * 展示拖动删除后的消失动画。在SDK11以下不作任何处理。
     * @param container ViewGroup
     */
    public void startDisappearAnimation(ViewGroup container) {
        
    }
    
    
    /**
     * SDK8及以上使用。
     * @author qumiao
     *
     */
    private static class Versioned8Helper extends VersionedHelper {
        
        @Override
        public float getScaledPagingTouchSlop(Context context) {
            return ViewConfiguration.get(context).getScaledPagingTouchSlop();
        }
        
    }
    
    
    /**
     * SDK11及以上使用。
     * @author qumiao
     *
     */
    private static class Versioned11Helper extends VersionedHelper {
        
        @Override
        public float getTranslationX(View v) {
            return v.getTranslationX();
        }
        
        @Override
        public float getTranslationY(View v) {
            return v.getTranslationY();
        }
        
        @Override
        public float getAlpha(View v) {
            return v.getAlpha();
        }
        
        @Override
        public boolean isHardwareAccelerated(View v) {
            return v.isHardwareAccelerated();
        }
        
        @Override
        public boolean isTransitionRunning(ViewGroup v) {
            LayoutTransition transition = v.getLayoutTransition();
            return transition != null && transition.isRunning();
        }
        
        @Override
        public void setTranslationX(View v, float translate) {
            v.setTranslationX(translate);
        }
        
        @Override
        public void setTranslationY(View v, float translate) {
            v.setTranslationY(translate);
        }
        
        @Override
        public void setAlpha(View v, float alpha) {
            v.setAlpha(alpha);
        }
        
        @Override
        public void setActivated(View v, boolean activated) {
            v.setActivated(activated);
        }
        
        @Override
        public void setLayoutTransitionCallback(Object layoutTransition,
                final VersionedTransitionListener listener) {
            if (layoutTransition instanceof LayoutTransition) {
                LayoutTransition transition = (LayoutTransition) layoutTransition;
                transition.addTransitionListener(new LayoutTransition.TransitionListener() {
                    @Override
                    public void startTransition(LayoutTransition transition,
                            ViewGroup container, View view, int transitionType) {
                        if (listener != null) {
                            listener.startTransition();
                        }
                    }

                    @Override
                    public void endTransition(LayoutTransition transition,
                            ViewGroup container, View view, int transitionType) {
                        if (listener != null) {
                            listener.endTransition();
                        }
                    }
                });
            }
        }
        
        @Override
        public void invalidateGlobalRegion(View view, RectF childBounds) {
            //childBounds.offset(view.getTranslationX(), view.getTranslationY());
            if (DEBUG) {
                Log.v(TAG, "-------------");
            }
            
            while (view.getParent() != null && view.getParent() instanceof View) {
                view = (View) view.getParent();
                view.getMatrix().mapRect(childBounds);
                view.invalidate((int) Math.floor(childBounds.left),
                                (int) Math.floor(childBounds.top),
                                (int) Math.ceil(childBounds.right),
                                (int) Math.ceil(childBounds.bottom));
                if (DEBUG) {
                    Log.v(TAG, "INVALIDATE(" + (int) Math.floor(childBounds.left)
                            + "," + (int) Math.floor(childBounds.top)
                            + "," + (int) Math.ceil(childBounds.right)
                            + "," + (int) Math.ceil(childBounds.bottom));
                }
            }
        }
        
        @Override
        public void startAnimation(final View animView, long duration,
                String propertyName, Object value, 
                final VersionedAnimationListener listener) {
            animView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            ObjectAnimator anim = ObjectAnimator.ofFloat(animView, propertyName, (Float) value);
//            anim.setInterpolator(sLinearInterpolator);
            anim.setDuration(duration);
            anim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (listener != null) {
                        listener.onAnimationEnd();
                    }
                    animView.setLayerType(View.LAYER_TYPE_NONE, null);
                }
            });
            anim.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (listener != null) {
                        listener.onAnimationUpdate();
                    }
                }
            });
            anim.start();
        }
        
        @Override
        public void startDisappearAnimation(ViewGroup container) {
            LayoutTransition transitioner = new LayoutTransition();
            container.setLayoutTransition(transitioner);
            
            final int duration = 200;
            transitioner.setDuration(duration);
            transitioner.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
            transitioner.setAnimator(LayoutTransition.DISAPPEARING, null);
        }
    }
    
  
    /**
     * 动画监听。
     * @author qumiao
     *
     */
    public interface VersionedAnimationListener {
        /**
         * 动画开始。
         */
        void onAnimationStart();
        
        /**
         * SDK11及以上的ObjectAnimator值更新的回调。
         */
        void onAnimationUpdate();
        
        /**
         * 动画重放。
         */
        void onAnimationRepeat();
        
        /**
         * 动画结束。
         */
        void onAnimationEnd();
    }
    
    /**
     * 转变监听。
     * @author qumiao
     *
     */
    public interface VersionedTransitionListener {
        /**
         * 开始转变。
         */
        void startTransition();
        /**
         * 结束转变。
         */
        void endTransition();
    }
}
