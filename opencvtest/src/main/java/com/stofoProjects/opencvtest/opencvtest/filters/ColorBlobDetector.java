package com.stofoProjects.opencvtest.opencvtest.filters;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin Stofanak on 5.5.2014.
 */
public class ColorBlobDetector {

    private static final double MIN_COUNTOUR_AREA = 0.1;
    private List<Scalar> mUpperBounds;
    private List<Scalar> mLowerBounds;
    //private Mat mHsvMat;

    public ColorBlobDetector() {
        mUpperBounds = new ArrayList<Scalar>();
        mLowerBounds = new ArrayList<Scalar>();
    }

    public ColorBlobDetector(Scalar minHsvColor, Scalar maxHsvColor) {
        mUpperBounds = new ArrayList<Scalar>();
        mLowerBounds = new ArrayList<Scalar>();

        addHsvColor(minHsvColor, maxHsvColor);

//        mUpperBound = minHsvColor;
//        mLowerBound = maxHsvColor;
    }

    /**
     * Add the color bounds in HSV color space, which we want to find
     * in image
     * @param minHsvColor Scalar - lower HSV color boundary
     * @param maxHsvColor Scalar - max HSV color boundary
     */
    public void addHsvColor(Scalar minHsvColor, Scalar maxHsvColor) {
        mLowerBounds.add(minHsvColor);
        mUpperBounds.add(maxHsvColor);

//        mUpperBound = minHsvColor;
//        mLowerBound = maxHsvColor;
    }

    /**
     * Find the biggest color blob in image
     */
    public List<MatOfPoint> findBiggestBlob(Mat rgbaImage) {
        List<MatOfPoint> contours = process(rgbaImage);
        MatOfPoint biggestContour = getBiggestContour(contours);
        contours.clear();

        if(biggestContour != null)
            contours.add(biggestContour);

        return contours;
    }

    /**
     * Finds all relevant color blobs in image
     */
    public List<MatOfPoint> findAllBlobs(Mat rgbaImage) {
        List<MatOfPoint> contours = process(rgbaImage);
        contours = filterContours(contours);
        return contours;
    }

    /**
     * Process rgba image and finds all color blobs in image
     * @param rgbaImage
     * @return List of all contours in image
     */
    private List<MatOfPoint> process(Mat rgbaImage) {

        //Downsample the image twice for faster processing
        Mat downsizedRgba = new Mat();
        Imgproc.pyrDown(rgbaImage, downsizedRgba);
        Imgproc.pyrDown(downsizedRgba, downsizedRgba);

        //Convert image to HSV color space
        Mat hsvMat = new Mat();
        Imgproc.cvtColor(downsizedRgba, hsvMat, Imgproc.COLOR_RGB2HSV_FULL);


        Mat mask = new Mat();
        Mat tmpMask = new Mat();
        final Mat dilatedMask = new Mat();

        for(int i = 0; i < mLowerBounds.size(); i++) {
            if(i != 0) {
                Core.inRange(hsvMat, mLowerBounds.get(i), mUpperBounds.get(i), tmpMask);
                Core.bitwise_or(mask, tmpMask, mask);
                tmpMask = null;
            } else {
                //BInarize image 1 means that color of that pixel is in specified interval
                Core.inRange(hsvMat, mLowerBounds.get(i), mUpperBounds.get(i), mask);
            }
        }

        //Join image components, new Mat means 3x3 element is used
        Imgproc.dilate(mask, dilatedMask, new Mat());

        final List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        final Mat hierarchy = new Mat();
        Imgproc.findContours(dilatedMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        return contours;
    }

    /**
     * Loop through all contours and return the biggest
     * @param contours List of contours
     * @return biggest contour
     */
    private MatOfPoint getBiggestContour(List<MatOfPoint> contours) {
        if(contours == null || contours.size() == 0)
            return null;

        //Find max contour area
        double maxArea = 0.0;
        MatOfPoint maxContour = null;
        for(MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if(area > maxArea) {
                maxArea = area;
                maxContour = contour;
            }
        }

        if(maxContour == null)
            return null;

        return scaleContourToOriginalSize(maxContour);
    }

    /**
     * Gets the size of the biggest contour
     * @param contours List<MatOfPoint> - list of all contours
     * @return double - size of biggest contour
     */
    private double getBiggestContourSize(List<MatOfPoint> contours) {
        //Find max contour area
        double maxArea = 0.0;
        for(MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if(area > maxArea)
                maxArea = area;
        }

        return maxArea;
    }

    /**
     * Filter contours and scales them back to fit the original image
     * @param contours List<MatOfPoint> - contours to filter
     * @return List<MatOfPoint> - filtered contours scaled back to original image size
     */
    private List<MatOfPoint> filterContours(List<MatOfPoint> contours) {
        double maxArea = getBiggestContourSize(contours);
        List<MatOfPoint> filteredContours = new ArrayList<MatOfPoint>();

        for(MatOfPoint contour : contours) {
            if(Imgproc.contourArea(contour) > MIN_COUNTOUR_AREA * maxArea) {
                contour = scaleContourToOriginalSize(contour);
                filteredContours.add(contour);
            }
        }

        return filteredContours;
    }

    /**
     * Multiplies contour back to size to fit original image size
     * @param contour MatOfPoint - contour to scale
     * @return MatOfPoint - contour compatible with original image
     */
    private MatOfPoint scaleContourToOriginalSize(MatOfPoint contour) {
        MatOfPoint result = new MatOfPoint();
        Core.multiply(contour, new Scalar(4,4), result);
        return result;
    }
}
