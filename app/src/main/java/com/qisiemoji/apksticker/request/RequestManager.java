package com.qisiemoji.apksticker.request;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.github.aurae.retrofit2.LoganSquareConverterFactory;
import com.qisi.datacollect.util.DeviceUtils;
import com.qisiemoji.apksticker.BuildConfig;
import com.qisiemoji.apksticker.StickerApplication;
import com.qisiemoji.apksticker.util.FileUtils2;
import com.qisiemoji.apksticker.util.MD5;
import com.qisiemoji.apksticker.util.MiscUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.internal.cache.InternalCache;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RequestManager {
    private static final String OFFICIAL_URL = "https://api.kikakeyboard.com/v1/";

    private OkHttpClient mStickerClient;
    private final Object mLock = new Object();
    private Context mContext;
    private StickerApi api;
    private LoganSquareConverterFactory mConvertFactory;
    private static RequestManager sInstance;
    private static final long REQUEST_DISK_CACHE_SIZE = 50 * 1024 * 1024;

    public synchronized static RequestManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RequestManager();
        }
        sInstance.init(context);
        return sInstance;
    }

    private RequestManager() {
    }

    public void init(@NonNull Context context) {
        if (mContext == null) {
            mContext = context;
        }
        if (mConvertFactory == null) {
            mConvertFactory = LoganSquareConverterFactory.create();
        }
    }

    public StickerApi initApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .client(getKikaClient())
                .addConverterFactory(mConvertFactory)
                .baseUrl(OFFICIAL_URL)
                .build();

        return retrofit.create(StickerApi.class);
    }

    public synchronized StickerApi stickerApi() {
        if (mContext == null) {
            throw new NullPointerException("Please call init(context) first");
        }
        if (api == null) {
            this.api = initApi();
        }
        return api;
    }

    public OkHttpClient getKikaClient() {
        if (mStickerClient == null) {
            synchronized (mLock) {
                if (mStickerClient == null) {
                    File cacheDir = FileUtils2.getCacheDir(mContext, "request-cache");
                    OkHttpClient.Builder builder = new OkHttpClient.Builder()
                            .addInterceptor(new RequestInterceptor(mContext))
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .cache(new Cache(cacheDir, REQUEST_DISK_CACHE_SIZE))
                            .followRedirects(true);
                    mStickerClient = builder.build();
                }
            }
        }
        return mStickerClient;
    }

    public static String getSign(Context context) {
        final String duid = DeviceUtils.getUID(context);
        String original = String.format((Locale) null, "app_key%1$sapp_version%2$sduid%3$s"
                , BuildConfig.AGENT_APPKEY, String.valueOf(BuildConfig.VERSION_CODE), duid);
        String md5String = MD5.getMD5(original);
        return md5String;
    }

    public static String generateUserAgent(Context context) {
        String country = Locale.getDefault().getCountry();
        if (!MiscUtil.isValidHeaderString(country)) {
            country = "US";
        }
        String language = Locale.getDefault().getLanguage();
        if (!MiscUtil.isValidHeaderString(language)) {
            language = "en";
        }
        final String duid = DeviceUtils.getUID(context);
        DisplayMetrics metric = Resources.getSystem().getDisplayMetrics();
        // （hdpi: 240 , ldpi: 120 , mdpi: 160 , xhdpi: 320）

        int dpi = metric.densityDpi;
        return String.format(Locale.US,
                "%s/%s (%s/%s) Country/%s Language/%s System/android Version/%s Screen/%s",
                BuildConfig.APPLICATION_ID, String.valueOf(BuildConfig.VERSION_CODE),
                duid, BuildConfig.AGENT_APPKEY, country, language,
                String.valueOf(Build.VERSION.SDK_INT), String.valueOf(dpi));
    }

    public static void removeCache(OkHttpClient client, Request request) {
        try {
            Method method = OkHttpClient.class.getDeclaredMethod("internalCache");
            method.setAccessible(true);
            InternalCache cache = (InternalCache) method.invoke(client);
            cache.remove(request);
        } catch (Exception e) {
        }

    }

    public static abstract class Callback<T> implements retrofit2.Callback<T> {
        @Override
        public void onResponse(Call<T> call, Response<T> response) {
            if (response == null) {
                unexpectedError(new RuntimeException("Unexpected response empty"));
                return;
            }
            int code = response.code();

            if (code >= 200 && code < 300) {
                success(response, response.body());
            } else if (code == 401) {
                unauthenticated(response);
            } else if (code >= 400 && code < 500) {
                ResponseBody errorBody = response.errorBody();
                Error error = null;
                if (errorBody != null) {
                    try {
                        InputStream in = errorBody.byteStream();
                        if (in != null) {
                            error = LoganSquare.parse(in, Error.class);
                        }
                    } catch (Exception e) {
                    }
                }
                if (error == null) {
                    error = new Error();
                    error.errorCode = -1;
                    error.errorMsg = "Unknown Error!";
                }
                clientError(response, error, error.errorMsg);
                if (!BuildConfig.DEBUG) {
                    Bundle bundle = new Bundle();
                    bundle.putString("error_code", String.valueOf(code));
                    bundle.putString("message", response.message());
                    if (call != null && call.request() != null && call.request().url() != null) {
                        bundle.putString("url", call.request().url().toString());
                    }
                }
            } else if (code >= 500 && code < 600) {
                serverError(response, "Server Error!");
                if (!BuildConfig.DEBUG) {
                    Bundle bundle = new Bundle();
                    bundle.putString("error_code", String.valueOf(code));
                    bundle.putString("message", response.message());
                    if (call != null && call.request() != null && call.request().url() != null) {
                        bundle.putString("url", call.request().url().toString());
                    }
                }
            } else {
                RuntimeException exception = new RuntimeException("Unexpected response " + response);
                unexpectedError(exception);
            }
        }

        public void onError() {
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            if (call != null && call.isCanceled()) {
                return;
            }
            if (t instanceof IOException) {
                networkError((IOException) t);
            } else {
                unexpectedError(t);
            }
        }

        public abstract void success(Response<T> response, T result);

        public void unauthenticated(Response<T> response) {
            onError();
        }

        public void clientError(Response<T> response, Error error, String message) {
            onError();
        }

        public void serverError(Response<T> response, String message) {
            onError();
        }

        public void networkError(IOException e) {
            onError();
        }

        public void unexpectedError(Throwable e) {
            onError();
        }
    }

    @JsonObject
    public static class Error {
        @JsonField
        public int errorCode;
        @JsonField
        public String errorMsg;

        public Error() {
        }
    }
}
