/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qisiemoji.apksticker.whatsapp.crop;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.opengl.GLES10;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.qisi.event.Tracker;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.tracker.TrackerCompat;
import com.qisiemoji.apksticker.whatsapp.manager.EditImageManager;
import com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.EXTRA_SELECTED_IMAGE_PATH;
import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.REQUEST_CODE_EDIT_IMAGE;
import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.RESULT_CODE_FINISH_EDIT_IMAGE;

/*
 * Modified from original in AOSP.
 */
public class CropImageActivity extends MonitoredActivity implements EditableCropOperation.EditableCropOperationListener {

    private static final int SIZE_DEFAULT = 2048;
    private static final int SIZE_LIMIT = 4096;

    private final Handler mHandler = new Handler();

    private int mAspectX;
    private int mAspectY;

    // Output image
    private int mMaxX;
    private int mMaxY;
    private int mExifRotation;

    private boolean mIsSaving;

    private RotateBitmap mNormalRotateBitmap;
    private CropImageView mCropImageView;
    private HighlightView mCropView;

    private AppCompatTextView mNext;

    private ImageView mButtonSquareCrop;
    private ImageView mButtonManualCrop;
    private ImageView mButtonHumanBodyCrop;

    private ProgressBar mProgressBar;

    private GetCropBitmapTask mGetCropBitmapTask;

    // Editable Operation Crop Tab
    private TabLayout mTabLayout;
    private FrameLayout mEraseTab;
    private FrameLayout mRestoreTab;
    private View mResetBtn;
    private ImageView mEidtBack;
    private ImageView mEidtNext;
    private SeekBar mSizeSeekBar;
    private float mSizeSeekBarPathSize;

    private View mBtnArea;
    private View mTabArea;

    @Override
    public void onCreate(Bundle icicle) {
        setupWindowFlags();
        super.onCreate(icicle);
        setupViews();

        Uri sourceUri = getIntent().getData();
        if (sourceUri != null) {
            loadInputByUri(sourceUri);
        } else {
            String sourcePath = getIntent().getStringExtra(EXTRA_SELECTED_IMAGE_PATH);
            loadInputByPath(sourcePath);
        }

        if (mNormalRotateBitmap == null) {
            finish();
            return;
        }
        startCrop();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.crop__activity_crop;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setupWindowFlags() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private void setupViews() {
        mCropImageView = findViewById(R.id.crop_image);
        mCropImageView.mContext = this;
        mNext = findViewById(R.id.next);
        mNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                reportCropType(mCropImageView.getCurrentTypeString());

                mIsSaving = true;
                setProgressBarVisibility(View.VISIBLE);
                mGetCropBitmapTask = new GetCropBitmapTask(new GetCropBitmapTaskCallback() {
                    @Override
                    public void onPreGetCroppedBitmap() {
                        mCropImageView.onPreGetCroppedBitmap();
                    }

                    @Override
                    public Bitmap getCroppedBitmap() {
                        return mCropImageView.getCroppedBitmap();
                    }

                    @Override
                    public void onFinishCropped(Bitmap bitmap) {
                        EditImageManager.getInstance().setCroppedImage(bitmap);
//                        Intent intent = new Intent(CropImageActivity.this, EditImageActivity.class);
//                        startActivityForResult(intent, REQUEST_CODE_EDIT_IMAGE);
//                        mIsSaving = false;
//                        setProgressBarVisibility(View.INVISIBLE);
                    }
                });
                mGetCropBitmapTask.execute();
            }
        });

        mButtonSquareCrop = findViewById(R.id.btn_crop);
        mButtonSquareCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCropMode(CropImageView.Mode.Square);
            }
        });
        mButtonManualCrop = findViewById(R.id.btn_manual);
        mButtonManualCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCropMode(CropImageView.Mode.HandFree);
                showContourCropTipIfNeed();
            }
        });
        mButtonHumanBodyCrop = findViewById(R.id.btn_human_body);
        mButtonHumanBodyCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCropMode(CropImageView.Mode.ContourClip);
                showContourCropTipIfNeed();
            }
        });
        setCropMode(CropImageView.Mode.Square);

        mProgressBar = findViewById(R.id.loading_progress_bar);
        setProgressBarVisibility(View.INVISIBLE);

        // Editable Operation
        setupEditableOperationViews();
    }

    private void setupEditableOperationViews() {
        mBtnArea = findViewById(R.id.btn_area);
        mTabArea = findViewById(R.id.tab_area);

        mResetBtn =findViewById(R.id.reset);
        mResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnArea.setVisibility(View.VISIBLE);
                mTabArea.setVisibility(View.GONE);
                mResetBtn.setVisibility(View.GONE);

                mCropImageView.setLayerType(View.LAYER_TYPE_NONE, null);
                mCropImageView.reset();
            }
        });

        final float editSizeMin = getResources().getDimensionPixelSize(R.dimen.draw_size_min);
        final float editSizeMax = getResources().getDimensionPixelSize(R.dimen.draw_size_max);
        final float editSizeDefault = getResources().getDimensionPixelSize(R.dimen.draw_size_default);
        final float editSizeStep = (editSizeMax - editSizeMin) / 100;
        mSizeSeekBar = findViewById(R.id.size_seek_bar);
        mSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float editSize = editSizeMin + (progress * editSizeStep);
                mSizeSeekBarPathSize = editSize;
                mCropImageView.setCurrentEditableCropOperationPathSize(editSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        float initValue = (editSizeDefault - editSizeMin) * 100 / (editSizeMax - editSizeMin);
        mSizeSeekBar.setProgress((int) initValue);

        mEidtBack = findViewById(R.id.eidt_back);
        mEidtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropImageView.setCurrentEditableCropOperationBackOrNext(true);
            }
        });

        mEidtNext = findViewById(R.id.eidt_next);
        mEidtNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropImageView.setCurrentEditableCropOperationBackOrNext(false);
            }
        });

        mTabLayout = findViewById(R.id.tabs);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    updateTabState(EditableCropOperation.EditableCropMode.Erase);
                    mCropImageView.setCurrentEditableCropOperationMode(EditableCropOperation.EditableCropMode.Erase);
                } else {
                    updateTabState(EditableCropOperation.EditableCropMode.Restore);
                    mCropImageView.setCurrentEditableCropOperationMode(EditableCropOperation.EditableCropMode.Restore);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        LayoutInflater inflater = LayoutInflater.from(this);
        mEraseTab = (FrameLayout) inflater.inflate(R.layout.custom_tab_view, null);
        AppCompatTextView textTabName = mEraseTab.findViewById(R.id.tab_name);
        textTabName.setText(getResources().getString(R.string.tab_erase));
        TabLayout.Tab tabText = mTabLayout.newTab();
        tabText.setCustomView(mEraseTab);
        mTabLayout.addTab(tabText);

        mRestoreTab = (FrameLayout) inflater.inflate(R.layout.custom_tab_view, null);
        AppCompatTextView drawTabName = mRestoreTab.findViewById(R.id.tab_name);
        drawTabName.setText(getResources().getString(R.string.tab_restore));
        TabLayout.Tab tabDraw = mTabLayout.newTab();
        tabDraw.setCustomView(mRestoreTab);
        mTabLayout.addTab(tabDraw);
    }

    private void loadInputByUri(Uri uri) {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            mAspectX = extras.getInt(Crop.Extra.ASPECT_X);
            mAspectY = extras.getInt(Crop.Extra.ASPECT_Y);
            mMaxX = extras.getInt(Crop.Extra.MAX_X);
            mMaxY = extras.getInt(Crop.Extra.MAX_Y);
        }

        if (uri != null) {
            mExifRotation = CropUtil.getExifRotation(CropUtil.getFromMediaUri(this, getContentResolver(), uri));

            InputStream is = null;
            try {
                int sampleSize = calculateBitmapSampleSize(uri);
                is = getContentResolver().openInputStream(uri);
                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inSampleSize = sampleSize;
                mNormalRotateBitmap = new RotateBitmap(BitmapFactory.decodeStream(is, null, option), mExifRotation);
            } catch (IOException e) {
                Log.e(CropUtil.TAG,"Error reading image: " + e.getMessage(), e);
                setResultException(e);
            } catch (OutOfMemoryError e) {
                Log.e(CropUtil.TAG,"OOM reading image: " + e.getMessage(), e);
                setResultException(e);
            } finally {
                CropUtil.closeSilently(is);
            }
        }
    }

    private void loadInputByPath(String path) {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            mAspectX = extras.getInt(Crop.Extra.ASPECT_X);
            mAspectY = extras.getInt(Crop.Extra.ASPECT_Y);
            mMaxX = extras.getInt(Crop.Extra.MAX_X);
            mMaxY = extras.getInt(Crop.Extra.MAX_Y);
        }

        if (path != null) {
            mExifRotation = CropUtil.getExifRotation(new File(path));
            try {
                mNormalRotateBitmap = new RotateBitmap(getScaledBitmap(path), mExifRotation);
            } catch (OutOfMemoryError e) {
                Log.e(CropUtil.TAG,"OOM reading image: " + e.getMessage(), e);
                setResultException(e);
            }
        }
    }

    private int calculateBitmapSampleSize(Uri bitmapUri) throws IOException {
        InputStream is = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            is = getContentResolver().openInputStream(bitmapUri);
            BitmapFactory.decodeStream(is, null, options); // Just get image size
        } finally {
            CropUtil.closeSilently(is);
        }

        int maxSize = getMaxImageSize();
        int sampleSize = 1;
        while (options.outHeight / sampleSize > maxSize || options.outWidth / sampleSize > maxSize) {
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

    private void startCrop() {
        if (isFinishing()) {
            return;
        }
        mCropImageView.setInputInfos(mMaxX, mMaxY, mExifRotation);
        mCropImageView.setImageRotateBitmapResetBase(mNormalRotateBitmap, true);
        mCropImageView.setEditableCropOperationListener(this);
        CropUtil.startBackgroundJob(this, null, getResources().getString(R.string.crop__wait),
                new Runnable() {
                    public void run() {
                        final CountDownLatch latch = new CountDownLatch(1);
                        mHandler.post(new Runnable() {
                            public void run() {
                                if (mCropImageView.getScale() == 1F) {
                                    mCropImageView.center();
                                }
                                latch.countDown();
                            }
                        });
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        new Cropper().crop();
                    }
                }, mHandler
        );
    }

    private void setProgressBarVisibility(int visibility) {
        mProgressBar.setVisibility(visibility);
    }

    @Override
    public void onEditableOperationStartDoCrop() {
        setProgressBarVisibility(View.VISIBLE);
    }

    @Override
    public void onEditableOperationFinishDoCrop() {
        setProgressBarVisibility(View.INVISIBLE);

        mBtnArea.setVisibility(View.GONE);
        mTabArea.setVisibility(View.VISIBLE);
        mResetBtn.setVisibility(View.VISIBLE);

        mCropImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        TabLayout.Tab tab = mTabLayout.getTabAt(0);
        if (tab != null) {
            tab.select();
        }
        updateTabState(EditableCropOperation.EditableCropMode.Erase);
        mCropImageView.setCurrentEditableCropOperationMode(EditableCropOperation.EditableCropMode.Erase);
        mCropImageView.setCurrentEditableCropOperationPathSize(mSizeSeekBarPathSize);
    }

    @Override
    public void onUpdateActionBackNextStates(boolean canBack, boolean canNext) {
        updateBackNextButtonState(canBack, canNext);
    }

    private void updateTabState(EditableCropOperation.EditableCropMode mode) {
        if (mEraseTab == null || mRestoreTab == null) {
            return;
        }
        AppCompatImageView eraseTabIcon = mEraseTab.findViewById(R.id.tab_icon);
        AppCompatTextView eraseTabName = mEraseTab.findViewById(R.id.tab_name);
        AppCompatImageView restoreTabIcon = mRestoreTab.findViewById(R.id.tab_icon);
        AppCompatTextView restoreTabName = mRestoreTab.findViewById(R.id.tab_name);

        Resources res = getResources();
        eraseTabIcon.setImageResource(mode.equals(EditableCropOperation.EditableCropMode.Erase) ? R.drawable.tab_ic_erase_down : R.drawable.tab_ic_erase_up);
        eraseTabName.setTextColor(mode.equals(EditableCropOperation.EditableCropMode.Erase) ? res.getColor(R.color.tab_select_text_color) : res.getColor(R.color.tab_unselect_text_color));
        restoreTabIcon.setImageResource(mode.equals(EditableCropOperation.EditableCropMode.Restore) ? R.drawable.tab_ic_restore_down : R.drawable.tab_ic_restore_up);
        restoreTabName.setTextColor(mode.equals(EditableCropOperation.EditableCropMode.Restore) ? res.getColor(R.color.tab_select_text_color) : res.getColor(R.color.tab_unselect_text_color));
    }

    private void updateBackNextButtonState(boolean canBack, boolean canNext) {
        mEidtBack.setEnabled(canBack);
        mEidtBack.setClickable(canBack);
        mEidtBack.setImageResource(canBack ? R.drawable.ic_erase_back : R.drawable.ic_erase_back_disabled);

        mEidtNext.setEnabled(canNext);
        mEidtNext.setClickable(canNext);
        mEidtNext.setImageResource(canNext ? R.drawable.ic_erase_next : R.drawable.ic_erase_next_disabled);
    }

    private class Cropper {

        private void makeDefault() {
            if (mNormalRotateBitmap == null) {
                return;
            }

            HighlightView hv = new HighlightView(mCropImageView);
            final int width = mCropImageView.getWidth();
            final int height = mCropImageView.getHeight();

            Rect imageRect = new Rect(0, 0, width, height);

            // Make the default size about 4/5 of the width or height
            int cropWidth = Math.min(width, height);// * 4 / 5;
            @SuppressWarnings("SuspiciousNameCombination")
            int cropHeight = cropWidth;

            if (mAspectX != 0 && mAspectY != 0) {
                if (mAspectX > mAspectY) {
                    cropHeight = cropWidth * mAspectY / mAspectX;
                } else {
                    cropWidth = cropHeight * mAspectX / mAspectY;
                }
            }

            int x = (width - cropWidth) / 2;
            int y = (height - cropHeight) / 2;
            RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
            hv.setup(mCropImageView.getUnrotatedMatrix(), imageRect, cropRect, mAspectX != 0 && mAspectY != 0);
            mCropImageView.addSquareHighlistView(hv);
        }

        public void crop() {
            mHandler.post(new Runnable() {
                public void run() {
                    makeDefault();
                    mCropImageView.invalidate();
                    if (mCropImageView.getSquareHighlistViewSize() == 1) {
                        mCropView = mCropImageView.getSquareHighlistView(0);
                        mCropView.setFocus(true);
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsSaving = false;
        mBtnArea.setVisibility(View.VISIBLE);
        mTabArea.setVisibility(View.GONE);
        mResetBtn.setVisibility(View.GONE);
        mCropImageView.setLayerType(View.LAYER_TYPE_NONE, null);
        mCropImageView.reset();
        reportShow();
    }

    private void clearImageView() {
        mCropImageView.clear();
        if (mNormalRotateBitmap != null) {
            mNormalRotateBitmap.recycle();
        }
        System.gc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearImageView();
        if (mNormalRotateBitmap != null) {
            mNormalRotateBitmap.recycle();
        }

        if (mGetCropBitmapTask != null) {
            mGetCropBitmapTask.cancel(true);
        }
    }

    @Override
    public boolean onSearchRequested() {
        return false;
    }

    public boolean isSaving() {
        return mIsSaving;
    }

    private void setResultException(Throwable throwable) {
        setResult(Crop.RESULT_ERROR, new Intent().putExtra(Crop.Extra.ERROR, throwable));
    }

    private Bitmap getScaledBitmap(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap srcBitmap = BitmapFactory.decodeFile(path, options);
        if (srcBitmap == null) {
            return null;
        }
        return getScaledBitmap(srcBitmap);
    }

    private Bitmap getScaledBitmap(Bitmap srcBitmap) {
        // compute bitmap Rect
        int srcBitmapWidth = srcBitmap.getWidth(); // a
        int srcBitmapHeight = srcBitmap.getHeight(); // b
        int imageSize = getResources().getDimensionPixelSize(R.dimen.edit_image_size); // i
        int imagePadding = 0;//getResources().getDimensionPixelOffset(R.dimen.edit_image_padding);
        int destLeft = 0;
        int destTop = 0;
        int destWidth = imageSize;
        int destHeight = imageSize;
        if (srcBitmapWidth > srcBitmapHeight && srcBitmapWidth > imageSize) { // a>b>i || a>i>b
            destWidth = imageSize;
            destHeight = (int) ((float)(srcBitmapHeight*destWidth) / (float)srcBitmapWidth);
            destTop = (imageSize - destHeight)/2;
        } else if (srcBitmapHeight > srcBitmapWidth && srcBitmapHeight > imageSize) { // i<a<b || a<i<b
            destHeight = imageSize;
            destWidth = (int) ((float)(srcBitmapWidth*destHeight) / (float)srcBitmapHeight);
            destLeft = (imageSize - destWidth)/2;
        } else if (srcBitmapHeight < srcBitmapWidth && srcBitmapWidth < imageSize) { // i>a>b
            destWidth = imageSize - imagePadding * 2;
            destHeight = (int) ((float)(srcBitmapHeight*destWidth) / (float)srcBitmapWidth);
            destLeft = imagePadding;
            destTop = (imageSize - destHeight)/2;
        } else if (srcBitmapWidth < srcBitmapHeight && srcBitmapHeight < imageSize) { // a<b<i
            destHeight = imageSize - imagePadding * 2;
            destWidth = (int) ((float)(srcBitmapWidth*destHeight) / (float)srcBitmapHeight);
            destTop = imagePadding;
            destLeft = (imageSize - destWidth)/2;
        }

        Rect srcRect = new Rect(0, 0, srcBitmapWidth, srcBitmapHeight);
        Rect destRect = new Rect(destLeft, destTop, destLeft + destWidth, destTop + destHeight);
        Bitmap scaledBitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
        Canvas destCanvas = new Canvas(scaledBitmap);
        destCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        destCanvas.drawBitmap(srcBitmap, srcRect, destRect, new Paint());
        return scaledBitmap;
    }

    private void setCropMode(CropImageView.Mode mode) {
        if (mode == CropImageView.Mode.Square) {
            mButtonSquareCrop.setImageResource(R.drawable.sc_ic_crop_p);
            mButtonManualCrop.setImageResource(R.drawable.sc_ic_manual_n);
            mButtonHumanBodyCrop.setImageResource(R.drawable.sc_ic_body_n);
        } else if (mode == CropImageView.Mode.HandFree) {
            mButtonSquareCrop.setImageResource(R.drawable.sc_ic_crop_n);
            mButtonManualCrop.setImageResource(R.drawable.sc_ic_manual_p);
            mButtonHumanBodyCrop.setImageResource(R.drawable.sc_ic_body_n);
        } else {
            mButtonSquareCrop.setImageResource(R.drawable.sc_ic_crop_n);
            mButtonManualCrop.setImageResource(R.drawable.sc_ic_manual_n);
            mButtonHumanBodyCrop.setImageResource(R.drawable.sc_ic_body_p);
        }
        mCropImageView.setMode(mode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_IMAGE) {
            if (resultCode == RESULT_CODE_FINISH_EDIT_IMAGE) {
                if (data != null) {
                    setResult(RESULT_CODE_FINISH_EDIT_IMAGE, data);
                    finish();
                }
            }
        }
    }

    private interface GetCropBitmapTaskCallback {
        void onPreGetCroppedBitmap();
        Bitmap getCroppedBitmap();
        void onFinishCropped(Bitmap bitmap);
    }

    private static class GetCropBitmapTask extends AsyncTask<Void, Void, Bitmap> {
        private GetCropBitmapTaskCallback callback;

        private GetCropBitmapTask(GetCropBitmapTaskCallback callback) {
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (callback != null) {
                callback.onPreGetCroppedBitmap();
            }
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            if (callback != null) {
                return callback.getCroppedBitmap();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (callback != null) {
                callback.onFinishCropped(bitmap);
            }
        }
    }

    private void showContourCropTipIfNeed() {
        if (!WAStickerManager.getInstance().isShowContourCropTip(CropImageActivity.this)) {
            return;
        }

//        ContourCropTipDialogFragment fragment = ContourCropTipDialogFragment.newInstance();
//        FragmentUtil.showDialogFragment(getSupportFragmentManager(), fragment, ContourCropTipDialogFragment.DIALOG_FRAGMENT);
//
//        WAStickerManager.getInstance().setShowContourCropTip(CropImageActivity.this, false);
    }

    private void reportCropType(String type) {
        Tracker.Extra extra = TrackerCompat.getExtra(this);
        extra.put("crop_type", type);
        TrackerCompat.getTracker().logEventRealTime("crop_image","enter_next","click",extra);
    }

    private void reportShow() {
        Tracker.Extra extra = TrackerCompat.getExtra(this);
        TrackerCompat.getTracker().logEventRealTime("crop_image","show","item",extra);
    }
}
