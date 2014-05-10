package com.stofoProjects.opencvtest.opencvtest.ui.activities;


public class MainActivity_old{

}

/*
public class MainActivity_old extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String    TAG = "OCVSample::Activity";

    private static final int       VIEW_MODE_RGBA     = 0;
    private static final int       VIEW_MODE_GRAY     = 1;
    private static final int       VIEW_MODE_CANNY    = 2;
    private static final int       VIEW_MODE_FEATURES = 5;
    private static final int       VIEW_MODE_HOUGH_LINES = 6;

    private int                    mViewMode;
    private Mat mRgba;
    private Mat                    mIntermediateMat;
    private Mat                    mGray;
    private Mat                    mLines;

    private MenuItem               mItemPreviewRGBA;
    private MenuItem               mItemPreviewGray;
    private MenuItem               mItemPreviewCanny;
    private MenuItem               mItemPreviewFeatures;
    private MenuItem               mItemPreviewHoughLines;

    private CameraBridgeViewBase   mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    //System.loadLibrary("mixed_sample");

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity_old() {
        Log.i(TAG, "Instantiated new MainActivity");
    }

    */
/** Called when the activity is first created. *//*

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial2_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial2_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewRGBA = menu.add("Preview RGBA");
        mItemPreviewGray = menu.add("Preview GRAY");
        mItemPreviewCanny = menu.add("Canny");
        mItemPreviewFeatures = menu.add("Find features");
        mItemPreviewHoughLines = menu.add("Hough lines");
        return true;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        mLines = new Mat(height, height, CvType.CV_32FC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
        mLines.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        final int viewMode = mViewMode;
        switch (viewMode) {
            case VIEW_MODE_GRAY:
                // input frame has gray scale format
                Imgproc.cvtColor(inputFrame.gray(), mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
            case VIEW_MODE_RGBA:
                // input frame has RBGA format
                mRgba = inputFrame.rgba();
                break;
            case VIEW_MODE_CANNY:
                // input frame has gray scale format
                mRgba = inputFrame.rgba();
                Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
                Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
            case VIEW_MODE_FEATURES:
                // input frame has RGBA format
                mRgba = inputFrame.rgba();
                mGray = inputFrame.gray();
                FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
                break;
            case VIEW_MODE_HOUGH_LINES:
                mGray = inputFrame.gray();
                Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
                //Imgproc.Sobel(inputFrame.gray(), mIntermediateMat, inputFrame.gray().depth(), 0, 1);
                Imgproc.HoughLines(mIntermediateMat, mLines, 1, Math.PI/30, 150);
                Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);

                if(mLines.rows() == 0)
                    return mRgba;

                for(int i = 0; i < mLines.cols(); i++ )
                {
                    if(mLines.get(0, i) == null) {
                        Log.e("ERR", "TU");
                    }
                    float r = (float)mLines.get(0, i)[0];
                    float t = (float)mLines.get(0, i)[1];
                    double cos_t = Math.cos(t), sin_t = Math.sin(t);
                    double x0 = r*cos_t, y0 = r*sin_t;
                    double alpha = 1000;


                    Point pt1 = new Point( Math.round(x0 + alpha*(-sin_t)), Math.round(y0 + alpha*cos_t));
                    Point pt2 = new Point( Math.round(x0 - alpha * (-sin_t)), Math.round(y0 - alpha * cos_t));

                    Core.line(mRgba, pt1, pt2, new Scalar(255, 0, 0));
                }

                //imshow( standard_name, standard_hough );

        }

        return mRgba;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mItemPreviewRGBA) {
            mViewMode = VIEW_MODE_RGBA;
        } else if (item == mItemPreviewGray) {
            mViewMode = VIEW_MODE_GRAY;
        } else if (item == mItemPreviewCanny) {
            mViewMode = VIEW_MODE_CANNY;
        } else if (item == mItemPreviewFeatures) {
            mViewMode = VIEW_MODE_FEATURES;
        } else if (item == mItemPreviewHoughLines) {
            mViewMode = VIEW_MODE_HOUGH_LINES;
        }


        return true;
    }

    public native void FindFeatures(long matAddrGr, long matAddrRgba);
}
*/
