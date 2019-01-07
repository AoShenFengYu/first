package com.qisiemoji.apksticker.whatsapp.manager;

import android.graphics.Bitmap;
import android.graphics.Color;

public class EditImageManager {

    private static EditImageManager sInstance;

    private Bitmap mCroppedImage;

    public class DrawSettings {
        float size;
        int color;
        boolean isEraser;

        private DrawSettings() {
            size = 12;
            color = Color.GREEN;
            isEraser = false;
        }

        public float getSize() {
            return size;
        }

        public int getColor() {
            return color;
        }

        public boolean isEraser() {
            return isEraser;
        }
    }
    private DrawSettings mDrawSettings;

    private int mTextToolColor;

    public static EditImageManager getInstance() {
        if (sInstance == null) {
            synchronized (WAStickerManager.class) {
                if (sInstance == null) {
                    sInstance = new EditImageManager();
                }
            }
        }
        return sInstance;
    }

    private EditImageManager() {
        mDrawSettings = new DrawSettings();
    }

    public void setDrawSettingSize(float size) {
        mDrawSettings.size = size;
    }

    public void setDrawSettingColor(int color) {
        mDrawSettings.color = color;
    }

    public void setDrawSettingIsEraser(boolean isEraser) {
        mDrawSettings.isEraser = isEraser;
    }

    public void setTextToolColor(int color) {
        mTextToolColor = color;
    }

    public int getTextToolColor() {
        return mTextToolColor;
    }

    public DrawSettings getDrawSettings() {
        return mDrawSettings;
    }

    public Bitmap getCroppedImage() {
        return mCroppedImage;
    }

    public void setCroppedImage(Bitmap bitmap) {
        if (bitmap == null && mCroppedImage != null) {
            mCroppedImage.recycle();
        }
        mCroppedImage = bitmap;
    }
}
