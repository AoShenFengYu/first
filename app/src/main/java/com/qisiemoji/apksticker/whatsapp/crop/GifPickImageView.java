package com.qisiemoji.apksticker.whatsapp.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class GifPickImageView extends CropImageView {
    public GifPickImageView(Context context) {
        this(context, null);
    }

    public GifPickImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifPickImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
