package com.qisiemoji.apksticker.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import retrofit2.http.Url;

public class GooglePlay {
    public static final String TAG = "GooglePlay";

    private static final String URL_HTTP = "https://play.google.com";
    private static final String URL_MARKET = "market://details?id=";

    public static final String URL_MARKET_REF_DIRECT_DOWNLOAD = "DirectDownload";
    public static final String URL_MARKET_REF_DETAIL_DOWNLOAD = "DetailDownload";

    public static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=";

    public static Intent newPlayStoreIntent(String value) {
        return newPlayStoreIntentWithRef(value, null);
    }

    public static Intent newPlayStoreIntentWithRef(String value, String ref) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        if (!value.startsWith(URL_HTTP) && !value.startsWith(URL_MARKET)) {
            if (value.matches("[a-z][a-z0-9A-Z_]*(\\.[a-z0-9A-Z_]+)+")) {
                value = URL_MARKET + value + "&referrer=utm_source%3D" + ref;
            } else {
                return null;
            }

        } else {
            if (!TextUtils.isEmpty(ref)) {
                value = value + "&referrer=utm_source%3D" + ref;
            }
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(value));
        intent.setPackage("com.android.vending");//直接google play打开
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static void gotoGooglePlay(Context context, String packageName) {
        gotoGooglePlayWithRef(context, packageName, null);
    }


    public static void gotoGooglePlayWithRef(Context context, String packageName, String ref) {
        if (context != null && !startGooglePlayOrByBrowserWithRef(context, packageName, ref)) {
            Toast.makeText(context, "No app market installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean startGooglePlayOrByBrowserWithRef(Context ctx, String url, String ref) {
        if (ctx == null) {
            return false;
        }
        Intent intent = newPlayStoreIntentWithRef(url,ref);
        if (intent == null) {
            return false;
        }
        try {
            ctx.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!url.startsWith(URL_HTTP)) {
            url = PLAY_STORE_URL + url;
        }
        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            ctx.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean startGooglePlayOrByBrowser(Context ctx, String url) {
        return startGooglePlayOrByBrowserWithRef(ctx, url, null);
    }

    public static boolean openBrowser(Context ctx, String url) {
        try {
            Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(it);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String makePlayStoreUrl(String refer, String applicationId) {
        StringBuilder builder = new StringBuilder(PLAY_STORE_URL);
        builder.append(applicationId);
        if (!TextUtils.isEmpty(refer)) {
            builder.append("&referrer=")
                    .append(refer);
        }
        return builder.toString();
    }
}
