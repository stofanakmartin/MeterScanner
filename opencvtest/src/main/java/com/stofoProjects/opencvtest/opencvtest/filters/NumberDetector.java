package com.stofoProjects.opencvtest.opencvtest.filters;

import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
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

    public Mat findNumbers(Mat grayImage, double[] verticalBoundaries) {
        Rect roi = new Rect(0, (int)Math.round(verticalBoundaries[0]), grayImage.width(), (int)Math.round(verticalBoundaries[1] - verticalBoundaries[0]));

        Mat subGray = grayImage.submat(roi);

        Mat edges = new Mat(subGray.height(), subGray.width(), CvType.CV_8UC1);
        Imgproc.Canny(subGray, edges, 150, 200);

        mSummedEdges = new Mat(1, edges.width(), CvType.CV_32SC1);

        //Sum columns -> reduce matrix to one row
        Core.reduce(edges, mSummedEdges, 0, Core.REDUCE_SUM, CvType.CV_32SC1);

        return mSummedEdges;
    }

    public void draphEdgesGraph(GraphView graphView, ViewGroup graphContainer) {
        if(mSummedEdges.cols() == 0) {
            LogUtils.LOGE(TAG, "No data to draw, call findNumbers first");
            return;
        }
        GraphViewData[] data = new GraphViewData[mSummedEdges.cols()];

        for(int i = 0; i < mSummedEdges.cols(); i++) {
            data[i] = new GraphViewData(i, mSummedEdges.get(0, i)[0]);
        }

        graphView.addSeries(new GraphViewSeries(data));

        graphContainer.removeAllViews();
        graphContainer.addView(graphView);
    }

    public Mat drawNumberPositions(Mat rgbaImage) {

        return rgbaImage;
    }

    private void verticalProjection(Mat rgbaImage) {

    }
}
