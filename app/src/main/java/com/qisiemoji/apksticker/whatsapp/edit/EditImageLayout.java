package com.qisiemoji.apksticker.whatsapp.edit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.qisiemoji.apksticker.R;

public class EditImageLayout extends FrameLayout {

    private EditImageHelper mHelper;

    public EditImageLayout(@NonNull Context context) {
        this(context, null);
    }

    public EditImageLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditImageLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHelper = new EditImageHelper(getContext(), this);
    }

    public void setEditImageHelperListener(EditImageHelper.EditImageHelperListener listener) {
        mHelper.setEditImageHelperListener(listener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mHelper.onDraw(canvas, false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHelper.onTouchDown(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                mHelper.onTouchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mHelper.onTouchUp(x, y);
                invalidate();
                break;
        }

        return true;
    }

    public Bitmap getOperationBitmap() {
        int imageSize = getResources().getDimensionPixelSize(R.dimen.edit_image_size);
        Bitmap operationBitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
        Canvas destBitmapCanvas = new Canvas(operationBitmap);
        mHelper.onDraw(destBitmapCanvas, true);
        return operationBitmap;
    }

    public void setTextToolColor(int color) {
        mHelper.setTextToolColor(color);
    }

    public void createTextToolText() {
        mHelper.createTextToolText();
    }

    public void setTextToolText(String text) {
        mHelper.setTextToolText(text);
    }
}
