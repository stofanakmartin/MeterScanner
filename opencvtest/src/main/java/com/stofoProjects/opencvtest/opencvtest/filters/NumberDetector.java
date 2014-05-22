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
import org.opencv.imgproc.Imgproc;

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
    private static final int NUMBERS_OFFSET = 10;
    private static final int MAX_PIXEL_INTENSITY = 255;
    private static final int TRIM_VERTICAL_ADDITION = 255;

    private Rectangle mBoundaries;
    private Mat mSummedEdges;
    private int mNumberOfSegments = DEFAULT_SEGMENTS_COUNT;
    private int mImageWidth;
    private int mRegionDistanceTolerance;
    private int mRegionWidthTolerance;
    private int mNumberWidthTolerance;
    private List<Segment> mNumberSegments;
    private List<Rect> mTrimmedNumbers;
    private int mTrimVerticalAddition;

    public NumberDetector(int imageWidth) {
        mBoundaries = null;
        mImageWidth = imageWidth;
        mRegionDistanceTolerance = imageWidth / 60;
        mRegionWidthTolerance = imageWidth / 60;
        mNumberWidthTolerance = imageWidth / 100;
        mTrimVerticalAddition = imageWidth / 160;
    }

    public void findNumbers(Mat grayImage, Rectangle boundaries) {
        if(boundaries == null)
            return;

        mBoundaries = boundaries;
        Rect roi = new Rect(boundaries.getX1Int(), boundaries.getY1Int(), boundaries.getWidthInt(), boundaries.getHeightInt());
        Mat subGray = grayImage.submat(roi);

        //Imgproc.equalizeHist(subGray, subGray);
        final Mat edgeImage = FilterCollection.cannyFilter(subGray);

        if(mSummedEdges == null)
            mSummedEdges = new Mat(1, grayImage.width(), CvType.CV_32SC1);
        mSummedEdges = MathUtils.verticalProjection(edgeImage);//verticalProjection(edgeImage);
        final List<MeanSegment> meanSegments = MathUtils.calculateMeanSegments(
                                                    mSummedEdges,
                                                    mNumberOfSegments,
                                                    DataUtils.ROW_VECTOR);
        mNumberSegments = findNumberSegments(mSummedEdges ,meanSegments);

        mTrimmedNumbers = trimNumbers(grayImage, mNumberSegments);

    }


    /**
     * Sums all columns into one row vector
     * @param edgeImage image with detected edges
     * @return one row vector with summed columns
     */
    /*private Mat verticalProjection(Mat edgeImage) {
        if(mSummedEdges == null) {
            mSummedEdges = new Mat(1, edgeImage.width(), CvType.CV_32SC1);
        }

        //Sum columns -> reduce matrix to one row
        Core.reduce(edgeImage, mSummedEdges, 0, Core.REDUCE_SUM, CvType.CV_32SC1);

        return mSummedEdges;
    }*/

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

        List<Segment> numberSegments = MathUtils.getSegmentsAboveMeanValue(vectorData, meanValues, DataUtils.ROW_VECTOR);

        numberSegments = joinSmallSegments(numberSegments);

        numberSegments = removeSmallSegments(numberSegments);
        List<Segment> numberSegmentsOrig = new ArrayList<Segment>(numberSegments);

        if(numberSegments != null && numberSegments.size() >= MINIMUM_SEGMENTS_COUNT) {
            final Segment medianSegment = MathUtils.medianSegment(numberSegments);
            numberSegments = splitLargeSegments(numberSegmentsOrig, medianSegment);
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

            if(nextSegment.getStart() - actualSegment.getEnd() < mRegionDistanceTolerance) {
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
            if(segments.get(i).getWidth() > mRegionWidthTolerance) {
                filteredSegments.add(segments.get(i));
            }
        }
        return  filteredSegments;
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
                                                            - (mRegionDistanceTolerance / 2)
                );

                Segment newSegmentRight = new Segment(
                                                segment.getStart()
                                                    + (mRegionDistanceTolerance / 2)
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

    private List<Rect> trimHorizontal(Mat grayImg, List<Rect> numberRects) {

        List<Rect> toRemove = new ArrayList<Rect>();

        for(Rect numberRect : numberRects) {
            Mat subGray = grayImg.submat(numberRect);

            subGray = FilterCollection.sobelHorizontal(subGray);

            Mat summedRows = MathUtils.horizontalProjection(subGray);

            List<MeanSegment> colMean = MathUtils.calculateMeanSegments(
                    summedRows,
                    1,
                    DataUtils.COLUMN_VECTOR
            );


            double threshold = colMean.get(0).getMeanValue() / 2;
            int startRow = 0;
            int endRow = summedRows.height();

            //From center to TOP
            for(int i = summedRows.height() / 2; i >= 0; i--) {
                if(Double.compare(summedRows.get(i, 0)[0], threshold) < 0) {
                    startRow = i;
                    break;
                }
            }

            //From center to BOTTOM
            for(int i = summedRows.height() / 2; i < summedRows.height(); i++) {

                if(Double.compare(summedRows.get(i, 0)[0], colMean.get(0).getMeanValue() / 2) < 0) {
                    endRow = i;
                    break;
                }
            }

            if(endRow - startRow >= mNumberWidthTolerance) {
                numberRect.y = numberRect.y + startRow;
                numberRect.height = endRow - startRow;
            } else
                toRemove.add(numberRect);

            LogUtils.LOGD(TAG, "newStart " + numberRect.y + " newHeight " + numberRect.height);
        }

        numberRects.removeAll(toRemove);

        return numberRects;
    }


    private List<Rect> trimVertical(Mat grayImg, List<Rect> numberRects) {

        for(Rect numberRect : numberRects) {
            Mat subGray = grayImg.submat(numberRect);

            subGray = FilterCollection.sobelVertical(subGray);

            Mat summedCols = MathUtils.verticalProjection(subGray);

            List<MeanSegment> colMean = MathUtils.calculateMeanSegments(
                                                                    summedCols,
                                                                    1,
                                                                    DataUtils.ROW_VECTOR
            );

            //double threshold = colMean.get(0).getMeanValue() / 2;
            //double threshold = MathUtils.medianFromVector(summedCols, DataUtils.COLUMN_VECTOR);
            double threshold = colMean.get(0).getMeanValue() * 0.75;
            int startColumn = 0;
            int endColumn = summedCols.width();

            //From center to LEFT
            for(int i = summedCols.width() / 2; i >= 0; i--) {
                if(Double.compare(summedCols.get(0, i)[0], threshold) < 0) {
                    startColumn = i;
                    break;
                }
            }

            //From center to RIGHT
            for(int i = summedCols.width() / 2; i < summedCols.width(); i++) {

                if(Double.compare(summedCols.get(0, i)[0], threshold) < 0) {
                    endColumn = i;
                    break;
                }
            }


            if(endColumn - startColumn >= mNumberWidthTolerance) {
                endColumn = (endColumn < summedCols.width() - mTrimVerticalAddition)
                                ? endColumn + mTrimVerticalAddition
                                : summedCols.width();

                startColumn = (startColumn > mTrimVerticalAddition)
                                ? startColumn - mTrimVerticalAddition
                                : 0;

                numberRect.width = (endColumn != 0) ? endColumn - startColumn : numberRect.width - startColumn;
                numberRect.x = numberRect.x + startColumn;
            }

            LogUtils.LOGD(TAG, "newStart " + numberRect.x + " newWidth " + numberRect.width);
            LogUtils.LOGD(TAG, "endColumn " + endColumn + " startColumn " + startColumn);
        }



        return numberRects;
    }

    private List<Rect> trimNumbers(Mat grayImage, List<Segment> numberSegments) {

        if(numberSegments == null || numberSegments.size() == 0)
            return null;

        List<Rect> numberRects = new ArrayList<Rect>(numberSegments.size());

        final int y1 = mBoundaries.getY1Int();
        final int height = mBoundaries.getHeightInt();

        for(Segment numberBoundary : numberSegments) {
            numberRects.add(new Rect(numberBoundary.getStart(), y1, numberBoundary.getWidth(), height));
        }

        numberRects = trimHorizontal(grayImage, numberRects);
        numberRects = trimVertical(grayImage, numberRects);

        return numberRects;
    }

    /************************* RENDERING METHODS ********************************

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

    public Mat drawNumbersToImage(Mat targetImage, Mat sourceImg, Point uiStartPoint) {
        if(mTrimmedNumbers == null || mTrimmedNumbers.size() == 0)
            return targetImage;

        int actualX = (int)Math.round(uiStartPoint.x);
        final int yPosition = (int)Math.round(uiStartPoint.y);

        for(Rect rect : mTrimmedNumbers) {
            LogUtils.LOGD(TAG, "Image height " + sourceImg.height() + " rect y " + rect.y + " height " + rect.height);
            Mat grayNumberImg = sourceImg.submat(rect);
            //Mat colorNumberImg = targetImage.submat(rect);


            //numberImg = FilterCollection.thresholdFilter(numberImg);
            //numberImg = FilterCollection.cannyFilter(numberImg);

//            numberImg = FilterCollection.sobelHorizontal(numberImg);
//            Imgproc.dilate(numberImg, numberImg, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));
//            Imgproc.erode(numberImg, numberImg, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));
//
//            Mat colorNumber = new Mat();
            Imgproc.cvtColor(grayNumberImg, grayNumberImg, Imgproc.COLOR_GRAY2RGBA, 4);

            grayNumberImg.copyTo(targetImage.colRange(actualX, actualX + rect.width)
                                      .rowRange(yPosition, yPosition + rect.height));
            //targetImage.setTo(numberImg);

            actualX += rect.width + NUMBERS_OFFSET;
        }

        return targetImage;
    }

    //************* GETTERS & SETTERS *******************
    public Mat getSummedEdges() {
        return mSummedEdges;
    }
}
