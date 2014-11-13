package com.avgtechie.videoencoderdecoder;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by ashish on 11/12/14.
 */
public class SurfaceRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "SurfaceRenderer";

    private FullFrameRect mFullScreen;
    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;
    private SurfaceHandler mSurfaceHandler;
    private final float[] mSTMatrix = new float[16];

    public SurfaceRenderer(SurfaceHandler handler) {
        mSurfaceHandler = handler;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");
        mFullScreen = new FullFrameRect(new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));
        mTextureId = mFullScreen.createTextureObject();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mSurfaceHandler.sendMessage(mSurfaceHandler.obtainMessage(SurfaceHandler.MSG_SET_SURFACE_TEXTURE, mSurfaceTexture));
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged");
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(TAG, "onDrawFrame");
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mFullScreen.drawFrame(mTextureId, mSTMatrix);
    }
}
