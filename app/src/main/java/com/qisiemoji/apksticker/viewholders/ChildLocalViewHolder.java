package com.qisiemoji.apksticker.viewholders;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.qisiemoji.apksticker.BuildConfig;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.domain.StickerItem;
import com.qisiemoji.apksticker.threads.PictureDownloadRunnable;
import com.qisiemoji.apksticker.views.ActivateDialog;

import java.io.File;
import java.lang.ref.WeakReference;

public class ChildLocalViewHolder extends BaseMainViewHolder<StickerItem> implements View.OnClickListener{

    public static final int USER_DOWNLOAD_SUCCESS = 100;
    public static final int USER_DOWNLOAD_FAIL = 101;

    public static final int FILE_COPY_LOADING = 1;
    public static final int FILE_COPY_FAIL = 2;
    public static final int FILE_COPY_SUCCESS = 3;

    private Context context;
    private Handler handler;
    private RelativeLayout layout;
    private ImageView img,lockImg;
    private ProgressBar progressBar;
    private StickerItem item;
    private File file;
    private int hasDownloadSucc;
    public ChildLocalViewHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;
        handler = new MyHandler(this);
        layout = itemView.findViewById(R.id.sticker_layout);
        img =  itemView.findViewById(R.id.sticker_img);
        lockImg = itemView.findViewById(R.id.sticker_lock);
        progressBar = itemView.findViewById(R.id.sticker_loading);

        layout.setOnClickListener(this);
    }

    @Override
    public void fillView(StickerItem entity) {
        item = entity;
        //gif图
        lockImg.setVisibility(View.GONE);
        if(isGifSupport()){
            saveFile();
        }else{
            progressBar.setVisibility(View.GONE);
            int resID = context.getResources()
                    .getIdentifier(entity.getUrl(), "drawable",context.getPackageName());
            if(resID <= 0){
                return;
            }
            img.setImageResource(resID);
            if(item.isLocked()){
                lockImg.setVisibility(View.VISIBLE);
            }else{
                lockImg.setVisibility(View.GONE);
            }
        }
    }

    private void updateUI(boolean succ){
        if(file == null || context == null){
            return;
        }
        if(!succ){
            img.setBackgroundColor(0xd6d6d6);
            progressBar.setVisibility(View.GONE);
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        Glide.with(context).load(file).listener(new RequestListener<File, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                img.setBackgroundColor(0xd6d6d6);
                progressBar.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                return false;
            }
        }).into(img);
        if(item.isLocked()){
            lockImg.setVisibility(View.VISIBLE);
        }else{
            lockImg.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sticker_layout:
                statistic("click",item.isLocked());
                if(item.isLocked()){
                    showDialog();
                    return;
                }
                //静态sticker
                if(!isGifSupport()){
                    saveFile();
                    return;
                }
                //gif图
                if(hasDownloadSucc == FILE_COPY_SUCCESS){
                    clickShare();
                    return;
                }
                if(hasDownloadSucc == FILE_COPY_FAIL){
                    Toast.makeText(context,"Please try again",Toast.LENGTH_LONG).show();
                    saveFile();
                    return;
                }
                if(hasDownloadSucc == FILE_COPY_LOADING){
                    Toast.makeText(context,"Please try again",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void showDialog(){
//        String str = FeatureConfig.getInstance().getString("sticker2_apkstyle","0");
//        if("0".equals(str)){
        final ActivateDialog dialog = new ActivateDialog(context,R.style.CustomDialog);
        dialog.show();
//        }else{
//            final ActivateDialog2 dialog = new ActivateDialog2(context,R.style.CustomDialog);
//            dialog.show();
//        }
    }

    public void clickShare(){
        if(file == null){
            Toast.makeText(context,"Please try again",Toast.LENGTH_LONG).show();
            return;
        }
        try{
            Intent intent = new Intent(Intent.ACTION_SEND);
            if (Build.VERSION.SDK_INT >= 24) {
                intent.putExtra(Intent.EXTRA_STREAM
                        , FileProvider.getUriForFile(context
                                , BuildConfig.APPLICATION_ID + ".provider.files", file));
            } else {
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType("image/gif");
            context.startActivity(intent);
        }catch (Exception e){
        }
    }

    public void saveFile(){
        if(hasDownloadSucc == FILE_COPY_LOADING){
            return;
        }
        hasDownloadSucc = FILE_COPY_LOADING;
        Thread thread = new Thread(new PictureDownloadRunnable(context,handler,item.getUrl()));
        thread.start();
    }

    private static class MyHandler extends Handler {
        WeakReference<ChildLocalViewHolder> thisLayout;

        MyHandler(ChildLocalViewHolder layout) {
            thisLayout = new WeakReference<>(layout);
        }

        @Override
        public void handleMessage(Message msg) {
            final ChildLocalViewHolder theLayout = thisLayout.get();
            if (theLayout == null) {
                return;
            }

            switch (msg.what) {
                case USER_DOWNLOAD_SUCCESS:
                    theLayout.hasDownloadSucc = FILE_COPY_SUCCESS;
                    theLayout.file = (File)msg.obj;
                    if(theLayout.isGifSupport()){
                        theLayout.updateUI(true);
                    }else{
                        theLayout.clickShare();
                    }
                    break;
                case USER_DOWNLOAD_FAIL:
                    theLayout.hasDownloadSucc = FILE_COPY_FAIL;
                    theLayout.updateUI(false);
                    break;
            }
        }
    }

    private void statistic(String item,boolean lock){
//        Tracker.Extra extra = TrackerCompat.getExtra(context);
//        extra.put("packageName",context.getPackageName());
//        extra.put("lockstate",lock?"lock":"unlock");
//        TrackerCompat.getTracker().logEventRealTime("sticker_local_child",item,"type",extra);
    }

    private boolean isGifSupport(){
        if(item == null){
            return false;
        }
        if(TextUtils.isEmpty(item.getUrl())){
            return false;
        }
        int resID = context.getResources()
                .getIdentifier(item.getUrl(), "raw",context.getPackageName());
        if(resID <= 0){
            return false;
        }
        return true;
    }
}
