package com.stofoProjects.opencvtest.opencvtest.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stofoProjects.opencvtest.opencvtest.R;
import com.stofoProjects.opencvtest.opencvtest.utils.LogUtils;
import com.stofoProjects.opencvtest.opencvtest.utils.MimeTypes;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Martin Stofanak on 4.5.2014.
 */
public class MainFragment extends Fragment {

    public static final String TAG = LogUtils.makeLogTag(MainFragment.class);

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        ButterKnife.inject(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @OnClick(R.id.btn_action_scan_numbers)
    public void onScanNumbersClick() {
        startScanCamera(MimeTypes.CAMERA_RECOGNIZER);
    }

    @OnClick(R.id.btn_action_filter_preview)
    public void onFilterPreviewClick() {
        startScanCamera(MimeTypes.CAMERA_FILTER_PREVIEW);
    }

    /**
     * Starts activity which responds to defined mime type
     * In this case our camera activity starts either recognizing or preview-ing filters
     * accordind to specified mime type
     * @param mimeType
     */
    private void startScanCamera(String mimeType) {
        //Intent intent = new Intent(getActivity(), CameraActivity.class);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType(mimeType);
        startActivity(intent);
    }
}
