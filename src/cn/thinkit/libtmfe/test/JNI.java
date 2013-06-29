package cn.thinkit.libtmfe.test;

public class JNI
{
	/** return value */
	public static final int MFE_SUCCESS                 = 0;
	public static final int MFE_ERROR_UNKNOWN           = -100;
	public static final int MFE_STATE_ERR               = -102;
	public static final int MFE_POINTER_ERR             = -103;
	public static final int MFE_MEMALLOC_ERR            = -107;
	public static final int MFE_PARAMRANGE_ERR          = -109;
	public static final int MFE_SEND_TOOMORE_DATA_ONCE  = -118;
	public static final int MFE_VAD_INIT_ERROR          = -120;

	/** parameter type */
	public static final int PARAM_MAX_WAIT_DURATION       = 1;
	public static final int PARAM_MAX_SP_DURATION         = 2;
	public static final int PARAM_MAX_SP_PAUSE            = 3;
	public static final int PARAM_MIN_SP_DURATION         = 4;
	public static final int PARAM_SLEEP_TIMEOUT           = 5;
	public static final int PARAM_ENERGY_THRESHOLD_SP     = 6;
	public static final int PARAM_ENERGY_THRESHOLD_EP     = 7;
	public static final int PARAM_OFFSET                  = 8;
	public static final int PARAM_SPEECH_END 			  = 9;
	public static final int PARAM_SPEECH_MODE 			  = 10;
	
	// add by jszhang for mfeInit func
	public static final int MFE_FORMAT_BV32_8K = 0;
	public static final int MFE_FORMAT_PCM_8K = 1;
	public static final int MFE_FORMAT_ADPCM_8K = 2;
	public static final int MFE_FORMAT_BV32_16K = 4;
	public static final int MFE_FORMAT_PCM_16K = 5;
 
	  public native int mfeDetect();

	  public native int mfeGetCallbackData(byte[] paramArrayOfByte, int paramInt);

	  public native int mfeInit(int paramInt);

	  public native int mfeOpen();

	  public native void mfeSendData(short[] paramArrayOfShort, int paramInt);

	  public native int mfeStart();

	  public native int mfeStop();
	
	static
	{
        System.loadLibrary("Audiofe_V1");
	}
}

