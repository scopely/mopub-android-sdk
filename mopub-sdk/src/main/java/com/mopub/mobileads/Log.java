package com.mopub.mobileads;

/**
 * Part of the Scopely™ Platform
 * © 2013 Scopely, Inc.
 */
public class Log {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = Log.class.getCanonicalName();

    private static final String VERBOSE = "verbose";
    private static final String DEBUG = "debug";
    private static final String INFO = "info";
    private static final String WARN = "warn";
    private static final String ERROR = "error";
    private static final String WTF = "wtf";

    public static int v(String tag, String msg) {
        combineAndLog(VERBOSE, tag, msg, null);
        return android.util.Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        combineAndLog(VERBOSE, tag, msg, tr);
        return android.util.Log.v(tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        combineAndLog(DEBUG, tag, msg, null);
        return android.util.Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        combineAndLog(DEBUG, tag, msg, tr);
        return android.util.Log.d(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        combineAndLog(INFO, tag, msg, null);
        return android.util.Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        combineAndLog(INFO, tag, msg, tr);
        return android.util.Log.i(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        combineAndLog(WARN, tag, msg, null);
        return android.util.Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        combineAndLog(WARN, tag, msg, tr);
        return android.util.Log.w(tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        combineAndLog(WARN, tag, null, tr);
        return android.util.Log.w(tag, tr);
    }

    public static int e(String tag, String msg) {
        combineAndLog(ERROR, tag, msg, null);
        return android.util.Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        combineAndLog(ERROR, tag, msg, tr);
        return android.util.Log.e(tag, msg, tr);
    }

    public static int wtf(String tag, String msg) {
        combineAndLog(WTF, tag, msg, null);
        return android.util.Log.wtf(tag, msg);
    }

    public static int wtf(String tag, Throwable tr) {
        combineAndLog(WTF, tag, null, tr);
        return android.util.Log.wtf(tag, tr);
    }

    public static int wtf(String tag, String msg, Throwable tr) {
        combineAndLog(WTF, tag, msg, tr);
        return android.util.Log.wtf(tag, msg, tr);
    }

    private static void combineAndLog(String level, String tag, String msg, Throwable tr) {
        String message = level + "/" + tag + (msg != null ? msg : "") + ": " + (tr != null ? tr.getMessage() : "");
        MoPubLogManager.logMessage(message);
    }
}
