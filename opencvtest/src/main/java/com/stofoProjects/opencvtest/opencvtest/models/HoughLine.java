package com.stofoProjects.opencvtest.opencvtest.models;

import org.opencv.core.Point;

/**
 * Created by Martin Stofanak on 6.5.2014.
 */
public class HoughLine {

    private double mCos;

    private Point mStartPoint;

    private Point mEndPoint;

    public HoughLine(double cos, Point startPoint, Point endPoint) {
        mCos = cos;
        mStartPoint = startPoint;
        mEndPoint = endPoint;
    }


    //******* GETTERS
    public double getCos() {
        return mCos;
    }

    public Point getStartPoint() {
        return mStartPoint;
    }

    public Point getEndPoint() {
        return mEndPoint;
    }
}
