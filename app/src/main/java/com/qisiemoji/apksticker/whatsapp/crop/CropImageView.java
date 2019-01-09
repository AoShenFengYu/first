package com.qisiemoji.apksticker.whatsapp.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.qisiemoji.apksticker.whatsapp.gifpick.GifPickActivity;

public class CropImageView extends ImageViewTouchBase {

    public Context mContext;

    enum Mode {
        None, Square, HandFree, HumanBody, ContourClip
    }
    public Mode mMode = Mode.None;

    public SquareCropOperation mSquareCropOperation;
    public BaseCropOperation mCurrentCropOperation;

    public boolean mIgnoreOnDraw;

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mSquareCropOperation = new SquareCropOperation(getContext(), this);
        setMode(Mode.Square);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mCurrentCropOperation != null) {
            mCurrentCropOperation.onLayout(changed, left, top, right, bottom);
        }
    }

    @Override
    protected void zoomTo(float scale, float centerX, float centerY) {
        super.zoomTo(scale, centerX, centerY);
        if (mCurrentCropOperation != null) {
            mCurrentCropOperation.zoomTo(scale, centerX, centerY);
        }
    }

    @Override
    protected void zoomIn() {
        super.zoomIn();
        if (mCurrentCropOperation != null) {
            mCurrentCropOperation.zoomIn();
        }
    }

    @Override
    protected void zoomOut() {
        super.zoomOut();
        if (mCurrentCropOperation != null) {
            mCurrentCropOperation.zoomOut();
        }
    }

    @Override
    protected void postTranslate(float deltaX, float deltaY) {
        super.postTranslate(deltaX, deltaY);
        if (mCurrentCropOperation != null) {
            mCurrentCropOperation.postTranslate(deltaX, deltaY);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if(mContext instanceof CropImageActivity){
            CropImageActivity cropImageActivity = (CropImageActivity) mContext;
            if (cropImageActivity.isSaving()) {
                return false;
            }
        }

        if(mContext instanceof GifPickActivity){
            GifPickActivity cropImageActivity = (GifPickActivity) mContext;
            if (cropImageActivity.isSaving()) {
                return false;
            }
        }

        if (mCurrentCropOperation != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mCurrentCropOperation.onTouchDown(event, event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    mCurrentCropOperation.onTouchUp(event, event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    mCurrentCropOperation.onTouchMove(event, event.getX(), event.getY());
                    break;
            }
        }

        return true;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (!mIgnoreOnDraw && mCurrentCropOperation != null) {
            mCurrentCropOperation.onDraw(canvas);
        }
    }

    public void addSquareHighlistView(HighlightView hv) {
        mSquareCropOperation.addSquareHighlistView(hv);
        invalidate();
    }

    public int getSquareHighlistViewSize() {
        return mSquareCropOperation.mHighlightViews.size();
    }

    public HighlightView getSquareHighlistView(int index) {
        return mSquareCropOperation.mHighlightViews.get(index);
    }

    public void clearSquareHighlistView() {
        mSquareCropOperation.mHighlightViews.clear();
    }

    public void ignoreOnDraw(boolean ignore) {
        mIgnoreOnDraw = ignore;
    }

    public void setMode(Mode mode) {
        if (mMode == mode) {
            return;
        }

        mMode = mode;
        if (mCurrentCropOperation != null) {
            mCurrentCropOperation.onUnselect();
        }
        reset();
        if (mMode == Mode.HandFree) {
//            mCurrentCropOperation = mHandFreeCropOperation;
        } else if (mMode == Mode.Square) {
            mCurrentCropOperation = mSquareCropOperation;
        } else if (mMode == Mode.ContourClip)  {
//            mCurrentCropOperation = mContourClipCropOperation;
        } else {
            mCurrentCropOperation = null;
        }

        if (mCurrentCropOperation != null) {
            mCurrentCropOperation.onSelect();
            center();
            invalidate();
        }
    }

    public Mode getMode() {
        return mMode;
    }

    public void reset() {
//        mHandFreeCropOperation.reset();
        mSquareCropOperation.reset();
//        mContourClipCropOperation.reset();
    }

    @Override
    public void clear() {
        super.clear();
//        mHandFreeCropOperation.clear();
        mSquareCropOperation.clear();
//        mContourClipCropOperation.clear();
    }

    public void setInputInfos(int maxX, int maxY, int exifRotation) {
//        mHandFreeCropOperation.setInputInfos(maxX, maxY, exifRotation);
        mSquareCropOperation.setInputInfos(maxX, maxY, exifRotation);
//        mContourClipCropOperation.setInputInfos(maxX, maxY, exifRotation);
    }

    public Bitmap getCroppedBitmap() {
        Bitmap croppedBitmap = mCurrentCropOperation.getCroppedBitmap();
        // to fix there's not free crop behavior case/ human body crop not finish -> use square crop
        if (croppedBitmap == null) {
            croppedBitmap = mSquareCropOperation.getCroppedBitmap();
        }
        return croppedBitmap;
    }

    public void  onPreGetCroppedBitmap() {
        // to fix there's not free crop behavior case -> use square crop
        mSquareCropOperation.onPreGetCroppedBitmap();
//        mHandFreeCropOperation.onPreGetCroppedBitmap();
    }

    public String getCurrentTypeString() {
//        if (mCurrentCropOperation instanceof HandFreeCropOperation) {
//            return "hand_free";
//        } else
        if (mCurrentCropOperation instanceof SquareCropOperation) {
            return "1:1";
        } else {
            return null;
        }
    }

    public void setEditableCropOperationListener(EditableCropOperation.EditableCropOperationListener listener) {
//        mHandFreeCropOperation.setEditableCropOperationListener(listener);
//        mContourClipCropOperation.setEditableCropOperationListener(listener);
    }

    public void setCurrentEditableCropOperationMode(EditableCropOperation.EditableCropMode mode) {
        if (mCurrentCropOperation instanceof EditableCropOperation) {
            ((EditableCropOperation)mCurrentCropOperation).setEditableCropMode(mode);
        }
    }

    public void setCurrentEditableCropOperationBackOrNext(boolean back) {
        if (mCurrentCropOperation instanceof EditableCropOperation) {
            if (back) {
                ((EditableCropOperation)mCurrentCropOperation).backEditAction();
            } else {
                ((EditableCropOperation)mCurrentCropOperation).nextEditAction();
            }
        }
    }

    public void setCurrentEditableCropOperationPathSize(float size) {
        if (mCurrentCropOperation instanceof EditableCropOperation) {
            ((EditableCropOperation)mCurrentCropOperation).setCurrentPathSize(size);
        }
    }
}
