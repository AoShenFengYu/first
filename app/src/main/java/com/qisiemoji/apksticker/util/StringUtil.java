package com.qisiemoji.apksticker.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class StringUtil {
    public static String getPostfix(String urlString) {
        int pos = urlString.lastIndexOf(".");
        String postfix = "";
        if(pos > 0) {
            postfix = urlString.substring(pos + 1);
        }

        return !"jpg".equalsIgnoreCase(postfix) && !"png".equalsIgnoreCase(postfix)
                && !"gif".equalsIgnoreCase(postfix) && !"jpeg".equalsIgnoreCase(postfix)
                ?"jpeg":postfix;
    }

    public static String getStickerName(Context context){
        String titleStr = "Kika Sticker Apk";
        try{
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            titleStr = appInfo.metaData.getString("STICKER_NAME");
        }catch (Exception e){

        }
        return titleStr;
    }
}