package com.qisiemoji.apksticker.request;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.qisiemoji.apksticker.util.FileUtils2;

import java.io.IOException;

public class MockDataGenerator {
    public static final String TAG = "mock";

    public static String getMockDataFromJsonFile(Context context, String assetPath) {
        final String data;
        AssetManager assetManager = context.getAssets();
        try {
            data = FileUtils2.getStringFromInputStream(
                    assetManager.open(assetPath));
            return data;
        } catch (IOException e) {
            Log.e(TAG, "getMockDataFromJsonFile: ", e);
        }
        return null;
    }
}
