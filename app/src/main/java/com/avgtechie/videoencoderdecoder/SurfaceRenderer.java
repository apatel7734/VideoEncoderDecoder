package com.avgtechie.videoencoderdecoder;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.io.File;

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
    private Context mContext;
    private boolean blendingEnabled = false;

    private float mScaleFactor = .4f;
    private float mRotationDegrees = 0.3f;
    private float mX;
    private float mY;

    private int surfaceWidth;
    private int surfaceHeight;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private final float[] mScaleMatrix = new float[16];
    private final float[] mTranslationMatrix = new float[16];
    private float[] mFinalMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mTempMatrix = new float[16];
    private ImageSprite sprite;
    private ImageSprite encodingSprite;

    //Encoder stuff
    private MyActivity.RecordingStatus mRecordingStatus = MyActivity.RecordingStatus.RECORDING_OFF;
    private MyActivity.RecordingStatus mCurrentRecordingStatus = MyActivity.RecordingStatus.RECORDING_OFF;
    //video encoder stuff
    private static TextureMovieEncoder mVideoEncoder;
    private File mOutputFile;

    public SurfaceRenderer(SurfaceHandler handler, Context context, TextureMovieEncoder videoEncoder) {
        mSurfaceHandler = handler;
        mContext = context;
        mVideoEncoder = videoEncoder;
        mOutputFile = FileUtil.getInstance().getOutputMemeFilePath();
        Log.d(TAG, "outputFile = " + mOutputFile);
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");
        mFullScreen = new FullFrameRect(new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));
        mTextureId = mFullScreen.createTextureObject();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mSurfaceHandler.sendMessage(mSurfaceHandler.obtainMessage(SurfaceHandler.MSG_SET_SURFACE_TEXTURE, mSurfaceTexture));
        //squareWithMemeTexture = new SquareWithMemeTexture(mContext);
        sprite = new ImageSprite(mContext);
        encodingSprite = new ImageSprite(mContext);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged");
        surfaceHeight = height;
        surfaceWidth = width;
        float ratio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        GLES20.glViewport(0, 0, (int) surfaceWidth, (int) surfaceHeight);
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
        Matrix.translateM(mModelMatrix, 0, (mX / surfaceWidth), -(mY / surfaceHeight), 0);


        Matrix.setRotateM(mRotationMatrix, 0, mRotationDegrees, 0, 0, -1.0f);
        mTempMatrix = mModelMatrix.clone();
        Matrix.multiplyMM(mModelMatrix, 0, mTempMatrix, 0, mRotationMatrix, 0);


        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.scaleM(mScaleMatrix, 0, mScaleFactor, mScaleFactor, 0);
        mTempMatrix = mModelMatrix.clone();
        Matrix.multiplyMM(mModelMatrix, 0, mTempMatrix, 0, mScaleMatrix, 0);


        mFinalMatrix = mModelMatrix.clone();
        Matrix.multiplyMM(mMVPMatrix, 0, mFinalMatrix, 0, mMVPMatrix, 0);
        processRecording(mMVPMatrix);
        sprite.doDraw(mMVPMatrix);

    }

    private void processRecording(float[] mvpMatrix) {
        if (!mCurrentRecordingStatus.equals(MyActivity.RecordingStatus.RECORDING_ON) && mRecordingStatus.equals(MyActivity.RecordingStatus.RECORDING_ON)) {
            mCurrentRecordingStatus = MyActivity.RecordingStatus.RECORDING_ON;
            //mVideoEncoder.startRecording(new TextureMovieEncoder.EncoderConfig(mOutputFile, 640, 480, 1000000, EGL14.eglGetCurrentContext()));
            mVideoEncoder.startRecording(new TextureMovieEncoder.EncoderConfig(mOutputFile, 640, 480, 1000000, EGL14.eglGetCurrentContext(), encodingSprite));
            Log.d(TAG, "***** started recording *****");
        } else if (!mCurrentRecordingStatus.equals(MyActivity.RecordingStatus.RECORDING_OFF) && mRecordingStatus.equals(MyActivity.RecordingStatus.RECORDING_OFF)) {
            mCurrentRecordingStatus = MyActivity.RecordingStatus.RECORDING_OFF;
            mVideoEncoder.stopRecording();
            Log.d(TAG, "***** stopped recording *****");
        }
        mVideoEncoder.setTextureId(mTextureId);
        //mVideoEncoder.frameAvailable(mSurfaceTexture);
        mVideoEncoder.frameAvailable(mSurfaceTexture, mvpMatrix.clone());
    }

    private void setupDefaultDrawing() {
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        if (blendingEnabled) {
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
        }
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

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


    public void setRecordingStatus(MyActivity.RecordingStatus recordingStatus) {
        this.mRecordingStatus = recordingStatus;
    }
}
