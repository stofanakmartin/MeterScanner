package com.stofoProjects.opencvtest.opencvtest.filters;

import com.stofoProjects.opencvtest.opencvtest.models.HoughLine;

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

    public static double[] detectLineOfNumbers(List<HoughLine> detectedLines, List<MatOfPoint> redBlob, int[] maxBounds) {

        if(detectedLines == null || detectedLines.size() == 0 || redBlob == null
                || redBlob.size() == 0 || redBlob.get(0) == null)
            return null;

        double topBoundary = (double)maxBounds[0];
        double bottomBoundary = topBoundary + (double)maxBounds[1];

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

        return new double[]{ topBoundary, bottomBoundary };
    }

    public static Mat drawVerticalBoundaries(Mat rgbaImage, double[] verticalBounds) {
        if(rgbaImage == null)
            return null;
        if(verticalBounds == null || verticalBounds.length == 0)
            return rgbaImage;

        Point p1Top = new Point(0.0, verticalBounds[0]);
        Point p2Top = new Point(rgbaImage.width(), verticalBounds[0]);
        Point p1Bottom = new Point(0.0, verticalBounds[1]);
        Point p2Bottom = new Point(rgbaImage.width(), verticalBounds[1]);

        Core.line(rgbaImage, p1Top, p2Top, new Scalar(0,255,0));
        Core.line(rgbaImage, p1Bottom, p2Bottom, new Scalar(0,255,0));

        return rgbaImage;
    }

    public static Mat getNumbersImage(Mat rgbaImage, double[] verticalBounds) {
        Rect rectROI = new Rect(0, (int)Math.round(verticalBounds[0]), rgbaImage.width(), (int)Math.round(verticalBounds[1] - verticalBounds[0]));
        Mat subRgba = rgbaImage.submat(rectROI);

        return subRgba;
    }
}
