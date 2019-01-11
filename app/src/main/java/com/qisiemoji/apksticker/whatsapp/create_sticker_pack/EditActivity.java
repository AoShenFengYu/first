package com.qisiemoji.apksticker.whatsapp.create_sticker_pack;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES10;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.whatsapp.crop.CropUtil;
import com.qisiemoji.apksticker.whatsapp.crop.GifPickImageView;
import com.qisiemoji.apksticker.whatsapp.crop.HighlightView;
import com.qisiemoji.apksticker.whatsapp.crop.RotateBitmap;
import com.qisiemoji.apksticker.whatsapp.edit.EditImageLayout;
import com.qisiemoji.apksticker.whatsapp.edit.widget.CircleColorImageView;
import com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.EXTRA_SELECTED_LIST;
import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.RESULT_CODE_FINISH_EDIT_IMAGE;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int USER_DOWNLOAD_SUCCESS = 100;

    public static final int USER_DOWNLOAD_FAIL = 102;

    public static final int MSG_PICK  = 1000;

    private static final int SIZE_DEFAULT = 2048;
    private static final int SIZE_LIMIT = 4096;

    private LinearLayout funcLayout;

    private RelativeLayout editContent;

    private ImageView recoverImg, addTextImg, addRandomText;

    private GifPickImageView gifPickImg;

    private RotateBitmap mNormalRotateBitmap;

    private HighlightView mCropView;

    private int mExifRotation = 0;

    private TextView addToGroup;

    private Handler handler;

    private boolean mIsSaving;

    private GetCropBitmapTask mGetCropBitmapTask;

    //text part
    private FrameLayout editImageContainer;
    private Bitmap editImageBaseBitmap;
    private EditImageLayout editImageLayout;
    private EditText enterText;

    private String hadledUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        handler = new MyHandler(this);
        recoverImg = findViewById(R.id.gifpick_recovery);
        addTextImg = findViewById(R.id.gifpick_add_text);
        addRandomText = findViewById(R.id.gifpick_random_text);
        addToGroup = findViewById(R.id.gifpick_add);
        funcLayout = findViewById(R.id.gifpick_func_layout);
        editContent = findViewById(R.id.edit_content);
        gifPickImg = findViewById(R.id.gifpick_crop_img);
        gifPickImg.mContext = this;

        addToGroup.setOnClickListener(this);
        recoverImg.setOnClickListener(this);
        addTextImg.setOnClickListener(this);
        addRandomText.setOnClickListener(this);

        hadledUrl = getIntent().getStringExtra("selected_url");

        setupEditContents();

        startCrop();
    }

    private void clickConfirm() {
        Bitmap bitmap;
        if (editImageBaseBitmap != null) {
            bitmap = editImageBaseBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(editImageLayout.getOperationBitmap(), 0, 0, new Paint());
        } else {
            gifPickImg.onPreGetCroppedBitmap();
            bitmap = gifPickImg.getCroppedBitmap();
        }
        gifPickImg.onPreGetCroppedBitmap();
        WAStickerManager.SaveTempImageFileTask task = new WAStickerManager
                .SaveTempImageFileTask(EditActivity.this, bitmap
                , new WAStickerManager.SaveTempImageFileCallback() {
            @Override
            public void onFinishSaved(String path) {
                Intent intent = new Intent();
                ArrayList<String> urls = new ArrayList<>();
                urls.add(path);
                intent.putStringArrayListExtra(EXTRA_SELECTED_LIST, urls);
                setResult(RESULT_CODE_FINISH_EDIT_IMAGE, intent);
                finish();
            }
        });
        task.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNormalRotateBitmap != null) {
            mNormalRotateBitmap.recycle();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.gifpick_recovery:
                editContent.setVisibility(View.GONE);
                break;
            case R.id.gifpick_add_text:
                doCropAndEnterAddTextMode(null);
                break;
            case R.id.gifpick_random_text:
                // TODO get random text
                String randomText = "Random";
                doCropAndEnterAddTextMode(randomText);
                break;
            case R.id.gifpick_add:
                clickConfirm();
                break;
            default:
                break;
        }
    }

    private void doCropAndEnterAddTextMode(final String defaultText) {
        editContent.setVisibility(View.VISIBLE);
        editImageContainer.setVisibility(View.VISIBLE);
        gifPickImg.onPreGetCroppedBitmap();
        Bitmap croppedBitmap = gifPickImg.getCroppedBitmap();
        if (croppedBitmap != null) {
            editImageBaseBitmap = Bitmap.createBitmap(croppedBitmap);
            ImageView editImageBase = findViewById(R.id.edit_image_view_base);
            editImageBase.setImageBitmap(editImageBaseBitmap);
        }
        editImageLayout.post(new Runnable() {
            @Override
            public void run() {
                editImageLayout.createTextToolText();
                editImageLayout.setTextToolColor(Color.WHITE);
                enterText.setText(defaultText);
            }
        });
        enterText.requestFocus();
        showSoftInput(enterText);
    }

    private void setupEditContents() {
        CircleColorImageView color1 = findViewById(R.id.color_1);
        color1.setColor(Color.WHITE);
        color1.setOnClickListener(new TextColorClickListener(Color.WHITE));
        CircleColorImageView color2 = findViewById(R.id.color_2);
        color2.setColor(Color.BLACK);
        color2.setOnClickListener(new TextColorClickListener(Color.BLACK));
        CircleColorImageView color3 = findViewById(R.id.color_3);
        color3.setColor(Color.RED);
        color3.setOnClickListener(new TextColorClickListener(Color.RED));
        CircleColorImageView color4 = findViewById(R.id.color_4);
        color4.setColor(Color.YELLOW);
        color4.setOnClickListener(new TextColorClickListener(Color.YELLOW));
        CircleColorImageView color5 = findViewById(R.id.color_5);
        color5.setColor(Color.GREEN);
        color5.setOnClickListener(new TextColorClickListener(Color.GREEN));
        CircleColorImageView color6 = findViewById(R.id.color_6);
        color6.setColor(Color.BLUE);
        color6.setOnClickListener(new TextColorClickListener(Color.BLUE));
        CircleColorImageView color7 = findViewById(R.id.color_7);
        color7.setColor(Color.MAGENTA);
        color7.setOnClickListener(new TextColorClickListener(Color.MAGENTA));

        ImageView finishAddText = findViewById(R.id.finish_add_text);
        finishAddText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editContent.setVisibility(View.GONE);
                hideSoftInput(enterText);
            }
        });

        editImageContainer = findViewById(R.id.edit_image_container);
        editImageLayout = findViewById(R.id.edit_image);
        editImageLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        enterText = findViewById(R.id.enter_text);
        enterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                editImageLayout.setTextToolText(text);
            }
        });
    }

    private class TextColorClickListener implements View.OnClickListener {
        int color;
        TextColorClickListener(int color) {
            this.color = color;
        }

        @Override
        public void onClick(View v) {
            editImageLayout.setTextToolColor(color);
        }
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
        Bitmap bitmap = BitmapFactory.decodeFile(hadledUrl);
        if (bitmap == null) {
            return;
        }

        gifPickImg.setVisibility(View.VISIBLE);

        mNormalRotateBitmap = new RotateBitmap(bitmap, mExifRotation);
        gifPickImg.setInputInfos(0, 0, mExifRotation);
        gifPickImg.setImageRotateBitmapResetBase(mNormalRotateBitmap, true);

        CropUtil.startBackgroundJob(this, null, getResources().getString(R.string.crop__wait),
                new Runnable() {
                    @Override
                    public void run() {
                        final CountDownLatch latch = new CountDownLatch(1);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (gifPickImg.getScale() == 1F) {
                                    gifPickImg.center();
                                }
                                latch.countDown();
                            }
                        });
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        new EditActivity.Cropper().crop();
                    }
                }, handler
        );
    }

    private class Cropper {

        private void makeDefault() {
            if (mNormalRotateBitmap == null) {
                return;
            }
            int mAspectX = 1, mAspectY = 1;

            HighlightView hv = new HighlightView(gifPickImg);
            final int width = gifPickImg.getWidth();
            final int height = gifPickImg.getHeight();

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
            hv.setup(gifPickImg.getUnrotatedMatrix(), imageRect, cropRect, mAspectX != 0 && mAspectY != 0);

            gifPickImg.addSquareHighlistView(hv);
        }

        public void crop() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    makeDefault();
                    gifPickImg.invalidate();
                    if (gifPickImg.getSquareHighlistViewSize() == 1) {
                        mCropView = gifPickImg.getSquareHighlistView(0);
                        mCropView.setFocus(true);
                    }
                }
            });
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

    private static class MyHandler extends Handler {
        WeakReference<EditActivity> thisLayout;

        File file = null;

        MyHandler(EditActivity layout) {
            thisLayout = new WeakReference<>(layout);
        }

        @Override
        public void handleMessage(Message msg) {
            final EditActivity theLayout = thisLayout.get();
            if (theLayout == null) {
                return;
            }

            switch (msg.what) {
                case USER_DOWNLOAD_SUCCESS:
                    file = (File) msg.obj;
                    break;
                case USER_DOWNLOAD_FAIL:
                    break;
                case MSG_PICK:
                    theLayout.startCrop();
                    break;
                default:
                    break;
            }
        }
    }

    private void showSoftInput(EditText editText) {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, 0);
        } catch (Exception e) {

        }
    }

    private void hideSoftInput(EditText editText) {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        } catch (Exception e) {

        }
    }
}
