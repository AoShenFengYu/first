package com.qisiemoji.apksticker.util;

import android.util.Log;

import com.qisiemoji.apksticker.BuildConfig;

public class LogUtil {
    public static void i(String tag,String msg){
        if(BuildConfig.DEBUG){
            Log.i(tag,msg);
        }
    }
}
