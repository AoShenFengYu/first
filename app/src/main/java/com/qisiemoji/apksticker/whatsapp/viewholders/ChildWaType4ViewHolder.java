package com.qisiemoji.apksticker.whatsapp.viewholders;

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
import com.qisiemoji.apksticker.viewholders.BaseChildViewHolder;
import com.qisiemoji.apksticker.whatsapp.StickerPack;
import com.qisiemoji.apksticker.whatsapp.StickerPackDetailsActivity;

public class ChildWaType4ViewHolder extends BaseWaChildViewHolder<StickerPack>  implements View.OnClickListener{

    private Context context;
    private RelativeLayout layout;
    private ImageView img,installImg,adImg;
    private ProgressBar progressBar;
    private TextView title;
    private StickerPack item;

    public ChildWaType4ViewHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;
        layout = itemView.findViewById(R.id.child1_layout);
        img =  itemView.findViewById(R.id.child1_img);
        progressBar = itemView.findViewById(R.id.child1_loading);
        title = itemView.findViewById(R.id.child1_title);
        installImg = itemView.findViewById(R.id.child1_install);
        adImg = itemView.findViewById(R.id.child1_ad);

        layout.setOnClickListener(this);
    }

    @Override
    public void fillView(final StickerPack entity) {
        item = entity;
        statistic(item,"show");
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), StickerPackDetailsActivity.class);
                intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, true);
                intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, entity);
                view.getContext().startActivity(intent);
            }
        });
        title.setText(item.name);
        installImg.setVisibility(View.GONE);
        adImg.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        Glide.with(context)
                .load(item.trayImageUrl)
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
                        installImg.setVisibility(View.GONE);
//                        adImg.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(img);
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.child1_layout:
//                statistic(item,"click");
//                if(PackageUtil.isPackageInstalled(context,item.getPackageName())){
////                    context.startActivity(context.getPackageManager()
////                            .getLaunchIntentForPackage(item.getPackageName()));
//                    PackageManager packageManager = context.getPackageManager();
//                    if (checkPackInfo(item.getPackageName())) {
//                        Intent intent = packageManager.getLaunchIntentForPackage(item.getPackageName());
//                        if(intent == null){
//                            return;
//                        }
//                        context.startActivity(intent);
//                    }
//                }else{
//                    GooglePlay.startGooglePlayOrByBrowser(context,item.getPackageName());
//                }
//                break;
//        }
    }

    private void statistic(StickerPack child,String item){
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
