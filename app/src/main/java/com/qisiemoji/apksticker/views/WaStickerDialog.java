package com.qisiemoji.apksticker.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.whatsapp.StickerPack;

public class WaStickerDialog extends Dialog{
    public static final int MAX_WA_STICKER = 30;
    public static final int MIN_WA_STICKER = 3;

    private Context context;

    private ImageView iconView;

    private ProgressBar progressBar;

    private TextView title;

    private StickerPack stickerPack;

    public WaStickerDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    public WaStickerDialog(@NonNull Context context, int themeResId, StickerPack pack) {
        super(context, themeResId);
        this.context = context;
        this.stickerPack = pack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.dialog_download_sticker_progress, null);
        setContentView(rootView);
        iconView = rootView.findViewById(R.id.dialog_download_sticker_progress_icon);
        progressBar = rootView.findViewById(R.id.dialog_download_sticker_progress_bar);
        title = rootView.findViewById(R.id.dialog_download_sticker_progress_title);
        Glide.with(context)
                .load(stickerPack.trayImageUrl)
                .into(iconView);
        title.setText(TextUtils.isEmpty(stickerPack.name) ? "" : stickerPack.name);
        int max = stickerPack.stickers.size() > MAX_WA_STICKER ? MAX_WA_STICKER + 1 : stickerPack.stickers.size() + 1;
        progressBar.setMax(max);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 0.8); // 高度设置为屏幕的0.6
        dialogWindow.setAttributes(lp);
    }

//    private void statistic(String item){
//        Tracker.Extra extra =TrackerCompat.getExtra(context);
//        extra.put("packageName",context.getPackageName());
//        TrackerCompat.getTracker().logEventRealTime("sticker_dialog1",item,String.valueOf(kikaState),extra);
//    }

    public void updateProgress(int totle,int count){
        if(progressBar == null){
            return;
        }
        progressBar.setMax(totle);
        progressBar.setProgress(count);
    }
}
