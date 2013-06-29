/*********************************************
 * ANDROID SOUND RECORDER APPLICATION
 * DESC   : Recording Thread that saves sounds into PCM file.  
 * AUTHOR : Junjie Wang
 * DATE   : 19 JUNE 2010
 * CHANGES: 
 *********************************************/

package com.baidu.hd.voicesearch;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.ByteArrayBuffer;
import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import cn.thinkit.libtmfe.test.JNI;
//import com.baidu.searchbox.SearchBox.QAConfig;
//import com.baidu.searchbox.net.ConnectManager;
//import com.baidu.searchbox.net.ProxyHttpClient;
//import com.baidu.searchbox.util.BaiduIdentityManager;

/**
 * 语音搜索录音管理类。负责和语音server交互，获取语音识别建议。
 */
public class Recorder implements Runnable {
    /** log 开关 。 */
	private static final boolean DEBUG = true;
	
	/** log tag .*/
	public static final String TAG = "Recorder";
	
	/** 当前是否正在录音。 */
	private volatile boolean mIsRecording = false;
	
	/** 采样率. */
	public static final int SAMPLE_RATE_IN_HZ = 8000;

	/** work handler. */
	private Handler mHandle;
	
	/** START POINT DETECTED 前端检测到语音起始点时送出此消息，程序在收到此消息时可以在图形界面上提示用户。*/
	public static final int SPD_MSG = 1;
	
	/** END POINT DETECTED 前端检测到语音终止点时送出此消息，程序在收到此消息时可以在图形界面上提示用户。*/
	public static final int EPD_MSG = 2;
	
	/** 前端发送出该消息通常是开始工作后用户没有说话，前端超时. */
	public static final int NO_VOICE_MSG = 3;
	
	/** 一次网络语音结果获得。 */
	public static final  int VOICE_RESULT = 4;
	
	/** socket 没有建立连接。 */
	public static final int SOCKET_UNCONN = 5;
	
	/** 搜索超时。 */
	public static final int VOICE_SEARCH_TIMEOUT = 6;
	
	/** 本地引擎接口。 */
	private JNI mVREngine;
	
	/** 当前audio record 示例。 */
	private AudioRecord mRecordInstance = null;
	
	/** application context. */
	private Context mContext = null;
	
	/** Changing the sample resolution changes sample type. byte vs. short.*/
	private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

	/** 采样数据队列。 */
    private ArrayList<AudioData> mPool = new ArrayList<AudioData>();
    
	/** 数据发送线程。 */
	private PostDataThread mPostThread = null; 
	
	/** 是否需要发送数据。 */
	private boolean mIsPostingData = false;
	
	/** 是否停止了 cancle() 设置为true。 */
	private boolean isStop = false;
	
	/** 语音识别结果。 */
	private ArrayList<String> mResult = null;
	
	//<add by fujiaxing 20110531 BEGIN
	/**手动结束录音.*/
	private boolean endSpeak = false;
	
	/** ConnectManager， 用于判断当前网络类型. */
//	private ConnectManager mConnectManager;
	
	/** 语音还没开始，无语音数据。 */
	private static final int DETECT_FLAG_NOVOICE = 0;
	
	/** 检测到语音起点，但还未到语音终点。 */
	private static final int DETECT_FLAG_START = 1;
	
	/** 检测到语音终点. */
	private static final int DETECT_FLAG_END = 2;
	
	/**  静音时间过长，无语音数据. */
	private static final int DETECT_FLAG_TIMEOUT = 3;
	
	/** 语音数据太短。 */
	private static final int DETECT_FLAG_SHORT = 4;

	/** header data length. */
	private static final int HEADER_DATA_LENGTH = 36;
	/**
	 * e)  global_key：请求语音串识别码，16字节长的字符串，由客户端生成，对于同一组语音串，必须保证sn值相同，并且对于不同语音串，sn值应该不同。
	 */
	private String mGlobalKey = null;
	
	/**
	 * 应用搜索此 id为64. 不同产品线此对应着不同的id，对应这server端语音不同的数据模型。关系到准确率。
	 */
	private static final int PRODUCT_ID = 128; // 百度搜索此 id为4. 
	
	/**
	 * 设置endSpeak.
	 * @param end false:手动结束录音
	 */
	public void setEndSpeak(boolean end) 
	{
		endSpeak = end;
	}
	//add by fujiaxing 20110531 END>
	
	/** 获得当前语音识别结果。
	 * @return result
	 */
	public ArrayList<String> getResult() {
		return mResult;
	}
	
	/** 数据对象。 */
	public static class AudioData {
	    /**
	     * 构造函数。
	     * @param b 数据
	     * @param seq 序列
	     */
		public AudioData(byte[] b, int seq) {
		    data = null;
			if (b != null) {
			    data = new byte[b.length];
			    System.arraycopy(b, 0, data, 0, b.length);
			}
			
			sequence = seq;
		}
		/** 数据。 */
		byte[] data = null;
		/** 序列。 */
		int sequence;
	}
	/** 清空当前数据队列。 */
	void clearQueue() {
		mPool.clear();
	}

	/**
	 * 向队列中添加一条数据。
	 * @param b AudioData
	 */
	void enQueue(AudioData b) {					//这里应该不存在同步问题，不需要synchronized
		mPool.add(b);
		mPool.notifyAll();
	}
	
	/** 
	 * 从队列的第0位置取一条数据，并从队列中删除。
	 * @return 第一条数据，没有返回 null
	 */
  	private AudioData outQueue() {
		if (mPool.size() > 0) {
			return mPool.remove(0);
		}
		return null;
	}
	
  	/**
  	 * 构造函数。
  	 * @param h handler
  	 * @param j jni
  	 * @param context context
  	 */
	public Recorder(Handler h, JNI j, Context context) {
		super();
		mHandle = h;
		mContext = context;
		mVREngine = j;
		
		mContext = context;

		isStop = false;
		
		mResult = new ArrayList<String>();
		
//		mConnectManager = new ConnectManager(context);
	}
	
	/**
	 * 不进行语音搜索.
	 */
	public void cancle() 
	{
		mIsPostingData = false;
		setRecording(false);
		isStop = true;
		mTimeout = false;
		mVREngine.mfeStop();
		//<deleted by fujiaxing 20110620 在run循环结束自己做 BEGIN
//		if(recordInstance != null) {
//			recordInstance.release();
//			recordInstance = null;
//		}
		//deleted by fujiaxing 20110620 END>
	}
	
	/**
	 * 进行语音搜索？。
	 */
	public void stop() 
	{
		setTimeout(true);
	}
	
    /**
     * 发送数据线程。
     */
    public class  PostDataThread extends Thread {
        /** 当前线程个数。 */
        private int mCurrentThreadNum = 0;
        /** 允许的最大线程个数。 */
        private static final int MAX_THREAD_NUM = 6;
        
        /** work method .*/
    	@Override
        public void run() {
    		if (DEBUG) {
    			Log.d(TAG, "start the PostDataThread ======================");
    		}
    		mCurrentThreadNum = 0;
    		
			postRun();
    	}
    	
    	/**
    	 * 多线程发送数据包
    	 */
    	private void postRun() {
    		while (mIsPostingData) {
    		    
    		        synchronized (PostDataThread.this) {
    		            try {
    		            	if (mCurrentThreadNum >= MAX_THREAD_NUM) {
    		            		wait();
    		            	}
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
    		    }
    		    
    		    final AudioData ad = outQueue();
    			if (ad != null) {
    			    
                    Runnable r = new Runnable() {
                        public void run() {
                            mCurrentThreadNum++;
                            
                            boolean bresult = postData(ad);
                            // 网络不通或者异常的情况，清除数据返回
                            if (!bresult) {
                                clearQueue();
                                
                                setRecording(false);
                                
                                mIsPostingData = false; 
                                mHandle.sendEmptyMessage(SOCKET_UNCONN);
                            }
                            
                            mCurrentThreadNum--;
                            
                            synchronized (PostDataThread.this) {
                                PostDataThread.this.notify();
                            }
                        }
                    };
                    // 由于全部切换为 http接口，另外语音搜索server接口支持乱序发送。
                    //为了提高效率，采用多线程操作。
                    Thread t = new Thread(r);
                    t.setName("voice-recognition");
                    t.start();
					
    				if (ad.sequence < 0) {
    				    mIsPostingData = false;
    				}
    			}
    		}
    		if (DEBUG) {
    		    Log.d(TAG, "exist from  the post data thread==========");
    		}
    	}
    }
	
 
    /**
     * int 转为 bytes 数组。
     * @param num int值
     * @return bytes 数组
     */
    public static byte[] intToBytes(int num) {
        byte[] b = new byte[4]; // SUPPRESS CHECKSTYLE
        int mask = 0x000000FF;  // SUPPRESS CHECKSTYLE
        b[0] = (byte) ((num >> 24) & mask); // SUPPRESS CHECKSTYLE
        b[1] = (byte) ((num >> 16) & mask); // SUPPRESS CHECKSTYLE
        b[2] = (byte) ((num >> 8) & mask); // SUPPRESS CHECKSTYLE
        b[3] = (byte) ((num) & mask); // SUPPRESS CHECKSTYLE
        
        byte temp = b[0];
        b[0] = b[3]; // SUPPRESS CHECKSTYLE
        byte temp2 = b[1];
        b[1] = b[2];
        b[2] = temp2;
        b[3] = temp; // SUPPRESS CHECKSTYLE
        
        return b;
    }

    /**
     * short 转为 byte 数组。
     * @param num byte 值
     * @return byte 数组
     */
    public static byte[] shortToBytes(short num) {
        byte[] b = new byte[2];
        for (int i = 0; i < 2; i++) {
            b[i] = (byte) ((num >> (i * 8)) * 0xff); // SUPPRESS CHECKSTYLE
        }
        return b;
    }

    /** 当前是否超时。 */
    public boolean mTimeout = false;
    
    /**
     * 设置当前是否超时。
     * @param timeout 是否超时
     */
    public void setTimeout(boolean timeout) {
    	mTimeout = timeout;
    }
    /**
     * 是否超时。
     * @return true/false
     */
    public boolean isTimeout() {
    	return mTimeout;
    }
    
    @Override
	public void run()
    {
		String strTipInfo = "";
        JNI j = mVREngine;
        
		if (DEBUG)
		{
		    Log.d(TAG, "Recorder start =================");
		}
        
		// 每一次新的 语音搜索，重新生成global id
		mGlobalKey = generateGlobalKey();
		
        j.mfeStart();											//打开语音引擎
        
		// We're important...
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		// Allocate Recorder and Start Recording...
		int bufferRead = 0;
		final int maxBufferSize = 8192;
		int bufferSize = Math.max(AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
		        AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT), maxBufferSize);

		if (this.mRecordInstance != null)
	    {
	      this.mRecordInstance.release();
	      this.mRecordInstance = null;
	    }
		
		mRecordInstance = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ, 
				AudioFormat.CHANNEL_CONFIGURATION_MONO, 
				AudioFormat.ENCODING_PCM_16BIT,  bufferSize);
		
		short[] realBuffer = new short[bufferSize];
		byte[] datBuffer = new byte[bufferSize];
		
		if (mRecordInstance.getState() != AudioRecord.STATE_INITIALIZED) {
		    Log.d(TAG, "AudioRecord init fail");
			return;
		}
		mRecordInstance.startRecording();
		
		long lastReadtime = System.currentTimeMillis();
		int detectFlag = 0;
		int sequence = 1;
		while (mIsRecording) {
			while (System.currentTimeMillis() - lastReadtime < 200) { //SUPPRESS CHECKSTYLE
				try {
					Thread.sleep(100); //SUPPRESS CHECKSTYLE
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			lastReadtime = System.currentTimeMillis();
			
			bufferRead = mRecordInstance.read(realBuffer, 0, bufferSize);
			
			if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION 
					|| bufferRead == AudioRecord.ERROR_BAD_VALUE) {
				return;
			}
			
			j.mfeSendData(realBuffer, bufferRead); 			
			
			detectFlag = j.mfeDetect();
			int msgval = NO_VOICE_MSG;
			int readlen = 0;
			if (endSpeak) { //add by fujiaxing 20110531 手动结束录音
				readlen = j.mfeGetCallbackData(datBuffer, bufferSize);
				mTimeout = true;
				
				if (readlen <= 0) {
				    msgval = VOICE_SEARCH_TIMEOUT; //NO_VOICE_MSG
				} else {
				    msgval = EPD_MSG;
				}
				
				setRecording(false);
			} else if (detectFlag == DETECT_FLAG_START) { //检测到语音起点，但还未到语音终点
                if (!mIsRecording) {
                    break;
                }
                
				readlen = j.mfeGetCallbackData(datBuffer, bufferSize);
				if (readlen > 0) {
					msgval = SPD_MSG;
				}
				if (mPostThread == null) {
					mPostThread = new PostDataThread();
					mIsPostingData = true;
					mPostThread.start();
				}
			} else if (detectFlag == DETECT_FLAG_END) { //检测到语音终点
				readlen = j.mfeGetCallbackData(datBuffer, bufferSize);
				if (readlen > 0) {
					msgval = EPD_MSG;
					setRecording(false);
				}
			} else if (detectFlag == DETECT_FLAG_TIMEOUT) { //静音时间过长，无语音数据
				if (DEBUG) {
					android.util.Log.d(TAG, "long time quiet");
				}
			} else if (detectFlag == DETECT_FLAG_SHORT) { //语音数据太短
				if (DEBUG) {
					android.util.Log.d(TAG, "voice is short");
				}
			}

			if (msgval == NO_VOICE_MSG && mTimeout) {
				msgval = VOICE_SEARCH_TIMEOUT;
				setRecording(false);
			}

			if (msgval != NO_VOICE_MSG) {
				Message msg = mHandle.obtainMessage(msgval, strTipInfo);
				mHandle.sendMessage(msg);
			}
			
			if (readlen > 0) {
				byte[] bits = new byte[readlen];
				//<modify by fujiaxing 20110611 BEGIN
				System.arraycopy(datBuffer, 0, bits, 0, readlen);
//				for(int i= 0; i < readlen; i++){
//					bits[i] = datBuffer[i];
//				}
//				//modify by fujiaxing 20110611 END>
				int seq = sequence;
				if (detectFlag == 2 || mTimeout) {			//找到结束语音
					seq = -sequence;
				}
				
				synchronized (mPool) {
					enQueue(new AudioData(bits, seq));
				}

				sequence++;
			}

			if (DEBUG) {
			    Log.d(TAG, "MFE detection returns " + detectFlag + " with " + readlen + " bytes");
			}

			if (DEBUG) {
			    Log.d(TAG, "msgval = " + msgval);
			}
		}
		j.mfeStop();
		// Close resources...
		if (mRecordInstance != null) {
			mRecordInstance.release();
			mRecordInstance = null;
		}
		if (DEBUG) {
		    Log.d(TAG, "Exit from the recording thread");
		}
	}
	
	/**
	 * 创建一个 http client。
	 * @return ProxyHttpClient
	 */
//	public ProxyHttpClient createHttpClient() 
	public HttpClient createHttpClient() 
	{
//	    ProxyHttpClient httpclient = new ProxyHttpClient(mContext, mConnectManager);
		HttpClient httpclient = new DefaultHttpClient();
	    
	    final int httpTimeout = 30000;
	    final int socketTimeout = 50000;
		HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), httpTimeout);
		HttpConnectionParams.setSoTimeout(httpclient.getParams(), socketTimeout);
		
	    return httpclient;
	}
	
	/**
	 * 通过http方式发送数据到服务端.
	 * @param ad 数据
	 * @return 是否成功
	 */
	boolean postData(AudioData ad) {
		HttpResponse response = null;
//		String url = QAConfig.voiceServer;
		String url = "http://vse.baidu.com/echo.fcgi";
		
		int sequence = ad.sequence;
		final int bufferSize = 2048;
		ByteArrayBuffer	bab1 = new ByteArrayBuffer(bufferSize);
		bab1.clear();
		StringBuilder pre = new StringBuilder();
		String boundary = "----------Baidu_audio";
		////////
		pre.append("\r\n--" + boundary);
		pre.append("\r\nContent-Disposition: form-data; name=\"identity\"\r\n");
//		pre.append("\r\n" + BaiduIdentityManager.getInstance(mContext).getUid());
		pre.append("\r\nunknow");
		
        pre.append("\r\n--" + boundary);
        pre.append("\r\nContent-Disposition: form-data; name=\"idx\"\r\n");
        pre.append("\r\n" + sequence);		
		
        pre.append("\r\n--" + boundary);
        pre.append("\r\nContent-Disposition: form-data; name=\"product\"\r\n");
        pre.append("\r\n" + PRODUCT_ID); // 软件搜索 product id 为 4

        pre.append("\r\n--" + boundary);
        pre.append("\r\nContent-Disposition: form-data; name=\"global_key\"\r\n");
        pre.append("\r\n" + mGlobalKey);   
		
        pre.append("\r\n--" + boundary);
        pre.append("\r\nContent-Disposition: form-data; name=\"return_type\"\r\n");
        pre.append("\r\n" + "xml");   
        
        pre.append("\r\n--" + boundary);
        pre.append("\r\nContent-Disposition: form-data; name=\"encode\"\r\n");
        pre.append("\r\n" + "utf-8");   
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		pre.append("\r\n--" + boundary 
		        + "\r\nContent-Disposition: form-data; name=\"content\"; Content-Type: audio/x-wav\r\n\r\n");
		
		if (DEBUG) {
		    System.out.println(pre.toString());
		}
		
		byte[] startpart = pre.toString().getBytes();
		bab1.append(startpart, 0, startpart.length);
		
		bab1.append(ad.data, 0, ad.data.length);
		
		String end = "\r\n--" + boundary + "--" + "\r\n";
		byte[] endpart = end.getBytes();
		bab1.append(endpart, 0, endpart.length);
		
		ByteArrayEntity bae = new ByteArrayEntity(bab1.toByteArray());
		bae.setContentType("multipart/form-data; boundary=" + boundary);
		
		
		if (DEBUG) {
		    Log.d(TAG, "postData  length     " + ad.data.length);
		}
		
		
		if (DEBUG) {
		    Log.d(TAG, "final post url  = " + url);
		}
		
		HttpClient httpclient = createHttpClient();
		HttpPost httppost = null;
		
		httppost = new HttpPost(url);
		httppost.setEntity(bae);

		
		InputStream inputStream = null;
      
		try {
			response = httpclient.execute(httppost);
			if (sequence < 0) {
			    
    	        HttpEntity resEntity = response.getEntity();
    	        inputStream = resEntity.getContent();
	        

			    //HttpEntity resEntity = response.getEntity();
	            //String s = EntityUtils.toString(resEntity);
	            //System.out.println(s);
			    
			    if (inputStream != null) {
 				
		    	if (DEBUG) {
		    	    Log.d(TAG, "post data sequence " + sequence + "stop" + isStop);
			   	}

   				if (sequence < 0 && !isStop) 
   				{
   				    parseData(inputStream);

   			    	if (DEBUG)
   			    	{
   			    		android.util.Log.e("recorder", "send  VOICE_RESULT");
   				   	}
   					mHandle.sendEmptyMessage(VOICE_RESULT);
   				}

	        	return true;
	        } else {
		    	if (DEBUG) {
		    	    Log.d(TAG, "post data return null =========");
			   	}
	        	return false;
	        }
			} else {
				return true;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
//		    httpclient.close();
			httpclient.getConnectionManager().shutdown();
		}
		
	}
	
	/**
	 * e)  global_key：请求语音串识别码，16字节长的字符串，由客户端生成，对于同一组语音串，必须保证sn值相同，并且对于不同语音串，sn值应该不同。 
	 * @return global_key
	 */
	public String generateGlobalKey() {
	    String globalKey = null;
        final int imeiLength = 16; // gsm imei 号长度 15
        final int ten = 10; // 产生10以内的随机数
        Random random = new Random();
        StringBuffer sb = new StringBuffer(imeiLength);
        for (int i = 0; i < imeiLength; i++) {
            int r = random.nextInt(ten);
            sb.append(r);
        }
        globalKey = sb.toString();
        return globalKey;
	}

	/**
	 * setRecording
	 * @param isRecording
	 *            the isRecording to set
	 */
	public void setRecording(boolean isRecording) {

		this.mIsRecording = isRecording;

	}

	/**
	 * isRecording
	 * @return the isRecording
	 */
	public boolean isRecording() {
		return mIsRecording;
	}

	/**
	 * getAudioEncoding
	 * @return the audioEncoding
	 */
	public int getAudioEncoding() {
		return AUDIO_ENCODING;
	}
	
	/**
	 * 解析 server 返回的数据。
	 * @param in 输入流
	 */
    public void parseData(InputStream in) {

        try {
            XmlPullParser xpp = Xml.newPullParser();
            xpp.setInput(in, null); // null = default to UTF-8
            
            int eventType;

            String suggestion = ""; // 关键词

            eventType = xpp.getEventType();
            
            mResult.clear(); // 清空之前的数据
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tag = xpp.getName();
                    if (tag.equals("item")) {
                        xpp.next();
                        suggestion = xpp.getText();
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String tag = xpp.getName();
                    if (tag.equals("item")) {
                        mResult.add(suggestion);
                        if (DEBUG) {
                            System.out.println(suggestion);
                        }
                    }
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
