package com.stofoProjects.opencvtest.opencvtest.models;

/**
 * Created by Martin Stofanak on 11.5.2014.
 */
public class Rectangle {

    private double mX1;
    private double mY1;
    private double mX2;
    private double mY2;

    private double mWidth;
    private double mHeight;


    public Rectangle(double x1, double y1, double x2, double y2) {
        mX1 = x1;
        mY1 = y1;
        mX2 = x2;
        mY2 = y2;

        mWidth = x2 - x1;
        mHeight = y2 - y1;
    }

    public double getX1() {
        return mX1;
    }

    public int getX1Int() {
        return (int)Math.round(mX1);
    }

    public double getY1() {
        return mY1;
    }

    public int getY1Int() {
        return (int)Math.round(mY1);
    }

    public double getX2() {
        return mX2;
    }

    public int getX2Int() {
        return (int)Math.round(mX2);
    }

    public double getY2() {
        return mY2;
    }

    public int getY2Int() {
        return (int)Math.round(mY2);
    }

    public double getWidth() {
        return mWidth;
    }

    public int getWidthInt() {
        return (int)Math.round(mWidth);
    }

    public double getHeight() {
        return mHeight;
    }

    public int getHeightInt() {
        return (int)Math.round(mHeight);
    }
}
