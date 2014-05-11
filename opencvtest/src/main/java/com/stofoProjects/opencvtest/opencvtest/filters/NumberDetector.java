package com.stofoProjects.opencvtest.opencvtest.filters;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.stofoProjects.opencvtest.opencvtest.models.Rectangle;
import com.stofoProjects.opencvtest.opencvtest.utils.LogUtils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Martin Stofanak on 7.5.2014.
 */
public class NumberDetector {

    private static final String TAG = LogUtils.makeLogTag(NumberDetector.class);

    private Mat mSummedEdges;

    public NumberDetector() {

    }

    public Mat findNumbers(Mat grayImage, Rectangle boundaries) {
        if(boundaries == null){
            LogUtils.LOGD(TAG, "Fucked up");
        }
        Rect roi = new Rect(boundaries.getX1Int(), boundaries.getY1Int(), boundaries.getWidthInt(), boundaries.getHeightInt());

        Mat subGray = grayImage.submat(roi);

        Mat edges = new Mat(subGray.height(), subGray.width(), CvType.CV_8UC1);
        Imgproc.Canny(subGray, edges, 150, 200);

        mSummedEdges = new Mat(1, edges.width(), CvType.CV_32SC1);

        //Sum columns -> reduce matrix to one row
        Core.reduce(edges, mSummedEdges, 0, Core.REDUCE_SUM, CvType.CV_32SC1);

        return mSummedEdges;
    }

    public GraphView updateEdgesGraphView(GraphView graphView) {

        if(mSummedEdges == null || mSummedEdges.cols() == 0) {
            LogUtils.LOGE(TAG, "No data to draw, call findNumbers first");
            return null;
        }
        GraphViewData[] data = new GraphViewData[mSummedEdges.cols()];

        for(int i = 0; i < mSummedEdges.cols(); i++) {
            data[i] = new GraphViewData(i, mSummedEdges.get(0, i)[0]);
        }

        graphView.removeAllSeries();
        graphView.addSeries(new GraphViewSeries(data));



        return graphView;
    }

    public Mat drawNumberPositions(Mat rgbaImage) {

        return rgbaImage;
    }

    private void verticalProjection(Mat rgbaImage) {

    }
}
