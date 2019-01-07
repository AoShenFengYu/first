package com.qisiemoji.apksticker.util;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;


public class CommonUtil {
    public static int kikaState(Context context){
        if(!PackageUtil.isPackageInstalled(context,PackageUtil.KIKA_PACKAGENAME)){
            //not install
            return 1;
        }
        InputMethodManager mImm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        try {
            if (!IMEUtils.isThisImeEnabled(context, mImm) ||
                    !IMEUtils.isThisImeCurrent(context, mImm)) {
                //not active
                return 2;
            }
        } catch (Exception e) {
            return 2;
        }

        return 3;
    }
}
