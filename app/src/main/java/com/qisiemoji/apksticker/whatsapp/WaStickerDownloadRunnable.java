package com.qisiemoji.apksticker.whatsapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.qisiemoji.apksticker.util.FileUtils2;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * webp文件下载线程，但不負責轉web，轉web交由PublishStickerPackTask
 */
public class WaStickerDownloadRunnable extends Thread {

    private final int ERROR_LIMIT = 10;
    public static final int WA_WEB_IMAGE_HEIGHT = 512;
    public static final int WA_WEB_IMAGE_WIDTH = 512;
    private static final long MAX_STICKER_SIZE = 100000;
    private static final long MAX_ICON_SIZE = 50000;
    public static final String WEBP = ".webp";
    public static final String TRAY_IMAGE_NAME = "file";
    public static final String STICKER_IMAGE_NAME = "file_";

    private Context context;
    private StickerPack stickerPack;
    private Handler handler;

    public WaStickerDownloadRunnable(Context context, Handler handler, StickerPack stickerPack) {
        this.context = context;
        this.stickerPack = stickerPack;
        this.handler = handler;
    }

    private boolean isCancel;

    public void cancel() {
        isCancel = true;
    }

    @Override
    public void run() {
        if (stickerPack == null) {
            onError();
            return;
        }
        if (stickerPack.stickers == null) {
            onError();
            return;
        }
        File file = null;
        int totleSize = computeTotalSize(stickerPack);
        int count = 0, errorTimes = 0;
        stickerPack.totle = totleSize;
        String path = FileUtils2.getFileDir(context, StickerContentProvider.STICKERS_FILE
                + File.separator + stickerPack.identifier) + File.separator;
        //下载icon(tray-image)
        while (errorTimes < ERROR_LIMIT) {
            // 停用stickerPack.trayImageFile，統一固定所有stickerPack.trayImageFile
            File waFile = new File(path + TRAY_IMAGE_NAME + WEBP);
            if (waFile.exists()) {
                waFile.delete();
            }
            //"https://cdn.kikakeyboard.com/navigation/sticker/sticker_store/MikaChristmas/assets/1/file_1.webp"
            file = downloadFile(stickerPack.trayImageFile);
            if (file == null) {
                errorTimes++;
            } else {
                boolean isValid = isValid(stickerPack.trayImageFile, file, MAX_ICON_SIZE);
                if (!isValid) {
                    generateWebp(file.getPath(), waFile.getPath(), MAX_ICON_SIZE);
                } else {
                    FileUtils2.copy(file, waFile);
                }

                if (waFile.exists()) {
                    count++;
                    downloadStatus(totleSize, count);
                    break;
                } else {
                    errorTimes++;
                }
            }
        }
        if (isCancel) {
            return;
        }
        //下载sticker
        for (int i = 0; i < totleSize - 1; i++) {
            Sticker sticker = stickerPack.stickers.get(i);

            if (isCancel) {
                return;
            }

            // 停用sticker.imageFileName，統一固定所有sticker.imageFileName
            File waFile = new File(path + STICKER_IMAGE_NAME + i + WEBP);
            if (waFile.exists()) {
                waFile.delete();
            }

            while (errorTimes < ERROR_LIMIT) {
                file = downloadFile(sticker.imageFileUrl);
                if (file == null) {
                    errorTimes++;
                } else {
                    boolean isValid = isValid(sticker.imageFileUrl, file, MAX_STICKER_SIZE);
                    if (!isValid) {
                        generateWebp(file.getPath(), waFile.getPath(), MAX_STICKER_SIZE);
                    } else {
                        FileUtils2.copy(file, waFile);
                    }

                    if (waFile.exists()) {
                        count++;
                        downloadStatus(totleSize, count);
                        break;
                    } else {
                        errorTimes++;
                    }
                }
            }
        }
        if (isCancel) {
            return;
        }

        //下载成功发送成功的消息
        if (errorTimes < ERROR_LIMIT && count >= totleSize) {
            Message msg = handler.obtainMessage();
            msg.obj = stickerPack;
            msg.what = StickerPackListActivity.USER_DOWNLOAD_SUCCESS;
            msg.sendToTarget();
        } else {
            onError();
        }
    }

    private int computeTotalSize(StickerPack stickerPack) {
        int result = 1;
        for (Sticker sticker : stickerPack.stickers) {
            if (!TextUtils.isEmpty(sticker.imageFileUrl)) {
                result++;
            }
        }
        return result;
    }

    private void onError() {
        Message msg = handler.obtainMessage();
        msg.what = StickerPackListActivity.USER_DOWNLOAD_FAIL;
        msg.obj = stickerPack;
        msg.sendToTarget();
    }

    /**
     * 通知界面更新下载进度
     *
     * @param totle
     * @param count
     */
    private void downloadStatus(int totle, int count) {
        stickerPack.count = count;
        stickerPack.totle = totle;
        Message msg = handler.obtainMessage();
        msg.what = StickerPackListActivity.USER_DOWNLOADING;
        msg.obj = stickerPack;
        msg.sendToTarget();
    }

    /**
     * 下载图片文件
     *
     * @param url
     * @return
     */
    private File downloadFile(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        File  file = new File(url);
        if (file.exists()) {
            // 本地端的圖片
            return file;
        }

        try {
            // 網路上的圖片
            file = Glide.with(context)
                    .load(url)
                    .downloadOnly(WA_WEB_IMAGE_WIDTH, WA_WEB_IMAGE_HEIGHT)
                    .get();
        } catch (Exception e) {

        }
        return file;
    }

    private boolean isValid(String url, File file, long maxFileSize) {
        if (!file.exists()) {
            return false;
        }

        if (TextUtils.isEmpty(url)) {
            return false;
        }

        if (!url.contains(WEBP)) {
            return false;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);

        if (options.outWidth != WA_WEB_IMAGE_HEIGHT || options.outHeight != WA_WEB_IMAGE_WIDTH) {
            return false;
        }

        if (file.length() > maxFileSize) {
            return false;
        }

        return true;
    }


    /**
     * 將圖片轉成 Webp
     *
     * @param srcFilePath
     * @param destFilePath
     * @param imageMax
     * @return streamLength
     */
    private long generateWebp(String srcFilePath, String destFilePath, long imageMax) {
        // orignal bmp
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap originalBmp = BitmapFactory.decodeFile(srcFilePath, options);

        // center inside bmp
        Bitmap targetBmp = getCenterInsideBitmap(originalBmp, WA_WEB_IMAGE_WIDTH, WA_WEB_IMAGE_HEIGHT);
        if(targetBmp == null){
            return 0;
        }
        // check file size
        // from 100 to check, subtract 5 every time
        int compressQuality = 105;
        long streamLength = imageMax;
        while (streamLength >= imageMax && compressQuality >= 0) {
            compressQuality -= 5;
            ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
            targetBmp.compress(Bitmap.CompressFormat.WEBP, compressQuality, bmpStream);
            byte[] bmpPicByteArray = bmpStream.toByteArray();
            streamLength = bmpPicByteArray.length;
        }

        try {
            // save webp to specific location
            File waFile = new File(destFilePath);
            FileOutputStream outputStreamWebp = new FileOutputStream(waFile);
            targetBmp.compress(Bitmap.CompressFormat.WEBP, compressQuality, outputStreamWebp);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return streamLength;
    }

    private Bitmap getCenterInsideBitmap(Bitmap inputBmp, int targetWidth, int targetHeight) {
        if(inputBmp == null){
            return null;
        }
        // scale bmp in 512*512 region
        float scale = Math.min(1f * targetWidth / inputBmp.getWidth(), 1f * targetHeight / inputBmp.getHeight());
        Bitmap scaleBmp = getScaleBitmap(inputBmp, scale);
        Bitmap outputBmp = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBmp);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Matrix matrix = new Matrix();
        int sbw = scaleBmp.getWidth();
        int sbh = scaleBmp.getHeight();
        Point targetCenter = new Point(targetWidth / 2, targetHeight / 2);
        Point scaleBmpCenter = new Point(sbw / 2, sbh / 2);
        matrix.postScale(1f, 1f, targetCenter.x, targetCenter.y);
        matrix.postTranslate(targetCenter.x - scaleBmpCenter.x, targetCenter.y - scaleBmpCenter.y);
        canvas.drawBitmap(scaleBmp, matrix, paint);
        return outputBmp;
    }

    private Bitmap getScaleBitmap(Bitmap origin, float scale) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        return newBM;
    }
}
