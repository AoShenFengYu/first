package com.qisiemoji.apksticker.whatsapp.search;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.giphy.sdk.core.models.Media;
import com.qisiemoji.apksticker.R;

/**
 * Created by xm180319 on 2018/3/26.
 */

public class GifSearchItemViewHolder extends BaseSearchViewHolder {
    private RelativeLayout layout;
    private ImageView img;
    private ImageView favImg;
    private ImageView errorImg;
    private ProgressBar progressBar;
    private Context mContext;
    private boolean isFromFav;
    private boolean hasError;
    private long loadingTime;
    private Media info;

    public GifSearchItemViewHolder(View itemView, final Context mContext) {
        super(itemView);
        this.mContext = mContext;
        layout = (RelativeLayout) itemView.findViewById(R.id.gif_item_layout);
        img = (ImageView) itemView.findViewById(R.id.gif_item_img);
        favImg = (ImageView) itemView.findViewById(R.id.gif_favorite);
        errorImg = (ImageView) itemView.findViewById(R.id.gif_favorite_error);
        progressBar = (ProgressBar) itemView.findViewById(R.id.gif_holder_progress_bar);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });

        errorImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestImg();
            }
        });
    }

    @Override
    public void fillView(Media entity) {
        info = entity;
        if (entity == null) {
            return;
        }
        showNormalGif();
    }

    private void showNormalGif() {
        img.setVisibility(View.VISIBLE);
        favImg.setVisibility(View.GONE);
        requestImg();
    }

    public Media getGifInfo() {
        return info;
    }

    private void requestImg() {
        hasError = false;
        final long tempTime = System.currentTimeMillis();
        errorImg.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        layout.setBackgroundResource(R.drawable.gifkb_bg_gif_loading);
        String url = "http://media2.giphy.com/media/"+info.getId()+"/100.gif";
        Glide.with(mContext).load(url)
//                .asGif()
//                .error(R.drawable.gifkb_ic_renew)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        favImg.setVisibility(View.GONE);
                        errorImg.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        hasError = true;
                        loadingTime = System.currentTimeMillis() - tempTime;
                        layout.setBackgroundColor(0xd6d6d6);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        loadingTime = System.currentTimeMillis() - tempTime;
                        hasError = false;
                        progressBar.setVisibility(View.GONE);
                        errorImg.setVisibility(View.GONE);
                        layout.setBackgroundColor(0xffffff);
                        return false;
                    }
                })
//                .transform(new CenterCrop(mContext), new GlideRoundTransform(mContext,5))
//                .crossFade()
//                .placeholder(R.color.gif_display_background)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(img);
    }

}
