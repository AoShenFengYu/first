package com.qisiemoji.apksticker.request;

import android.app.Application;

import com.qisiemoji.apksticker.BuildConfig;
import com.qisiemoji.apksticker.util.FileUtils2;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class ABRequestManager {
    private static final long REQUEST_DISK_CACHE_SIZE = 50 * 1024 * 1024;
    private static final String CACHE_REQUEST_1 = "request-cache";

    private OkHttpClient mClient;
    private Object mLock =  new Object();


    private ABRequestManager(){}

    private static class InstanceHolder {
        private final static ABRequestManager INSTANCE = new ABRequestManager();
    }

    public static ABRequestManager getInstance(){
        return InstanceHolder.INSTANCE;
    }

    public OkHttpClient getHttpClient(Application mContext) {
        if (mClient == null) {
            synchronized (mLock) {
                if (mClient == null) {
                    File cacheDir = FileUtils2.getCacheDir(mContext, CACHE_REQUEST_1);
                    OkHttpClient.Builder builder = new OkHttpClient.Builder()
                            .addInterceptor(new RequestInterceptor(mContext
                                    ,RequestManager.getSign(mContext),RequestManager.generateUserAgent(mContext)
                                    ,false))
                            .connectTimeout(BuildConfig.DEBUG ? 30 : 15, TimeUnit.SECONDS)
                            .cache(new Cache(cacheDir, REQUEST_DISK_CACHE_SIZE))
                            .followRedirects(true);
                    mClient = builder
                            .build();
                }
            }
        }
        return mClient;
    }
}
