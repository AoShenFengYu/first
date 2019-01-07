package com.qisiemoji.apksticker;

import android.app.Application;
import android.content.Context;

import com.qisiemoji.apksticker.receivers.ApkMonitorReceiver;

public class StickerApplication extends Application {
    public static Context appContext;
    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        ApkMonitorReceiver.register(this);
//        FeatureConfigManager.init(this);
    }
}
