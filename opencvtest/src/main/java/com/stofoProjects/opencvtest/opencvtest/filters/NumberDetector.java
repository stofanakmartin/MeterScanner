package com.stofoProjects.opencvtest.opencvtest.filters;

import com.stofoProjects.opencvtest.opencvtest.models.MeanSegment;
import com.stofoProjects.opencvtest.opencvtest.models.Segment;
import com.stofoProjects.opencvtest.opencvtest.models.Rectangle;
import com.stofoProjects.opencvtest.opencvtest.utils.DataUtils;
import com.stofoProjects.opencvtest.opencvtest.utils.LogUtils;
import com.stofoProjects.opencvtest.opencvtest.utils.MathUtils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin Stofanak on 7.5.2014.
 */
public class NumberDetector {

    private static final String TAG = LogUtils.makeLogTag(NumberDetector.class);
    private static final int DEFAULT_SEGMENTS_COUNT = 3;
    private static final int DEFAULT_CANNY_THRESHOLD_1 = 150;
    private static final int DEFAULT_CANNY_THRESHOLD_2 = 200;

    private Rectangle mBoundaries;
    private Mat mSummedEdges;
    private int mNumberOfSegments = DEFAULT_SEGMENTS_COUNT;
    private int mImageWidth;
    private int mRegionDistanceTollerance;
    private int mRegionWidthTollerance;
    private List<Segment> mNumberSegments;

    public NumberDetector(int imageWidth) {
        mBoundaries = null;
        mImageWidth = imageWidth;
        mRegionDistanceTollerance = 10;//imageWidth / 60;
        mRegionWidthTollerance = imageWidth / 60;
    }

    public void findNumbers(Mat grayImage, Rectangle boundaries) {
        if(boundaries == null)
            return;

        mBoundaries = boundaries;
        final Mat edgeImage = applyCanny(grayImage, boundaries);
        final Mat summedEdges = verticalProjection(edgeImage);
        final List<MeanSegment> meanSegments = MathUtils.calculateMeanSegments(
                                                    summedEdges,
                                                    mNumberOfSegments,
                                                    DataUtils.ROW_VECTOR);
        mNumberSegments = findNumberSegments(summedEdges ,meanSegments);

    }

    /**
     * Edge detection by Canny operator
     * @param grayImage gray source image
     * @param boundaries coords in which line of number should be
     * @return edge image
     */
    private Mat applyCanny(Mat grayImage, Rectangle boundaries) {
        Rect roi = new Rect(boundaries.getX1Int(), boundaries.getY1Int(), boundaries.getWidthInt(), boundaries.getHeightInt());

        Mat subGray = grayImage.submat(roi);

        Mat edges = new Mat(subGray.height(), subGray.width(), CvType.CV_8UC1);
        Imgproc.Canny(subGray, edges, DEFAULT_CANNY_THRESHOLD_1, DEFAULT_CANNY_THRESHOLD_2);

        return edges;
    }

    /**
     * Sums all columns into one row vector
     * @param edgeImage image with detected edges
     * @return one row vector with summed columns
     */
    private Mat verticalProjection(Mat edgeImage) {
        if(mSummedEdges == null) {
            mSummedEdges = new Mat(1, edgeImage.width(), CvType.CV_32SC1);
        }

        //Sum columns -> reduce matrix to one row
        Core.reduce(edgeImage, mSummedEdges, 0, Core.REDUCE_SUM, CvType.CV_32SC1);

        return mSummedEdges;
    }

    private List<Segment> findNumberSegments(Mat vectorData, List<MeanSegment> meanValues) {

        List<Segment> numberSegments = new ArrayList<Segment>();

        int tmpStart = -1;
        for(int i = 0; i < meanValues.size(); i++) {
            for (int j = meanValues.get(i).getStart(); j < meanValues.get(i).getEnd(); j++) {
                if (vectorData.get(0, j)[0] > meanValues.get(i).getMeanValue()) {
                    if(tmpStart == -1)
                        tmpStart = j;
                } else if (vectorData.get(0, j)[0] < meanValues.get(i).getMeanValue()) {
                    if(tmpStart != -1) {
                        numberSegments.add(new Segment(tmpStart, j));
                        tmpStart = -1;
                    }
                }
            }
        }

        numberSegments = joinSmallSegments(numberSegments);

        numberSegments = removeSmallSegments(numberSegments);

        return numberSegments;
    }

    /**
     * Loops through all founded regions looking for regions that are close to each other, joining
     * them into one
     * @param segments List of all segments
     * @return List of filtered segments
     */
    private List<Segment> joinSmallSegments(List<Segment> segments) {
        List<Segment> filteredSegments = new ArrayList<Segment>();

        Segment segment = null;
        for(int i = 0; i < segments.size() - 1 ; i++) {

            Segment actualSegment = segments.get(i);
            Segment nextSegment = segments.get(i + 1);

            if(nextSegment.getStart() - actualSegment.getEnd() < mRegionDistanceTollerance) {
                if(segment == null)
                    segment = new Segment(actualSegment.getStart(), nextSegment.getEnd());

                segment = new Segment(segment.getStart(), nextSegment.getEnd());

            } else {
                if(segment != null) {
                    filteredSegments.add(segment);
                    segment = null;
                } else
                    filteredSegments.add(actualSegment);
            }
        }

        if(segment != null) {
            filteredSegments.add(segment);
        }

        return filteredSegments;
    }

    private List<Segment> removeSmallSegments(List<Segment> segments) {
        List<Segment> filteredSegments = new ArrayList<Segment>();

        for(int i = 0; i < segments.size(); i++) {
            if(segments.get(i).getWidth() > mRegionWidthTollerance) {
                filteredSegments.add(segments.get(i));
            }
        }
        return  filteredSegments;
    }

    public Mat drawNumberSegments(Mat rgbaImage) {
        if(mBoundaries == null)
            return rgbaImage;

        for(int i = 0; i < mNumberSegments.size(); i++) {
            Point p1 = new Point(mNumberSegments.get(i).getStart(), mBoundaries.getY1Int());
            Point p2 = new Point(mNumberSegments.get(i).getEnd(), mBoundaries.getY2Int());
            Core.rectangle(rgbaImage, p1, p2, new Scalar(0, 255, 0));
        }

        return rgbaImage;
    }

    //************* GETTERS & SETTERS *******************
    public Mat getSummedEdges() {
        return mSummedEdges;
    }
}
