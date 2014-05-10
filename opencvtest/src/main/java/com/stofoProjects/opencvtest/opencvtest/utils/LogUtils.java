package com.stofoProjects.opencvtest.opencvtest.utils;

import android.util.Log;

import com.stofoProjects.opencvtest.opencvtest.BuildConfig;

/**
 * Created by Martin Stofanak on 30.3.2014.
 */
public class LogUtils {

    private static final String LOG_PREFIX = "scanner_";
    private static final int MAX_LOG_TAG_LENGTH = 23;
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();

    public static String makeLogTag(String str) {
        if(str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH)
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);

        return LOG_PREFIX + str;
    }

    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    public static void LOGD(String tag, String message) {
        if(BuildConfig.DEBUG || Log.isLoggable(tag, Log.DEBUG))
        Log.d(tag, message);
    }

    public static void LOGE(String tag, String message) {
        if(BuildConfig.DEBUG || Log.isLoggable(tag, Log.ERROR))
            Log.e(tag, message);
    }
}
