package com.qisiemoji.apksticker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.qisiemoji.apksticker.StickerApplication;

public class ApkMonitorReceiver extends BroadcastReceiver {
    public ApkMonitorReceiver(){

    }
    public static void register(Context application){
        IntentFilter packageIntentFilter = new IntentFilter();
        packageIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageIntentFilter.addAction(Intent.ACTION_INSTALL_PACKAGE);
        packageIntentFilter.addAction("com.android.vending.INSTALL_REFERRER");
        packageIntentFilter.addDataScheme("package");
        application.registerReceiver(new ApkMonitorReceiver(),packageIntentFilter);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null || intent.getData() == null) {
            return;
        }
        Context applicationContext = context.getApplicationContext();
        StickerApplication.appContext = applicationContext;
        if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
            String packageName = intent.getData().getSchemeSpecificPart();
            if(TextUtils.isEmpty(packageName)){
                return;
            }
        }
    }

    private void statistic(Context context,String item){
//        Log.i("xth","sta");
//        Tracker.Extra extra = TrackerCompat.getExtra(context);
//        extra.put("packageName",context.getPackageName());
//        TrackerCompat.getTracker().logEventRealTime("sticker_apk_main",item,"type",extra);
    }
}
