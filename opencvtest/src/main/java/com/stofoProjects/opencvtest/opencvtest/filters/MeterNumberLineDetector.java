package com.stofoProjects.opencvtest.opencvtest.filters;

import com.stofoProjects.opencvtest.opencvtest.models.HoughLine;
import com.stofoProjects.opencvtest.opencvtest.models.Rectangle;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * Created by Martin Stofanak on 6.5.2014.
 */
public class MeterNumberLineDetector {

    private static final double BOUNDARY_TOLLERANCE = 0.1;

    private int mScreenWidth;
    private int mScreenHeight;

    private Mat mBinnaryImage;
    private Mat mLines;
    private List<HoughLine> mLinesXY;

    private HoughLineDetector mHoughLineDetector;
    private HorizontalTextDetector mHorizontalTextDetector;

    public MeterNumberLineDetector(int screenW, int screenH) {
        mScreenWidth = screenW;
        mScreenHeight = screenH;

        mHoughLineDetector = new HoughLineDetector(screenW, screenH);
        mHorizontalTextDetector = new HorizontalTextDetector(screenW, screenH);
    }

    /**
     * Detection by horizontal projection of binary image
     * Peak position -> text position
     */
    public void detectLineOfNumbers(Mat grayImage) {
        mHorizontalTextDetector.process(grayImage);
    }

    public Rectangle detectLineOfNumbers(List<HoughLine> detectedLines, List<MatOfPoint> redBlob, Rectangle maxBounds) {

        if(detectedLines == null || detectedLines.size() == 0 || redBlob == null
                || redBlob.size() == 0 || redBlob.get(0) == null)
            return maxBounds;

        double topBoundary = maxBounds.getY1();
        double bottomBoundary = maxBounds.getY2();

        final Rect boundBlobRect = Imgproc.boundingRect(redBlob.get(0));

        for(HoughLine line : detectedLines) {

            //We want to loop only through horizontal lines
            if(line.getCos() < 0.2) {
                double tmpBound = line.getStartPoint().y;

                if(tmpBound < boundBlobRect.y + (boundBlobRect.height * BOUNDARY_TOLLERANCE) && tmpBound > topBoundary && tmpBound < bottomBoundary)
                    topBoundary = tmpBound;
                else if(tmpBound > boundBlobRect.y + boundBlobRect.height - ((boundBlobRect.height * BOUNDARY_TOLLERANCE)) && tmpBound < bottomBoundary && tmpBound > topBoundary)
                    bottomBoundary = tmpBound;
            }
        }

        return new Rectangle(maxBounds.getX1(), topBoundary, maxBounds.getX2(), bottomBoundary);
    }

    public static Mat drawVerticalBoundaries(Mat rgbaImage, Rectangle boundaries) {
        if(rgbaImage == null)
            return null;
        if(boundaries == null)
            return rgbaImage;

        Point p1Top = new Point(0.0, boundaries.getY1());
        Point p2Top = new Point(rgbaImage.width(), boundaries.getY1());
        Point p1Bottom = new Point(0.0, boundaries.getY2());
        Point p2Bottom = new Point(rgbaImage.width(), boundaries.getY2());

        Core.line(rgbaImage, p1Top, p2Top, new Scalar(0,255,0));
        Core.line(rgbaImage, p1Bottom, p2Bottom, new Scalar(0,255,0));

        return rgbaImage;
    }

    public static Mat getNumbersImage(Mat rgbaImage, double[] verticalBounds) {
        Rect rectROI = new Rect(0, (int)Math.round(verticalBounds[0]), rgbaImage.width(), (int)Math.round(verticalBounds[1] - verticalBounds[0]));
        Mat subRgba = rgbaImage.submat(rectROI);

        return subRgba;
    }



    public HorizontalTextDetector horizontalTextDetector() {
        return mHorizontalTextDetector;
    }
}
