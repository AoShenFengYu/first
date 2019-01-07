package com.qisiemoji.apksticker.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.qisi.datacollect.sdk.common.CommonUtil;

import java.util.Locale;

public class PrivacyHelper {

    private static final String TAG = "privacy";

    //欧盟成员国ISOcode
    public static String EUcountryCode[] = {"AT", "BE", "BG", "CI", "CY", "CZ", "DK", "EE", "FI", "FR", "DE", "GR",
            "HU", "IE", "IT", "LV", "LT", "LU", "MT", "NL", "PL", "PT", "RO", "SK", "SI", "ES", "SE", "GB"};

    public static boolean isEu(Context context) {
        boolean isDebug = Log.isLoggable(TAG, Log.DEBUG);
        if (isDebug) {
            for (String nationCode : EUcountryCode) {
                if (Log.isLoggable(nationCode, Log.DEBUG)) {
                    return true;
                }
            }
        }
        String nation = getNation(context);
        for (String nationCode : EUcountryCode) {
            if (nationCode.equalsIgnoreCase(nation)) {
                return true;
            }
        }
        return false;
    }

    public static String getNation(Context context) {
        String nation = "";
        if (context != null) {
            try {
                TelephonyManager tm = (TelephonyManager)((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE));
                nation =  tm.getSimCountryIso() == null ? "" : tm.getSimCountryIso().toLowerCase();
            } catch (Exception var2) {
            }
        }
        if(TextUtils.isEmpty(nation)){
            nation = Locale.getDefault().getCountry();
        }
        return nation;
    }

}
