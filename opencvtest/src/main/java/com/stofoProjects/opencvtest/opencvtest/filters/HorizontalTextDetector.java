package com.stofoProjects.opencvtest.opencvtest.filters;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Created by Martin Stofanak on 17.5.2014.
 */
public class HorizontalTextDetector {

    private int mImageWidth;
    private int mImageHeight;
    private Mat mSummedRows;

    public HorizontalTextDetector(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;

        mSummedRows = new Mat(height, 1, CvType.CV_32SC1);
    }

    public void process(Mat grayImage) {
        Mat binary = FilterCollection.cannyFilter(grayImage);

        Core.reduce(binary, mSummedRows, 1, Core.REDUCE_SUM, CvType.CV_32SC1);
    }

    public Mat getSummedRows() {
        return mSummedRows;
    }
}
