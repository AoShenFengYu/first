package com.qisiemoji.apksticker.whatsapp.gifpick;

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

import com.bumptech.glide.Glide;
import com.giphy.sdk.core.models.Media;
import com.qisiemoji.apksticker.util.FileUtils2;
import com.qisiemoji.apksticker.whatsapp.Sticker;
import com.qisiemoji.apksticker.whatsapp.StickerContentProvider;
import com.qisiemoji.apksticker.whatsapp.StickerPack;
import com.qisiemoji.apksticker.whatsapp.StickerPackListActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * webp文件下载线程
 */
public class GifPickDownloadRunnable extends Thread {

    private Context context;
    private Media stickerPack;
    private Handler handler;

    public GifPickDownloadRunnable(Context context, Handler handler, Media stickerPack){
        this.context = context;
        this.stickerPack = stickerPack;
        this.handler = handler;
    }

    @Override
    public void run() {
//        if(stickerPack == null){
//            return;
//        }
//        if(stickerPack.getUrl() == null){
//            return;
//        }
        File file = downloadFile("https://media3.giphy.com/media/GCvktC0KFy9l6/200w.gif");

        if(file == null){
            Message msg = handler.obtainMessage();
            msg.what = GifPickActivity.USER_DOWNLOAD_FAIL;
            msg.sendToTarget();
        }

        Message msg = handler.obtainMessage();
        msg.obj = file;
        msg.what = GifPickActivity.USER_DOWNLOAD_SUCCESS;
        msg.sendToTarget();
    }

    /**
     * 下载图片文件
     * @param url
     * @return
     */
    private File downloadFile(String url){
        if(TextUtils.isEmpty(url)){
            return null;
        }
        File file = null;
        try{
            file = Glide.with(context)
                    .load(url)
                    .downloadOnly(512,512)
                    .get();
        }catch (Exception e){

        }
        return file;
    }

}
