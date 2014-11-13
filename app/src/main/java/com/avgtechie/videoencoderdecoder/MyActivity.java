package com.avgtechie.videoencoderdecoder;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import java.io.File;


public class MyActivity extends Activity implements MoviePlayer.PlayerFeedback, SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "MyActivity";
    private GLSurfaceView mMainSurfaceView;
    private SurfaceRenderer mSurfaceRenderer;
    private MoviePlayer.PlayTask mPlayTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        SurfaceHandler surfaceHandler = new SurfaceHandler(this);
        mMainSurfaceView = (GLSurfaceView) findViewById(R.id.main_surfaceView);
        mMainSurfaceView.setEGLContextClientVersion(2);
        mSurfaceRenderer = new SurfaceRenderer(surfaceHandler);
        mMainSurfaceView.setRenderer(mSurfaceRenderer);
        mMainSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        File videoOutputFile = FileUtil.getInstance().getOutputFile(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMainSurfaceView.onPause();
    }

    public void handleDrawSurfaceTexture(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "handleDrawSurfaceTexture");
        surfaceTexture.setOnFrameAvailableListener(this);
        //create new surface using surfacetexture to use as decoder output
        Surface surface = new Surface(surfaceTexture);
        GlUtil.clearSurface(surface);
        playMovie(surface);
    }


    public void playMovie(Surface surface) {
        if (mPlayTask != null) {
            Log.w(TAG, "movie already playing");
            return;
        }
        Log.d(TAG, "starting movie");
        SpeedControlCallback callback = new SpeedControlCallback();

        MoviePlayer player = null;
        try {
            File inputMediaFile = FileUtil.getInstance().getMemeFilePath();
            File dir = FileUtil.getInstance().getMemeDirPath();
            Log.d(TAG, "dir absolute path = " + dir.getAbsolutePath());
            Log.d(TAG, "dir canonical path = " + dir.getCanonicalFile());
            File memeVideo = new File(dir.getPath() + File.separator + "memeVideos");
            Log.d(TAG, "dir absolute path = " + memeVideo.getAbsolutePath());
            if (!memeVideo.exists()) {
                Log.d(TAG, "Directory doesnot exist");
            }
            File[] files = FileUtil.getInstance().getMemeDirPath().listFiles();
            Log.d(TAG, "File  = " + inputMediaFile.getAbsolutePath());
            if (!inputMediaFile.exists()) {
                Log.d(TAG, "Video doesnot exist : " + inputMediaFile);
                Toast.makeText(this, "No Video", Toast.LENGTH_SHORT).show();
                return;
            }
            player = new MoviePlayer(inputMediaFile, surface, callback);
        } catch (Exception ioe) {
            Log.e(TAG, "Unable to play movie", ioe);
            surface.release();
            return;
        }
        mPlayTask = new MoviePlayer.PlayTask(player, this);
        mPlayTask.setLoopMode(false);
        mPlayTask.execute();
    }

    //first clear surface to make sure
    public void startRecording() {
        Log.d(TAG, "startRecording");
    }

    public void stopRecording() {
        Log.d(TAG, "stopRecording");
    }

    @Override
    public void playbackStopped() {
        Log.d(TAG, "playbackStopped");
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "onFrameAvailable");
        mMainSurfaceView.requestRender();
    }
}
