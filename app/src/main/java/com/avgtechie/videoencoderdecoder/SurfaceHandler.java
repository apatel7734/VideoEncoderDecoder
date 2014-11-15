package com.avgtechie.videoencoderdecoder;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by ashish on 11/12/14.
 */
public class SurfaceHandler extends Handler {

    public static final int MSG_SET_SURFACE_TEXTURE = 0;
    private static final String TAG = "SurfaceHandler";

    private WeakReference<MyActivity> mWeakActivity;

    public SurfaceHandler(MyActivity activity) {
        mWeakActivity = new WeakReference<MyActivity>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        int what = msg.what;
        Log.d(TAG, "SurfaceHandler [" + this + "]: what=" + what);
        MyActivity activity = mWeakActivity.get();
        if (activity == null) {
            Log.d(TAG, "Null activity found");
            return;
        }
        switch (what) {
            case MSG_SET_SURFACE_TEXTURE: {
                activity.handleDrawSurfaceTexture((SurfaceTexture) msg.obj);
                break;
            }
            default: {

                break;
            }
        }
    }
}
