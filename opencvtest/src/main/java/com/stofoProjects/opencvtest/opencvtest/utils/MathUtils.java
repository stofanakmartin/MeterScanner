package com.stofoProjects.opencvtest.opencvtest.utils;

import com.stofoProjects.opencvtest.opencvtest.models.MeanSegment;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.ArrayList;
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
}
