package com.stofoProjects.opencvtest.opencvtest.filters;

import com.stofoProjects.opencvtest.opencvtest.models.HoughLine;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin Stofanak on 6.5.2014.
 */
public class HoughLineDetector {

    private static final int CANNY_THRESHOLD_1 = 80;
    private static final int CANNY_THRESHOLD_2 = 100;
    private static final double HOUGH_LINES_RHO = 1;
    private static final double HOUGH_LINES_THETA = Math.PI/30;
    //TODO: Threshold depends on resolution of the screen
    private int mHoughLinesThreshold;

    private int mScreenWidth;
    private int mScreenHeight;

    private Mat mBinnaryImage;
    private Mat mLines;
    private List<HoughLine> mLinesXY;

    public HoughLineDetector(int width, int height){
        mLines = new Mat();

        mScreenWidth = width;
        mScreenHeight = height;

        mHoughLinesThreshold = width / 4;

        mLinesXY = new ArrayList<HoughLine>();
    }

    /**
     * Finds lines in image
     * @param grayImage Mat - gray image
     * @return Mat - lines
     */
    public void findLines(Mat grayImage) {
        mLinesXY.clear();

        if(mBinnaryImage == null)
            mBinnaryImage = new Mat(grayImage.height(), grayImage.width(), CvType.CV_8UC4);

        Imgproc.Canny(grayImage, mBinnaryImage, CANNY_THRESHOLD_1, CANNY_THRESHOLD_2);
        //Imgproc.Sobel(inputFrame.gray(), mIntermediateMat, inputFrame.gray().depth(), 0, 1);
        Imgproc.HoughLines(mBinnaryImage, mLines, HOUGH_LINES_RHO, HOUGH_LINES_THETA, mHoughLinesThreshold);
    }

    /**
     * Draw founded lines into RGBA image
     * @param rgbaImage RGBA image to which we are drawing
     * @return RGBA image with drawed lines
     */
    public Mat drawLines(Mat rgbaImage) {

        if(mLines.rows() == 0)
            return rgbaImage;

        for(int i = 0; i < mLines.cols(); i++ )
        {
            float r = (float)mLines.get(0, i)[0];
            float t = (float)mLines.get(0, i)[1];

            HoughLine line = convertLineToXYCoords(r, t);

            Core.line(rgbaImage, line.getStartPoint(), line.getEndPoint(), new Scalar(255, 0, 0));
        }

        return rgbaImage;
    }

    private HoughLine convertLineToXYCoords(float r, float t) {
        double cos_t = Math.cos(t), sin_t = Math.sin(t);
        double x0 = r*cos_t, y0 = r*sin_t;
        double alpha = 1280;

        Point p1 = new Point(Math.round(x0 + alpha * (-sin_t)), Math.round(y0 + alpha * cos_t));
        Point p2 = new Point(Math.round(x0 - alpha * (-sin_t)), Math.round(y0 - alpha * cos_t));

        HoughLine line = new HoughLine(cos_t, p1, p2);

        //When we are drawing we also converting the points to XY
        mLinesXY.add(line);

        return line;
    }

    private void convertLinesToXYCoords() {
        if(mLines == null || mLines.cols() == 0)
            return;

        for(int i = 0; i < mLines.cols(); i++ )
        {
            float r = (float)mLines.get(0, i)[0];
            float t = (float)mLines.get(0, i)[1];

            HoughLine line = convertLineToXYCoords(r,t);

            mLinesXY.add(line);
        }
    }

    public List<HoughLine> getLinesXY() {
        if(hasFoundLines() == false)
            return null;

        // Means that we already converted them
        if(mLinesXY.size() == mLines.cols())
            return mLinesXY;

        convertLinesToXYCoords();

        return mLinesXY;
    }

    private boolean hasFoundLines() {
        return mLines != null && mLines.cols() != 0 && mLines.rows() != 0;
    }
}
