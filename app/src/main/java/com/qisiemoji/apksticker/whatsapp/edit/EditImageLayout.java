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

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//
//        int width = getMeasuredWidth();
//        setMeasuredDimension(width, width);
//    }
//
//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom);
//        generateDestBitmap();
//    }

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

    public void finishOperation() {
        mHelper.finishOperation();
    }

    public void setCurrentToolType(EditImageHelper.ToolType toolType) {
        mHelper.setCurrentToolType(toolType);
    }

    public void preOperation() {
        mHelper.preOperation();
    }

    public void nextOperation() {
        mHelper.nextOperation();
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

    public void createTextToolText(boolean showKeyboard) {
        mHelper.createTextToolText(showKeyboard);
    }

    public void enableTextToolTextBorder(boolean enable) {
        mHelper.enableTextToolTextBorder(enable);
    }

    public int getDrawCount() {
        return mHelper.getDrawCount();
    }

    public int getTextCount() {
        return mHelper.getTextCount();
    }
}
