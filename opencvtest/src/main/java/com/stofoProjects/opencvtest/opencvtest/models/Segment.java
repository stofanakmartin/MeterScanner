package com.stofoProjects.opencvtest.opencvtest.models;

/**
 * Created by Martin Stofanak on 15.5.2014.
 */
public class Segment {

    private int mStart;

    private int mEnd;

    public Segment(int start, int end) {
        mStart = start;
        mEnd = end;
    }

    public Segment(Segment segment) {
        mStart = segment.getStart();
        mEnd = segment.getEnd();
    }

    public int getStart() {
        return mStart;
    }

    public int getEnd() {
        return mEnd;
    }

    public int getWidth() {
        return mEnd - mStart;
    }
}
