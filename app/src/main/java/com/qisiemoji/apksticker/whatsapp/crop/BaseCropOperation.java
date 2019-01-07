package com.qisiemoji.apksticker.whatsapp.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

public abstract class BaseCropOperation {

    protected Context mContext;
    protected CropImageView mView;

    protected int mMaxX;
    protected int mMaxY;
    protected int mExifRotation;

    protected Bitmap mCropViewBitmap;

    public BaseCropOperation(Context context, CropImageView view) {
        mContext = context;
        mView = view;
    }

    public abstract void onTouchDown(MotionEvent event, float x, float y);

    public abstract void onTouchUp(MotionEvent event, float x, float y);

    public abstract void onTouchMove(MotionEvent event, float x, float y);

    public abstract Bitmap getCroppedBitmap();

    public void onSelect() {
    }

    public void onUnselect() {
    }

    public void onPreGetCroppedBitmap() {
    }

    public void reset() {
    }

    public void clear() {
        mContext = null;
        mView = null;
        if (mCropViewBitmap != null) {
            mCropViewBitmap.recycle();
            mCropViewBitmap = null;
        }
    }

    public void onDraw(Canvas canvas) {
    }

    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    public void zoomTo(float scale, float centerX, float centerY) {
    }

    public void zoomIn() {
    }

    public void zoomOut() {
    }

    public void postTranslate(float deltaX, float deltaY) {
    }

    public void setInputInfos(int maxX, int maxY, int exifRotation) {
        mMaxX = maxX;
        mMaxY = maxY;
        mExifRotation = exifRotation;
    }

    public void createCropViewBitmap() {
        if (mCropViewBitmap != null) {
            mCropViewBitmap.recycle();
        }
        mView.setDrawingCacheEnabled(true);
        mCropViewBitmap = Bitmap.createBitmap(mView.getDrawingCache());
        mView.setDrawingCacheEnabled(false);
    }
}
