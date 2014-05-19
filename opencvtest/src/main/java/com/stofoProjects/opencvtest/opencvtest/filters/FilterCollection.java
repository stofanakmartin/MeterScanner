package com.stofoProjects.opencvtest.opencvtest.filters;

import com.stofoProjects.opencvtest.opencvtest.utils.LogUtils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Martin Stofanak on 17.5.2014.
 */
public class FilterCollection {

    private static final String TAG = LogUtils.makeLogTag(FilterCollection.class);

    private static final int SOBEL_THRESHOLD_VALUE = 100;
    private static final int SOBEL_THRESHOLD_MAXVALUE = 255;
    private static final int CANNY_THRESHOLD_1 = 80;
    private static final int CANNY_THRESHOLD_2 = 100;

    private static final int DIRECTION_HORIZONTAL = 1;
    private static final int DIRECTION_VERTICAL = 2;

    //private int mImageWidth;
    //private int mImageHeight;

    //Sobel
    //private Mat mSobelGrad;

//    public FilterCollection(int width, int height) {
//        mImageWidth = width;
//        mImageHeight = height;
//    }

    public static Mat sobelVertical(Mat gray) {
        return sobelFilter(gray, DIRECTION_VERTICAL);
    }
    public static Mat sobelHorizontal(Mat gray) {
        return sobelFilter(gray, DIRECTION_HORIZONTAL);
    }
    public static Mat sobelBoth(Mat gray) {
        Mat sobelX = sobelVertical(gray);
        Mat sobelY = sobelHorizontal(gray);

        Core.addWeighted(sobelX, 1.0, sobelY, 1.0, 0, sobelX);
        return sobelX;
    }


    private static Mat sobelFilter(Mat grayImage, int direction) {
        Mat sobelGrad = new Mat(grayImage.width(), grayImage.height(), CvType.CV_8UC1);

        if(direction == DIRECTION_HORIZONTAL)
            Imgproc.Sobel(grayImage, sobelGrad, grayImage.depth(), 1, 0, 3, 1, 0, Imgproc.BORDER_DEFAULT);
        else if(direction == DIRECTION_VERTICAL)
            Imgproc.Sobel(grayImage, sobelGrad, grayImage.depth(), 0, 1, 3, 1, 0, Imgproc.BORDER_DEFAULT);
        else {
            LogUtils.LOGE(TAG, "Sobel, direction not specified");
            return null;
        }

        Imgproc.threshold(sobelGrad, sobelGrad, SOBEL_THRESHOLD_VALUE,
                SOBEL_THRESHOLD_MAXVALUE, Imgproc.THRESH_BINARY);
        Core.convertScaleAbs(sobelGrad, sobelGrad);

        return sobelGrad;
    }

    public static Mat cannyFilter(Mat grayImage) {
        Mat binary = new Mat(grayImage.height(), grayImage.width(), CvType.CV_8UC1);
        Imgproc.Canny(grayImage, binary, CANNY_THRESHOLD_1, CANNY_THRESHOLD_2);
        return binary;
    }

    public static Mat thresholdFilter(Mat grayImage) {
        Mat binary = new Mat(grayImage.height(), grayImage.width(), CvType.CV_8UC1);
        Imgproc.threshold(grayImage, binary, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        return binary;
    }

    public static Mat dilate(Mat image) {
        Mat output = new Mat();
        Imgproc.dilate(image, output, Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                        new Size(3, 3)));

        return output;
    }

    public static Mat erode(Mat image) {
        Mat output = new Mat();
        Imgproc.erode(image, output, Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                new Size(1, 1)));

        return output;
    }
}
