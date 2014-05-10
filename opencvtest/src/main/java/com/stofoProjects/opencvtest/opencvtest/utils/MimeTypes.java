package com.stofoProjects.opencvtest.opencvtest.utils;

/**
 * Helper static class with mime types, used when starting camera activity
 * Created by Martin Stofanak on 8.5.2014.
 */
public class MimeTypes {

    public static final String CAMERA = "camera/";
    public static final String CAMERA_FILTER_PREVIEW = CAMERA + "filter_preview";
    public static final String CAMERA_RECOGNIZER = CAMERA + "recognizer";

    public static String getType(String mimeType) {
        if (mimeType != null && mimeType.contains("/")) {
            return mimeType.split("/")[1];
        }
        return "";
    }
}
