package com.stofoProjects.opencvtest.opencvtest.models;

/**
 * Created by Martin Stofanak on 12.5.2014.
 */
public class MeanSegment {

    private int mStart;

    private int mEnd;

    private double mMeanValue;

    public MeanSegment(int start, int end, double value) {
        mStart = start;
        mEnd = end;
        mMeanValue = value;
    }

    public int getStart() {
        return mStart;
    }

    public int getEnd() {
        return mEnd;
    }

    public double getMeanValue() {
        return mMeanValue;
    }
}
