package com.stofoProjects.opencvtest.opencvtest.filters;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: rebuild this class to extend from ColorBlobDetection
 * TODO: than this class is going have sense
 * Created by Martin Stofanak on 5.5.2014.
 */
public class RedBlobDetector {

    private static final Scalar CONTOUR_COLOR = new Scalar(255,0,0,255);

    private static final Scalar bottomRedFirst = new Scalar(0,120,120, 0);
    private static final Scalar topRedFirst = new Scalar(20,255,255, 255);
    private static final Scalar bottomRedSecond = new Scalar(245,120,120, 0);
    private static final Scalar topRedSecond = new Scalar(255,255,255, 255);

    private ColorBlobDetector mColorDetector;
    private List<MatOfPoint> mBlobs;


    public RedBlobDetector() {
        mColorDetector = new ColorBlobDetector();
        mColorDetector.addHsvColor(bottomRedFirst, topRedFirst);
        mColorDetector.addHsvColor(bottomRedSecond, topRedSecond);
        mBlobs = new ArrayList<MatOfPoint>();
    }

    public void findBiggestBlob(Mat rgbaImage) {
        mBlobs = mColorDetector.findBiggestBlob(rgbaImage);
    }

    public void findAllBlobs(Mat rgbaImage) {
        mBlobs = mColorDetector.findAllBlobs(rgbaImage);
    }

    public void drawBlobs(Mat rgbImage) {
        if(hasFoundedBlobs())
            Imgproc.drawContours(rgbImage, mBlobs, -1, CONTOUR_COLOR);
    }

    public List<MatOfPoint> getBlobs() {
        return mBlobs;
    }

    public boolean hasFoundedBlobs() {
        return mBlobs != null && mBlobs.size() != 0;
    }
}
