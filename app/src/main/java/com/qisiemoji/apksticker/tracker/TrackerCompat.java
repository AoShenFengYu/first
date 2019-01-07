package com.qisiemoji.apksticker.tracker;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.kikatech.common.service.BaseService;
import com.kikatech.common.service.ServiceCreator;
import com.kikatech.common.service.ServiceManager;
import com.kikatech.koala.AdIdProxy;
import com.kikatech.koala.AnalyticsService;
import com.qisi.event.Tracker;
import com.qisi.utils.SharedPreferencesUtils;

public class TrackerCompat {

    public static final String EXTRA_PKG_NAME = "pkgName";
    private static final String EXTRA_THEME_VERSION = "themeVersion";
    private static final String EXTRA_THEME_VERSION_VALUE = "20180126";

    private static ServiceCreator sAnalyticsCreator;
    private static Application sApplication;

    public static ServiceCreator init(Application application, String agentSecret, String agentKey, String agentChannel) {
        sApplication = application;
        sAnalyticsCreator = AnalyticsService.getCreator(BaseService.ThreadMode.WORK_THREAD, agentKey,
                agentSecret, agentChannel, false, false, ".*", new AdIdProxy() {
                    @Override
                    public String get(Context context) {
                        try {
                            AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                            if (adInfo != null) {
                                String id = adInfo.getId();
                                if (id != null) {
                                    SharedPreferencesUtils.setString(context, SharedPreferencesUtils.PREF_GA_ID, id);
                                    return id;
                                }
                            }
                        } catch (Throwable e) {
                        }
                        return null;
                    }
                });
        return sAnalyticsCreator;
    }

    public static AnalyticsService getAnalyticsService() {
        return ServiceManager.getService(sApplication, sAnalyticsCreator);
    }

    public static Tracker getTracker() {
        return Tracker.getTracker(sApplication, getAnalyticsService(), new Tracker.ExtraInfoBuilder() {
            @Override
            public void addExtraInfo(String s, String s1, Bundle bundle) {

            }
        });
    }

    public static Tracker.Extra getExtra(Context ctx) {
        Tracker.Extra extra = new Tracker.Extra();
        try {
            extra.put(EXTRA_PKG_NAME, ctx.getPackageName());
            extra.put(EXTRA_THEME_VERSION, EXTRA_THEME_VERSION_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return extra;

    }

    static Tracker.Extra getExtra(Context ctx, Tracker.Extra extra) {
        if (null == extra) extra = new Tracker.Extra();

        try {
            extra.put(EXTRA_PKG_NAME, ctx.getPackageName());
            extra.put(EXTRA_THEME_VERSION, EXTRA_THEME_VERSION_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return extra;

    }

}
