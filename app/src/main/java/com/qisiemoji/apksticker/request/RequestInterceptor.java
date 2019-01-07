package com.qisiemoji.apksticker.request;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.Locale;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RequestInterceptor implements Interceptor {
    private static final String TAG = "request";

    private String mSign;
    private String mUserAgent;
    private boolean mIsLockerAppListInterceptor;
    private Context mContext;

    public RequestInterceptor(Context mContext) {
        this.mContext = mContext;
    }

    public RequestInterceptor(Context mContext,String sign, String userAgent, boolean isLockerAppListInterceptor) {
        this.mContext = mContext;
        this.mSign = sign;
        this.mUserAgent = userAgent;
        mIsLockerAppListInterceptor = isLockerAppListInterceptor;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (mContext == null) {
            throw new NullPointerException("Please call RequestManager.getInstance().init(context) first");
        }
        Request request = chain.request();

        Request.Builder builder = request.newBuilder();

        HttpUrl.Builder urlBuilder = request.url().newBuilder();
        if (TextUtils.isEmpty(mSign)) {
            mSign = RequestManager.getSign(mContext.getApplicationContext());
        }
        if (!TextUtils.isEmpty(mSign)) {
            urlBuilder.addEncodedQueryParameter("sign", mSign);
        }

        builder.addHeader("User-Agent", RequestManager.generateUserAgent(mContext.getApplicationContext()));
        builder.addHeader("Accept-Charset", "UTF-8");
        try {
            builder.addHeader("Accept-Language", Locale.getDefault().toString());
        } catch (Exception e) {
            builder.addHeader("Accept-Language", "en_US");
        }
        String model = Build.MODEL;
        builder.addHeader("X-Model", model);

        Request newRequest = builder.url(urlBuilder.build()).build();

        try {
            Response response = chain.proceed(newRequest);
            return response;
        } catch (Exception e) {
            throw new IOException(e);
        }

    }
}
