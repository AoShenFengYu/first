package com.qisiemoji.apksticker.whatsapp.gifpick;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES10;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.giphy.sdk.core.models.Media;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.recyclerview.SpacesItemDecoration;
import com.qisiemoji.apksticker.util.GifDecoder;
import com.qisiemoji.apksticker.whatsapp.crop.CropUtil;
import com.qisiemoji.apksticker.whatsapp.crop.EditableCropOperation;
import com.qisiemoji.apksticker.whatsapp.crop.GifPickImageView;
import com.qisiemoji.apksticker.whatsapp.crop.HighlightView;
import com.qisiemoji.apksticker.whatsapp.crop.RotateBitmap;
import com.qisiemoji.apksticker.whatsapp.manager.EditImageManager;
import com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.EXTRA_SELECTED_LIST;
import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.RESULT_CODE_FINISH_EDIT_IMAGE;

public class GifPickActivity extends AppCompatActivity implements View.OnClickListener, EditableCropOperation.EditableCropOperationListener {
    public static final int GIF_PICK_SPAN_COUNT = 3;

    public static final int USER_DOWNLOAD_SUCCESS = 100;

    public static final int USER_DOWNLOAD_FAIL = 102;

    public static final int MSG_PICK  = 1000;

    private static final int SIZE_DEFAULT = 2048;
    private static final int SIZE_LIMIT = 4096;

    private RecyclerView recyclerView;

    private RelativeLayout funcLayout;

    private ImageView img, recoverImg, addTextImg, addRandomText;

    private GifPickImageView gifPickImg;

    private RotateBitmap mNormalRotateBitmap;

    private HighlightView mCropView;

    private int mExifRotation = 0;

    private TextView addToGroup;

    private GifPickAdapter adapter;

    private Handler handler;

    private boolean mIsSaving;

    private GetCropBitmapTask mGetCropBitmapTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gifpick);
        handler = new MyHandler(this);
        recyclerView = findViewById(R.id.gifpick_recycleview);
        img = findViewById(R.id.gifpick_img);
        recoverImg = findViewById(R.id.gifpick_recovery);
        addTextImg = findViewById(R.id.gifpick_add_text);
        addRandomText = findViewById(R.id.gifpick_random_text);
        addToGroup = findViewById(R.id.gifpick_add);
        funcLayout = findViewById(R.id.gifpick_func_layout);
        gifPickImg = findViewById(R.id.gifpick_crop_img);
        gifPickImg.mContext = this;

        addToGroup.setOnClickListener(this);
        recoverImg.setOnClickListener(this);
        addTextImg.setOnClickListener(this);
        addRandomText.setOnClickListener(this);

        adapter = new GifPickAdapter(this,handler);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, GIF_PICK_SPAN_COUNT);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new SpacesItemDecoration(4));
        recyclerView.setAdapter(adapter);
    }

    public boolean isSaving() {
        return mIsSaving;
    }

    private void clickConfirm() {
        if (adapter.obtainBitmap() == null) {
            Toast.makeText(this, "Sorry,pick one please.", Toast.LENGTH_LONG).show();
            return;
        }

        gifPickImg.onPreGetCroppedBitmap();
        WAStickerManager.SaveTempImageFileTask task = new WAStickerManager
                .SaveTempImageFileTask(GifPickActivity.this, gifPickImg.getCroppedBitmap()
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
    protected void onResume() {
        super.onResume();
        mIsSaving = false;
        Media media = getIntent().getParcelableExtra("media");
        if (media == null) {
            return;
        }
        Thread thread = new GifPickDownloadRunnable(this, handler, media);
        thread.start();

        String url = "http://media2.giphy.com/media/" + media.getId() + "/200.gif";
        Glide.with(this).load(url).into(img);
//        Glide.with(this).load("https://media3.giphy.com/media/GCvktC0KFy9l6/200w.gif").into(img);

    }

    private void save(){
        mIsSaving = true;
//        setProgressBarVisibility(View.VISIBLE);
        mGetCropBitmapTask = new GetCropBitmapTask(new GetCropBitmapTaskCallback() {
            @Override
            public void onPreGetCroppedBitmap() {
                gifPickImg.onPreGetCroppedBitmap();
            }

            @Override
            public Bitmap getCroppedBitmap() {
                return gifPickImg.getCroppedBitmap();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNormalRotateBitmap != null) {
            mNormalRotateBitmap.recycle();
        }
    }

    private void updateUI(File file) {
        if (file == null) {
            return;
        }
        try {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            GifDecoder gd = new GifDecoder();
            int result = gd.read(fis);
            if (result != 0) {
                return;
            }
            LinkedList<GifPickItem> list = new LinkedList<>();
            int mod = gd.getFrameCount() / 20;
            if (mod <= 0) {
                mod = 1;
            }
            for (int i = 0; i < gd.getFrameCount(); i++) {
                if (i % mod == 0) {
                    // FIXME for crop view, we sync the bitmap size as 1:1
                    Bitmap bitmap = Bitmap.createScaledBitmap(gd.getFrame(i), 600, 600, false);
                    list.add(new GifPickItem((bitmap)));
//                    list.add(new GifPickItem((gd.getFrame(i))));
                }
            }
            adapter.setDataSet(list);
            adapter.notifyDataSetChanged();
        } catch (Exception e) {

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.gifpick_recovery:
                break;
            case R.id.gifpick_add_text:
                break;
            case R.id.gifpick_random_text:
                break;
            case R.id.gifpick_add:
                clickConfirm();
                break;
            default:
                break;
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

    private int calculateBitmapSampleSize(Bitmap bitmap) throws IOException {
        int maxSize = getMaxImageSize();
        int sampleSize = 1;
        while (bitmap.getHeight() / sampleSize > maxSize || bitmap.getWidth() / sampleSize > maxSize) {
            sampleSize = sampleSize << 1;
        }
        return sampleSize;
    }

    private void startCrop() {
        if (isFinishing()) {
            return;
        }
        Bitmap bitmap = adapter.obtainBitmap();
        if (bitmap == null) {
            return;
        }

        img.setVisibility(View.INVISIBLE);
        gifPickImg.setVisibility(View.VISIBLE);

        mNormalRotateBitmap = new RotateBitmap(bitmap, mExifRotation);
        gifPickImg.setInputInfos(0, 0, mExifRotation);
        gifPickImg.setImageRotateBitmapResetBase(mNormalRotateBitmap, true);

        gifPickImg.setEditableCropOperationListener(this);
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
                        new GifPickActivity.Cropper().crop();
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

    @Override
    public void onEditableOperationStartDoCrop() {

    }

    @Override
    public void onEditableOperationFinishDoCrop() {

    }

    @Override
    public void onUpdateActionBackNextStates(boolean canBack, boolean canNext) {

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
        WeakReference<GifPickActivity> thisLayout;

        File file = null;

        MyHandler(GifPickActivity layout) {
            thisLayout = new WeakReference<>(layout);
        }

        @Override
        public void handleMessage(Message msg) {
            final GifPickActivity theLayout = thisLayout.get();
            if (theLayout == null) {
                return;
            }

            switch (msg.what) {
                case USER_DOWNLOAD_SUCCESS:
                    file = (File) msg.obj;
                    theLayout.updateUI(file);
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
}
