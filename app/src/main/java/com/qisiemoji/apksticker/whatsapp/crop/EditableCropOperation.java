package com.qisiemoji.apksticker.whatsapp.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.MotionEvent;

import com.qisiemoji.apksticker.R;

import java.util.ArrayList;

public class EditableCropOperation extends BaseCropOperation {

    private static final float TOUCH_TOLERANCE = 4;

    public interface EditableCropOperationListener {
        void onEditableOperationStartDoCrop();
        void onEditableOperationFinishDoCrop();
        void onUpdateActionBackNextStates(boolean canBack, boolean canNext);
    }
    private EditableCropOperationListener mEditableCropOperationListener;

    /** 3 edit modes : crop / erase / restore **/
    public enum EditableCropMode {
        Crop, Erase, Restore
    }
    protected  EditableCropMode mEditableCropMode;

    /** for erase / restore mode, 1 down+up = 1 action **/
    protected  class EditAction {
        static final int TYPE_ERASE = 0;
        static final int TYPE_RESTORE = 1;
        int type;
        Path path;
        float pathSize;
    }
    protected ArrayList<EditAction> mActions = new ArrayList<>();
    protected ArrayList<EditAction> mTmpActions = new ArrayList<>();

    protected  boolean mCropped;
    private boolean mShowTouchedIcon = false;
    private float mX, mY;
    protected float mCurrentPathSize;

    /** paints **/
    protected Paint mCropPathPaint;
    protected Paint mErasePaint;
    protected Paint mRestorePaint;
    private Paint mShowTouchedIconPaint;

    /** current path **/
    protected  Path mCropPath;
    private Path mErasePath;
    private Path mRestorePath;

    /** restore **/
    protected Bitmap mRestoreOverlayBitmap;
    private Canvas mRestoreOverlayCanvas;
    private Bitmap mRestoreOverlayBaseBitmap;
    protected float mRestorePathMinX;
    protected float mRestorePathMinY;
    protected float mRestorePathMaxX;
    protected float mRestorePathMaxY;
    private PorterDuffXfermode mPorterDuffXfermodeSrc;
    private PorterDuffXfermode mPorterDuffXfermodeSrcIn;
    protected PorterDuffXfermode mPorterDuffXfermodeSrcOver;

    EditableCropOperation(Context context, CropImageView view) {
        super(context, view);
        mEditableCropMode = EditableCropMode.Crop;
        mCropped = false;

        // erase paint
        mErasePaint = new Paint();
        mErasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mErasePaint.setStrokeWidth(50);
        mErasePaint.setAntiAlias(true);
        mErasePaint.setDither(true);
        mErasePaint.setStyle(Paint.Style.STROKE);
        mErasePaint.setStrokeJoin(Paint.Join.ROUND);
        mErasePaint.setStrokeCap(Paint.Cap.ROUND);

        // restore paint
        mRestorePaint = new Paint();
        mRestorePaint.setDither(true);
        mRestorePaint.setAntiAlias(true);
        mRestorePaint.setFilterBitmap(true);
        mRestorePaint.setStyle(Paint.Style.STROKE);
        mRestorePaint.setStrokeJoin(Paint.Join.ROUND);
        mRestorePaint.setStrokeCap(Paint.Cap.ROUND);
        mRestorePaint.setStrokeWidth(50);
        resetRestorePathMaxMinXY();

        // touched icon paint
        mShowTouchedIconPaint = new Paint();
        mShowTouchedIconPaint.setAntiAlias(false);
        mShowTouchedIconPaint.setColor(mContext.getResources().getColor(R.color.crop_edit_touched_icon));

        // PorterDuffXfermode
        mPorterDuffXfermodeSrc = new PorterDuffXfermode(PorterDuff.Mode.SRC);
        mPorterDuffXfermodeSrcIn = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        mPorterDuffXfermodeSrcOver = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
    }

    @Override
    public void onTouchDown(MotionEvent event, float x, float y) {
        switch (mEditableCropMode) {
            case Crop:
                onCrop(MotionEvent.ACTION_DOWN, x, y);
                break;
            case Erase:
                onErase(MotionEvent.ACTION_DOWN, x, y);
                break;
            case Restore:
                onRestore(MotionEvent.ACTION_DOWN, x, y);
                break;
            default:
                break;
        }
    }

    @Override
    public void onTouchUp(MotionEvent event, float x, float y) {
        switch (mEditableCropMode) {
            case Crop:
                onCrop(MotionEvent.ACTION_UP, x, y);
                break;
            case Erase:
                onErase(MotionEvent.ACTION_UP, x, y);
                break;
            case Restore:
                onRestore(MotionEvent.ACTION_UP, x, y);
                break;
            default:
                break;
        }
    }

    @Override
    public void onTouchMove(MotionEvent event, float x, float y) {
        switch (mEditableCropMode) {
            case Crop:
                onCrop(MotionEvent.ACTION_MOVE, x, y);
                break;
            case Erase:
                onErase(MotionEvent.ACTION_MOVE, x, y);
                break;
            case Restore:
                onRestore(MotionEvent.ACTION_MOVE, x, y);
                break;
            default:
                break;
        }
    }

    @Override
    public Bitmap getCroppedBitmap() {
        return null;
    }

    @Override
    public void reset() {
        super.reset();
        mEditableCropMode = EditableCropMode.Crop;
        mActions.clear();
        mTmpActions.clear();
        mCropped = false;
        mCropPath = null;
        mCropViewBitmap = null;
        if (mRestoreOverlayBitmap != null) {
            mRestoreOverlayBitmap.recycle();
            mRestoreOverlayBitmap = null;
        }
        mRestoreOverlayCanvas = null;
        resetRestorePathMaxMinXY();
    }

    @Override
    public void clear() {
        super.clear();
        if (mRestoreOverlayBitmap != null) {
            mRestoreOverlayBitmap.recycle();
            mRestoreOverlayBitmap = null;
        }
        if (mRestoreOverlayBaseBitmap != null) {
            mRestoreOverlayBaseBitmap.recycle();
            mRestoreOverlayBaseBitmap = null;
        }
    }

    @Override
    public void onSelect() {
        super.onSelect();

        if (mRestoreOverlayBaseBitmap == null) {
            mView.setDrawingCacheEnabled(true);
            mRestoreOverlayBaseBitmap = Bitmap.createBitmap(mView.getDrawingCache());
            mView.setDrawingCacheEnabled(false);
        }
    }

    protected void onCrop(int action, float x, float y) {

    }

    protected void onErase(int action, float x, float y) {
        switch (action) {
            case MotionEvent.ACTION_DOWN :
                mShowTouchedIcon = true;

                mX = x;
                mY = y;
                mErasePath = new Path();
                mErasePath.reset();
                mErasePath.moveTo(x, y);

                EditAction editAction = new EditAction();
                editAction.type = EditAction.TYPE_ERASE;
                editAction.path = mErasePath;
                editAction.pathSize = mCurrentPathSize;
                mActions.add(editAction);
                break;
            case MotionEvent.ACTION_MOVE :
                if (mErasePath != null) {
                    float dx = Math.abs(x - mX);
                    float dy = Math.abs(y - mY);
                    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                        mErasePath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                        mX = x;
                        mY = y;
                    }

                    if (mRestoreOverlayCanvas != null) {
                        mErasePaint.setStrokeWidth(mCurrentPathSize);
                        mRestoreOverlayCanvas.drawPath(mErasePath, mErasePaint);
                    }
                }
                break;
            case MotionEvent.ACTION_UP :
                mShowTouchedIcon = false;

                mTmpActions.clear();
                if (mEditableCropOperationListener != null) {
                    mEditableCropOperationListener.onUpdateActionBackNextStates(canBack(), canNext());
                }
                break;
        }
    }

    protected void onRestore(int action, float x, float y) {
        updateRestorePathMaxMinXY(x, y);
        switch (action) {
            case MotionEvent.ACTION_DOWN :
                mShowTouchedIcon = true;

                createRestoreOverlay();

                mX = x;
                mY = y;
                mRestorePath = new Path();
                mRestorePath.reset();
                mRestorePath.moveTo(x, y);

                EditAction editAction = new EditAction();
                editAction.type = EditAction.TYPE_RESTORE;
                editAction.path = mRestorePath;
                editAction.pathSize = mCurrentPathSize;
                mActions.add(editAction);
                break;
            case MotionEvent.ACTION_MOVE :
                if (mRestorePath != null) {
                    float dx = Math.abs(x - mX);
                    float dy = Math.abs(y - mY);
                    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                        mRestorePath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                        mX = x;
                        mY = y;
                    }

                    mRestorePaint.setStrokeWidth(mCurrentPathSize);
                    mRestorePaint.setXfermode(mPorterDuffXfermodeSrc);
                    mRestoreOverlayCanvas.drawPath(mRestorePath, mRestorePaint);
                    mRestorePaint.setXfermode(mPorterDuffXfermodeSrcIn);
                    mRestoreOverlayCanvas.drawBitmap(mRestoreOverlayBaseBitmap, 0, 0, mRestorePaint);
                }
                break;
            case MotionEvent.ACTION_UP :
                mShowTouchedIcon = false;

                mTmpActions.clear();
                if (mEditableCropOperationListener != null) {
                    mEditableCropOperationListener.onUpdateActionBackNextStates(canBack(), canNext());
                }
                break;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // erase : path
        for (EditAction editAction : mActions) {
            if (editAction.type == EditAction.TYPE_ERASE) {
                mErasePaint.setStrokeWidth(editAction.pathSize);
                canvas.drawPath(editAction.path, mErasePaint);
            }
        }

        // restore : bitmap
        if (mRestoreOverlayBitmap != null) {
            mRestorePaint.setXfermode(mPorterDuffXfermodeSrcOver);
            canvas.drawBitmap(mRestoreOverlayBitmap, 0, 0, mRestorePaint);
        }

        // touched icon
        if (mShowTouchedIcon) {
            canvas.drawCircle(mX, mY, mCurrentPathSize/2, mShowTouchedIconPaint);
        }
    }

    public void setEditableCropMode(EditableCropMode mode) {
        mEditableCropMode = mode;
    }

    private boolean canBack() {
        return mActions.size() > 0;
    }

    private boolean canNext() {
        return mTmpActions.size() > 0;
    }

    public void backEditAction() {
        if (canBack()) {
            EditAction tmpAction = mActions.remove(mActions.size() - 1);
            mTmpActions.add(tmpAction);
            createRestoreOverlay();
            mView.invalidate();
            if (mEditableCropOperationListener != null) {
                mEditableCropOperationListener.onUpdateActionBackNextStates(canBack(), canNext());
            }
        }
    }

    public void nextEditAction() {
        if (canNext()) {
            EditAction tmpAction = mTmpActions.remove(mTmpActions.size() - 1);
            mActions.add(tmpAction);
            createRestoreOverlay();
            mView.invalidate();
            if (mEditableCropOperationListener != null) {
                mEditableCropOperationListener.onUpdateActionBackNextStates(canBack(), canNext());
            }
        }
    }

    private void createRestoreOverlay() {
        if (mRestoreOverlayBitmap != null) {
            mRestoreOverlayBitmap.recycle();
        }
        mRestoreOverlayBitmap = Bitmap.createBitmap(mRestoreOverlayBaseBitmap.getWidth(), mRestoreOverlayBaseBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        mRestoreOverlayCanvas = new Canvas(mRestoreOverlayBitmap);
        for (EditAction editAction : mActions) {
            if (editAction.type == EditAction.TYPE_RESTORE) {
                mRestorePaint.setStrokeWidth(editAction.pathSize);
                mRestorePaint.setXfermode(mPorterDuffXfermodeSrc);
                mRestoreOverlayCanvas.drawPath(editAction.path, mRestorePaint);
                mRestorePaint.setXfermode(mPorterDuffXfermodeSrcIn);
                mRestoreOverlayCanvas.drawBitmap(mRestoreOverlayBaseBitmap, 0, 0, mRestorePaint);
            } else if (editAction.type == EditAction.TYPE_ERASE) {
                mErasePaint.setStrokeWidth(editAction.pathSize);
                mRestoreOverlayCanvas.drawPath(editAction.path, mErasePaint);
            }
        }
    }

    public void setCurrentPathSize(float size) {
        mCurrentPathSize = size;
    }

    public void startDoCrop() {
        mCropped = true;
        if (mEditableCropOperationListener != null) {
            mEditableCropOperationListener.onEditableOperationStartDoCrop();
        }
    }

    public void finishDoCrop() {
        if (mEditableCropOperationListener != null) {
            mEditableCropOperationListener.onEditableOperationFinishDoCrop();
            mEditableCropOperationListener.onUpdateActionBackNextStates(canBack(), canNext());
        }
    }

    public void setEditableCropOperationListener(EditableCropOperationListener listener) {
        mEditableCropOperationListener = listener;
    }

    private void updateRestorePathMaxMinXY(float x, float y) {
        float xMin = x - (mCurrentPathSize/2);
        float xMax = x + (mCurrentPathSize/2);
        float yMin = y - (mCurrentPathSize/2);
        float yMax = y + (mCurrentPathSize/2);

        if (xMin < mRestorePathMinX) {
            mRestorePathMinX = xMin;
        }
        if (xMax > mRestorePathMaxX) {
            mRestorePathMaxX = xMax;
        }
        if (yMin < mRestorePathMinY) {
            mRestorePathMinY = yMin;
        }
        if (yMax > mRestorePathMaxY) {
            mRestorePathMaxY = yMax;
        }
    }

    private void resetRestorePathMaxMinXY() {
        mRestorePathMinX = Integer.MAX_VALUE;
        mRestorePathMinY = Integer.MAX_VALUE;
        mRestorePathMaxX = Integer.MIN_VALUE;
        mRestorePathMaxY = Integer.MIN_VALUE;
    }
}
