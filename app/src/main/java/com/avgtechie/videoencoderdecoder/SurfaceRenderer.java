package com.avgtechie.videoencoderdecoder;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
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

    private SquareWithMemeTexture squareWithMemeTexture;

    private float mScaleFactor = .4f;
    private float mRotationDegrees = 0.3f;
    private float mX;
    private float mY;

    private int surfaceWidth;
    private int surfaceHeight;

    int boxWidth = 400;
    int boxHeight = 400;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private final float[] mScaleMatrix = new float[16];
    private final float[] mTranslationMatrix = new float[16];
    private float[] mFinalMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mTempMatrix = new float[16];

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");
        mFullScreen = new FullFrameRect(new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));
        mTextureId = mFullScreen.createTextureObject();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mSurfaceHandler.sendMessage(mSurfaceHandler.obtainMessage(SurfaceHandler.MSG_SET_SURFACE_TEXTURE, mSurfaceTexture));
        squareWithMemeTexture = new SquareWithMemeTexture();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged");
        surfaceHeight = height;
        surfaceWidth = width;
        float ratio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        //Matrix.orthoM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        Log.d(TAG, String.format("Width = %d and Height = %d", width, height));
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        setupDefaultDrawing();
        // draw decoded frame on surfacetexture
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mFullScreen.drawFrame(mTextureId, mSTMatrix);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, mX / surfaceWidth, -mY / surfaceHeight, 0);

        Matrix.setRotateM(mRotationMatrix, 0, mRotationDegrees, 0, 0, -1.0f);
        mTempMatrix = mModelMatrix.clone();
        Matrix.multiplyMM(mModelMatrix, 0, mTempMatrix, 0, mRotationMatrix, 0);


        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.scaleM(mScaleMatrix, 0, mScaleFactor, mScaleFactor, 0);
        mTempMatrix = mModelMatrix.clone();
        Matrix.multiplyMM(mModelMatrix, 0, mTempMatrix, 0, mScaleMatrix, 0);


        mFinalMatrix = mModelMatrix.clone();
        Matrix.multiplyMM(mMVPMatrix, 0, mFinalMatrix, 0, mMVPMatrix, 0);

/*
        // add translation
        Matrix.setIdentityM(mTranslationMatrix, 0);
        Matrix.translateM(mTranslationMatrix, 0, -mX / surfaceWidth, -mY / surfaceHeight, 0);
        Matrix.multiplyMM(mFinalMatrix, 0, mMVPMatrix, 0, mTranslationMatrix, 0);

        //add rotation
        //Log.d(TAG, "Rotation Degree = " + mRotationDegrees);
        Matrix.setRotateM(mRotationMatrix, 0, mRotationDegrees, 0, 0, 1.0f);
        Matrix.multiplyMM(mFinalMatrix, 0, mFinalMatrix, 0, mRotationMatrix, 0);


        //add scalling
        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.scaleM(mScaleMatrix, 0, mScaleFactor, mScaleFactor, 0);
        Matrix.multiplyMM(mFinalMatrix, 0, mFinalMatrix, 0, mScaleMatrix, 0);
*/

        squareWithMemeTexture.draw(mMVPMatrix);
        //drawBox();
    }

    private void setupDefaultDrawing() {
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

    /*
        private void drawBox() {
            GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
            GLES20.glScissor((surfaceWidth / 2) - (boxWidth / 2), (surfaceHeight / 2) - (boxHeight / 2), boxWidth, boxHeight);
            GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        }
    */
    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public void setY(float y) {
        this.mY = y;
    }

    public void setX(float x) {
        this.mX = x;
    }

    public void setScaleFactor(float mScaleFactor) {
        this.mScaleFactor = mScaleFactor;
    }

    public void setRotationDegrees(float rotationDegree) {
        this.mRotationDegrees = rotationDegree;
    }
}
