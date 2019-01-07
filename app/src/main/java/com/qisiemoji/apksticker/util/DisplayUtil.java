package com.qisiemoji.apksticker.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

/**
 * Created by xm180319 on 2018/3/27.
 */

public class DisplayUtil {
    private static Method mGetRawW;

    private static Method mGetRawH;

    private static DisplayMetrics dm = new DisplayMetrics();

    private static int screenWidth;
    private static int screenHeight;

    /**
     * 获取竖屏时的屏幕宽度
     * @param activity
     * @return
     */
    public static int getScreenWidth(Context activity) {
        if (screenWidth > 0) {
            return screenWidth;
        }

        ScreenSize size = new ScreenSize();
        getScreenSize(activity, size);
        screenWidth = size.getScreenWidth();
        return size.getScreenWidth();
    }

    /**
     * 获取横屏时的屏幕高度
     * @param activity
     * @return
     */
    public static int getScreenHeight(Context activity) {
        if (screenHeight > 0) {
            return screenHeight;
        }

        ScreenSize size = new ScreenSize();
        getScreenSize(activity, size);
        screenHeight = size.getScreenHeight();
        return size.getScreenHeight();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void getScreenSize(Context activity, ScreenSize size) {
        int width;
        int height;
        WindowManager manager = ((WindowManager) activity
                .getSystemService(Context.WINDOW_SERVICE));
        Display display = manager.getDefaultDisplay();

        // 默认方式获取
        display.getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;

        // SDK版本13-16特殊处理
        if (Build.VERSION.SDK_INT >= 13 && Build.VERSION.SDK_INT < 17) {
            try {
                if (mGetRawW == null || mGetRawH == null) {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB_MR2) {
                        mGetRawW = Display.class.getMethod("getRealWidth");
                        mGetRawH = Display.class.getMethod("getRealHeight");
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        mGetRawW = Display.class.getMethod("getRawWidth");
                        mGetRawH = Display.class.getMethod("getRawHeight");
                    }
                }
                width = (Integer) mGetRawW.invoke(display);
                height = (Integer) mGetRawH.invoke(display);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // SDK版本高于17特殊处理
            Point outSize = new Point();
            display.getRealSize(outSize);
            width = outSize.x;
            height = outSize.y;
        }

        // 通过屏幕方向修正
//        int orient = activity.getResources().getConfiguration().orientation;
//        if (orient == Configuration.ORIENTATION_PORTRAIT) {
//            if (width > height) {
//                int tmp = width;
//                width = height;
//                height = tmp;
//            }
//        } else if (orient == Configuration.ORIENTATION_LANDSCAPE) {
//            if (width < height) {
//                int tmp = width;
//                width = height;
//                height = tmp;
//            }
//        }
        if (width > height) {
            int tmp = width;
            width = height;
            height = tmp;
        }
        size.setScreenWidth(width);
        size.setScreenHeight(height);
    }

    static class ScreenSize {
        private int screenWidth;

        private int screenHeight;

        public ScreenSize() {
        }

        public int getScreenWidth() {
            return screenWidth;
        }

        public void setScreenWidth(int screenWidth) {
            this.screenWidth = screenWidth;
        }

        public int getScreenHeight() {
            return screenHeight;
        }

        public void setScreenHeight(int screenHeight) {
            this.screenHeight = screenHeight;
        }
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(Context context, float spVal)    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());

    }
}
