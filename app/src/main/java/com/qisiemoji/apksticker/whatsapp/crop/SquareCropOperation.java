package com.qisiemoji.apksticker.whatsapp.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.opengl.GLES10;
import android.util.Log;
import android.view.MotionEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SquareCropOperation extends BaseCropOperation {

    private static final int SIZE_DEFAULT = 2048;
    private static final int SIZE_LIMIT = 4096;

    ArrayList<HighlightView> mHighlightViews = new ArrayList<>();
    HighlightView mMotionHighlightView;

    private float mLastX;
    private float mLastY;
    private int mMotionEdge;
    private int mValidPointerId;

    private int mSampleSize;

    public SquareCropOperation(Context context, CropImageView view) {
        super(context, view);
    }

    @Override
    public void reset() {
        for (HighlightView hv : mHighlightViews) {
            hv.reset();
            mView.zoomTo(1, (float)mView.getWidth()/2, (float)mView.getHeight()/2);
            centerBasedOnHighlightView(hv);
        }
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mView.bitmapDisplayed.getBitmap() != null) {
            for (HighlightView hv : mHighlightViews) {

                hv.matrix.set(mView.getUnrotatedMatrix());
                hv.invalidate();
                if (hv.hasFocus()) {
                    centerBasedOnHighlightView(hv);
                }
            }
        }
    }

    @Override
    public void zoomTo(float scale, float centerX, float centerY) {
        super.zoomTo(scale, centerX, centerY);
        for (HighlightView hv : mHighlightViews) {
            hv.matrix.set(mView.getUnrotatedMatrix());
            hv.invalidate();
        }
    }

    @Override
    public void zoomIn() {
        super.zoomIn();
        for (HighlightView hv : mHighlightViews) {
            hv.matrix.set(mView.getUnrotatedMatrix());
            hv.invalidate();
        }
    }

    @Override
    public void zoomOut() {
        super.zoomOut();
        for (HighlightView hv : mHighlightViews) {
            hv.matrix.set(mView.getUnrotatedMatrix());
            hv.invalidate();
        }
    }

    @Override
    public void postTranslate(float deltaX, float deltaY) {
        super.postTranslate(deltaX, deltaY);
        for (HighlightView hv : mHighlightViews) {
            hv.matrix.postTranslate(deltaX, deltaY);
            hv.invalidate();
        }
    }

    @Override
    public void onTouchDown(MotionEvent event, float x, float y) {
        for (HighlightView hv : mHighlightViews) {
            int edge = hv.getHit(x, y);
            if (edge != HighlightView.GROW_NONE) {
                mMotionEdge = edge;
                mMotionHighlightView = hv;
                mLastX = x;
                mLastY = y;
                // Prevent multiple touches from interfering with crop area re-sizing
                mValidPointerId = event.getPointerId(event.getActionIndex());
                mMotionHighlightView.setMode((edge == HighlightView.MOVE)
                        ? HighlightView.ModifyMode.Move
                        : HighlightView.ModifyMode.Grow);
                break;
            }
        }
    }

    @Override
    public void onTouchUp(MotionEvent event, float x, float y) {
        if (mMotionHighlightView != null) {
            centerBasedOnHighlightView(mMotionHighlightView);
            mMotionHighlightView.setMode(HighlightView.ModifyMode.None);
        }
        mMotionHighlightView = null;
        mView.center();
    }

    @Override
    public void onTouchMove(MotionEvent event, float x, float y) {
        if (mMotionHighlightView != null && event.getPointerId(event.getActionIndex()) == mValidPointerId) {
            mMotionHighlightView.handleMotion(mMotionEdge, x- mLastX, y- mLastY);
            mLastX = x;
            mLastY = y;
        }

        // If we're not zoomed then there's no point in even allowing the user to move the image around.
        // This call to center puts it back to the normalized location.
        if (mView.getScale() == 1F) {
            mView.center();
        }
    }

    @Override
    public void onPreGetCroppedBitmap() {
        // get CropViewBitmap Bitmap and ignore highlight
        mView.ignoreOnDraw(true);
        mView.invalidate();
        createCropViewBitmap();
        mSampleSize = calculateBitmapSampleSize(mCropViewBitmap);
        mView.ignoreOnDraw(false);
    }

    @Override
    public Bitmap getCroppedBitmap() {
        Bitmap croppedImage;
        Rect r = mHighlightViews.get(0).getScaledCropRect(mSampleSize);
        int width = r.width();
        int height = r.height();

        int outWidth = width;
        int outHeight = height;
        if (mMaxX > 0 && mMaxY > 0 && (width > mMaxX || height > mMaxY)) {
            float ratio = (float) width / (float) height;
            if ((float) mMaxX / (float) mMaxY > ratio) {
                outHeight = mMaxY;
                outWidth = (int) ((float) mMaxY * ratio + .5f);
            } else {
                outWidth = mMaxX;
                outHeight = (int) ((float) mMaxX / ratio + .5f);
            }
        }

        try {
            croppedImage = decodeRegionCrop(r, outWidth, outHeight);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return croppedImage;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (HighlightView highlightView : mHighlightViews) {
            highlightView.draw(canvas);
        }
    }

    // Pan the displayed image to make sure the cropping rectangle is visible.
    private void ensureVisible(HighlightView hv) {
        Rect r = hv.drawRect;

        int panDeltaX1 = Math.max(0, mView.getLeft() - r.left);
        int panDeltaX2 = Math.min(0, mView.getRight() - r.right);

        int panDeltaY1 = Math.max(0, mView.getTop() - r.top);
        int panDeltaY2 = Math.min(0, mView.getBottom() - r.bottom);

        int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
        int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;

        if (panDeltaX != 0 || panDeltaY != 0) {
            mView.panBy(panDeltaX, panDeltaY);
        }
    }

    // If the cropping rectangle's size changed significantly, change the
    // view's center and scale according to the cropping rectangle.
    private void centerBasedOnHighlightView(HighlightView hv) {
//        Rect drawRect = hv.drawRect;
//
//        float width = drawRect.width();
//        float height = drawRect.height();
//
//        float thisWidth = mView.getWidth();
//        float thisHeight = mView.getHeight();
//
//        float z1 = thisWidth / width * .6F;
//        float z2 = thisHeight / height * .6F;
//
//        float zoom = Math.min(z1, z2);
//        zoom = zoom * mView.getScale();
//        zoom = Math.max(1F, zoom);
//
//        if ((Math.abs(zoom - mView.getScale()) / zoom) > .1) {
//            float[] coordinates = new float[] { hv.cropRect.centerX(), hv.cropRect.centerY() };
//            mView.getUnrotatedMatrix().mapPoints(coordinates);
//            mView.zoomTo(zoom, coordinates[0], coordinates[1], 300F);
//        }
//
//        ensureVisible(hv);
    }

    public void addSquareHighlistView(HighlightView hv) {
        mHighlightViews.add(hv);
    }

    private Bitmap decodeRegionCrop(Rect rect, int outWidth, int outHeight) {
        InputStream is = null;
        Bitmap croppedImage = null;
        try {
            byte[] tmp = convertBitmapToBytes(mCropViewBitmap);
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(tmp, 0, tmp.length, false);

            final int width = decoder.getWidth();
            final int height = decoder.getHeight();

//            if (mExifRotation != 0) {
//                // Adjust crop area to account for image rotation
//                Matrix matrix = new Matrix();
//                matrix.setRotate(-mExifRotation);
//
//                RectF adjusted = new RectF();
//                matrix.mapRect(adjusted, new RectF(rect));
//
//                // Adjust to account for origin at 0,0
//                adjusted.offset(adjusted.left < 0 ? width : 0, adjusted.top < 0 ? height : 0);
//                rect = new Rect((int) adjusted.left, (int) adjusted.top, (int) adjusted.right, (int) adjusted.bottom);
//            }

            try {
                croppedImage = decoder.decodeRegion(rect, new BitmapFactory.Options());
                if (croppedImage != null && (rect.width() > outWidth || rect.height() > outHeight)) {
                    Matrix matrix = new Matrix();
                    matrix.postScale((float) outWidth / rect.width(), (float) outHeight / rect.height());
                    croppedImage = Bitmap.createBitmap(croppedImage, 0, 0, croppedImage.getWidth(), croppedImage.getHeight(), matrix, true);
                }
            } catch (IllegalArgumentException e) {
                // Rethrow with some extra information
                throw new IllegalArgumentException("Rectangle " + rect + " is outside of the image ("
                        + width + "," + height + "," + mExifRotation + ")", e);
            } catch (Exception e) {
                Log.e(CropUtil.TAG,"Error cropping image: " + e.getMessage(), e);
            }

        } catch (IOException e) {
            Log.e(CropUtil.TAG,"IOException cropping image: " + e.getMessage(), e);
        } catch (OutOfMemoryError e) {
            Log.e(CropUtil.TAG,"OOM cropping image: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(CropUtil.TAG,"Exception cropping image: " + e.getMessage(), e);
        } finally {
            CropUtil.closeSilently(is);
        }
        return croppedImage;
    }

    private int calculateBitmapSampleSize(Bitmap bitmap) {
        int maxSize = getMaxImageSize();
        int sampleSize = 1;
        while (bitmap.getHeight() / sampleSize > maxSize || bitmap.getWidth() / sampleSize > maxSize) {
            sampleSize = sampleSize << 1;
        }
        return sampleSize;
    }

    private int getMaxImageSize() {
        int textureLimit = getMaxTextureSize();
        if (textureLimit == 0) {
            return SIZE_DEFAULT;
        } else {
            return Math.min(textureLimit, SIZE_LIMIT);
        }
    }

    private int getMaxTextureSize() {
        // The OpenGL texture size is the maximum size that can be drawn in an ImageView
        int[] maxSize = new int[1];
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);
        return maxSize[0];
    }

    private byte[] convertBitmapToBytes(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
