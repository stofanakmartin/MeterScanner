package com.stofoProjects.opencvtest.opencvtest.ui.activities;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.WindowManager;

import com.stofoProjects.opencvtest.opencvtest.R;
import com.stofoProjects.opencvtest.opencvtest.ui.fragments.FilterPreviewFragment;
import com.stofoProjects.opencvtest.opencvtest.ui.fragments.RecognizerCameraFragment;
import com.stofoProjects.opencvtest.opencvtest.utils.LogUtils;
import com.stofoProjects.opencvtest.opencvtest.utils.MimeTypes;

/**
 * Created by Martin Stofanak on 4.5.2014.
 */
public class CameraActivity extends Activity {

    private static final String TAG = LogUtils.makeLogTag(CameraActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON ,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN
                                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_general);

        if(savedInstanceState == null) {

            Fragment cameraFragment;
            String fragmentTag;

            if(getIntent().getType().equals(MimeTypes.CAMERA_RECOGNIZER)) {
                cameraFragment = RecognizerCameraFragment.newInstance();
                fragmentTag = RecognizerCameraFragment.TAG;
            } else if (getIntent().getType().equals( MimeTypes.CAMERA_FILTER_PREVIEW)) {
                cameraFragment = FilterPreviewFragment.newInstance();
                fragmentTag = FilterPreviewFragment.TAG;
            } else {
                LogUtils.LOGE(TAG, "Unknown mime type, cannot insert any camera fragment");
                return;
            }

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, cameraFragment, fragmentTag)
                    .commit();
        }
    }
}
