package com.stofoProjects.opencvtest.opencvtest.utils;

import com.stofoProjects.opencvtest.opencvtest.models.Rectangle;
import com.stofoProjects.opencvtest.opencvtest.models.Segment;

/**
 * Created by Martin Stofanak on 17.5.2014.
 */
public class DataUtils {

    //Types of summed vectors (from rows/columns)
    public static final int ROW_VECTOR = 1;
    public static final int COLUMN_VECTOR = 2;


    /**
     * Creates rectangle from segment object (y axis boundaries)
     * @param segment boundaries in y axis
     * @param imageWidth width of image to set x axis from 0 to width
     * @return rectangle object
     */
    public static Rectangle rectangleFromSegment(Segment segment, int imageWidth) {
        return new Rectangle(0, segment.getStart(),
                            imageWidth, segment.getEnd());
    }
}
