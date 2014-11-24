package com.avgtechie.videoencoderdecoder;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import java.io.File;


public class MyActivity extends Activity implements MoviePlayer.PlayerFeedback, SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "MyActivity";
    private MainSurfaceView mMainSurfaceView;
    private MoviePlayer.PlayTask mPlayTask;

    //private static TextureMovieEncoder mVideoEncoder = new TextureMovieEncoder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        mMainSurfaceView = (MainSurfaceView) findViewById(R.id.main_surfaceView);
        FileUtil.getInstance().copyFileFromAssets(this);
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
    public void startRecording(View view) {
        Log.d(TAG, "***** startRecording *****");
        mMainSurfaceView.updateRecordingStatus(RecordingStatus.RECORDING_ON);
        Toast.makeText(this, "Started..", Toast.LENGTH_LONG).show();
    }

    public void stopRecording(View view) {
        Log.d(TAG, "***** stopRecording *****");
        mMainSurfaceView.updateRecordingStatus(RecordingStatus.RECORDING_OFF);
        Toast.makeText(this, "Stopping..", Toast.LENGTH_LONG).show();
    }

    @Override
    public void playbackStopped() {
        Log.d(TAG, "playbackStopped");
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //Log.d(TAG, "onFrameAvailable");
        mMainSurfaceView.requestRender();
    }

    public enum RecordingStatus {
        RECORDING_ON, RECORDING_OFF;
    }
}
