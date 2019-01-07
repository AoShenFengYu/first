package com.qisiemoji.apksticker.whatsapp.edit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.ViewGroup;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.whatsapp.manager.EditImageManager;

public class DrawToolOperation extends BaseToolOperation {

    private static final float TOUCH_TOLERANCE = 4;

    private Path mPath;
    private Paint mPathPaint;

    private Paint mEraserPaint;
    private Paint mEraserBorderPaint;
    private float mEraserSize;

    private float mX, mY;

    private boolean mShowEraserIcon = false;

    public DrawToolOperation(Context context, ViewGroup viewGroup) {
        super(context, viewGroup);

        mPath = new Path();
        mPathPaint = new Paint();
        mPathPaint.setAntiAlias(true);
        mPathPaint.setDither(true);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);

        mEraserPaint = new Paint();
        mEraserPaint.setAntiAlias(false);
        mEraserPaint.setColor(context.getResources().getColor(R.color.draw_eraser_icon));
        mEraserBorderPaint = new Paint();
        mEraserBorderPaint.setAntiAlias(false);
        mEraserBorderPaint.setColor(context.getResources().getColor(R.color.draw_eraser_icon_border));
        mEraserBorderPaint.setStrokeWidth(context.getResources().getDimensionPixelOffset(R.dimen.draw_eraser_icon_border_size));
        mEraserBorderPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void onTouchDown(float x, float y) {
        if (EditImageManager.getInstance().getDrawSettings().isEraser()) {
            mPathPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mShowEraserIcon = true;
            mEraserSize = EditImageManager.getInstance().getDrawSettings().getSize() / 2;
        } else {
            mPathPaint.setXfermode(null);
            mPathPaint.setColor(EditImageManager.getInstance().getDrawSettings().getColor());
        }
        mPathPaint.setStrokeWidth(EditImageManager.getInstance().getDrawSettings().getSize());
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        mHasFocus = true;
    }

    @Override
    public void onTouchUp(float x, float y) {
        finishOperation();
        mHasFocus = false;
        mShowEraserIcon = false;
    }

    @Override
    public void onTouchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }

    @Override
    public void finishOperation() {
        mPath.lineTo(mX, mY);
    }

    @Override
    public void onDraw(Canvas canvas, boolean finalDest) {
        canvas.drawPath(mPath, mPathPaint);

        if (mShowEraserIcon) {
            canvas.drawCircle(mX, mY, mEraserSize, mEraserPaint);
            canvas.drawCircle(mX, mY, mEraserSize, mEraserBorderPaint);
        }
    }
}
