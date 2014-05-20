package com.stofoProjects.opencvtest.opencvtest.filters;

import com.stofoProjects.opencvtest.opencvtest.models.MeanSegment;
import com.stofoProjects.opencvtest.opencvtest.models.Rectangle;
import com.stofoProjects.opencvtest.opencvtest.models.Segment;
import com.stofoProjects.opencvtest.opencvtest.utils.DataUtils;
import com.stofoProjects.opencvtest.opencvtest.utils.LogUtils;
import com.stofoProjects.opencvtest.opencvtest.utils.MathUtils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin Stofanak on 7.5.2014.
 */
public class NumberDetector {

    private static final String TAG = LogUtils.makeLogTag(NumberDetector.class);
    private static final int DEFAULT_SEGMENTS_COUNT = 5;
    private static final int DEFAULT_CANNY_THRESHOLD_1 = 150;
    private static final int DEFAULT_CANNY_THRESHOLD_2 = 200;
    private static final int MINIMUM_SEGMENTS_COUNT = 5;

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
        mRegionDistanceTollerance = imageWidth / 60;
        mRegionWidthTollerance = imageWidth / 50;
    }

    public void findNumbers(Mat grayImage, Rectangle boundaries) {
        if(boundaries == null)
            return;

        mBoundaries = boundaries;
        Rect roi = new Rect(boundaries.getX1Int(), boundaries.getY1Int(), boundaries.getWidthInt(), boundaries.getHeightInt());
        Mat subGray = grayImage.submat(roi);

        final Mat edgeImage = FilterCollection.cannyFilter(grayImage);
        final Mat summedEdges = verticalProjection(edgeImage);
        final List<MeanSegment> meanSegments = MathUtils.calculateMeanSegments(
                                                    summedEdges,
                                                    mNumberOfSegments,
                                                    DataUtils.ROW_VECTOR);
        mNumberSegments = findNumberSegments(summedEdges ,meanSegments);

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
     * Method which from summed columns vector of edge image and mean values(number of segments)
     * from these data extract the numbers position. If the value in vector is greater than mean
     * value in actual segment than we consider it as a number
     *
     * After that basic algorithm, method process extracted segments:
     * - join neighbour segments
     * - remove small segments
     * - split large segments into two smaller
     *
     * @param vectorData Summed columns of edge binary image
     * @param meanValues Mean values of vectorData, can have more values(for each segment of
     *                   vectorData one)
     * @return List of filtered segments - regions where number should be
     */
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

        if(numberSegments != null && numberSegments.size() >= MINIMUM_SEGMENTS_COUNT) {
            final Segment medianSegment = MathUtils.medianSegment(numberSegments);
            numberSegments = splitLargeSegments(numberSegments, medianSegment);
        }
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
                } else {
                    filteredSegments.add(actualSegment);
                }
            }
        }

        if(segment != null) {
            filteredSegments.add(segment);
        }

        return filteredSegments;
    }

    /**
     * Removes segments which have very small width
     * @param segments List of segments
     * @return Filtered list of segments
     */
    private List<Segment> removeSmallSegments(List<Segment> segments) {
        List<Segment> filteredSegments = new ArrayList<Segment>();

        for(int i = 0; i < segments.size(); i++) {
            if(segments.get(i).getWidth() > mRegionWidthTollerance) {
                filteredSegments.add(segments.get(i));
            }
        }
        return  filteredSegments;
    }

    /**
     * Draws founded segments. Rectangles which should bounds the number in image
     * @param rgbaImage Image to which we are drawing
     * @return Image with drawed rectangles
     */
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

    /**
     * Splits large segments of image, which potentionaly holds two numbers
     * Method is looking on widths of founded segments.
     * If segment in List is wider that specified multiplicator * width of etalon segment, than
     * the segment is split in two, with the small gap between them
     *
     * @param segments List of segments to be filtered
     * @param etalonSegment Etalon segment according to which we do filtration
     * @return filtered segments with split big segments
     */
    private List<Segment> splitLargeSegments(List<Segment> segments, Segment etalonSegment) {

        final double multiplier = 2;
        List<Segment> filteredSegments = new ArrayList<Segment>();

        for(Segment segment : segments) {
            if(segment.getWidth() >= etalonSegment.getWidth() * multiplier) {
                //Split segment and put tollerance space between them
                Segment newSegmentLeft = new Segment(
                                                    segment.getStart(),
                                                    segment.getStart()
                                                            + (segment.getWidth() / 2)
                                                            - (mRegionDistanceTollerance / 2)
                );

                Segment newSegmentRight = new Segment(
                                                segment.getStart()
                                                    + (mRegionDistanceTollerance / 2)
                                                    + (segment.getWidth() / 2),
                                                segment.getEnd()
                );
                filteredSegments.add(newSegmentLeft);
                filteredSegments.add(newSegmentRight);

            } else
                filteredSegments.add(segment);
        }

        return filteredSegments;
    }

    //************* GETTERS & SETTERS *******************
    public Mat getSummedEdges() {
        return mSummedEdges;
    }
}
