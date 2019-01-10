package com.qisiemoji.apksticker.whatsapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.whatsapp.gifpick.BaseGifPickViewHolder;
import com.qisiemoji.apksticker.whatsapp.gifpick.GifPickItem;

/**
 * Created by xm180319 on 2018/3/26.
 */

public class TopDetailViewHolder extends RecyclerView.ViewHolder {
    public ImageView img;
    private Context mContext;

    public TopDetailViewHolder(View itemView, final Context mContext) {
        super(itemView);
        this.mContext = mContext;
        img = itemView.findViewById(R.id.top_detail_item_img);
    }
}
