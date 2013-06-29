package com.baidu.video;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;


public class CyberPlayerSurface extends SurfaceView
{
	public static final String TAG = "CyberPlayerSurface";
    
    // EGL private objects
    private static EGLContext  mEGLContext;
    private static EGLSurface  mEGLSurface;
    private static EGLDisplay  mEGLDisplay;
    private static EGLConfig   mEGLConfig;
    private static int mGLMajor, mGLMinor;
    
    // Startup    
    public CyberPlayerSurface(Context context) {
        super(context);
        init(context);
    }
    
    public CyberPlayerSurface(Context context, AttributeSet Attris) {
 		super(context, Attris);
 		init(context);
 	}

 	public CyberPlayerSurface(Context context, AttributeSet Attris, int defStyle) {
 		super(context, Attris, defStyle);
 		init(context);
 	}
 	
	private void init(Context context) {
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
	}

	
    // EGL functions
	public boolean initEGL(int majorVersion, int minorVersion) {
		if (CyberPlayerSurface.mEGLDisplay == null) {
			try { 
						
				EGL10	egl = (EGL10) EGLContext.getEGL();
				EGLDisplay	dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
		
				int[] version = new int[2];
				egl.eglInitialize(dpy, version);
		
				int EGL_OPENGL_ES_BIT = 1;
				int EGL_OPENGL_ES2_BIT = 4;
				int renderableType = 0;
				if (majorVersion == 2) {
					renderableType = EGL_OPENGL_ES2_BIT;
				} else if (majorVersion == 1) {
					renderableType = EGL_OPENGL_ES_BIT;
				}
				int[] configSpec = {
						// EGL10.EGL_DEPTH_SIZE, 16,
						EGL10.EGL_RENDERABLE_TYPE, renderableType,
						EGL10.EGL_NONE };
				EGLConfig[] configs = new EGLConfig[1];	
				int[] num_config = new int[1];
				if (!egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config)
						|| num_config[0] == 0) {
					Log.e(TAG, "No EGL config available");
					return false;
				}
					
				EGLConfig config = configs[0];
				/*
				int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
				int contextAttrs[] = new int[] { EGL_CONTEXT_CLIENT_VERSION,
						majorVersion, EGL10.EGL_NONE };
					
				EGLContext ctx = egl.eglCreateContext(dpy, config,
						EGL10.EGL_NO_CONTEXT, contextAttrs);
				if (ctx == EGL10.EGL_NO_CONTEXT) {
					
					int error = egl.eglGetError();
					Log.e(TAG, "Couldn't create context, error code=" + error);
					return false;
				}
				//egl.eglDestroyContext(mEGLDisplay, context)
				
				EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, this,	null);
				if (surface == EGL10.EGL_NO_SURFACE) {
					Log.e(TAG, "Couldn't create surface");
					return false;
				}
	
				if (!egl.eglMakeCurrent(dpy, surface, surface, ctx)) {
					Log.e(TAG, "Couldn't make context current");
					return false;
				}
	
				mEGLContext = ctx;
				mEGLDisplay = dpy;
				mEGLSurface = surface;
				*/

				CyberPlayerSurface.mEGLDisplay = dpy;
				CyberPlayerSurface.mEGLConfig = config;
				CyberPlayerSurface.mGLMajor = majorVersion;
				CyberPlayerSurface.mGLMinor = minorVersion;

                return createEGLSurface();
				
			} catch (Exception e) {
				Log.v(TAG, e + "");
				for (StackTraceElement s : e.getStackTrace()) {
					Log.v(TAG, s.toString());
				}
			}
		}else{
			return createEGLSurface();
		}
		return true;
	}

	
	
	
	
    // EGL buffer flip
    public void flipEGL() {
        try {
            EGL10 egl = (EGL10)EGLContext.getEGL();
            egl.eglWaitNative(EGL10.EGL_CORE_NATIVE_ENGINE, null);
            // drawing here
            egl.eglWaitGL();
            egl.eglSwapBuffers(mEGLDisplay, mEGLSurface);           
        } catch(Exception e) {
            Log.v(TAG, "flipEGL(): " + e);
            for (StackTraceElement s : e.getStackTrace()) {
                Log.v(TAG, s.toString());
            }
        }
    }
    
    
    
    public  boolean createEGLContext() {
        EGL10 egl = (EGL10)EGLContext.getEGL();
        int EGL_CONTEXT_CLIENT_VERSION=0x3098;
        int contextAttrs[] = new int[] { EGL_CONTEXT_CLIENT_VERSION, CyberPlayerSurface.mGLMajor, EGL10.EGL_NONE };
        CyberPlayerSurface.mEGLContext = egl.eglCreateContext(CyberPlayerSurface.mEGLDisplay, CyberPlayerSurface.mEGLConfig, EGL10.EGL_NO_CONTEXT, contextAttrs);
        if (CyberPlayerSurface.mEGLContext == EGL10.EGL_NO_CONTEXT) {
            Log.e("SDL", "Couldn't create context");
            return false;
        }
        return true;
    }

    public  boolean createEGLSurface() {
        if (CyberPlayerSurface.mEGLDisplay != null && CyberPlayerSurface.mEGLConfig != null) {
            EGL10 egl = (EGL10)EGLContext.getEGL();
            if (CyberPlayerSurface.mEGLContext == null) createEGLContext();

            Log.v("SDL", "Creating new EGL Surface");
            
            EGLSurface surface = null;
            try{
            	surface	= egl.eglCreateWindowSurface(CyberPlayerSurface.mEGLDisplay, CyberPlayerSurface.mEGLConfig, this, null);
            }catch(java.lang.IllegalArgumentException e){
            	Log.w("SDL", "get the java.lang.IllegalArgumentException");
            	return false;
            }
            
            if (surface == EGL10.EGL_NO_SURFACE) {
                Log.e("SDL", "Couldn't create surface");
                return false;
            }
            	
            if (!egl.eglMakeCurrent(CyberPlayerSurface.mEGLDisplay, surface, surface, CyberPlayerSurface.mEGLContext)) {
                Log.e("SDL", "Old EGL Context doesnt work, trying with a new one");
                Log.e("SDL", "eglmakecurrent error,error code=" + egl.eglGetError());
                createEGLContext();
                if (!egl.eglMakeCurrent(CyberPlayerSurface.mEGLDisplay, surface, surface, CyberPlayerSurface.mEGLContext)) {
                    Log.e("SDL", "Failed making EGL Context current");
                    return false;
                }
            }
            CyberPlayerSurface.mEGLSurface = surface;
            return true;
        }
        return false;
    }
    
    public void releaseEGLContextFromThread(){
    	if (CyberPlayerSurface.mEGLDisplay != null && CyberPlayerSurface.mEGLConfig != null) {
    		Log.v(TAG, "releaseEGLContextFromThread");
    		EGL10 egl = (EGL10)EGLContext.getEGL();
       	 	egl.eglMakeCurrent(CyberPlayerSurface.mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
    	}
    }
}


