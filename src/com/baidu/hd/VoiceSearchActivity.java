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
 * @Description: ����������
 * @author LEIKANG 
 * @date 2013-6-25 ����12:14:25
 */
public class VoiceSearchActivity extends BaseActivity implements OnClickListener {
	private static final boolean DEBUG = true;
	
    private static final String SERVER_URL = "http://audiotest.baidu.com:8187/echo.fcgi";

    private static final int PRODUCT_ID = 1;  
	
    /** TAG��*/
    private static final String TAG = "VoiceSearchActivity";
    
    /** ��������������*/
    public static final String TAG_VOICE_RESULT_ = "tag_voice_result_";
    /** �������������ʼλ��*/
    public static final String TAG_VOICE_START_FROM = "tag_voice_start_from";
    
    /** ���Ϊ������������*/
    public static final int EXTRA_START_FROM_NONE = -1;
    /** ��Ǵ�native������������*/
    public static final int EXTRA_START_FROM_NATIVE_PANEL = 0;
    /** ��Ǵ���ҳ����������������*/
    public static final int EXTRA_START_FROM_MAIN_PANEL = 1;
    /** ��Ǵ�����ҳ�������������*/
    public static final int EXTRA_START_FROM_SEARCH_PANEL = 2;
    
    /** ��������ؼ��ֵ�Extra */
    public static final String EXTRA_QUERY = "extra_key_query";
    /** ������������Extra */
    public static final String EXTRA_SUGGESTIONS = "extra_key_suggestions";
    
    /** START POINT DETECTED ǰ�˼�⵽������ʼ��ʱ�ͳ�����Ϣ���������յ�����Ϣʱ������ͼ�ν�������ʾ�û��� */
    public static final int SPD_MSG = 1;

    /** END POINT DETECTED ǰ�˼�⵽������ֹ��ʱ�ͳ�����Ϣ���������յ�����Ϣʱ������ͼ�ν�������ʾ�û��� */
    public static final int EPD_MSG = 2;

    /** ǰ�˷��ͳ�����Ϣͨ���ǿ�ʼ�������û�û��˵����ǰ�˳�ʱ. */
    public static final int NO_VOICE_MSG = 3;

    /** һ���������������á� */
    public static final int VOICE_RESULT = 4;

    /** socket û�н������ӡ� */
    public static final int SOCKET_UNCONN = 5;

    /** ������ʱ�� */
    public static final int VOICE_SEARCH_TIMEOUT = 6;

    /** �����������붯��. */
    public static final int UPDATE_INPUT_ANIM = 7;
    
    /** ����sdk��ʼ��ʧ�ܡ� */
    public static final int VOICE_RECORDER_INITIALIZED_FAIL = 8;
    
    /**
     * ״ֵ̬��
     */
    static enum ENUMSTATUS {
        /** ������ʼ�� */
        ENUM_START,
        /** ��ʼ¼���� */
        ENUM_BEGIN,
        /** ��ʼ������ */
        ENUM_THINK,
        /** û�����ݡ� */
        ENUM_NOCONTENT,
        /** ������� */
        ENUM_NETERROR,
        /** ��ʱ�� */
        EMUM_TIMEOUT,
        /** ��ʼ��ʧ�ܡ� */
        EMUM_RECORDER_INITIALIZED_FAIL,
    }
    
    
    /** ��Ϣ��ʾ�� */
    private TextView mTextVoiceMsg = null;
    /** image ��ʾ�� */
    private ImageView mImgVoiceview = null;
    /** image ��ʾ�� */
    private ImageView mImgVoiceBg = null;
    /** ���� drawable . */
    private AnimationDrawable mVoiceAnim = null;
    /** ȡ����ť�� */
    private Button mCancle = null;
    /** ȷ����ť�� */
    private Button mOk = null;
    
    private Context mContext;
    
    private static int voiceFrom = EXTRA_START_FROM_NONE;
    
    
    /** �������붯��. */
    private boolean mVoiceInputAnimRun = false;
    /** ��ǰ״̬�� */
    ENUMSTATUS menumstatus = ENUMSTATUS.ENUM_BEGIN;
    /** �������붯���߳�. */
    private Thread mAnimThread;
    
    /** work handler. */
    public Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SPD_MSG: // ¼����ʼ
                    onVoiceBegin();
                    break;
                case NO_VOICE_MSG: // û�з�����������
                     updateUI(ENUMSTATUS.ENUM_NOCONTENT);
                    //mVoiceMsg.setText(R.string.no_voice_data);
                    //  ����¼���Ĳ�������
                    mVoiceInputAnimRun = false;
                    // end add
                    break;
                case EPD_MSG: // �ҵ������յ�
                    //  ����¼���Ĳ�������
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
                    // ����¼���Ĳ�������
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
                    //������ʰҳ��
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
     * ȡ����������.
     */
    void oncancle() {
        stopVoiceRecognition();
        stopVoiceAnim();
        VoiceRecognitionClient.releaseInstance();
        finish();
    }
    
    /**
     * ��������ʶ��
     */
    private void startVoiceRecognition() {
        VoiceRecognitionConfig config = new VoiceRecognitionConfig(PRODUCT_ID);
        config.setServerUrl(SERVER_URL);  
        config.setProp("250");
        
        //���ü�������ǿ�ȡ�
        config.enableVoicePower(true);
        
        int code  = VoiceRecognitionClient.getInstance(this).startVoiceRecognition(
                new VoiceSearchStatusChangeListener(), config);
        
        // ������������ͳ��
        if (code != VoiceRecognitionClient.START_WORK_RESULT_WORKING) {
           // addVoiceRecognitionErrorStatistic(code);
        }
        
        if (code == VoiceRecognitionClient.START_WORK_RESULT_WORKING) {
            //��������
            mHandle.sendEmptyMessage(UPDATE_INPUT_ANIM);
        } else if (code == VoiceRecognitionClient.START_WORK_RESULT_NET_UNUSABLE) {
            mHandle.sendEmptyMessage(SOCKET_UNCONN);
        } else {
            //��ʼ��ʧ�����˳�
            mHandle.sendEmptyMessage(VOICE_RECORDER_INITIALIZED_FAIL);
        }
    }
    
    /** stop voice search. */
    void stopVoiceRecognition() {
        mVoiceInputAnimRun = false;
        
        VoiceRecognitionClient.getInstance(this).stopVoiceRecognition();
    }
    
    /** ��ʼ¼���� */
    void onVoiceBegin() {
        updateUI(ENUMSTATUS.ENUM_BEGIN);  
    }
    
    /** ���������� */
    void onVoiceEnd() {
        updateUI(ENUMSTATUS.ENUM_THINK);
    }
    
    /** 
     * ��¼��������
     * @param result ��������
     */
    void onVoiceSearchFinished(List<String> result) {
        
        if (result != null && result.size() > 0) {
        	 Toast.makeText(mContext, result.toString(), Toast.LENGTH_SHORT).show();
            startSearch(result);
            oncancle();
        } 
        else {
        	result = new ArrayList<String>();
        	result.add("���");
        	result.add("�����");
        	result.add("��ò���");
        	result.add("��ú�");
        	result.add("��û�");
        	result.add("���ɵ");
        	result.add("��ô�");
        	result.add("�������");
        	startSearch(result);
        	oncancle();
        }
        
		//        else {
		//            if (DEBUG) {
		//                Log.d(TAG, "server�������ݴ���");
		//            }
		//            updateUI(ENUMSTATUS.ENUM_NOCONTENT);
		//        }
    }
    
    /** recorder��ʼ��ʧ�ܡ� */
    private void onVoiceRecorderInitializedFail() {
        if (DEBUG) {
            Log.d(TAG, "onVoiceRecorderInitializedFail");
        }
        
        updateUI(ENUMSTATUS.EMUM_RECORDER_INITIALIZED_FAIL);
    }
    
    /** ¼������ */
    void onVoiceError() {
        if (DEBUG) {
            Log.d(TAG, "OnVoiceError");
        }
        
        updateUI(ENUMSTATUS.ENUM_NETERROR);
    }
    
    /** timeout�� */
    private void onVoiceSearchTimeOut() {
        if (DEBUG) {
            Log.d(TAG, "OnVoiceSearchTimeOut");
        }
        
        updateUI(ENUMSTATUS.EMUM_TIMEOUT);
    }
    
    /**
     * �������������������������
     * 
     * @param voiceResult
     *            �������������б�
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
     * ���ݵ�ǰ״̬���½��档
     * 
     * @param status
     *            ״̬
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
            case ENUM_BEGIN:// ��ʼ¼��
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
            case ENUM_THINK:// ��ʼ˼��
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
            case ENUM_NOCONTENT:// server�������ݴ���
            case ENUM_NETERROR:// ��������
            case EMUM_TIMEOUT:// ��ʱ
            case EMUM_RECORDER_INITIALIZED_FAIL:// ��ʼ��ʧ��
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
     * ���°�ť״̬
     * @param btn ��ť
     * @param styleRid ��ť��ʽ
     * @param textRid ��ť�ı�
     */
    private void updateButton(Button btn, int styleRid, int textRid) {
        btn.setVisibility(View.VISIBLE);
        btn.setEnabled(true);
        btn.setText(textRid);
        btn.setBackgroundResource(styleRid);
    }
    
    /**
     * �������붯��.
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
     * ��ʼ���������붯���߳�(��openInputAmin�������)
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
                            // modify by leikang ֻ��¼��״̬ʱ����
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

                // ֱ���ÿ�
                mAnimThread = null;
                // end add
            }

        });
        mAnimThread.setName("openInputAmin");
    }
    
    /**
     * ֹͣ��������
     */
    private void stopVoiceAnim() {
        if (mVoiceAnim != null) {
            mVoiceAnim.stop();
        }
    }
    
    /**
     * ȷ������������
     * 
     * @param rid
     *            ��Դid
     */
    private void forceShowVoiceAnim(int rid) {
        if (!mVoiceAnim.isRunning()) {
            mImgVoiceview.setImageResource(rid);
            mVoiceAnim = (AnimationDrawable) mImgVoiceview.getDrawable();
            
            //����������ʹȡ����ť�ɵ������������
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
            // ʹ����ʱ������Ϊ�״��� oncreate�У�����ʱû�ж�����
            mHandle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mVoiceAnim.start();
                }
            }, postDelay);
        }
    }
    
    /**
     * ����widget���������ť����Ϊͳ��
     * @param context context
     * @param intent intent
     */
    private void addWidgetVoiceButtonStatistic(Context context, Intent intent) {
 
    }
    
    /** ����Դkey*/
    public static final String ENTRANCE_KEY = "search_source";
    public static final String ENTRANCE_MAIN_VOICE = "app_mainbox_voice";
    
    /** ��Դ������ʱ����������*/
    public static final String ENTRANCE_DIGITAL_VOICE = "widget_digit_voice";
    /** ��Դ������ʱ������������*/
    public static final String ENTRANCE_DIGITAL_BOX = "widget_digit_txt";
    /** ��Դ��ģ��ʱ����������*/
    public static final String ENTRANCE_ANALOG_VOICE = "widget_analog_voice";
    /** ��Դ��ģ��ʱ������������*/
    public static final String ENTRANCE_ANALOG_BOX = "widget_analog_txt";
    /** ������widget�ϵĿ�������� */
    public static final String ENTRANCE_BOXWIDGET_BOX = "widget_box_txt";
    
    /**
     * ��ʼ��������
     * @param context context
     * @param intent intent
     * @param query Ҫ�����Ĺؼ���
     * @param voiceSuggestions �������������б�
     * @param isFromWidgetVoiceSearch �Ƿ��Ǵ�widget��������������
     */
    public static void startVoiceSearch(Context context, Intent intent, String query,
            String[] voiceSuggestions) {

        SearchManager.launchVoiceSearch(context, query, voiceSuggestions, voiceFrom);
        
        intent.removeExtra(ENTRANCE_KEY);
 
    }
    
    /**
     * ����ʶ��ص�����
     */
    class VoiceSearchStatusChangeListener implements VoiceClientStatusChangeListener {

        @Override
        public void onClientStatusChange(int status, Object obj) {
            if (DEBUG) {
                Log.e(TAG, "status:" + status);
            }
            switch (status) {
            case VoiceRecognitionClient.CLIENT_STATUS_SPEECH_START: //��⵽�������
                break;
            case VoiceRecognitionClient.CLIENT_STATUS_SPEECH_END://�Ѿ���⵽�����յ㣬�ȴ����緵��
                //��⵽�������
                mHandle.sendEmptyMessage(EPD_MSG);
                break;
            case VoiceRecognitionClient.CLIENT_STATUS_FINISH:
                //����ʶ��ɹ����
                Message msg = mHandle.obtainMessage(VOICE_RESULT, obj);
                mHandle.sendMessage(msg);
                break;
            case VoiceRecognitionClient.CLIENT_STATUS_START_RECORDING://����ʶ��ʵ�ʿ�ʼ������������ʼʶ���ʱ��㣬���ڽ�����ʾ�û�˵���� 
                //����ʶ��״̬: ¼����ʼ���û����Կ�ʼ������������
                mHandle.sendEmptyMessage(SPD_MSG);
                break;
            case VoiceRecognitionClient.CLIENT_STATUS_ERROR:        //����ʶ�������16������ʾ�����룬���庬��μ�api
                Message msg2 = mHandle.obtainMessage(VOICE_RESULT, obj);
                mHandle.sendMessage(msg2);
                break;
            case VoiceRecognitionClient.CLIENT_STATUS_USER_CANCELED://֪ͨ�û���ȡ��
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
            case VoiceRecognitionClient.ERROR_NETWORK: //����������:�������� 
            case VoiceRecognitionClient.ERROR_NETWORK_UNUSABLE: //���繤��״̬:���粻����
            case VoiceRecognitionClient.ERROR_NETWORK_CONNECT_ERROR: //���繤��״̬:���緢�������
            case VoiceRecognitionClient.ERROR_NETWORK_TIMEOUT: //���繤��״̬:���籾������ʱ
            case VoiceRecognitionClient.ERROR_NETWORK_PARSE_EERROR: //���繤��״̬:����ʧ��
                //����ʶ��״̬����֪ͨ:�û�δ˵�� 
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
     * ��Ƶ�����߼���������������ʼʱ����ͣ��Ƶ���ţ���mp3��������ʱ�ٻָ���
     */
    private abstract static class AudioSettings {
        /**Context.*/
        Context mContext;
        
        /**������*/
        static AudioSettings mSettings;
        
        /**
         * ��ȡ������
         * @param context Context.
         * @return AudioSettings.
         */
        static AudioSettings getInstance(Context context) {
            context = context.getApplicationContext(); //  ��ֹactivityй©
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
         * ���졣
         * @param context Context.
         */
        public AudioSettings(Context context) {
            mContext = context;
        }
        
        /**
         * ��ͣ������Ƶ���š�
         */
        abstract void pauseOtherAudio();
        
        /**
         * �ָ�������Ƶ���š�
         */
        abstract void resumeOtherAudio();
    }
    
    /**
     * SDK7�����µ�AudioSettings.
     *
     */
    private static class AudioSettings7 extends AudioSettings {
        
        /**
         * ���졣
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
     * SDK8�����ϵ�AudioSettings.
     */
    private static class AudioSettings8 extends AudioSettings {

        /** AudioManager instance. */
        private AudioManager mAudioManager;
        
        /**
         * ���졣
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
