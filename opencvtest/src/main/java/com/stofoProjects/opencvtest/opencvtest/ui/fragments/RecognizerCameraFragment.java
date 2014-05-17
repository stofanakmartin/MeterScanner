package com.stofoProjects.opencvtest.opencvtest.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.stofoProjects.opencvtest.opencvtest.R;
import com.stofoProjects.opencvtest.opencvtest.filters.HoughLineDetector;
import com.stofoProjects.opencvtest.opencvtest.filters.MeterNumberLineDetector;
import com.stofoProjects.opencvtest.opencvtest.filters.NumberDetector;
import com.stofoProjects.opencvtest.opencvtest.filters.RedBlobDetector;
import com.stofoProjects.opencvtest.opencvtest.models.Rectangle;
import com.stofoProjects.opencvtest.opencvtest.utils.LogUtils;
import com.stofoProjects.opencvtest.opencvtest.widgets.LineGraphViewWidget;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Martin Stofanak on 8.5.2014.
 */
public class RecognizerCameraFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {

    public static final String TAG = LogUtils.makeLogTag(RecognizerCameraFragment.class);

    private static final double ROI_Y_OFFSET = 0.35;
    private static final double ROI_Y_HEIGHT = 0.3;

    private CameraBridgeViewBase mCameraView;
    private RedBlobDetector mRedBlobDetector;
    private HoughLineDetector mLineDetector;
    private NumberDetector mNumberDetector;
    private Mat mRgba;
    private Rectangle mMaxBoundaries;
    private Rectangle mBoundaries;

    private LineGraphViewWidget mGraphWidget;

    @InjectView(R.id.graph_container)
    public FrameLayout mGraphContainer;


    public static RecognizerCameraFragment newInstance() {
        RecognizerCameraFragment fragment = new RecognizerCameraFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recognizer_camera, container, false);

        ButterKnife.inject(this, view);

        mCameraView = ButterKnife.findById(view, R.id.camera_view);
        mCameraView.setCvCameraViewListener(this);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        if(mCameraView != null)
            mCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, getActivity(), mLoaderCallback);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRedBlobDetector = new RedBlobDetector();
        mLineDetector = new HoughLineDetector(width, height);
        mNumberDetector = new NumberDetector(mRgba.width());

        final double roiYOffset = height * ROI_Y_OFFSET;
        final double roiYHeight = height * ROI_Y_HEIGHT;
        mBoundaries = new Rectangle(0, roiYOffset, width, roiYOffset + roiYHeight);
        mMaxBoundaries = new Rectangle(0, roiYOffset, width, roiYOffset + roiYHeight);

        mGraphWidget = new LineGraphViewWidget(getActivity(), "Summed edges", mGraphContainer);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        Rect processingROI = new Rect(mMaxBoundaries.getX1Int(), mMaxBoundaries.getY1Int(), mMaxBoundaries.getWidthInt(), mMaxBoundaries.getHeightInt());
        Mat subRgba = mRgba.submat(processingROI);
        Mat subGray = inputFrame.gray().submat(processingROI);
        mBoundaries = new Rectangle(0, 0, subRgba.width() - 1, subRgba.height() - 1);

        Imgproc.equalizeHist(subGray, subGray);

        //Mat subRgba = new Mat();
        mRedBlobDetector.findBiggestBlob(subRgba);

        if(mRedBlobDetector.hasFoundedBlobs()) {
            mRedBlobDetector.drawBlobs(subRgba);

            // Hough lines
            mLineDetector.findLines(subGray);
            subRgba = mLineDetector.drawLines(subRgba);

            mBoundaries = MeterNumberLineDetector.detectLineOfNumbers(mLineDetector.getLinesXY(), mRedBlobDetector.getBlobs(), mBoundaries);

            MeterNumberLineDetector.drawVerticalBoundaries(subRgba, mBoundaries);
            mNumberDetector.findNumbers(subGray, mBoundaries);
            subRgba = mNumberDetector.drawNumberSegments(subRgba);

            updateGraph(mNumberDetector.getSummedEdges());
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


    private void updateGraph(final Mat edgesVector) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGraphWidget.updateGraph(edgesVector);
            }
        });
    }
}
