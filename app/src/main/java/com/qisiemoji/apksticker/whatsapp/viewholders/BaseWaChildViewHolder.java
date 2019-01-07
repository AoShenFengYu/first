package com.qisiemoji.apksticker.whatsapp.viewholders;


import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.qisiemoji.apksticker.whatsapp.Sticker;
import com.qisiemoji.apksticker.whatsapp.StickerPack;

public abstract class BaseWaChildViewHolder<E extends StickerPack> extends RecyclerView.ViewHolder {

    public BaseWaChildViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void fillView(E entity);

}
