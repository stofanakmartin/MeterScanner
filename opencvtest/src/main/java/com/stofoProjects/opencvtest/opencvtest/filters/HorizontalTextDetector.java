package com.stofoProjects.opencvtest.opencvtest.filters;

import com.stofoProjects.opencvtest.opencvtest.models.MeanSegment;
import com.stofoProjects.opencvtest.opencvtest.models.Rectangle;
import com.stofoProjects.opencvtest.opencvtest.models.Segment;
import com.stofoProjects.opencvtest.opencvtest.utils.DataUtils;
import com.stofoProjects.opencvtest.opencvtest.utils.MathUtils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin Stofanak on 17.5.2014.
 */
public class HorizontalTextDetector {

    public static final int INIT_POSITION_DEFAULT = -1;
    public static final int TOLERANCE_VALUE_DENOMINATOR = 4;

    private int mImageWidth;
    private int mImageHeight;
    private Mat mSummedRows;
    private List<MeanSegment> mMeanSegments;
    private double mMaxValue;
    private Segment mNumberLineSegment;
    private Rectangle mNumberLineBoundaries;

    public HorizontalTextDetector(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;

        mSummedRows = new Mat(height, 1, CvType.CV_32SC1);
    }

    /**
     *
     * @param grayImage
     * TODO: initPosition initial position which is where the text should be text position
     *                     in "global minimum valley(graph)" if not specified (-1) function
     *                     global minimum
     */
    public void process(Mat grayImage) {
        //Mat binary = FilterCollection.cannyFilter(grayImage);
        Mat binary = FilterCollection.thresholdFilter(grayImage);

        Core.reduce(binary, mSummedRows, 1, Core.REDUCE_SUM, CvType.CV_32SC1);

        mMeanSegments = MathUtils.calculateMeanSegments(mSummedRows, 1, DataUtils.COLUMN_VECTOR);

        List<Segment> segments = findDarkRegions(mSummedRows);

        List<Segment> joinedSegments = joinSegments(mSummedRows, segments);

        mNumberLineSegment = MathUtils.findBiggestSegment(joinedSegments);

        if(mNumberLineSegment != null) {
            mNumberLineSegment = expandBiggestSegment(mSummedRows, mNumberLineSegment);
            mNumberLineBoundaries = DataUtils.rectangleFromSegmentVertical(mNumberLineSegment, grayImage.width());
        }
    }

    /**
     *
     * @param vectorData Summed rows/columns where we are looking for intervals smaller than average
     */
    private List<Segment> findDarkRegions(Mat vectorData) {
        final int vectorLength = vectorData.height();

        List<Segment> intervals = new ArrayList<Segment>();
        int actualStartSegment = -1;
        mMaxValue = -1.0;
        for(int i = 0; i < mMeanSegments.size(); i++ ){
            for(int j = 0; j < mMeanSegments.get(i).getEnd(); j++) {
                if(vectorData.get(j, 0)[0] < mMeanSegments.get(0).getMeanValue()) {

                    if(actualStartSegment == -1)
                        actualStartSegment = j;
                } else if(actualStartSegment != -1) {
                    intervals.add(new Segment(actualStartSegment, j));
                    actualStartSegment = -1;
                }

                if(mMaxValue < vectorData.get(j, 0)[0])
                    mMaxValue = vectorData.get(j, 0)[0];
            }
        }
        return intervals;
    }

    /**
     * Join segments that are close to each other and they are separated by small value difference ->
     * small hump in graph
     * @return
     */
    private List<Segment> joinSegments(Mat vectorValue, List<Segment> segments) {

        if(segments == null || segments.size() == 0)
            return null;

        List<Segment> joinedSegments = new ArrayList<Segment>();
        final double tolerance = (mMaxValue - mMeanSegments.get(0).getMeanValue()) / TOLERANCE_VALUE_DENOMINATOR;
        boolean join = false;

        if(segments.size() > 1) {
            for (int i = 0; i < segments.size() - 1; i++) {
                for (int j = segments.get(i).getEnd(); j < segments.get(i + 1).getStart(); j++) {
                    if (vectorValue.get(j, 0)[0] - mMeanSegments.get(0).getMeanValue() > tolerance) {
                        join = false;
                        break;
                    }
                }
                if (join == true)
                    joinedSegments.add(new Segment(segments.get(i).getStart(), segments.get(i + 1).getEnd()));
                else {
                    joinedSegments.add(segments.get(i));
                    //IF this is second to last and we are not joining, add last to collection
                    if (i == segments.size() - 2)
                        joinedSegments.add(segments.get(i + 1));
                }
                join = true;
            }
        } else
            return segments;

        return joinedSegments;
    }

//    private Segment findBiggestSegment(List<Segment> segments) {
//        if(segments == null || segments.size() == 0)
//            return null;
//
//        Segment biggestSegment = segments.get(0);
//        int maxWidth = biggestSegment.getWidth();
//
//        for(Segment segment : segments){
//            if(maxWidth < segment.getWidth()) {
//                maxWidth = segment.getWidth();
//                biggestSegment = segment;
//            }
//        }
//
//        return biggestSegment;
//    }

    /**
     * Expands boundaries of the biggest segment
     * @param vectorData summed rows/columns into 1D vector
     * @param biggestSegment biggest segment - number of lines
     */
    private Segment expandBiggestSegment(Mat vectorData, Segment biggestSegment) {
        if(biggestSegment == null)
            return null;

        final double tolerance = (mMaxValue - mMeanSegments.get(0).getMeanValue()) / TOLERANCE_VALUE_DENOMINATOR;
        int newStart = biggestSegment.getStart();
        final double startValue = vectorData.get(newStart, 0)[0];
        for(int i = biggestSegment.getStart(); i >= 0; i--) {
            if(vectorData.get(i, 0)[0] - startValue < tolerance)
                newStart = i;
            else break;
        }

        int newEnd = biggestSegment.getEnd();
        final double endValue = vectorData.get(newStart, 0)[0];
        for(int i = biggestSegment.getStart(); i >= vectorData.height(); i++) {
            if(vectorData.get(i, 0)[0] - endValue < tolerance)
                newEnd = i;
            else break;
        }

        return new Segment(newStart, newEnd);
    }

    /**
     * Draws number line segment as lines into rgba image
     * @param rgbaImage rgba image to which lines are drawed
     */
    public Mat drawVerticalBoundaries(Mat rgbaImage){
        if(mNumberLineSegment == null)
            return rgbaImage;

        Point pStart1 = new Point(0.0, mNumberLineSegment.getStart());
        Point pEnd1 = new Point(rgbaImage.width(), mNumberLineSegment.getStart());
        Point pStart2 = new Point(0.0, mNumberLineSegment.getEnd());
        Point pEnd2 = new Point(rgbaImage.width(), mNumberLineSegment.getEnd());

        Core.line(rgbaImage, pStart1, pEnd1, new Scalar(255, 255, 0));
        Core.line(rgbaImage, pStart2, pEnd2, new Scalar(255, 255, 0));

        return rgbaImage;
    }


    public Mat getSummedRows() {
        return mSummedRows;
    }

    public Rectangle getNumberLineBoundaries() {
        return mNumberLineBoundaries;
    }
}
