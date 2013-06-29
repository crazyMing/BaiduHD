package com.baidu.hd;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.browser.SearchManager;
import com.baidu.hd.R;
import com.baidu.voicerecognition.android.VoiceRecognitionClient;
import com.baidu.voicerecognition.android.VoiceRecognitionClient.VoiceClientStatusChangeListener;
import com.baidu.voicerecognition.android.VoiceRecognitionConfig;

/**
 * @ClassName: VoiceSearchActivity 
 * @Description: 语音搜索类
 * @author LEIKANG 
 * @date 2013-6-25 下午12:14:25
 */
public class VoiceSearchActivity extends BaseActivity implements OnClickListener {
	private static final boolean DEBUG = true;
	
    private static final String SERVER_URL = "http://audiotest.baidu.com:8187/echo.fcgi";

    private static final int PRODUCT_ID = 1;  
	
    /** TAG。*/
    private static final String TAG = "VoiceSearchActivity";
    
    /** 标记语音搜索结果*/
    public static final String TAG_VOICE_RESULT_ = "tag_voice_result_";
    /** 标记语音搜索开始位置*/
    public static final String TAG_VOICE_START_FROM = "tag_voice_start_from";
    
    /** 标记为不是语音搜索*/
    public static final int EXTRA_START_FROM_NONE = -1;
    /** 标记从native进入语音搜索*/
    public static final int EXTRA_START_FROM_NATIVE_PANEL = 0;
    /** 标记从首页输入框进入语音搜索*/
    public static final int EXTRA_START_FROM_MAIN_PANEL = 1;
    /** 标记从搜索页面进入语音搜索*/
    public static final int EXTRA_START_FROM_SEARCH_PANEL = 2;
    
    /** 标记搜索关键字的Extra */
    public static final String EXTRA_QUERY = "extra_key_query";
    /** 标记语音建议的Extra */
    public static final String EXTRA_SUGGESTIONS = "extra_key_suggestions";
    
    /** START POINT DETECTED 前端检测到语音起始点时送出此消息，程序在收到此消息时可以在图形界面上提示用户。 */
    public static final int SPD_MSG = 1;

    /** END POINT DETECTED 前端检测到语音终止点时送出此消息，程序在收到此消息时可以在图形界面上提示用户。 */
    public static final int EPD_MSG = 2;

    /** 前端发送出该消息通常是开始工作后用户没有说话，前端超时. */
    public static final int NO_VOICE_MSG = 3;

    /** 一次网络语音结果获得。 */
    public static final int VOICE_RESULT = 4;

    /** socket 没有建立连接。 */
    public static final int SOCKET_UNCONN = 5;

    /** 搜索超时。 */
    public static final int VOICE_SEARCH_TIMEOUT = 6;

    /** 更新语音输入动画. */
    public static final int UPDATE_INPUT_ANIM = 7;
    
    /** 语音sdk初始化失败。 */
    public static final int VOICE_RECORDER_INITIALIZED_FAIL = 8;
    
    /**
     * 状态值。
     */
    static enum ENUMSTATUS {
        /** 搜索开始。 */
        ENUM_START,
        /** 开始录音。 */
        ENUM_BEGIN,
        /** 开始分析。 */
        ENUM_THINK,
        /** 没有内容。 */
        ENUM_NOCONTENT,
        /** 网络错误。 */
        ENUM_NETERROR,
        /** 超时。 */
        EMUM_TIMEOUT,
        /** 初始化失败。 */
        EMUM_RECORDER_INITIALIZED_FAIL,
    }
    
    
    /** 信息提示框。 */
    private TextView mTextVoiceMsg = null;
    /** image 提示框。 */
    private ImageView mImgVoiceview = null;
    /** image 提示框。 */
    private ImageView mImgVoiceBg = null;
    /** 动画 drawable . */
    private AnimationDrawable mVoiceAnim = null;
    /** 取消按钮。 */
    private Button mCancle = null;
    /** 确定按钮。 */
    private Button mOk = null;
    
    private Context mContext;
    
    private static int voiceFrom = EXTRA_START_FROM_NONE;
    
    
    /** 控制输入动画. */
    private boolean mVoiceInputAnimRun = false;
    /** 当前状态。 */
    ENUMSTATUS menumstatus = ENUMSTATUS.ENUM_BEGIN;
    /** 语音输入动画线程. */
    private Thread mAnimThread;
    
    /** work handler. */
    public Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SPD_MSG: // 录音开始
                    onVoiceBegin();
                    break;
                case NO_VOICE_MSG: // 没有发现语音数据
                     updateUI(ENUMSTATUS.ENUM_NOCONTENT);
                    //mVoiceMsg.setText(R.string.no_voice_data);
                    //  结束录音的采样工作
                    mVoiceInputAnimRun = false;
                    // end add
                    break;
                case EPD_MSG: // 找到语音终点
                    //  结束录音的采样工作
                    mVoiceInputAnimRun = false;
                    // end add
                    onVoiceEnd();
                    break;
                case VOICE_RESULT:
                    mVoiceInputAnimRun = false;
                    ArrayList<String> result = (ArrayList<String>) msg.obj;
                    onVoiceSearchFinished(result);
                    
                    break;
                case SOCKET_UNCONN:
                    mVoiceInputAnimRun = false;
                    onVoiceError();
                    break;
                case VOICE_SEARCH_TIMEOUT:
                    // 结束录音的采样工作
                    mVoiceInputAnimRun = false;
                    onVoiceSearchTimeOut();
                    break;
                case UPDATE_INPUT_ANIM:
                    mOk.setEnabled(false);
                    mOk.setText(R.string.voice_end_speak);
                    mTextVoiceMsg.setText("");
                    
                    mVoiceInputAnimRun = true;
                    openInputAmin();
                    break;
                case VOICE_RECORDER_INITIALIZED_FAIL:
                    onVoiceRecorderInitializedFail();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.xvoice_window);
        mTextVoiceMsg = (TextView) this.findViewById(R.id.voice_msg);
        mCancle = (Button) this.findViewById(R.id.cancel_voice);
        mCancle.setOnClickListener(this);
        mOk = (Button) this.findViewById(R.id.Once_voice);
        mOk.setOnClickListener(this);
        mImgVoiceBg = (ImageView) this.findViewById(R.id.voice_img);
        mImgVoiceview = (ImageView) this.findViewById(R.id.voice_anim);
        mVoiceAnim = (AnimationDrawable) mImgVoiceview.getDrawable();

        super.onCreate(savedInstanceState);

        startVoiceRecognition();
        
        addWidgetVoiceButtonStatistic(getApplicationContext(), getIntent());
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setFinishOnTouchOutside(false);
        }
        
        voiceFrom = getIntent().getIntExtra(TAG_VOICE_START_FROM, EXTRA_START_FROM_NONE);
        
        mContext = this;
 
    }
    
    @Override
    protected void onDestroy() {
        
        super.onDestroy();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        AudioSettings.getInstance(this).pauseOtherAudio();
    }
    
    @Override
    protected void onPause() {
        oncancle();
        super.onPause();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        AudioSettings.getInstance(this).resumeOtherAudio();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.cancel_voice:
            oncancle();
            
            break;
        case R.id.Once_voice:
            if (menumstatus == ENUMSTATUS.ENUM_BEGIN) { 
                int code = VoiceRecognitionClient.getInstance(this).speakFinish();
                if (code == VoiceRecognitionClient.SPEAK_FINISH_RESULT_ILLEGAL_STATE) {
                    //进入重拾页面
                    mHandle.sendEmptyMessage(VOICE_SEARCH_TIMEOUT);
                }
                
            } else if (menumstatus != ENUMSTATUS.ENUM_THINK) {
                startVoiceRecognition();
            }

            break;
        default:
            break;
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK/* && mIsMoveTaskToBack*/) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    /**
     * 取消语音搜索.
     */
    void oncancle() {
        stopVoiceRecognition();
        stopVoiceAnim();
        VoiceRecognitionClient.releaseInstance();
        finish();
    }
    
    /**
     * 启动语音识别
     */
    private void startVoiceRecognition() {
        VoiceRecognitionConfig config = new VoiceRecognitionConfig(PRODUCT_ID);
        config.setServerUrl(SERVER_URL);  
        config.setProp("250");
        
        //启用计算声音强度。
        config.enableVoicePower(true);
        
        int code  = VoiceRecognitionClient.getInstance(this).startVoiceRecognition(
                new VoiceSearchStatusChangeListener(), config);
        
        // 增加语音错误统计
        if (code != VoiceRecognitionClient.START_WORK_RESULT_WORKING) {
           // addVoiceRecognitionErrorStatistic(code);
        }
        
        if (code == VoiceRecognitionClient.START_WORK_RESULT_WORKING) {
            //启动动画
            mHandle.sendEmptyMessage(UPDATE_INPUT_ANIM);
        } else if (code == VoiceRecognitionClient.START_WORK_RESULT_NET_UNUSABLE) {
            mHandle.sendEmptyMessage(SOCKET_UNCONN);
        } else {
            //初始化失败则退出
            mHandle.sendEmptyMessage(VOICE_RECORDER_INITIALIZED_FAIL);
        }
    }
    
    /** stop voice search. */
    void stopVoiceRecognition() {
        mVoiceInputAnimRun = false;
        
        VoiceRecognitionClient.getInstance(this).stopVoiceRecognition();
    }
    
    /** 开始录音。 */
    void onVoiceBegin() {
        updateUI(ENUMSTATUS.ENUM_BEGIN);  
    }
    
    /** 语音结束。 */
    void onVoiceEnd() {
        updateUI(ENUMSTATUS.ENUM_THINK);
    }
    
    /** 
     * 当录音结束。
     * @param result 语音数据
     */
    void onVoiceSearchFinished(List<String> result) {
        
        if (result != null && result.size() > 0) {
        	 Toast.makeText(mContext, result.toString(), Toast.LENGTH_SHORT).show();
            startSearch(result);
            oncancle();
        } 
        else {
        	result = new ArrayList<String>();
        	result.add("你好");
        	result.add("你好吗");
        	result.add("你好不好");
        	result.add("你好好");
        	result.add("你好坏");
        	result.add("你好傻");
        	result.add("你好蠢");
        	result.add("你好无聊");
        	startSearch(result);
        	oncancle();
        }
        
		//        else {
		//            if (DEBUG) {
		//                Log.d(TAG, "server返回数据错误");
		//            }
		//            updateUI(ENUMSTATUS.ENUM_NOCONTENT);
		//        }
    }
    
    /** recorder初始化失败。 */
    private void onVoiceRecorderInitializedFail() {
        if (DEBUG) {
            Log.d(TAG, "onVoiceRecorderInitializedFail");
        }
        
        updateUI(ENUMSTATUS.EMUM_RECORDER_INITIALIZED_FAIL);
    }
    
    /** 录音错误。 */
    void onVoiceError() {
        if (DEBUG) {
            Log.d(TAG, "OnVoiceError");
        }
        
        updateUI(ENUMSTATUS.ENUM_NETERROR);
    }
    
    /** timeout。 */
    private void onVoiceSearchTimeOut() {
        if (DEBUG) {
            Log.d(TAG, "OnVoiceSearchTimeOut");
        }
        
        updateUI(ENUMSTATUS.EMUM_TIMEOUT);
    }
    
    /**
     * 根据语音搜索结果发起搜索。
     * 
     * @param voiceResult
     *            语音搜索建议列表
     */
    private void startSearch(List<String> voiceResult) {
        if (voiceResult == null || voiceResult.size() == 0) {
            return;
        }
 
        String query = voiceResult.get(0);
        String[] suggestons = new String[voiceResult.size()];
        voiceResult.toArray(suggestons);
        
        startVoiceSearch(this, getIntent(), query, suggestons);
    }
    
    /**
     * 根据当前状态更新界面。
     * 
     * @param status
     *            状态
     */
    void updateUI(ENUMSTATUS status) {
        switch (status) {
            case ENUM_START:  
                mTextVoiceMsg.setText(R.string.voice_start);

                mImgVoiceBg.setVisibility(View.VISIBLE);
                mImgVoiceBg.setBackgroundResource(R.drawable.mic);
                mImgVoiceBg.setImageResource(R.drawable.mic_3);
                mImgVoiceview.setVisibility(View.GONE);
                stopVoiceAnim();
                mCancle.setEnabled(true);
                updateButton(mCancle,
                        R.drawable.voice_search_background_btn_selector,
                        R.string.cancel);
                break;
            case ENUM_BEGIN:// 开始录音
                mTextVoiceMsg.setText(R.string.voice_start);

                mImgVoiceBg.setVisibility(View.VISIBLE);
                mImgVoiceview.setVisibility(View.GONE);
                stopVoiceAnim();
                mImgVoiceBg.setBackgroundResource(R.drawable.mic);
                mImgVoiceBg.setImageResource(R.drawable.mic_1);
                mCancle.setEnabled(true);

                updateButton(
                        mOk,
                        R.drawable.voice_search_background_btn_right_selector,
                        R.string.voice_end_speak);
                updateButton(mCancle,
                        R.drawable.voice_search_background_btn_left_selector,
                        R.string.cancel);

                // forceShowVoiceAnim(R.drawable.voice);

                break;
            case ENUM_THINK:// 开始思考
                if (menumstatus == ENUMSTATUS.ENUM_NETERROR) {
                    break;
                }
                mTextVoiceMsg.setText(R.string.voice_nodifying);

                stopVoiceAnim();
                mImgVoiceBg.setVisibility(View.GONE);
                mImgVoiceview.setVisibility(View.VISIBLE);

                mImgVoiceview.setImageResource(R.drawable.voice_thinking);
                mVoiceAnim = (AnimationDrawable) mImgVoiceview.getDrawable();

                mOk.setVisibility(View.GONE);
                updateButton(mCancle,
                        R.drawable.voice_search_background_btn_selector,
                        R.string.cancel);

                forceShowVoiceAnim(R.drawable.voice_thinking);
                break;
            case ENUM_NOCONTENT:// server返回数据错误
            case ENUM_NETERROR:// 网络问题
            case EMUM_TIMEOUT:// 超时
            case EMUM_RECORDER_INITIALIZED_FAIL:// 初始化失败
                if (DEBUG) {
                    android.util.Log.e("voice search", status.name());
                }
                stopVoiceAnim();
                mImgVoiceview.setVisibility(View.GONE);
                mImgVoiceBg.setVisibility(View.VISIBLE);

                int textId = R.string.voice_search_recognize_fail;
                int rid = R.drawable.voice_search_recognize_error;
                if (status == ENUMSTATUS.ENUM_NETERROR) {
                    textId = R.string.voice_search_connect_fail;
                    rid = R.drawable.voice_search_connect_error;
                } else if (status == ENUMSTATUS.EMUM_RECORDER_INITIALIZED_FAIL) {
                    textId = R.string.voice_search_recorder_initialized_fail;
                }
                mTextVoiceMsg.setText(textId);
                mImgVoiceBg.setBackgroundResource(rid);
                mImgVoiceBg.setImageDrawable(null);
                mCancle.setEnabled(true);

                updateButton(mOk,
                        R.drawable.voice_search_background_btn_right_selector,
                        R.string.once);
                updateButton(mCancle,
                        R.drawable.voice_search_background_btn_left_selector,
                        R.string.cancel);
                break;
            default:
                break;
        }
        menumstatus = status;
    }
    
    /**
     * 更新按钮状态
     * @param btn 按钮
     * @param styleRid 按钮样式
     * @param textRid 按钮文本
     */
    private void updateButton(Button btn, int styleRid, int textRid) {
        btn.setVisibility(View.VISIBLE);
        btn.setEnabled(true);
        btn.setText(textRid);
        btn.setBackgroundResource(styleRid);
    }
    
    /**
     * 语音输入动画.
     */
    private void openInputAmin() {
        if (mAnimThread != null && mAnimThread.isAlive()) {
            return;
        }

        initAnimThread();

        try {
            mAnimThread.start();
        } catch (IllegalThreadStateException e) {
            if (DEBUG) {
                Log.w(TAG, "openInputAmin, " + e.getMessage());
            }
        }
    }
    
    /**
     * 初始化语音输入动画线程(从openInputAmin中提出来)
     */
    private void initAnimThread() {
        mAnimThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (mVoiceInputAnimRun) {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            int pcm = 0;
                            // modify by leikang 只在录音状态时采样
                            // if (mRecorder == null
                            // || mImgVoiceBg.getVisibility() != View.VISIBLE) {
                            // return;
                            // }
                            if (menumstatus != ENUMSTATUS.ENUM_BEGIN
                                    || mImgVoiceBg.getVisibility() != View.VISIBLE) {
                                return;
                            }
                            // end modify

                            pcm = (int) VoiceRecognitionClient.getInstance(VoiceSearchActivity.this)
                                    .getCurrentDBLevelMeter();

                            if (DEBUG) {
                                Log.e(TAG, "pcm: " + pcm);
                            }
                            int drawable = R.drawable.mic_1;
                            
                            switch (pcm / 10) {  
                            case 0:
                                drawable = R.drawable.mic_1;
                                break;
                            case 1:
                                drawable = R.drawable.mic_2;
                                break;
                            case 2:
                            case 3:  
                                drawable = R.drawable.mic_3;
                                break;
                            case 4:  
                            case 5:  
                                drawable = R.drawable.mic_4;
                                break;
                            case 6: 
                            case 7:  
                                drawable = R.drawable.mic_5;
                                break;
                            case 8:  
                                drawable = R.drawable.mic_6;
                                break;
                            case 9:  
                                drawable = R.drawable.mic_7;
                                break;
                            default:
                                drawable = R.drawable.mic_7;
                            }

                            mImgVoiceBg.setImageResource(drawable);

                        }
                    });

                    try {
                        Thread.sleep(50);  
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // 直接置空
                mAnimThread = null;
                // end add
            }

        });
        mAnimThread.setName("openInputAmin");
    }
    
    /**
     * 停止声音动画
     */
    private void stopVoiceAnim() {
        if (mVoiceAnim != null) {
            mVoiceAnim.stop();
        }
    }
    
    /**
     * 确保动画动起来
     * 
     * @param rid
     *            资源id
     */
    private void forceShowVoiceAnim(int rid) {
        if (!mVoiceAnim.isRunning()) {
            mImgVoiceview.setImageResource(rid);
            mVoiceAnim = (AnimationDrawable) mImgVoiceview.getDrawable();
            
            //动画过程中使取消按钮可点击，避免误点击
            mCancle.setEnabled(false);
            mVoiceAnim.setCallback(new Callback() {
                
                @Override
                public void unscheduleDrawable(Drawable who, Runnable what) {
                    mHandle.removeCallbacks(what, who);
                }
                
                @Override
                public void scheduleDrawable(Drawable who, Runnable what, long when) {
                    if (!mCancle.isEnabled()) {
                        if (mVoiceAnim.getFrame(mVoiceAnim.getNumberOfFrames() - 1).equals(who.getCurrent())) {
                            mCancle.setEnabled(true);
                        }
                    }
                    
                    mHandle.postAtTime(what, who, when);
                }
                
                @Override
                public void invalidateDrawable(Drawable who) {
                    mImgVoiceview.invalidate();
                }
            });

            final int postDelay = 100;
            // 使用延时处理，因为首次在 oncreate中，不延时没有动画。
            mHandle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mVoiceAnim.start();
                }
            }, postDelay);
        }
    }
    
    /**
     * 增加widget点击语音按钮的行为统计
     * @param context context
     * @param intent intent
     */
    private void addWidgetVoiceButtonStatistic(Context context, Intent intent) {
 
    }
    
    /** 搜索源key*/
    public static final String ENTRANCE_KEY = "search_source";
    public static final String ENTRANCE_MAIN_VOICE = "app_mainbox_voice";
    
    /** 来源于数字时钟语音搜索*/
    public static final String ENTRANCE_DIGITAL_VOICE = "widget_digit_voice";
    /** 来源于数字时钟搜索框搜索*/
    public static final String ENTRANCE_DIGITAL_BOX = "widget_digit_txt";
    /** 来源于模拟时钟语音搜索*/
    public static final String ENTRANCE_ANALOG_VOICE = "widget_analog_voice";
    /** 来源于模拟时钟搜索框搜索*/
    public static final String ENTRANCE_ANALOG_BOX = "widget_analog_txt";
    /** 搜索框widget上的框发起的搜索 */
    public static final String ENTRANCE_BOXWIDGET_BOX = "widget_box_txt";
    
    /**
     * 开始语音搜索
     * @param context context
     * @param intent intent
     * @param query 要搜索的关键词
     * @param voiceSuggestions 语音搜索建议列表
     * @param isFromWidgetVoiceSearch 是否是从widget的语音搜索发起
     */
    public static void startVoiceSearch(Context context, Intent intent, String query,
            String[] voiceSuggestions) {

        SearchManager.launchVoiceSearch(context, query, voiceSuggestions, voiceFrom);
        
        intent.removeExtra(ENTRANCE_KEY);
 
    }
    
    /**
     * 语音识别回调处理
     */
    class VoiceSearchStatusChangeListener implements VoiceClientStatusChangeListener {

        @Override
        public void onClientStatusChange(int status, Object obj) {
            if (DEBUG) {
                Log.e(TAG, "status:" + status);
            }
            switch (status) {
            case VoiceRecognitionClient.CLIENT_STATUS_SPEECH_START: //检测到语音起点
                break;
            case VoiceRecognitionClient.CLIENT_STATUS_SPEECH_END://已经检测到语音终点，等待网络返回
                //检测到语音完成
                mHandle.sendEmptyMessage(EPD_MSG);
                break;
            case VoiceRecognitionClient.CLIENT_STATUS_FINISH:
                //本次识别成功完成
                Message msg = mHandle.obtainMessage(VOICE_RESULT, obj);
                mHandle.sendMessage(msg);
                break;
            case VoiceRecognitionClient.CLIENT_STATUS_START_RECORDING://语音识别实际开始，这是真正开始识别的时间点，需在界面提示用户说话。 
                //语音识别状态: 录音开始，用户可以开始进行语音输入
                mHandle.sendEmptyMessage(SPD_MSG);
                break;
            case VoiceRecognitionClient.CLIENT_STATUS_ERROR:        //语音识别出错，用16进制显示错误码，具体含义参见api
                Message msg2 = mHandle.obtainMessage(VOICE_RESULT, obj);
                mHandle.sendMessage(msg2);
                break;
            case VoiceRecognitionClient.CLIENT_STATUS_USER_CANCELED://通知用户已取消
                break;
            default:
                break;
            }
        }

        @Override
        public void onError(int errorType, int errorCode) {
            if (DEBUG) {
                Log.e(TAG, "errorCode:" + errorCode);
            }
            
            switch (errorCode) {
            case VoiceRecognitionClient.ERROR_NETWORK: //错误码类型:联网错误 
            case VoiceRecognitionClient.ERROR_NETWORK_UNUSABLE: //网络工作状态:网络不可用
            case VoiceRecognitionClient.ERROR_NETWORK_CONNECT_ERROR: //网络工作状态:网络发生错误别
            case VoiceRecognitionClient.ERROR_NETWORK_TIMEOUT: //网络工作状态:网络本次请求超时
            case VoiceRecognitionClient.ERROR_NETWORK_PARSE_EERROR: //网络工作状态:解析失败
                //语音识别状态错误通知:用户未说话 
                mHandle.sendEmptyMessage(SOCKET_UNCONN);
                break;
            default:
                mHandle.sendEmptyMessage(VOICE_SEARCH_TIMEOUT);
                break;
            }
        }

        @Override
        public void onNetworkStatusChange(int arg0, Object arg1) {
            switch (arg0) {
            case VoiceRecognitionClient.NETWORK_STATUS_FINISH:
                
                break;
            default:
                
            }
        }
        
    }
    
    /**
     * 音频设置逻辑，在语音搜索开始时，暂停音频播放（如mp3），结束时再恢复。
     */
    private abstract static class AudioSettings {
        /**Context.*/
        Context mContext;
        
        /**单例。*/
        static AudioSettings mSettings;
        
        /**
         * 获取单例。
         * @param context Context.
         * @return AudioSettings.
         */
        static AudioSettings getInstance(Context context) {
            context = context.getApplicationContext(); //  防止activity泄漏
            if (mSettings == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                    mSettings = new AudioSettings8(context);
                } else {
                    mSettings = new AudioSettings7(context);
                }
            }
            
            return mSettings;
        }
        
        /**
         * 构造。
         * @param context Context.
         */
        public AudioSettings(Context context) {
            mContext = context;
        }
        
        /**
         * 暂停其它音频播放。
         */
        abstract void pauseOtherAudio();
        
        /**
         * 恢复其它音频播放。
         */
        abstract void resumeOtherAudio();
    }
    
    /**
     * SDK7及以下的AudioSettings.
     *
     */
    private static class AudioSettings7 extends AudioSettings {
        
        /**
         * 构造。
         * @param context Context.
         */
        public AudioSettings7(Context context) {
            super(context);
        }

        @Override
        void pauseOtherAudio() {
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "pause");
            mContext.sendBroadcast(i);
        }

        @Override
        void resumeOtherAudio() {
            
        }
        
    }
    
    /**
     * SDK8及以上的AudioSettings.
     */
    private static class AudioSettings8 extends AudioSettings {

        /** AudioManager instance. */
        private AudioManager mAudioManager;
        
        /**
         * 构造。
         * @param context Context.
         */
        public AudioSettings8(Context context) {
            super(context);
            mAudioManager = (AudioManager) context.getSystemService("audio");
        }
        
        @Override
        void pauseOtherAudio() {
            if (mAudioManager != null) {
                mAudioManager.requestAudioFocus(null,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            }
        }

        @Override
        void resumeOtherAudio() {
            if (mAudioManager != null) {
                mAudioManager.abandonAudioFocus(null);
            }
        }
        
    }
}
