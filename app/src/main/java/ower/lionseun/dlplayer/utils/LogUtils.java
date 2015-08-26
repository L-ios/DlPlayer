package ower.lionseun.dlplayer.utils;

import android.util.Log;

/**
 * Created by lingyang on 8/22/15.
 */
public final class LogUtils {

    /**
     * Priority constant for the println method; use Log.v.
     */
    public static final int VERBOSE = 2;

    /**
     * Priority constant for the println method; use Log.d.
     */
    public static final int DEBUG = 3;

    /**
     * Priority constant for the println method; use Log.i.
     */
    public static final int INFO = 4;

    /**
     * Priority constant for the println method; use Log.w.
     */
    public static final int WARN = 5;

    /**
     * Priority constant for the println method; use Log.e.
     */
    public static final int ERROR = 6;

    /**
     * Priority constant for the println method.
     */
    public static final int ASSERT = 7;

    /**
     * log level, can use to shutdown some log.
     */
    public static final int LEVEL = VERBOSE - 1;

    public static final void v(String tag, String msg) {
        if (VERBOSE > LEVEL) {
            Log.v(tag, msg);
        }
    }
    public static final void d(String tag, String msg) {
        if (DEBUG > LEVEL) {
            Log.d(tag, msg);
        }
    }
    public static final void i(String tag, String msg) {
        if (INFO > LEVEL) {
            Log.i(tag, msg);
        }
    }
    public static final void w(String tag, String msg) {
        if (WARN > LEVEL) {
            Log.w(tag, msg);
        }
    }
    public static final void e(String tag, String msg) {
        if (ERROR > LEVEL) {
            Log.e(tag, msg);
        }
    }

    // TODO: not handle the return of log.*,and some method of class Log.
}
