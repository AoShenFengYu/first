package com.qisiemoji.apksticker.whatsapp.gifpick;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.giphy.sdk.core.models.Media;

/**
 * Created by xm180319 on 2018/3/26.
 */

public abstract class BaseGifPickViewHolder<E extends GifPickItem> extends RecyclerView.ViewHolder {

    public ImageView tick;

    public BaseGifPickViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void fillView(E entity);
}
