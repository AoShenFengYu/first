package com.qisiemoji.apksticker.viewholders;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.domain.ChildItem;
import com.qisiemoji.apksticker.util.GooglePlay;
import com.qisiemoji.apksticker.util.PackageUtil;

public class ChildType2ViewHolder extends BaseChildViewHolder<ChildItem>  implements View.OnClickListener{

    private Context context;
    private RelativeLayout layout;
    private ImageView img,installImg,adImg;
    private ProgressBar progressBar;
    private ChildItem item;

    public ChildType2ViewHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;
        layout = itemView.findViewById(R.id.child1_layout);
        img =  itemView.findViewById(R.id.child1_img);
        progressBar = itemView.findViewById(R.id.child1_loading);
        installImg = itemView.findViewById(R.id.child1_install);
        adImg = itemView.findViewById(R.id.child1_ad);

        layout.setOnClickListener(this);
    }

    @Override
    public void fillView(ChildItem entity) {
        item = entity;
        statistic(item,"show");
        installImg.setVisibility(View.GONE);
        adImg.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        Glide.with(context)
                .load(item.getIconBig())
                .listener(new RequestListener<String, GlideDrawable>() {

                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        img.setBackgroundColor(0xd6d6d6);
                        progressBar.setVisibility(View.GONE);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        if(PackageUtil.isPackageInstalled(context,item.getPackageName())){
                            installImg.setVisibility(View.VISIBLE);
                        }else{
                            installImg.setVisibility(View.GONE);
                        }
                        adImg.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(img);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.child1_layout:
                statistic(item,"click");
                if(PackageUtil.isPackageInstalled(context,item.getPackageName())){
//                    context.startActivity(context.getPackageManager()
//                            .getLaunchIntentForPackage(item.getPackageName()));
                    PackageManager packageManager = context.getPackageManager();
                    if (checkPackInfo(item.getPackageName())) {
                        Intent intent = packageManager.getLaunchIntentForPackage(item.getPackageName());
                        if(intent == null){
                            return;
                        }
                        context.startActivity(intent);
                    }
                }else{
                    GooglePlay.startGooglePlayOrByBrowser(context,item.getPackageName());
                }
                break;
        }
    }

    private void statistic(ChildItem child,String item){
//        Tracker.Extra extra = TrackerCompat.getExtra(context);
//        extra.put("packageName",child.getPackageName());
//        extra.put("key",String.valueOf(child.getKey()));
//        extra.put("installed",PackageUtil.isPackageInstalled(context,child.getPackageName())?"1":"0");
//        TrackerCompat.getTracker().logEventRealTime("sticker_main_child",item,"type",extra);
    }

    /**
     * 检查包是否存在
     *
     * @param packname
     * @return
     */
    private boolean checkPackInfo(String packname) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }

}
