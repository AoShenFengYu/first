package com.qisiemoji.apksticker.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.util.CommonUtil;
import com.qisiemoji.apksticker.util.GooglePlay;
import com.qisiemoji.apksticker.util.PackageUtil;
import com.qisiemoji.apksticker.util.StringUtil;

public class ActivateDialog extends Dialog implements View.OnClickListener{

    private Context context;

    private TextView title,btn;

    private ImageView closeImg;

    private int kikaState;

    public ActivateDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.dialog_kika, null);
        setContentView(rootView);
        statistic("show");
        kikaState = CommonUtil.kikaState(context);
        title = rootView.findViewById(R.id.dialog_title);
        btn = rootView.findViewById(R.id.dialog_btn);
        closeImg = rootView.findViewById(R.id.dialog_close);

        btn.setOnClickListener(this);
        closeImg.setOnClickListener(this);
        switch (kikaState){
            case 1:
                btn.setText("DOWNLOAD");
                break;
            default:
                btn.setText("ACTIVATE");
                break;
        }
        title.setText(context.getString(R.string.active_dialog,StringUtil.getStickerName(context)));

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 0.8); // 高度设置为屏幕的0.6
        dialogWindow.setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dialog_btn:
                statistic("click");
                switch (kikaState){
                    case 1:
                        GooglePlay.startGooglePlayOrByBrowserWithRef(context, PackageUtil.KIKA_PACKAGENAME,"stickerApk");
                        break;
                    default:
                        context.startActivity(context.getPackageManager()
                                .getLaunchIntentForPackage(PackageUtil.KIKA_PACKAGENAME));
                        break;
                }
                dismiss();
                break;
            case R.id.dialog_close:
                statistic("close");
                dismiss();
                break;
        }
    }

    private void statistic(String item){
//        Tracker.Extra extra =TrackerCompat.getExtra(context);
//        extra.put("packageName",context.getPackageName());
//        TrackerCompat.getTracker().logEventRealTime("sticker_dialog1",item,String.valueOf(kikaState),extra);
    }
}
