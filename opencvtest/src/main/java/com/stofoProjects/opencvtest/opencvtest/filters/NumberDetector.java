package com.stofoProjects.opencvtest.opencvtest.filters;

import com.stofoProjects.opencvtest.opencvtest.models.MeanSegment;
import com.stofoProjects.opencvtest.opencvtest.models.NumberSegment;
import com.stofoProjects.opencvtest.opencvtest.models.Rectangle;
import com.stofoProjects.opencvtest.opencvtest.utils.LogUtils;

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
    private List<NumberSegment> mNumberSegments;

    public NumberDetector(int imageWidth) {
        mBoundaries = null;
        mImageWidth = imageWidth;
        mRegionDistanceTollerance = imageWidth / 60;
        mRegionWidthTollerance = imageWidth / 60;
    }

    public void findNumbers(Mat grayImage, Rectangle boundaries) {
        if(boundaries == null){
            LogUtils.LOGD(TAG, "Fucked up");
        }
        mBoundaries = boundaries;
        final Mat edgeImage = applyCanny(grayImage, boundaries);
        final Mat summedEdges = verticalProjection(edgeImage);
        final List<MeanSegment> meanSegments = calcMeanSegments(summedEdges);
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

    /**
     * Calculates mean for every segment in vector data
     * @param vectorData vector of data which we segments and calculates mean
     * @return list of calculated mean object
     */
    private List<MeanSegment> calcMeanSegments(Mat vectorData) {
        final int segmentLength = vectorData.width() / mNumberOfSegments;
        List<MeanSegment> averages = new ArrayList<MeanSegment>(mNumberOfSegments);
        //double[] averages = new double[mAverageSegments];

        for(int i = 0; i < mNumberOfSegments; i++) {
            int start = i * segmentLength;
            int end = ((i + 1) * segmentLength) - 1;
            Mat segmentVector = vectorData.colRange(start, end);
            averages.add(new MeanSegment(start, end, countAverage(segmentVector)));
        }
        return averages;
    }

    /**
     * Count average value from Mat one row vector
     * @param vectorData vector
     * @return average value
     */
    private double countAverage(Mat vectorData) {
        Scalar mean = Core.mean(vectorData);
        return mean.val[0];
    }

    private List<NumberSegment> findNumberSegments(Mat vectorData, List<MeanSegment> meanValues) {

        List<NumberSegment> numberSegments = new ArrayList<NumberSegment>();

        int tmpStart = -1;
        for(int i = 0; i < meanValues.size(); i++) {
            for (int j = meanValues.get(i).getStart(); j < meanValues.get(i).getEnd(); j++) {
                if (vectorData.get(0, j)[0] > meanValues.get(i).getMeanValue()) {
                    if(tmpStart == -1)
                        tmpStart = j;
                } else if (vectorData.get(0, j)[0] < meanValues.get(i).getMeanValue()) {
                    if(tmpStart != -1) {
                        numberSegments.add(new NumberSegment(tmpStart, j));
                        tmpStart = -1;
                    }
                }
            }
        }

        numberSegments = joinSmallSegments(numberSegments);

        numberSegments = removeSmallSegments(numberSegments);

        return numberSegments;
    }

    private List<NumberSegment> joinSmallSegments(List<NumberSegment> segments) {
        List<NumberSegment> filteredSegments = new ArrayList<NumberSegment>();

        NumberSegment segment = null;
        for(int i = 0; i < segments.size() - 1 ; i++) {

            NumberSegment actualSegment = segments.get(i);
            NumberSegment nextSegment = segments.get(i + 1);

            if(nextSegment.getStart() - actualSegment.getEnd() < mRegionDistanceTollerance) {
                if(segment == null)
                    segment = new NumberSegment(actualSegment.getStart(), nextSegment.getEnd());

                segment = new NumberSegment(segment.getStart(), nextSegment.getEnd());

            } else {
                if(segment != null) {
                    filteredSegments.add(segment);
                    segment = null;
                }
                filteredSegments.add(segments.get(i));
            }
        }
        return filteredSegments;
    }

    private List<NumberSegment> removeSmallSegments(List<NumberSegment> segments) {
        List<NumberSegment> filteredSegments = new ArrayList<NumberSegment>();

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
            Core.rectangle(rgbaImage, p1, p2, new Scalar(128, 0, 255));
        }

        return rgbaImage;
    }

    //************* GETTERS & SETTERS *******************
    public Mat getSummedEdges() {
        return mSummedEdges;
    }
}
