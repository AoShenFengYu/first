package com.qisiemoji.apksticker.whatsapp.gifpick;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.qisiemoji.apksticker.whatsapp.search.BaseSearchViewHolder;

import java.io.ByteArrayOutputStream;

/**
 * Created by xm180319 on 2018/3/26.
 */

public class GifPickViewHolder extends BaseGifPickViewHolder {
    private RelativeLayout layout;
    private ImageView img;
    private ImageView errorImg;
    private Context mContext;
    private GifPickItem info;

    public GifPickViewHolder(View itemView, final Context mContext) {
        super(itemView);
        this.mContext = mContext;
        layout = (RelativeLayout) itemView.findViewById(R.id.gif_item_layout);
        tick = itemView.findViewById(R.id.gif_pick_tick);
        img = (ImageView) itemView.findViewById(R.id.gif_pick_img);
        errorImg = (ImageView) itemView.findViewById(R.id.gif_favorite_error);
        errorImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestImg();
            }
        });
    }

    @Override
    public void fillView(GifPickItem entity) {
        info = entity;
        if (entity == null) {
            return;
        }
        img.setVisibility(View.VISIBLE);
        requestImg();
    }

    private void requestImg() {
        errorImg.setVisibility(View.INVISIBLE);
        layout.setBackgroundResource(R.drawable.gifkb_bg_gif_loading);
        img.setImageBitmap(info.bitmap);
    }

}
