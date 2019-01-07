package com.qisiemoji.apksticker.whatsapp.edit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.ViewGroup;

public class BaseBitmapToolOperation extends BaseToolOperation {

    private Bitmap mBitmap;
    private Paint mBaseBitmapPaint;

    public BaseBitmapToolOperation(Context context, ViewGroup viewGroup, Bitmap bitmap) {
        super(context, viewGroup);
        mBitmap = bitmap;
        mBaseBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    public void onTouchDown(float x, float y) {
    }

    @Override
    public void onTouchUp(float x, float y) {
    }

    @Override
    public void onTouchMove(float x, float y) {
    }

    @Override
    public void onDraw(Canvas canvas, boolean finalDest) {
        canvas.drawBitmap(mBitmap, 0, 0, mBaseBitmapPaint);
    }
}
