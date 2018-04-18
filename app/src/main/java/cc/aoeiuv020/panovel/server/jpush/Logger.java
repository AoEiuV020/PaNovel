package cc.aoeiuv020.panovel.server.jpush;

import android.util.Log;

import cc.aoeiuv020.panovel.BuildConfig;

/**
 * Created by efan on 2017/4/13.
 */

@SuppressWarnings("all")
public class Logger {

    //设为false关闭日志
    private static final boolean LOG_ENABLE = BuildConfig.DEBUG;

    public static void i(String tag, String msg) {
        if (LOG_ENABLE) {
            Log.i(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (LOG_ENABLE) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (LOG_ENABLE) {
            Log.d(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (LOG_ENABLE) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (LOG_ENABLE) {
            Log.e(tag, msg);
        }
    }

}
