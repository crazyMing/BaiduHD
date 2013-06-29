package com.baidu.hd.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.baidu.browser.net.ProxyHttpClient;
import com.baidu.hd.util.Utility;
import com.baidu.hd.R;

/**
 * @ClassName: NetImageView 
 * @Description: 具有联网获取数据、缓存数据功能的imageview.
 * @author LEIKANG 
 * @date 2012-12-12 下午1:36:40
 */
public class NetImageView extends ImageView {
	/** TAG.*/
	private static final String TAG = NetImageView.class.getSimpleName();
	/** DEBUG SWITCH.*/
	private static final boolean DEBUG = false;
	
    /** VIEW_TAG_KEY */
    private static final int VIEW_TAG_KEY = ((0x2f << 24) | android.R.id.summary);

    /** 是否已从网络抓取过资源，或者已被本地数据覆盖*/
	private boolean mHasGrabbed;
	
	/** 下载数据的url.*/
	private String mUrl;
	
	/** 是否保存imageview数据到内存.*/
    private boolean mCacheToRAM = false;
    
    /** 延时联网获取数据.*/
    private long mDelayGrabData = 0;
    
    /** {@link ProxyHttpClient} .*/
    private ProxyHttpClient mHttpClient;
    
	/** imageview背景资源.*/
	private Drawable mImageBackground;
	
	/** 默认image src.*/
	private int mDefaultImageSrc = R.drawable.multi_window_item_icon;
	
	/** 对图片的大小，使用存储无约束.*/
	static final int UNCONSTRAINED = -1;
	
	/** 图片的最大宽度.*/
	private int mMaxWidth = UNCONSTRAINED;
	
	/** 图片的最大高度.*/
	private int mMaxHeight = UNCONSTRAINED;
	
	/** 对下载的图片做内存缓存.*/
	private static Map<String, Bitmap> cacheBitmap = new HashMap<String, Bitmap>();
	
	/** 对下载的图片做内存缓存的最大个数.*/
	private static final int MAX_CACHE_TO_RAM_SIZE = 5;
	
	/**
	 * 构造函数
	 * @param context Context
	 * @param attrs AttributeSet
	 * @param defStyle int
	 */
	public NetImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		if (getDrawable() == null) {
		    setDefaultImage();
		}
	}
	
	/**
	 * 取出内部的ImageView
	 * @return 内部的ImageView
	 */
	public ImageView getImageView() {
	    return this;
	}
	
	/**
	 * 构造函数
	 * @param context Context
	 * @param attrs AttributeSet
	 */
	public NetImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	/**
	 * 构造函数
	 * @param context Context
	 */
	public NetImageView(Context context) {
		this(context, null, 0);
	}

	/**
	 * 通过设置url，根据最大宽度和最大高度设置获取imageview.
	 * @param url image url
	 * @param delayTime 延时获取网络数据的时间
	 * @param cacheToRam 是否对数据进行内存缓存
	 * @param maxWidth 最大宽度
	 * @param maxHeight 最大高度
	 */
	public void setParam(String url, long delayTime, boolean cacheToRam, int maxWidth, int maxHeight) {
		if (DEBUG) {
			Log.d(TAG, "setParam: url:" + url);
		}
	    setTag(VIEW_TAG_KEY, url);
	    
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
        mDelayGrabData = delayTime;
        
        setParam(url, cacheToRam);
    }

	/**
	 * 通过设置url，获取imageview数据.
	 * @param url image url
	 * @param cacheToRam 是否对数据进行内存缓存
	 */
	public void setParam(String url, boolean cacheToRam) {
		mUrl = url;
		mCacheToRAM = cacheToRam;
	}
	
	/**
	 * 通过设置url，获取imageview数据.
	 * @param url image url
	 */
	public void setParam(String url) {
	    setParam(url, false);
	}

	/**
	 * 断开前一次连接.
	 */
	private void closeConnect() {
		if (mHttpClient != null) {
			mHttpClient.close();
		}
	}
	
	/**
	 * 调用super.setImageResource
	 * @param resId resId
	 */
	private void callSuperSetImageResource(int resId) {
	    super.setImageResource(resId);
	}
	
	/**
	 * 调用super.setImageDrawable
	 * @param drawable drawable
	 */
	private void callSuperSetImageDrawable(Drawable drawable) {
	    super.setImageDrawable(drawable);
	}
	
	/**
	 * 设置imageview的默认icon引用
	 * @param resId 资源id
	 */
	public void setDefaultImageResource(int resId) {
		mDefaultImageSrc = resId;
	}
	
	/**
	 * 设置imageview的默认icon
	 */
	public void setDefaultImage() {
	    mHasGrabbed = false;
	    callSuperSetImageResource(mDefaultImageSrc);
	}
	
	@Override
	public void setImageDrawable(Drawable drawable) {
	    mHasGrabbed = true;
	    callSuperSetImageDrawable(drawable);
	}
	
	@Override
	public void setImageResource(int resId) {
	    mHasGrabbed = true;
	    callSuperSetImageResource(resId);
	}
	
	@Override
	public void setImageBitmap(Bitmap bm) {
	    mHasGrabbed = true;
	    super.setImageBitmap(bm);
	}
	
	/**
	 * 抓取image数据.
	 */
	public void grabViewData() {
	    mHasGrabbed = false;

		Bitmap bitmap = cacheBitmap.get(mUrl);
		if (bitmap != null) { //使用内存中的缓存
			requestSetImageBitmap(bitmap);
		} else {
			requestSetDefaultImage();
			Runnable r = new Runnable() {
				@Override
				public void run() {
					if (mDelayGrabData > 0) {
						try {
							Thread.sleep(mDelayGrabData);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					String url = mUrl;
					byte[] data = getViewDataFromNet(url);
					boolean suc = false;
					if (data != null) {
						suc = setGrabbedImage(url, data);
					}
					if (!suc) {
						requestSetDefaultImage();
					}
				}
			};
			new Thread(r, "getViewDataFromNet").start();
		}
		
	}
	
	/**
	 * 通过数据构造bitmap，设置imageview。更加mCacheToRAM保存bitmap于内存.
	 * @param url 获取的网络数据对应的url，实现绑定
	 * @param data byte[],bitmap数据
	 * @return 是否设置成功
	 */
	private boolean setGrabbedImage(String url, byte[] data) {  
		if (data != null) {
		    Bitmap bitmap = null;
		    try {
		        if (mMaxWidth == UNCONSTRAINED
		                && mMaxHeight == UNCONSTRAINED) {
		            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		        } else {  
                    if (mMaxWidth == UNCONSTRAINED) {
                        mMaxWidth = mMaxHeight;
                    } else if (mMaxHeight == UNCONSTRAINED) {
                        mMaxHeight = mMaxWidth;
                    }
                    bitmap = makeBitmap(data, Math.min(mMaxWidth, mMaxHeight), UNCONSTRAINED);
		        }
		    } catch (OutOfMemoryError er) {  
	            if (DEBUG) {
	                Log.w(TAG, "setImageView outofmemory. data.length=" + data.length);
	                er.printStackTrace();
	            }
	        }
			if (bitmap != null) {
				if (mCacheToRAM) {
					cacheBitmap(url, bitmap);
				}
				String tagUrl = (String) getTag(VIEW_TAG_KEY);
				if (TextUtils.equals(tagUrl, url)) {
				    requestSetImageBitmap(bitmap);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 对bitmap进行内存缓存
	 * @param url 作为key
	 * @param bitmap Bitmap
	 */
	private void cacheBitmap(String url, Bitmap bitmap) {
		if (cacheBitmap.size() >= MAX_CACHE_TO_RAM_SIZE) {
			cacheBitmap.clear();
		}
		
		cacheBitmap.put(url, bitmap);
	}
	/**
	 * 请求设置image资源，如果default资源已被替换，忽略设置
	 * @param bitmap Bitmap
	 */
	private void requestSetImageBitmap(final Bitmap bitmap) {
		
		((Activity) getContext()).runOnUiThread(new Runnable() {
			public void run() {
			    if (mHasGrabbed) {
			        return;
			    }
			    
			    setImageBitmap(bitmap);
			    setBackgroundDrawable(mImageBackground);
			    
			    invalidate();
			}
		});
	}
	
	/**
	 * 请求设置default资源，如果default资源已被替换，忽略设置
	 */
	private void requestSetDefaultImage() {
		((Activity) getContext()).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
			    if (mHasGrabbed) {
			        return;
			    }
			    
			    setDefaultImage();
			}
		});
	}
	/**
	 * 从net获取数据.
	 * @param url 获取数据的url
	 * @return 获取的数据
	 */
	private byte[] getViewDataFromNet(String url) {
		if (TextUtils.isEmpty(url)) {
			Log.e(TAG, "getViewDataFromNet: url is null or empty.");
			return null;
		}
		closeConnect();

		Context context = getContext();
		url = Utility.encodeUrl(url);
		HttpGet httpget = new HttpGet(url);
		mHttpClient = Utility.createHttpClient(context);
		
		try {
			HttpResponse response = mHttpClient.execute(httpget);
	        HttpEntity resEntity = response.getEntity();
	        InputStream inputStream = resEntity.getContent();
	        ByteArrayBuffer buf = new ByteArrayBuffer(1024); // SUPPRESS CHECKSTYLE
	        
	    	byte[] buffer = new byte[1024]; // SUPPRESS CHECKSTYLE
	    	do {
	    		int len = 0;
				try {
					len = inputStream.read(buffer, 0, buffer.length);
				} catch (IOException e) {
					e.printStackTrace();
				}
	    		if (len != -1) {
	    			buf.append(buffer, 0, len);
	    		} else {
	    			break;
	    		}
	    	} while (true);
	    	
	    	return buf.toByteArray();
	        
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mHttpClient.close();
		}
		return null;
	}

    /**
     * 生成bitmap.
     * @param data 图片数据
     * @param minSideLength 最小边
     * @param maxNumOfPixels 最大pix点数，用来限制内存使用量
     * @return bitmap
     */
	 
    public static Bitmap makeBitmap(byte[] data, int minSideLength, int maxNumOfPixels) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            if (options.mCancel || options.outWidth == -1 || options.outHeight == -1) {
                return null;
            }
            options.inSampleSize = computeSampleSize(options, minSideLength, maxNumOfPixels);
            options.inJustDecodeBounds = false;

            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeByteArray(data, 0, data.length, options);
        } catch (OutOfMemoryError ex) {
            Log.e(TAG, "Got oom exception ", ex);
            return null;
        }
    }
    
    /**
     * Compute the sample size as a function of minSideLength and maxNumOfPixels. minSideLength is used to specify that
     * minimal width or height of a bitmap. maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in terms of memory usage. The function returns a sample size based on the constraints. Both size and
     * minSideLength can be passed in as IImage.UNCONSTRAINED, which indicates no care of the corresponding constraint.
     * The functions prefers returning a sample size that generates a smaller bitmap, unless minSideLength =
     * IImage.UNCONSTRAINED. Also, the function rounds up the sample size to a power of 2 or multiple of 8 because
     * BitmapFactory only honors sample size this way. For example, BitmapFactory downsamples an image by 2 even though
     * the request is 3. So we round up the sample size to avoid OOM.
     *
     * @param options BitmapFactory.Options
     * @param minSideLength 最小边
     * @param maxNumOfPixels 最大pix点数，用来限制内存使用量
     * @return samle size
     */
    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        final int bits = 8;
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

        int roundedSize;
        if (initialSize <= bits) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + bits - 1) / bits * bits;
        }

        return roundedSize;
    }

    /**
     * computeInitialSampleSize 见computeSampleSize介绍.
     * @param options BitmapFactory.Options
     * @param minSideLength 最小边
     * @param maxNumOfPixels 最大pix点数，用来限制内存使用量
     * @return int
     */
    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        final int defaultUpperBound = 128;
        double w = options.outWidth;
        double h = options.outHeight;

        
        int lowerBound = 1;
        if (maxNumOfPixels != UNCONSTRAINED) {
            lowerBound = (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        }
        int upperBound = defaultUpperBound;
        if (minSideLength != UNCONSTRAINED) {
            upperBound = (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        }

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == UNCONSTRAINED) && (minSideLength == UNCONSTRAINED)) {
            return 1;
        } else if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
}

