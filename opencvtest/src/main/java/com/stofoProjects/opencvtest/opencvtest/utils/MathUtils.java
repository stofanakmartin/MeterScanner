package com.stofoProjects.opencvtest.opencvtest.utils;

import com.stofoProjects.opencvtest.opencvtest.models.MeanSegment;
import com.stofoProjects.opencvtest.opencvtest.models.Segment;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Martin Stofanak on 17.5.2014.
 */
public class MathUtils {

    /**
     * Calculates mean for every segment in vector data
     * @param vectorData vector of data which we segments and calculates mean
     * @param numberOfSegments in how many parts functions splits vector data, for each segment
     *                         then calculates mean
     * @return list of calculated mean object
     */
    public static List<MeanSegment> calculateMeanSegments(Mat vectorData, int numberOfSegments,
                                                            int vectorType) {
        final int segmentLength = (vectorType == DataUtils.COLUMN_VECTOR)
                                        ? vectorData.height() / numberOfSegments
                                        : vectorData.width() / numberOfSegments;
        List<MeanSegment> averages = new ArrayList<MeanSegment>(numberOfSegments);
        //double[] averages = new double[mAverageSegments];

        for(int i = 0; i < numberOfSegments; i++) {
            int start = i * segmentLength;
            int end = ((i + 1) * segmentLength) - 1;
            Mat segmentVector = (vectorType == DataUtils.COLUMN_VECTOR)
                                    ? vectorData.rowRange(start, end)
                                    : vectorData.colRange(start, end);
            averages.add(new MeanSegment(start, end, countAverage(segmentVector)));
        }
        return averages;
    }

    /**
     * Count average value from Mat one row vector
     * @param vectorData vector
     * @return average value
     */
    private static double countAverage(Mat vectorData) {
        Scalar mean = Core.mean(vectorData);
        return mean.val[0];
    }

    /**
     * Returns segment with median width from all segments
     * @param segments segments colletion
     * @return median segment
     */
    public static Segment medianSegment(List<Segment> segments) {

        Collections.sort(segments, new Comparator<Segment>() {
            @Override
            public int compare(Segment segment1, Segment segment2) {
                return segment1.getWidth() < segment2.getWidth()
                        ? -1
                        : (segment1.getWidth() == segment1.getWidth()
                            ? 0
                            : 1);
            }
        });

        return segments.get(Math.round(segments.size() / 2));
    }

    /**
     * Sums all columns into one row vector
     * @param image image which columns we want to sum
     * @return one row vector with summed columns
     */
    public static Mat verticalProjection(Mat image) {
        Mat summedColumns = new Mat();

        //Sum columns -> reduce matrix to one row
        Core.reduce(image, summedColumns, 0, Core.REDUCE_SUM, CvType.CV_32SC1);

        return summedColumns;
    }

    public static Mat horizontalProjection(Mat image) {
        Mat summedRows = new Mat();

        //Sum rows -> reduce matrix to one column
        Core.reduce(image, summedRows, 1, Core.REDUCE_SUM, CvType.CV_32SC1);

        return summedRows;
    }

    public static List<Segment> getSegmentsAboveMeanValue(Mat summedVector, List<MeanSegment> meanValues, int typeOfVector) {
        List<Segment> numberSegments = new ArrayList<Segment>();

        int tmpStart = -1;
        for(int i = 0; i < meanValues.size(); i++) {
            for (int j = meanValues.get(i).getStart(); j < meanValues.get(i).getEnd(); j++) {

                double actualValue = (typeOfVector == DataUtils.COLUMN_VECTOR)
                                        ? summedVector.get(j, 0)[0]
                                        : summedVector.get(0, j)[0];

                if (actualValue > meanValues.get(i).getMeanValue()) {
                    if(tmpStart == -1)
                        tmpStart = j;
                } else if (actualValue < meanValues.get(i).getMeanValue()) {
                    if(tmpStart != -1) {
                        numberSegments.add(new Segment(tmpStart, j));
                        tmpStart = -1;
                    }
                }
            }
        }

        return numberSegments;
    }

    public static Segment findBiggestSegment(List<Segment> segments) {
        if(segments == null || segments.size() == 0)
            return null;

        Segment biggestSegment = segments.get(0);
        int maxWidth = biggestSegment.getWidth();

        for(Segment segment : segments){
            if(maxWidth < segment.getWidth()) {
                maxWidth = segment.getWidth();
                biggestSegment = segment;
            }
        }

        return biggestSegment;
    }

    public static double medianFromVector(Mat vectorData, int typeOfVector) {

        Mat sorted = new Mat();
        Core.sort(vectorData, sorted, Core.SORT_ASCENDING);

        final double median = (typeOfVector == DataUtils.ROW_VECTOR)
                                ? sorted.get(0, vectorData.width() / 2)[0]
                                : sorted.get(vectorData.height() / 2, 0)[0];
        return median;
    }

    public static double quarterFromVector(Mat vectorData, int typeOfVector) {

        Mat sorted = new Mat();
        Core.sort(vectorData, sorted, Core.SORT_ASCENDING);

        final double median = (typeOfVector == DataUtils.ROW_VECTOR)
                ? sorted.get(0, vectorData.width() / 4)[0]
                : sorted.get(vectorData.height() / 4, 0)[0];
        return median;
    }
}
