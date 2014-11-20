package com.avgtechie.videoencoderdecoder;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.almeros.android.multitouch.MoveGestureDetector;
import com.almeros.android.multitouch.RotateGestureDetector;

/**
 * Created by ashish on 11/13/14.
 */
public class MainSurfaceView extends GLSurfaceView {

    private static final String TAG = "MainSurfaceView";
    private SurfaceRenderer mRenderer;

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    private float mScaleFactor = .4f;
    private float mRotationDegrees = 0.f;
    private float mFocusX = 0.f;
    private float mFocusY = 0.f;

    private ScaleGestureDetector mScaleDetector;
    private RotateGestureDetector mRotateDetector;
    private MoveGestureDetector mMoveDetector;

    private static TextureMovieEncoder mVideoEncoder = new TextureMovieEncoder();


    public MainSurfaceView(Context context) {
        super(context);
        Log.d(TAG, "MainSurfaceView(Context)");
        init(context);
    }

    public MainSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "MainSurfaceView(Context,AttributeSet)");
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(2);
        // Set the Renderer for drawing on the GLSurfaceView
        SurfaceHandler surfaceHandler = new SurfaceHandler((MyActivity) context);
        mRenderer = new SurfaceRenderer(surfaceHandler, getContext(), mVideoEncoder);
        setRenderer(mRenderer);
        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mScaleDetector = new ScaleGestureDetector(this.getContext(), new ScaleListener());
        mRotateDetector = new RotateGestureDetector(this.getContext(), new RotateListener());
        mMoveDetector = new MoveGestureDetector(this.getContext(), new MoveListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event != null) {

            mScaleDetector.onTouchEvent(event);
            mRotateDetector.onTouchEvent(event);
            mMoveDetector.onTouchEvent(event);

            mRenderer.setScaleFactor(mScaleFactor);
            mRenderer.setRotationDegrees(mRotationDegrees);
            mRenderer.setX(mFocusX);
            mRenderer.setY(mFocusY);
            requestRender();
        }
        return true;
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor(); // scale change since previous event

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
            return true;
        }
    }

    private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            mRotationDegrees -= detector.getRotationDegreesDelta();
            return true;
        }
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            PointF d = detector.getFocusDelta();
            mFocusX += d.x;
            mFocusY += d.y;
            return true;
        }
    }

    public void updateRecordingStatus(MyActivity.RecordingStatus recordingStatus) {
        mRenderer.setRecordingStatus(recordingStatus);
    }

}
