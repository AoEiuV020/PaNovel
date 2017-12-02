package cc.aoeiuv020.reader.complex.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by newbiechen on 17-5-1.
 */

public class ScreenUtils {
    public static int dpToPx(int dp) {
        DisplayMetrics metrics = getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    public static int pxToDp(int px) {
        DisplayMetrics metrics = getDisplayMetrics();
        return (int) (px / metrics.density);
    }

    public static int spToPx(int sp) {
        DisplayMetrics metrics = getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics);
    }

    public static int pxToSp(int px) {
        DisplayMetrics metrics = getDisplayMetrics();
        return (int) (px / metrics.scaledDensity);
    }

    public static DisplayMetrics getDisplayMetrics() {
        DisplayMetrics metrics = App
                .getContext()
                .getResources()
                .getDisplayMetrics();
        return metrics;
    }

    public static class App {
        private static Context context;

        public static Context getContext() {
            return context;
        }

        public static void setContext(Context context) {
            App.context = context;
        }
    }
}
