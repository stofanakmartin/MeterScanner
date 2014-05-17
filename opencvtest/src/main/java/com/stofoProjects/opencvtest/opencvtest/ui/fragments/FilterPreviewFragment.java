package com.stofoProjects.opencvtest.opencvtest.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.stofoProjects.opencvtest.opencvtest.R;
import com.stofoProjects.opencvtest.opencvtest.adapters.FilterAdapter;
import com.stofoProjects.opencvtest.opencvtest.filters.FilterCollection;
import com.stofoProjects.opencvtest.opencvtest.utils.LogUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Martin Stofanak on 8.5.2014.
 */
public class FilterPreviewFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {

    public static final String TAG = LogUtils.makeLogTag(FilterPreviewFragment.class);

    private CameraBridgeViewBase mCameraView;
    private Mat mRgba;
    private Mat mBinary;
    private int mSelectedItemIndex = -1;

    private Mat mSobelGradX;
    //private Mat mSobelAbsGradX;
    private Mat mSobelGradY;
    private Mat mSobelGrad;
    //private Mat mSobelAbsGradY;

    @InjectView(R.id.filters_list)
    ListView mFilterList;

    public static FilterPreviewFragment newInstance() {
        FilterPreviewFragment fragment = new FilterPreviewFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_preview, container, false);

        ButterKnife.inject(this, view);

        mCameraView = ButterKnife.findById(view, R.id.camera_view);
        mCameraView.setCvCameraViewListener(this);

        FilterAdapter adapter = new FilterAdapter(getActivity());
        mFilterList.setAdapter(adapter);

        mFilterList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                mSelectedItemIndex = position;
            }
        });

        mFilterList.setSelection(0);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, getActivity(), mLoaderCallback);
    }

    @Override
    public void onPause() {
        super.onPause();

        if(mCameraView != null)
            mCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mBinary = new Mat(height, width, CvType.CV_8UC1);

        mSobelGradX = new Mat(height, width, CvType.CV_8UC1);
        //mSobelAbsGradX = new Mat(height, width, CvType.CV_8UC1);
        mSobelGradY = new Mat(height, width, CvType.CV_8UC1);
        mSobelGrad = new Mat(height, width, CvType.CV_8UC1);
        //mSobelAbsGradY = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        switch(mSelectedItemIndex){
            case FilterAdapter.CANNY:
                mBinary = FilterCollection.cannyFilter(inputFrame.gray());
                Imgproc.cvtColor(mBinary, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
            case FilterAdapter.SOBEL_VERTICAL:
                Mat sobelX = FilterCollection.sobelVertical(inputFrame.gray());
                Imgproc.cvtColor(sobelX, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
            case FilterAdapter.SOBEL_HORIZONTAL:
                Mat sobelY = FilterCollection.sobelHorizontal(inputFrame.gray());
                Imgproc.cvtColor(sobelY, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
            case FilterAdapter.SOBEL_BOTH:
                Mat sobelResult = FilterCollection.sobelBoth(inputFrame.gray());
                Imgproc.cvtColor(sobelResult, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);

            case FilterAdapter.SCHARR:
                Imgproc.Scharr( inputFrame.gray(), mSobelGradX, inputFrame.gray().depth(), 1, 0, 1, 0, Imgproc.BORDER_DEFAULT );
                Imgproc.threshold(mSobelGradX, mSobelGradX, 220, 255, Imgproc.THRESH_BINARY);
                Core.convertScaleAbs(mSobelGradX, mSobelGradX);
                Imgproc.Scharr( inputFrame.gray(), mSobelGradY, inputFrame.gray().depth(), 0, 1, 1, 0, Imgproc.BORDER_DEFAULT );
                Imgproc.threshold(mSobelGradY, mSobelGradY, 220, 255, Imgproc.THRESH_BINARY);
                Core.convertScaleAbs(mSobelGradY, mSobelGradY);

                Core.addWeighted(mSobelGradX, 0.5, mSobelGradY, 0.5, 0, mSobelGrad);
                Imgproc.cvtColor(mSobelGrad, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);

                break;
            default:
                mRgba = inputFrame.rgba();
        }

        return mRgba;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);

            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                    LogUtils.LOGD(TAG, "OpenCV manager loaded successfully");
                    mCameraView.enableView();
                    break;
            }
        }
    };
}
