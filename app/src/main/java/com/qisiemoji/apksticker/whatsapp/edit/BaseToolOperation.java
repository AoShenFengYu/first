package com.qisiemoji.apksticker.whatsapp.edit;

import android.content.Context;
import android.graphics.Canvas;
import android.view.ViewGroup;

public abstract class BaseToolOperation {

    protected Context mContext;
    protected ViewGroup mViewGroup;
    protected boolean mHasFocus;

    public BaseToolOperation(Context context, ViewGroup viewGroup) {
        mContext = context;
        mViewGroup = viewGroup;
    }

    public abstract void onTouchDown(float x, float y);

    public abstract void onTouchUp(float x, float y);

    public abstract void onTouchMove(float x, float y);

    public void onDraw(Canvas canvas, boolean finalDest) {
    }

    public void finishOperation() {
    }

    public boolean handleTouchEvent(float x, float y) {
        return false;
    }

    public boolean shouldKeep() {
        return true;
    }

    public void setHasFocus(boolean hasFocus) {
        mHasFocus = hasFocus;
    }

    public void clear() {
    }
}
