package com.qisiemoji.apksticker.request;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.qisiemoji.apksticker.BuildConfig;
import com.qisiemoji.apksticker.StickerApplication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class MockDataApiInterceptor implements Interceptor {
    public static final String TAG = MockDataApiInterceptor.class.getSimpleName();
    private int count = 0;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = null;
        String path = chain.request().url().uri().getPath();
        Log.d(TAG, "intercept: path=" + path);
        Map<String, String> query = splitQuery(chain.request().url().url());
        response = interceptRequestWhenDebug(chain, path, query);
        if (null == response) {
            Log.i(TAG, "intercept: null == response");
            response = chain.proceed(chain.request());
        }
        return response;
    }

    public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

    /**
     * 测试环境下拦截需要的接口请求，伪造数据返回
     *
     * @param chain 拦截器链
     * @param path  请求的路径path
     * @return 伪造的请求Response，有可能为null
     */
    private Response interceptRequestWhenDebug(Chain chain, String path, Map<String, String> query) {
        Response response = null;
        if (BuildConfig.DEBUG) {
            Request request = chain.request();
            if (path.contains("/search_suggest/goods_list_v2")) {
//                response = getMockEventSearchTagData(request, "SearchTagDataIp.json");
            }
        }
        return response;
    }

    /**
     * 伪造活动详情接口响应
     *
     * @param request 用户的请求
     * @return 伪造的活动详情HTTP响应
     */
    private Response getMockEventSearchTagData(Request request, String fileName) {
        Response response;
        String data = MockDataGenerator.getMockDataFromJsonFile(StickerApplication.appContext,"mock/" + fileName);
        response = getHttpSuccessResponse(request, data);
        return response;
    }

    /**
     * 伪造活动列表接口响应
     *
     * @param request 用户的请求
     * @return 伪造的活动列表HTTP响应
     */
    private Response getMockEventListResponse(Request request) {
        Response response;
        String data = MockDataGenerator.getMockDataFromJsonFile(StickerApplication.appContext, "mock/EventList.json");
        response = getHttpSuccessResponse(request, data);
        return response;
    }

    /**
     * 根据数据JSON字符串构造HTTP响应，在JSON数据不为空的情况下返回200响应，否则返回500响应
     *
     * @param request  用户的请求
     * @param dataJson 响应数据，JSON格式
     * @return 构造的HTTP响应
     */
    private Response getHttpSuccessResponse(Request request, String dataJson) {
        Response response;
        if (TextUtils.isEmpty(dataJson)) {
            Log.w(TAG, "getHttpSuccessResponse: dataJson is empty!");
            response = new Response.Builder()
                    .code(500)
                    .protocol(Protocol.HTTP_1_0)
                    .request(request)
                    //必须设置protocol&request，否则会抛出异常
                    .build();
        } else {
            response = new Response.Builder()
                    .code(200)
                    .message(dataJson)
                    .request(request)
                    .protocol(Protocol.HTTP_1_0)
                    .addHeader("Content-Type", "application/json")
                    .body(ResponseBody.create(MediaType.parse("application/json"), dataJson)).build();
        }
        return response;
    }

    private Response getHttpFailedResponse(Interceptor.Chain chain, int errorCode, String errorMsg) {
        if (errorCode < 0) {
            throw new IllegalArgumentException("httpCode must not be negative");
        }
        Response response;
        response = new Response.Builder()
                .code(errorCode)
                .message(errorMsg)
                .request(chain.request())
                .protocol(Protocol.HTTP_1_0)
                .build();
        return response;
    }
}
