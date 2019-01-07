package com.qisiemoji.apksticker.threads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.qisiemoji.apksticker.util.FileUtils2;
import com.qisiemoji.apksticker.util.MD5;
import com.qisiemoji.apksticker.util.StringUtil;
import com.qisiemoji.apksticker.viewholders.ChildLocalViewHolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PictureDownloadRunnable implements Runnable {
    private Context context;

    private Handler handler;

    private String url;

    public PictureDownloadRunnable(Context context, Handler handler, String url){
        this.context = context;
        this.handler = handler;
        this.url = url;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        try {
            deleteCacheFile(FileUtils2.getFileDir(context, "download"));
            File destFile = new File(FileUtils2.getFileDir(context, "download")
                    + "/" + MD5.getMD5(url) + "." + StringUtil.getPostfix(url)+".gif");

            boolean result = false;
            int resID = context.getResources()
                    .getIdentifier(url, "raw",context.getPackageName());
            if(resID > 0){
                result = rawToFile(url,destFile);
            }else{
                Bitmap bitmap = getBitmapByName(context,url);
                result = bitmapToFile(destFile.getAbsolutePath(),bitmap);
            }
            if(result){
                Message msg = handler.obtainMessage();
                msg.obj = destFile;
                msg.what = ChildLocalViewHolder.USER_DOWNLOAD_SUCCESS;
                msg.sendToTarget();
            }else{
                Message msg = handler.obtainMessage();
                msg.what = ChildLocalViewHolder.USER_DOWNLOAD_FAIL;
                msg.sendToTarget();
            }
        } catch (Exception ex) {
            Message msg = handler.obtainMessage();
            msg.what = ChildLocalViewHolder.USER_DOWNLOAD_FAIL;
            msg.sendToTarget();
        }
    }

    private void deleteCacheFile(File dirFile) {
        try {
            File[] e = dirFile.listFiles();
            if (e.length <= 12) {
                return;
            }

            ArrayList fileList = new ArrayList();
            File[] var7 = e;
            int var6 = e.length;

            File file;
            for (int var5 = 0; var5 < var6; ++var5) {
                file = var7[var5];
                fileList.add(file);
            }

            Collections.sort(fileList, new Comparator<File>() {
                public int compare(File file, File newFile) {
                    return file.lastModified() < newFile.lastModified() ? -1 : (file.lastModified() == newFile.lastModified() ? 0 : 1);
                }
            });

            do {
                file = (File) fileList.get(0);
                file.delete();
                fileList.remove(file);
            } while (fileList.size() > 0);
        } catch (Exception var8) {
            var8.printStackTrace();
        }
    }

    public Bitmap getBitmapByName(Context context, String name) {
        int resID = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        return BitmapFactory.decodeResource(context.getResources(), resID);
    }

    public boolean bitmapToFile(String path, Bitmap bitmap) {
        File f = new File(path);
        try {
            f.createNewFile();
            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();
            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            FileUtils2.closeQuietly(fos);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean assetsToFile(String name,File file){
        if(TextUtils.isEmpty(name) || file == null){
            return false;
        }
        if(file.exists()){
            return true;
        }
        InputStream is = null;
        FileOutputStream fos = null;
        boolean result = false;
        try{
            is = context.getAssets().open(name);
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int byteCount=0;
            while((byteCount=is.read(buffer))!=-1) {//循环从输入流读取 buffer字节
                fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
            }
            fos.flush();//刷新缓冲区
            result = true;
        }catch (Exception e){
            file.delete();
        }finally {
            FileUtils2.closeQuietly(is);
            FileUtils2.closeQuietly(fos);
        }
        return result;
    }

    public boolean rawToFile(String name,File file){
        if(TextUtils.isEmpty(name) || file == null){
            return false;
        }
        if(file.exists()){
            return true;
        }
        InputStream is = null;
        FileOutputStream fos = null;
        boolean result = false;
        try{
            int resID = context.getResources().getIdentifier(name, "raw", context.getPackageName());
            is = context.getResources().openRawResource(resID);
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int byteCount=0;
            while((byteCount=is.read(buffer))!=-1) {//循环从输入流读取 buffer字节
                fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
            }
            fos.flush();//刷新缓冲区
            result = true;
        }catch (Exception e){
            file.delete();
        }finally {
            FileUtils2.closeQuietly(is);
            FileUtils2.closeQuietly(fos);
        }
        return result;
    }
}
